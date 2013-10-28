package com.bluetext.nextapp;

import java.net.Socket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private final static String TAG = "AGG";
	private static String phoneNumber;
	static AsyncTask<String, Void, Socket> task;
	static AsyncTask<String, String, String> sqlTask;
	protected static Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sqlTask = null;
		ctx = this.getApplicationContext();
		//new QuerySMSActivity().execute(ctx); // TODO Still experimenting with this
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
				
		/*
		 * Starts an AsyncTask to verify the phone number and password for the 
		 * data given.  If the number and password are correct, then the AsyncTask
		 * will grab the most recent IP address for the account that has logged in
		 * from the PC side client.
		 * After this task completes, we kick off the checkLogin() method, which 
		 * actually starts the ServerListener thread on the IP Address found earlier.
		 */
		sqlTask = new SQLLoginActivity().execute(phoneNumber, password);
	}
	
	public static void checkLogin(String loginResult)
	{
		if(loginResult.contains("Login Successful!")){
			Toast.makeText(ctx, "Login Successful!", Toast.LENGTH_LONG).show();
			
			// Get the IP address of the PC client from the return string and open
			// a socket in a new thread through this AsyncTask
			String IPaddr = loginResult.substring(loginResult.indexOf('!') + 2);
			task = new ServerListener().execute(IPaddr, Integer.toString(1300));
			
			// Now put the Phone's IP into the database
			new PutIPActivity().execute(phoneNumber);
			
			//TODO bring user to a new window after they login
		} 
		else {
			// Let user retry the login proccess
			Toast.makeText(ctx, loginResult, Toast.LENGTH_LONG).show();
			sqlTask = null;
		}
	}
	
	//TODO Still testing this with the QuerySMS AsyncTask
	public void getContacts()
	{
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