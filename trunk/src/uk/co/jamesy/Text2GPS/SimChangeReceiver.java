package uk.co.jamesy.Text2GPS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

/**
 * @author James
 * 
 */

public class SimChangeReceiver extends BroadcastReceiver {
	/**
	 * Process the intent received from Android system, then checks for SIM
	 * change and alerts designated contact
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 *      android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// get preferences and extract known owner phone number
		SharedPreferences prefs = context.getSharedPreferences(
				MainApp.PASSWORD_PREF_KEY, 0);

		String contact = prefs.getString("swstatus", null);
		// if phone number is set then sim watch is enabled
		if (contact != null) {
			// get known phone number for device
			String phoneid = prefs.getString("swstatus", null);
			// check known against current
			String result = checkPhoneId(context, phoneid);
			// if a result is returned apart from pass notify owner
			if (result != null && result != "pass") {
				// inform user of failure or new number
				if (result == "nocomp") {
					sendSMS(contact, "" + context.getText(R.string.sw_nonum));
				} else {
					sendSMS(contact, "" + context.getText(R.string.sw_note)
							+ result);
				}
			}

		}

	}

	/**
	 * compares the stored phone number against the current sim
	 * 
	 * @param Context
	 *            the context the application is running in
	 * @param phoneid
	 *            the stored phone number
	 * @return String containing "pass" if matched, the new number if changed or
	 *         no comp if not a gsm phone
	 */
	private String checkPhoneId(Context context, String phoneid) {
		String result = "";

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
			if (tm.getLine1Number() == phoneid) {
				result = "pass";
				return result;
			} else {
				result = tm.getLine1Number();
				return result;
			}

		} else {
			result = "nocomp";
			return result;
		}
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
}
