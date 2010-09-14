package uk.co.jamesy.Text2GPS;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;

/**
 * @author James
 * 
 */
public class FindResponse extends Service {

	private String from;
	private String message;
	private String correctMd5;

	private LocationManager lm;
	private LocationListener locationListener;
	private String strlat;
	private String strlon;
	private String accura;
	private Timer timer;
	private PowerManager pm;
	private WakeLock wl;
	private int locs = 0;
	private final int MAXLOCS = 60;

	/**
	 * @param savedInstanceState
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate();
	}

	/**
	 * Checks password and if appropriate starts location finder
	 * 
	 * @return void
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		// get message details from intent
		Bundle bundle = intent.getExtras();
		from = bundle.getString("FROM");
		message = bundle.getString("MESSAGE");
		correctMd5 = bundle.getString("MD5");

		// get & check password
		String[] tokens = message.split(":");

		if (tokens.length >= 2) {

			String md5hash = PhoneFinder.getMd5Hash(tokens[1]);

			if (md5hash.equals(correctMd5)) {
				startGPS();
			} else {
				stopService();
			}
		} else {

			startGPS();
		}
	}

	/**
	 * Sets up Location Manager and phone hardware to locate GPS location
	 * 
	 * @return void
	 */
	private void startGPS() {

		// get location manager
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocListener();

		// check if gps is enabled
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			// start request for location
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 800, 0,
					locationListener);

			startTimer(120);

			// turn the screen to allow gps to update
			try {

				pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.ON_AFTER_RELEASE, "My Tag");
				wl.acquire(); // ..screen will stay on until "wl.release();"

			} catch (Exception e) {
				System.out.print(e.toString());
			}

		} else {
			failedGPS();
		}
	}

	/**
	 * Watches and processes location changes
	 * 
	 * @author James
	 * @see android.location.LocationListner
	 */
	public class LocListener implements LocationListener {

		/**
		 * 
		 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
		 */
		public void onLocationChanged(Location loc) {
			try {
				if (loc != null && locs == MAXLOCS || loc.getAccuracy() > 0.0
						&& loc.getAccuracy() < 0.2) {
					// convert to strings
					strlat = Double.toString(loc.getLatitude());
					strlon = Double.toString(loc.getLongitude());
					accura = Float.toString(loc.getAccuracy());

					// send the location
					sendSMS(from, "" + getText(R.string.gps_lat) + strlat
							+ "\n" + getText(R.string.gps_long) + strlon + "\n"
							+ getText(R.string.gps_acc) + accura);
					// generate maps link & send
					String strWebLink = mapLink(strlat, strlon);
					if (strWebLink != null) {
						sendSMS(from, strWebLink);
					}

					stopGPS();
				} else {
					locs++;
				}
			} catch (Exception e) {
				System.out.print(e.toString());
			}
		}

		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			// not used
		}

		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			// not used
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			// not used
		}

	}

	private void startTimer(int seconds) {
		timer = new Timer();
		timer.schedule(new ToDoTask(), seconds * 1000);
	}

	class ToDoTask extends TimerTask {
		public void run() {
			failedGPS();
		}
	}

	/**
	 * Generates a String containing a web link specific to provided lat/long
	 * 
	 * @param lat
	 *            String representing Latitude of phone
	 * @param lon
	 *            String representing Longitude of phone
	 * @return String containing link
	 */
	private String mapLink(String lat, String lon) {
		try {
			// lat = lat.substring(0, (lat.indexOf(".") + 15));
			// lon = lon.substring(0, (lon.indexOf(".") + 10));

			String webAddress = "http://maps.google.com/maps?q=" + lat + ","
					+ lon;
			return webAddress;

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Triggered if GPS location not available, attempts to fail over to last
	 * known cooirdidates the to stop GPS
	 * 
	 * @return void
	 */
	private void failedGPS() {
		// send last known locations

		Location lastKnownLoc = lm
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLoc != null) {

			String lastLat = Double.toString(lastKnownLoc.getLatitude());
			String lastLon = Double.toString(lastKnownLoc.getLongitude());
			String lastAcc = Float.toString(lastKnownLoc.getAccuracy());
			String timeat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
					.format(lastKnownLoc.getTime());

			sendSMS(from, (String) getText(R.string.old_gps) + timeat + "\n"
					+ getText(R.string.gps_lat) + lastLat + "\n"
					+ getText(R.string.gps_long) + lastLon + "\n"
					+ getText(R.string.gps_acc) + lastAcc);

			String strWebLink = mapLink(lastLat, lastLon);
			if (strWebLink != null) {
				sendSMS(from, strWebLink);
			}
		} else {
			sendSMS(from, (String) getText(R.string.gps_fail));
		}

		stopGPS();
	}

	/**
	 * Kills Location manager clears timers and wake locks Triggered if location
	 * is not available
	 * 
	 * @return void
	 */
	private void stopGPS() {
		// clean up and kill the service
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			lm.removeUpdates(locationListener);
		}

		if (timer != null) {
			timer.cancel();
		}

		if (wl != null) {
			wl.release();
		}

		stopService();
	}

	/**
	 * Stops the location service
	 * 
	 * @return void
	 */
	private void stopService() {
		stopSelf();
	}

	/**
	 * Sends a text message to the specified number with the specified text
	 * 
	 * @param number
	 *            Phone number to send message to
	 * @param body
	 *            Message Text
	 * @return void
	 */
	private void sendSMS(String number, String body) {
		try {
			SmsManager sm = SmsManager.getDefault();
			sm.sendTextMessage(number, null, body, null, null);
		} catch (Exception e) {
			System.out.print(e.toString());
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}