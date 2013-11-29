package com.bluetext.nextapp;

import java.util.ArrayList;

import bigsky.BlueTextRequest;
import bigsky.BlueTextRequest.REQUEST;
import bigsky.BlueTextResponse;
import bigsky.Contact;
import bigsky.TextMessage;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;

public class BlueTextRequestActivity extends AsyncTask<BlueTextRequest, Void, BlueTextResponse>
{
	private final static String TAG = "AGG";
	private Context ctx;
	private BlueTextRequest request;

	@Override
	protected BlueTextResponse doInBackground(BlueTextRequest... params) 
	{
		ctx = MainActivity.ctx;
		request = params[0];
		if(request == null){
			Log.d(TAG, "ERROR: got null request inside BlueTextRequestActivity");
			return null;
		}
			
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
		
		return null;
	}
	
	protected void onPostExecute(BlueTextResponse result){
		if(result != null){
			ServerListener.sendObjectToPC(result);
		}		
	}
	
	private int getBatteryLevel()
	{
		if(ServerListener.batteryStatus != null)
		{
			int level = ServerListener.batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = ServerListener.batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			
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
				
		//final String SMS_URI_INBOX = "content://sms/inbox";
		//final String SMS_URI_ALL = "content://sms/";
		Uri uri = Uri.parse("content://sms/");
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
					int int_Type = cur.getInt(index_Type);      // 1 if received, 2 if sent
					//long longDate = cur.getLong(index_Date);    // TODO may include date later
					
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
//		for(TextMessage tm : toRet){
//			Log.d(TAG, "FROM: " + tm.getSender().getFirstName() + "  TO: " + tm.getReceiver().getFirstName() + "    BODY: " + tm.getContent());
//		}
		
		return toRet;
	}
	
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
}

// Reference code in case stuff blows up
//StringBuilder smsBuilder = new StringBuilder();
//final String SMS_URI_INBOX = "content://sms/inbox";
//final String SMS_URI_ALL = "content://sms/";
//Uri uri = Uri.parse(SMS_URI_ALL);
//String[] projection = new String[] { "_id", "address", "person","body", "date", "type" };
//Cursor cur = ctx.getContentResolver().query(uri, null,"address='+15072542815'", null, "date desc");
//cur.moveToFirst();
//Log.d(TAG, "Number of texts: " + cur.getCount());
////for(int i = 0; i < cur.getColumnCount(); i++){
////	Log.d(TAG, cur.getColumnName(i) + ":\t " + cur.getString(i));
////}
//if (cur.moveToFirst()) {
//	int index_Address = cur.getColumnIndex("address");
//	int index_Person = cur.getColumnIndex("person");
//	int index_Body = cur.getColumnIndex("body");
//	int index_Date = cur.getColumnIndex("date");
//	int index_Type = cur.getColumnIndex("type");
//	do {
//		String strAddress = cur.getString(index_Address);
//		int intPerson = cur.getInt(index_Person);
//		String strbody = cur.getString(index_Body);
//		long longDate = cur.getLong(index_Date);
//		int int_Type = cur.getInt(index_Type);
//		
//		if(int_Type == 2) Log.d(TAG, "GOT A TYPE 2");
//
//		smsBuilder.append(strAddress + ", ");
//		smsBuilder.append(intPerson + ", ");
//		smsBuilder.append(strbody + ", ");
//		smsBuilder.append(longDate);
//		smsBuilder.append("NEWCOL: " + cur.getString(cur.getColumnIndex("type")));
//		smsBuilder.append("\n\n");
//	} while (cur.moveToNext());
//
//	if (!cur.isClosed()) {
//		cur.close();
//		cur = null;
//	}
//} else {
//	smsBuilder.append("no result!");
//} // end if
//Log.d(TAG, smsBuilder.toString());
