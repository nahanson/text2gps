package uk.co.jamesy.Text2GPS;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PhoneFinder extends Activity {

	public static final String PASSWORD_PREF_KEY = "passwd";

	private EditText pass1;
	private EditText pass2;


	@Override
	public void onCreate(Bundle bun) {
		super.onCreate(bun);
		
			setContentView(R.layout.main);
			// no password set
			pass1 = (EditText) findViewById(R.id.password);
			pass2 = (EditText) findViewById(R.id.password_confirm);


		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(clickListener);

	}

	private OnClickListener clickListener = new OnClickListener() {

		public void onClick(View v) {
			// get inputs & context
			Context context = getApplicationContext();
			
			String p1 = pass1.getText().toString();
			String p2 = pass2.getText().toString();

	
				// check new passwords match
				if (p1.equals(p2)) {
					// validate new password
					if (p1.length() >= 6 || p2.length() >= 6) {
						
						changePass(p1,context);

					} else

						popNote(context, R.string.pass_valfail,
								Toast.LENGTH_SHORT);

				} else {
					pass1.setText("");
					pass2.setText("");

					popNote(context, R.string.pass_noMatch, Toast.LENGTH_SHORT);
				}
			

		}

	};

	public static String getMd5Hash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);

			while (md5.length() < 32)
				md5 = "0" + md5;

			return md5;
		} catch (NoSuchAlgorithmException e) {
			Log.e("MD5", e.getMessage());
			return null;
		}
	}

	public boolean checkPass(String pass, String hash) {
		boolean check = false;
		pass = getMd5Hash(pass);
		if (hash.equals(pass)) {
			check = true;
		}

		return check;
	}

	public void popNote(Context context, int note, int duration) {
		Toast.makeText(context, note, duration).show();
	}

	public void changePass(String pass,Context context) {
		Editor passwdfile = getSharedPreferences(PhoneFinder.PASSWORD_PREF_KEY,
				0).edit();

		String md5hash = getMd5Hash(pass);
		passwdfile.putString(PhoneFinder.PASSWORD_PREF_KEY, md5hash);
		passwdfile.commit();

		popNote(context, R.string.pass_upd, Toast.LENGTH_SHORT);
	}

}