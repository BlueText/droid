package com.bluetext.nextapp;

import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import bigsky.Contact;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Main activity for BlueText android application.
 * @author Andrew
 */
public class MainActivity extends Activity {
	
	@SuppressWarnings("unused")
	private final static String TAG = "AGG";
	public static String phoneNumber;
	static AsyncTask<String, Void, Socket> serverListener;
	static AsyncTask<String, String, String> sqlTask;
	static AsyncTask<Object, Void, ConcurrentLinkedQueue<Contact>> getAllContacts;
	protected static Context ctx;
	public static Contact userContact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Give our loginActivity a reference to main thread
		SQLLoginActivity.ma = this;
		sqlTask = null;
		
		// Save application context for other activities to use
		ctx = this.getApplicationContext();		
		
		// For better performance, kick off the activity to gather
		// all of the necessary contacts right away when the application starts.
		getAllContacts = new GetAllContactsActivity().execute();
		
		// Get the phone number of this device, enter it into username field, and set to unchangeable
		phoneNumber = ((TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		phoneNumber = formatPhoneNumber(phoneNumber.toCharArray());
		((EditText) findViewById(R.id.phoneNumberField)).setFocusable(false);
		((EditText) findViewById(R.id.phoneNumberField)).setText(phoneNumber);
		
		// Set a generic user contact just in case SQL database doesn't have first and last name
		userContact = new Contact("Me", "", phoneNumber, "");
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
		String password    = ((EditText) findViewById(R.id.passwordField)).getText().toString();
		
		// Remove any wildcard chars that were entered as input
		password = password.replace('%', ' ');
		password = password.replace('_', ' ');
		
		// Wipe out password on login attempt
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
		
		//addThumbnail();
		//Log.d(TAG, "Added image");
	}
	
	/**
	 *  This method is called in the onComplete() of SQLLoginActivity
	 * @param loginResult the result of the SQLLoginActivity
	 */
	public void checkLogin(String loginResult)
	{
		if(loginResult.contains("Login Successful!")){
			Toast.makeText(ctx, "Information correct!\nWaiting for PC login...", Toast.LENGTH_SHORT).show();
			
			// Get the IP address of the PC client from the return string and open
			// a socket in a new thread through this AsyncTask
			String IPaddr = loginResult.substring(loginResult.indexOf('!') + 2);
			ServerListener.ma = this;
			serverListener = new ServerListener().execute(IPaddr, Integer.toString(1300));			
		} 
		else {
			// Let user retry the login proccess
			Toast.makeText(ctx, loginResult, Toast.LENGTH_LONG).show();
			sqlTask = null;
		}
	}	
	
	/**
	 *  This method is called once the ServerListener gets a connection
	 */
	public void gotoPostLoginActivity(){
		Intent i = new Intent(getApplicationContext(), PostLoginActivity.class);
		startActivity(i);
	}
	
	/**
	 * Formats a phone number string by removing any non-numerical chars
	 * @param no
	 * @return Phone number string of the format +1AAAXXXYYYY
	 */
	private String formatPhoneNumber(char[] no){
		StringBuffer sb = new StringBuffer();
		for(char c : no){
			if(c >= '0' && c <= '9'){
				sb.append(c);
			}
		}
		// Remove the country code for US numbers
		if(sb.length() == 11 && sb.charAt(0) == '1'){
			return sb.substring(1);
		}
		return sb.toString();
	}
}
