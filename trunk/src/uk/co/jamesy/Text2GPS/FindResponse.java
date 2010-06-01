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
import android.telephony.gsm.SmsManager;

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {

		Bundle bundle = intent.getExtras();
		from = bundle.getString("FROM");
		message = bundle.getString("MESSAGE");
		correctMd5 = bundle.getString("MD5");

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

	/** start getting gps cords */
	private void startGPS() {

		// ---use the LocationManager class to obtain GPS locations---
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MyLocationListener();

		// first check if provider is enabled
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 800, 0,
					locationListener); // start requests

			startTimer(120); // start timer for two mins

			// turn the screen on else GPS wont update
			try {

				pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | // could
						// use
						// bright
						// instead
						PowerManager.ACQUIRE_CAUSES_WAKEUP | // so we actually
						// wake the
						// device
						PowerManager.ON_AFTER_RELEASE // and we keep it on for a
				// bit after release
						, "My Tag");
				wl.acquire(); // ..screen will stay on until "wl.release();" is
				// called
			} catch (Exception e) {
				System.out.print(e.toString());
			}

		} else {
			failedGPS();
		}
	}

	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			try {
				if (loc != null) { // if we have cords
					// get coordinates and convert to strings
					strlat = Double.toString(loc.getLatitude());
					strlon = Double.toString(loc.getLongitude());
					accura = Float.toString(loc.getAccuracy());
					// call method to send cords
					sendSMS(from, "" + getText(R.string.gps_lat) + strlat
							+ "\n" + getText(R.string.gps_long) + strlon + "\n"
							+ getText(R.string.gps_acc) + accura);

					String strWebLink = mapGPS(strlat, strlon);
					if (strWebLink != null) {
						sendSMS(from, strWebLink);
					}

					stopGPS();
				}
			} catch (Exception e) {
				System.out.print(e.toString());
			}
		}

		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

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

	/** create a link to google maps with the given cords */
	private String mapGPS(String lat, String lon) {
		try {
			lat = lat.substring(0, (lat.indexOf(".") + 8));
			lon = lon.substring(0, (lon.indexOf(".") + 8));

			String webAddress = "http://maps.google.com/maps?q=" + lat + ","
					+ lon;
			return webAddress;
		} catch (Exception e) {
			return null;
		}
	}

	/** when gps location can not be gotten */
	private void failedGPS() {
		// failed to get cords so we send the last know

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

			String strWebLink = mapGPS(strlat, strlon);
			if (strWebLink != null) {
				sendSMS(from, strWebLink);
			}
		} else {
			sendSMS(from, (String) getText(R.string.gps_fail));
		}

		stopGPS();
	}

	/**
	 * stop gps updates, kill timer, release the wake lock, and stop the service
	 */
	private void stopGPS() {
		// only stop if it was ever started
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			lm.removeUpdates(locationListener); // stop GPS updating
		}

		if (timer != null) {
			timer.cancel(); // Terminate the thread
		}

		if (wl != null) {
			wl.release();
		}

		stopService();
	}

	/** stop this service and save the debug log if we should */
	private void stopService() {
		stopSelf(); // stop service
	}

	/** send a text message args phone number and the message */
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