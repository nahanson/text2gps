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

public class MainApp extends Activity {

	public static final String PASSWORD_PREF_KEY = "passwd";

	private EditText pass1;
	private EditText pass2;
	private EditText oldPass;
	private int mode = -1;
	private String expass;

	@Override
	public void onCreate(Bundle bun) {
		super.onCreate(bun);
		// check for existing password and display relevant UI
		SharedPreferences passwdfile = this.getSharedPreferences(
				MainApp.PASSWORD_PREF_KEY, 0);

		if (passwdfile.getString(MainApp.PASSWORD_PREF_KEY, null) == null) {
			setContentView(R.layout.main);
			// no password set
			pass1 = (EditText) findViewById(R.id.password);
			pass2 = (EditText) findViewById(R.id.password_confirm);

		} else {
			// password existing add confirm field
			mode = 1;
			expass = passwdfile.getString(MainApp.PASSWORD_PREF_KEY, null);
			setContentView(R.layout.passprompt);

			pass1 = (EditText) findViewById(R.id.password);
			pass2 = (EditText) findViewById(R.id.password_confirm);
			oldPass = (EditText) findViewById(R.id.old_password);
		}

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(clickListener);

	}

	private OnClickListener clickListener = new OnClickListener() {

		public void onClick(View v) {
			// get inputs & context
			Context context = getApplicationContext();

			String p1 = pass1.getText().toString();
			String p2 = pass2.getText().toString();
			String eP = "";

			if (mode == 1) {
				eP = oldPass.getText().toString();

				// check for forgotten password code
				if (eP.equals(getText(R.string.idiot))) {
					mode = -1;
				}
			}

			// check mode & existing password
			if ((mode == 1 && checkPass(eP, expass)) || mode == -1) {
				// check new passwords match
				if (p1.equals(p2)) {
					// validate new password
					if (p1.length() >= 6 || p2.length() >= 6) {

						changePass(p1, context);

					} else

						popNote(context, R.string.pass_valfail,
								Toast.LENGTH_SHORT);

				} else {
					pass1.setText("");
					pass2.setText("");

					popNote(context, R.string.pass_noMatch, Toast.LENGTH_SHORT);
				}
			} else {
				pass1.setText("");
				pass2.setText("");
				oldPass.setText("");

				popNote(context, R.string.pass_OldnoMatch, Toast.LENGTH_SHORT);
			}

		}

	};

	/**
	 * Hashes a string
	 * 
	 * @param input
	 *            String to be hashed
	 * @return String containing md5 hash of input
	 */
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

	/**
	 * Checks if a password matches the stored hash
	 * 
	 * @param pass
	 *            String containing password
	 * @param hash
	 *            String Containing stored hash
	 * @return boolean Passwords matching
	 */
	public boolean checkPass(String pass, String hash) {
		boolean check = false;
		pass = getMd5Hash(pass);
		if (hash.equals(pass)) {
			check = true;
		}

		return check;
	}

	/**
	 * show a "Toast" message to the user containing given text
	 * 
	 * @param context
	 *            the context in which to display the toast
	 * @param note
	 *            text of the toast
	 * @param duration
	 *            time to display
	 */
	public void popNote(Context context, int note, int duration) {
		Toast.makeText(context, note, duration).show();
	}

	/**
	 * Hash and store new password
	 * 
	 * @param pass
	 *            new password
	 * @param context
	 *            application context
	 * @see uk.co.jamesy.MainApp.popNote
	 * @see uk.co.jamesy.MainApp.getMd5Hash
	 */
	public void changePass(String pass, Context context) {
		Editor passwdfile = getSharedPreferences(MainApp.PASSWORD_PREF_KEY,
				0).edit();

		String md5hash = getMd5Hash(pass);
		passwdfile.putString(MainApp.PASSWORD_PREF_KEY, md5hash);
		passwdfile.commit();

		popNote(context, R.string.pass_upd, Toast.LENGTH_SHORT);
	}

}