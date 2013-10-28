package com.bluetext.nextapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;

import bigsky.Contact;
import bigsky.TextMessage;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

/**
 * Queries the phone for text message objects
 * @param objects...
 *        objects[0] Context: the Context of the caller
 *        objects[1] String:  the phone number of the contact to retrieve the history of
 *        objects[2] Integer: the number of messages to retrieve 
 * @author Andrew
 */
public class QuerySMSActivity extends AsyncTask<Object, SynchronousQueue<TextMessage>, String>
{
	class ContactObj{
		public ContactObj(String thread_id){
			this.tId = thread_id;
		}
		public String tId;
		public String name;
		public String number;
	}

	private static final String TAG = "AGG";
	private SynchronousQueue<TextMessage> messageList = new SynchronousQueue<TextMessage>();
	private Context ctx;
	private HashMap<String,ContactObj> contacts = new HashMap<String,ContactObj>();

	@Override
	protected String doInBackground(Object... params) 
	{
		//Contact thisContact = new Contact(start_first_name, start_last_name, start_phone_number, start_second_phone)
		Cursor cursor = null;
		ctx = (Context)params[0];
		try{
//			cursor = ctx.getContentResolver().query(Uri.parse("content://sms/conversations/"),
//			          null, //new String[]{"thread_id"}, //new String[] {"address", "body", "thread_id", "type"}, // which columns to return
//			          null, //"address = '+15072542815'",//null,  // selection     (same as an SQL WHERE clause)
//			          null,  // selectionArgs (get put into ? for selection param
//			          null); // sortOrder
			
			// Use this for finding individual contact: 
			//Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number),
			
			Cursor people = ctx.getContentResolver().query(
					ContactsContract.Contacts.CONTENT_URI, 
					null, 
					"has_phone_number = '1'", 
					null, 
					null);
			printColumnNames(people);
			while(people.moveToNext()) {
			   int nameFieldColumnIndex = people.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			   Log.d(TAG, "Name is: " + people.getString(nameFieldColumnIndex));
			   int numberFieldColumnIndex = people.getColumnIndex(PhoneLookup.NUMBER);
			   if(numberFieldColumnIndex != -1)
				   Log.d(TAG, "Number is: " + people.getString(numberFieldColumnIndex));
			}
			people.close();
		} catch(Exception e){
			Log.d(TAG, "got exception on SMSquery");
			System.out.println(e.getCause());
			e.printStackTrace();
		}
		return null;
	}
		
	private void log10Columns2(Cursor cursor)
	{
		Log.d(TAG, "Row count: " + Integer.toString(cursor.getCount()));
		cursor.moveToFirst();		
		while(cursor.moveToNext()){
			Log.d(TAG, cursor.getColumnName(0) + ": " + cursor.getString(cursor.getColumnIndex("thread_id")));
		}
	}
	
	private void printColumnNames(Cursor cursor)
	{
		Log.d(TAG, "Column count: " + Integer.toString(cursor.getColumnCount()));
		cursor.moveToFirst();
		for(String name : cursor.getColumnNames())
			Log.d(TAG, "Column name: " + name);
	}
}
