package com.bluetext.nextapp;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private final String TAG = "AGG";
	private static String phoneNumber;
	static AsyncTask<Integer, Void, Socket> task;
	static AsyncTask<String, String, String> sqlTask;
	private Socket sock = null;
	protected static Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sqlTask = null;
		ctx = this.getApplicationContext();
		new QuerySMSActivity().execute(ctx);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
		
	public void getLogin(View view)
	{				
		// Prevents user from spamming login button
		if(sqlTask != null){
			return;
		}
		
		// Pull the username and password out of the login and password field
		phoneNumber = ((EditText) findViewById(R.id.phoneNumberField)).getText().toString();
		String password    = ((EditText) findViewById(R.id.passwordField)).getText().toString();
		
		// Remove any wildcard chars that were entered as input
		phoneNumber = phoneNumber.replace('%', ' ');
		phoneNumber = phoneNumber.replace('_', ' ');
		password = password.replace('%', ' ');
		password = password.replace('_', ' ');
		
		((EditText) findViewById(R.id.phoneNumberField)).setText(null);
		((EditText) findViewById(R.id.passwordField)).setText("");
				
		// Execute SQL statement
		String sqlString = "SELECT * FROM testTable WHERE phoneNumber='" + phoneNumber + "' AND password='" + password + "'";
		sqlTask = new SQLLoginActivity().execute(sqlString);
	}
	
	public static void checkLogin(String loginResult)
	{
		Toast.makeText(ctx, loginResult, Toast.LENGTH_LONG).show();
		
		if("Login Successful!".equalsIgnoreCase(loginResult)) {
			new PutIPActivity().execute(phoneNumber);
			//task = new ServerListener().execute(Integer.valueOf(1301));
			//TODO bring user to a new window after they login
		} 
		else {
			// Let user retry the login proccess
			sqlTask = null;
		}
	}
	
	public void getContacts(){
		Log.d(TAG, "in getContacts");
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(Phone.CONTENT_URI, null, Phone.DISPLAY_NAME + "=?", new String[]{"*Andy*"}, null);
		
		if(cursor.getCount() > 0){
		    cursor.moveToFirst();
		    do {
		       String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
		       Log.d(TAG, "Andys number is: " + number);
		    }while (cursor.moveToNext() ); 
		}
	}	
}
