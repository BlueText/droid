package com.bluetext.nextapp;

import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import bigsky.BlueTextRequest;
import bigsky.BlueTextRequest.REQUEST;
import bigsky.BlueTextResponse;
import bigsky.Contact;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	@SuppressWarnings("unused")
	private final static String TAG = "AGG";
	private static String phoneNumber;
	static AsyncTask<String, Void, Socket> serverListener;
	static AsyncTask<String, String, String> sqlTask;
	static AsyncTask<Object, Void, ConcurrentLinkedQueue<Contact>> getAllContacts;
	protected static Context ctx;
	public static Contact userContact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SQLLoginActivity.ma = this;
		sqlTask = null;
		
		ctx = this.getApplicationContext();		
		getAllContacts = new GetAllContactsActivity().execute();
		phoneNumber = ((TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		if(phoneNumber.length() == 11){
			phoneNumber = phoneNumber.substring(1);
		}
		((EditText) findViewById(R.id.phoneNumberField)).setFocusable(false);
		((EditText) findViewById(R.id.phoneNumberField)).setText(phoneNumber);
		
		// TODO get the actual name of the user
		userContact = new Contact("Jonathan", "Mielke", phoneNumber, null);
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
	
	public void gotoPostLoginActivity(){
		Intent i = new Intent(getApplicationContext(), PostLoginActivity.class);
		startActivity(i);
	}
	
	private static final String[] PHOTO_ID_PROJECTION = new String[] {
	    ContactsContract.Contacts.PHOTO_ID
	};

	private static final String[] PHOTO_BITMAP_PROJECTION = new String[] {
	    ContactsContract.CommonDataKinds.Photo.PHOTO
	};
	
//	public void addThumbnail() {		
//		ImageView iv = (ImageView)findViewById(R.id.imageView1);
//	    final Integer thumbnailId = fetchThumbnailId();
//	    if (thumbnailId != null) {
//	        final Bitmap thumbnail = fetchThumbnail(thumbnailId);
//	        if (thumbnail != null) {
//	            iv.setImageBitmap(thumbnail);
//	        }
//	    }
//
//	}

	private Integer fetchThumbnailId() {
		ContentResolver contentResolver = getContentResolver();
	    final Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode("5072542815"));
	    final Cursor cursor = contentResolver.query(uri, PHOTO_ID_PROJECTION, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

	    try {
	        Integer thumbnailId = null;
	        if (cursor.moveToFirst()) {
	            thumbnailId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
	        }
	        return thumbnailId;
	    }
	    finally {
	        cursor.close();
	    }

	}

	final Bitmap fetchThumbnail(final int thumbnailId) {
		ContentResolver contentResolver = getContentResolver();
	    final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);
	    final Cursor cursor = contentResolver.query(uri, PHOTO_BITMAP_PROJECTION, null, null, null);

	    try {
	        Bitmap thumbnail = null;
	        if (cursor.moveToFirst()) {
	            final byte[] thumbnailBytes = cursor.getBlob(0);
	            if (thumbnailBytes != null) {
	                thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
	            }
	        }
	        return thumbnail;
	    }
	    finally {
	        cursor.close();
	    }
	}
}
