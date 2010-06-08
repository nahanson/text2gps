package uk.co.jamesy.Text2GPS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;

public class FinderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String from = "";
		String message = "";

		SharedPreferences passwdfile = context.getSharedPreferences(
				PhoneFinder.PASSWORD_PREF_KEY, 0);

		String correctMd5 = passwdfile.getString(PhoneFinder.PASSWORD_PREF_KEY,
				null);

		// get the calling message
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;

		if (bundle != null) {
			// retrieve the message
			Object[] pdus = (Object[]) bundle.get("pdus");

			msgs = new SmsMessage[pdus.length];

			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				from = msgs[i].getOriginatingAddress();
				message = msgs[i].getMessageBody().toString();
			}

			// check password is set and message contains activation key
			if (correctMd5 != null
					&& message.contains(context.getResources().getString(
							(R.string.key)))) {

				// store message details and start the location service
				Intent i = new Intent(context, FindResponse.class);
				Bundle b = new Bundle();

				b.putString("FROM", from);
				b.putString("MESSAGE", message);
				b.putString("MD5", correctMd5);
				i.putExtras(b);

				context.startService(i);
			}
		}
	}
}
