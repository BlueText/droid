package com.bluetext.nextapp;

import java.net.Socket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	private final String TAG = "AGG";
	AsyncTask<Integer, Void, Socket> task;
	AsyncTask<String, String, Integer> sqlTask;
	Socket sock = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//task = new ServerListener().execute(Integer.valueOf(1301));
		sqlTask = new SQLActivity().execute("select * from testTable");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void connectToServer(View view){
		if(sock == null){
			try {
				sock = task.get();
				Log.d(TAG, "Got socket ok");
			} catch (Exception e){
				Log.e(TAG, "Error in serverListener ctor: " + e.getMessage());
			}
		}
		else{
			Log.d(TAG, "Already connected to socket.");
		}
    }
	
	public void printFields(View view)
	{
		String phoneNumber = ((EditText) findViewById(R.id.phoneNumberField)).getText().toString();
		String password    = ((EditText) findViewById(R.id.passwordField)).getText().toString();
		
		sqlTask = new SQLActivity().execute("SELECT * FROM testTable WHERE phoneNumber='" + phoneNumber + "' " +
				"AND password='" + password + "'");		
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
