package com.bluetext.nextapp;

import java.util.concurrent.ConcurrentLinkedQueue;

import bigsky.Contact;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

/**
 * Queries the phone for all existing contacts that have a phone number
 * @author Andrew
 */
public class GetAllContactsActivity extends AsyncTask<Object, Void, ConcurrentLinkedQueue<Contact>>
{
	private static final String TAG = "AGG";
	private ConcurrentLinkedQueue<Contact> contactList = new ConcurrentLinkedQueue<Contact>();
	private Context ctx;

	@Override
	protected ConcurrentLinkedQueue<Contact> doInBackground(Object... params) 
	{
		ctx = MainActivity.ctx;
		try{			
			// Make a query for all contacts which have a phone number
			Cursor people = ctx.getContentResolver().query(
					ContactsContract.Contacts.CONTENT_URI, 
					null, 
					"has_phone_number = '1'", 
					null, 
					null);

			// Iterate through all contacts
			while(people.moveToNext()) {
			   int nameFieldColumnIndex = people.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			   String name = people.getString(nameFieldColumnIndex);
			   String id = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));
			   
			   // Do nested query to get the actual phone number of the contact
                Cursor pCur = ctx.getContentResolver().query(
                          ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                          null,
                          ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                          new String[]{id}, 
                          null);
                // Get 1 or 2 phone numbers corresponding to this contact
                while (pCur.moveToNext()) {
                    String phoneNo = formatPhoneNumber(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).toCharArray());
                    String phoneNo2 = null;
                    if(pCur.moveToNext()){
                    	phoneNo2 = formatPhoneNumber(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).toCharArray());
                    }
                    //Log.d(TAG, "Person: " + name + " has phoneNo: " + phoneNo);
                    String first = null, last = null;
                    if(name.contains(" ")){
                    	first = name.substring(0, name.indexOf(' '));
                        last = name.substring(name.indexOf(' ') + 1);
                    }                    
                    else{ //User only has first name
                    	first = name;
                    }                    
                    contactList.add(new Contact(first, last, phoneNo, phoneNo2));
                }
                pCur.close();
			}
			people.close();
		} catch(Exception e){
			Log.d(TAG, "got exception on SMSquery");
			System.out.println(e.getCause());
			e.printStackTrace();
		}
		
		Log.d(TAG, "QuerySMSActivity contactListSize: " + contactList.size());
		return contactList;
	}
	
	/**
	 * Add results to the HashMap for use later
	 */
	protected void onPostExecute(ConcurrentLinkedQueue<Contact> result){
		for(Contact c : result){
			Global.numberToContact.put(c.getPhoneNumber(), c);
		}
	}
		
	@SuppressWarnings("unused")
	private void log10Columns2(Cursor cursor)
	{
		Log.d(TAG, "Row count: " + Integer.toString(cursor.getCount()));
		cursor.moveToFirst();		
		while(cursor.moveToNext()){
			Log.d(TAG, cursor.getColumnName(0) + ": " + cursor.getString(cursor.getColumnIndex("thread_id")));
		}
	}
	
	@SuppressWarnings("unused")
	private void printColumnNames(Cursor cursor)
	{
		Log.d(TAG, "Column count: " + Integer.toString(cursor.getColumnCount()));
		cursor.moveToFirst();
		for(String name : cursor.getColumnNames())
			Log.d(TAG, "Column name: " + name);
	}
	
	/**
	 * Formats a phone number string by removing any non-numerical chars
	 * @param no
	 * @return
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
