package uk.co.jamesy.Text2GPS;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PhoneFinder extends Activity {

	public static final String PASSWORD_PREF_KEY = "passwd";

	private EditText pass1;
	private EditText pass2;

	@Override
	public void onCreate(Bundle bun) {
		super.onCreate(bun);
		setContentView(R.layout.main);

		pass1 = (EditText) findViewById(R.id.password);
		pass2 = (EditText) findViewById(R.id.password_confirm);

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(clickListener);

	}

	private OnClickListener clickListener = new OnClickListener() {

		public void onClick(View v) {
			String p1 = pass1.getText().toString();
			String p2 = pass2.getText().toString();

			Dialog di = new Dialog(PhoneFinder.this);
			di.setContentView(R.layout.appdia);
			di.setTitle(getText(R.string.alert));
			di.setCancelable(true);
			di.setCanceledOnTouchOutside(true);
			TextView t = (TextView) di.findViewById(R.id.TextView01);

			if (p1.equals(p2)) {

				if (p1.length() >= 6 || p2.length() >= 6) {

					Editor passwdfile = getSharedPreferences(
							PhoneFinder.PASSWORD_PREF_KEY, 0).edit();
					String md5hash = getMd5Hash(p1);
					passwdfile
							.putString(PhoneFinder.PASSWORD_PREF_KEY, md5hash);
					passwdfile.commit();

					t.setText(R.string.pass_upd);
					di.show();

				} else
					t.setText(R.string.pass_valfail);
				di.show();

			} else {
				pass1.setText("");
				pass2.setText("");
				t.setText(R.string.pass_noMatch);
				di.show();
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

}