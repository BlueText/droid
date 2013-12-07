package com.bluetext.nextapp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import bigsky.BlueTextRequest;
import bigsky.BlueTextRequest.REQUEST;
import bigsky.BlueTextResponse;
import bigsky.Contact;
import bigsky.TextMessage;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;

/**
 * Asynchronous activity that will get executed by the ServerListener class when
 * it receives a BlueTextRequest from the ObjectStream. This thread will handle
 * all BlueTextResponse.Request cases and a return a result if necessary.
 * 
 * @param BlueTextRequest
 *            request that this response should be constructed for
 * @author Andy Guibert
 */
public class BlueTextRequestActivity extends AsyncTask<BlueTextRequest, Void, BlueTextResponse>
{
	private final static String TAG = "AGG";
	private Context ctx;
	private BlueTextRequest request;
	
	/**
	 * Constructor method
	 */
	@Override
	protected BlueTextResponse doInBackground(BlueTextRequest... params) 
	{
		ctx = MainActivity.ctx;
		request = params[0];
		if(request == null){
			Log.d(TAG, "ERROR: got null request inside BlueTextRequestActivity");
			return null;
		}
			
		/* Handle each different request differently */
		if(REQUEST.CONTACT_CHAT_HISTORY == request.getRequest())
		{
			ArrayList<TextMessage> chatHistory = queryTextMessageHistory(request.getContact());
			return new BlueTextResponse(request, chatHistory);
		}
		else if(REQUEST.SUBMIT_NEW_CONTACT == request.getRequest())
		{
			insertNewContact(request.getContact());
			return null;
		}
		else if(REQUEST.BATTERY_PERCENTAGE == request.getRequest())
		{
			int batteryLevel = getBatteryLevel();
			return new BlueTextResponse(request, batteryLevel);
		}
		else if(REQUEST.CONTACT_PICTURE == request.getRequest())
		{
			Bitmap bmp = getContactPhoto(request.getContact());
			
			// Try to get the default contact image if there is one
			if(bmp != null && bmp.getHeight() > 1 && bmp.getWidth() > 1){
				ByteArrayOutputStream oStream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, oStream);
				return new BlueTextResponse(request, Boolean.valueOf(true), oStream.toByteArray());	
			}
			// If the user has no image locally on the phone, then we need
			// to try to access the facebook profile picture using a graph
			else{
				return new BlueTextResponse(request, Boolean.valueOf(false), "http://graph.facebook.com/" 
									+ request.getContact().getFirstName() + '.'
									+ request.getContact().getLastName()
									+ "/picture?type=square");
			}
		}
		
		return null;
	}
	
	/**
	 * Once doInBackground() completes, send any non-null result
	 * to to PC application for processing.
	 */
	protected void onPostExecute(BlueTextResponse result){
		if(result != null){
			ServerListener.sendObjectToPC(result);
		}		
	}
	
	/**
	 * Check the battery level of the phone.
	 * @return Current phone battery level (1-100)
	 */
	private int getBatteryLevel()
	{
		if(ServerListener.batteryStatus != null)
		{
			int level = ServerListener.batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = ServerListener.batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			
			// If the battery level isn't on a scale of 100
			// for whatever reason...
			if(scale != 100){
				level = (int) ((float)level / (float)scale);
			}
			
			return level;
		}
		// Otherwise we need to register/unregister a new receiver... 
		// this is expensive so we don't want to do it unless we have to
		else
		{
			IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatus = ctx.registerReceiver(null, filter);
			
			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			
			if(scale != 100){
				level = (int) ((float)level / (float)scale);
			}
			
			return level;
		}
	}
		
	/**
	 * Query text message history of a given Contact c.
	 * @param c The contact we want to retrieve the chat history of.
	 * @return An ArrayList of TextMessages representing the chat history
	 * between the Contact c and the BlueText user in chronological order.
	 */
	private ArrayList<TextMessage> queryTextMessageHistory(Contact c)
	{
		ArrayList<TextMessage> toRet = new ArrayList<TextMessage>();
		
		// Construct phone number in the format +1AAAXXXYYYY
		String phoneNumber = "+";
		if(c.getPhoneNumber().length() == 10){
			// Contact phone number is format AAAXXXYYYY
			phoneNumber += '1';
		}
		phoneNumber += c.getPhoneNumber();
				
		Uri uri = Uri.parse("content://sms/");
		// Query for chat history, retrieving only information we care about
		String[] projection = new String[] {"body", "date", "type"};
		Cursor cur = ctx.getContentResolver().query(
				uri, 
				projection,
				"address='" + phoneNumber + "'", 
				null, 
				"date desc");
		try{
			Log.d(TAG, "Got " + cur.getCount() + " texts from " + phoneNumber);
			if (cur.moveToFirst()) {
				int index_Body = cur.getColumnIndex("body");
				//int index_Date = cur.getColumnIndex("date");
				int index_Type = cur.getColumnIndex("type");
				do {
					String textMessageBody = cur.getString(index_Body); // Body of text message
					int int_Type = cur.getInt(index_Type);              // 1 if received, 2 if sent
					//long longDate = cur.getLong(index_Date);          // TODO may include date later
					
					// Construct a RECEIVED TextMessage
					if(int_Type == 1){ 					
						toRet.add(new TextMessage(c, MainActivity.userContact, textMessageBody));
					}
					// Construct a SENT TextMessage
					else if(int_Type == 2){
						toRet.add(new TextMessage(MainActivity.userContact, c, textMessageBody));
					}
					else{
						Log.d(TAG, "ERROR: Got unknown int_type of " + int_Type);
					}
				} while (cur.moveToNext());
			}
		} finally{
			if(cur != null) cur.close();
		}
		
		return toRet;
	}
	
	/**
	 * Insert a new contact that his been created on the PC side
	 * into the Android's actual phone contacts list.
	 * @param c The Contact to be inserted
	 * @return 0 on success.  -1 if an exception occurred.
	 */
	private int insertNewContact(Contact c){
		ArrayList<ContentProviderOperation> cpoList = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = cpoList.size();

		cpoList.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
		   .withValue(RawContacts.ACCOUNT_TYPE, null)
		   .withValue(RawContacts.ACCOUNT_NAME, null )
		   .build());
		cpoList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
		   .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
		   .withValue(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE)
		   .withValue(Phone.NUMBER, "+1" + c.getPhoneNumber())
		   .build());
		cpoList.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
		   .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
		   .withValue(Data.MIMETYPE,StructuredName.CONTENT_ITEM_TYPE)
		   .withValue(StructuredName.DISPLAY_NAME, c.getFirstName() + ' ' + c.getLastName())
		   .build());  		
		try {
			ServerListener.pla.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpoList);
			Log.d(TAG, "Contact " + c.getFirstName() + ' ' + c.getLastName() + " has been added.");
			return 0;
		} catch (Exception e) {
			Log.d(TAG, "exception on applyBatch()" + e.getMessage());
			return -1;
		}		
	}
	
	/**
	 * Attempt to retrieve the photo associated with
	 * a given Contact c.
	 * @param c The contact to retrieve the photo of.
	 * @return A Bitmap of the Contact c's photo, or null
	 * if there was no photo associated with this contact.
	 */
    public Bitmap getContactPhoto(Contact c) {
    	String phoneNumber = "+1" + c.getPhoneNumber();
	    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
	    Uri photoUri = null;
	    
	    // Query for the contact's internal ID
	    ContentResolver cr = ctx.getContentResolver();
	    Cursor contact = cr.query(
	    		phoneUri,
	            new String[] { ContactsContract.Contacts._ID }, 
	            null, null, null);
	    
	    try{
		    // Get the photo URI of the corresponding contact
	    	long userId = -1;
		    if (contact.moveToFirst()) {
		        userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
		        photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
		        
			    if (photoUri != null) {
			        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
			        if (input != null) {
			            return BitmapFactory.decodeStream(input);
			        }
			    }			    
		    }
		    
		    // If the contact had no photo, return null
		    // and worry about what to do with the photo later
	    	return null;
	    } finally{
	    	contact.close();
	    }	    		
	}
}
