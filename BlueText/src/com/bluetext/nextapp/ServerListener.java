package com.bluetext.nextapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.telephony.SmsManager;
import android.util.Log;
import bigsky.BlueTextRequest;
import bigsky.BlueTextResponse;
import bigsky.Contact;
import bigsky.TextMessage;
import bigsky.BlueTextRequest.REQUEST;


/**
 * Background process that does forwarding and receiving for the phone.
 * First argument must be IP address<br>
 * Second argument must be the port to create the socket on<br>
 * (Both must be strings)
 * @author Andy Guibert
 */
public class ServerListener extends AsyncTask<String, Void, Socket>
{
	private int port;
	private static int prevBatteryLevel = -1;
	private String ipAddress;
	private final static String TAG = "AGG";
	private static Socket sock;
	public static Intent batteryStatus = null;
	private static ObjectInputStream fromPC;
	private static ObjectOutputStream toPC;
	public static PostLoginActivity pla;
	public static MainActivity ma;
	
	protected Socket doInBackground(String... params)
	{
		Log.d(TAG, "Connecting to IP: " + params[0]);
		this.ipAddress = params[0];	
		this.port = Integer.parseInt(params[1]);
		
		try{
			sock = new Socket(this.ipAddress, this.port);
		}catch(Exception e){
			Log.d(TAG, "Error in serverListener constructor: " + e.getMessage());
		}
		
		try{
			fromPC = new ObjectInputStream(sock.getInputStream());
			toPC = new ObjectOutputStream(sock.getOutputStream());
		}catch(Exception e){
			Log.d(TAG, "Error creating ObjectStreams ctor: " + e.getMessage());
		}
		
		// Once the PC and phone are connected, go to
		// the post login screen 
		ma.gotoPostLoginActivity();
		
		// Initiate the SMS listener on the phone
		SmsListener.setServerListener(this);
		try {
			sendContactsToPc(MainActivity.getAllContacts.get());
		} catch (Exception e) {
			Log.d(TAG, "Error getting all contacts inside ServerListener.");
		}
		
		// Register the battery level checker
		batteryStatus = pla.registerReceiver(batteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
				
		Log.d(TAG, "Listening for messages from PC forever...");
		
		int sendCode = 0;
		while(fromPC != null && sendCode == 0)
		{
			try {
				Object streamObject = fromPC.readObject();
				if(streamObject instanceof TextMessage)
				{
					TextMessage txtMsg = (TextMessage) streamObject;
					if(txtMsg != null){
						sendCode = phoneSendText(txtMsg);
					}
				}
				else if(streamObject instanceof BlueTextRequest){
					BlueTextRequest request = (BlueTextRequest) streamObject;
					new BlueTextRequestActivity().executeOnExecutor(THREAD_POOL_EXECUTOR, request);
				}
				else{
					Log.d(TAG, "Unknown class was sent through the stream: " + streamObject.toString());
				}
								
			} catch (Exception e) {	
				Log.d(TAG, "Socket was broken, closing the port");
				// If the socket dies, set to null in Main to allow reconnecting
				MainActivity.serverListener = null;
				MainActivity.sqlTask = null;
				// If the PC side broke the stream, take us back to login window
				if(pla != null){
					MainActivity.getAllContacts = new GetAllContactsActivity().execute();
					pla.finish();
				}
				
				closeStream();
				
				return sock;
			}
		}
		return sock;
	}
	
	/**
	 * Sends any amount of contact objects to the PC
	 * @param contacts
	 */
	public void sendContactsToPc(ConcurrentLinkedQueue<Contact> contacts)
	{
		Log.d(TAG, "Sending " + contacts.size() + " contacts to PC." );
		
		while(!contacts.isEmpty()){
			sendObjectToPC(contacts.remove());
		}
	}
	
	/**
	 * Centralized method for sending an object to the PC.
	 * @param o The object to be sent to the PC.
	 */
	public synchronized static void sendObjectToPC(Object o)
	{
		try{
			toPC.writeObject(o);
			toPC.flush();
		} catch(Exception e){
			System.out.println("Got exception in ServerListener.sendObject(): " + e.getMessage());
		}
	}
	
	/**
	 * Sends a text message to the intended receiver through the phone's default
	 * messaging service.
	 * 
	 * @param msg
	 * @return
	 */
	private int phoneSendText(TextMessage msg)
    {
		// Actually send the text message
    	try{
    		SmsManager.getDefault().sendTextMessage(msg.getReceiver().getPhoneNumber(), null, msg.getContent(), null, null);
    		Log.d(TAG, "SMS sent to " + msg.getReceiver().getFirstName() + "!");
    	} catch (Exception e){
    		Log.d(TAG, "SMS delivery failed.");
    		return -1;
    	}
    	
    	// Now add the text message to the sent queue
    	ContentValues sentSms = new ContentValues();
    	String address = "+1" + msg.getReceiver().getPhoneNumber();
    	sentSms.put("address", address);
	    sentSms.put("body", msg.getContent());
	    ContentResolver contentResolver = ma.getContentResolver();
	    contentResolver.insert(Uri.parse("content://sms/sent"), sentSms);
	    Log.d(TAG, "Added sent message to conversation with: " + address);
	    
	    return 0;
    }
	
	/**
	 * Clean up the Object streams and server socket.
	 * Print a report for each component closed.
	 */
	public static void closeStream()
	{
		if(pla != null && batteryLevelReceiver != null) pla.unregisterReceiver(batteryLevelReceiver);
		pla = null;
		String cleanUpStatus = "Socket cleanup Status: ";
		try{
			cleanUpStatus += "InputStream=";
			if(fromPC != null) fromPC.close();
			cleanUpStatus += "SUCCESS   ";
		} catch(IOException e1){
			cleanUpStatus += "FAILED   ";
		}
		try{
			cleanUpStatus += "OutputStream=";
			if(toPC != null) toPC.close();
			cleanUpStatus += "SUCCESS   ";
		} catch(IOException e1){
			cleanUpStatus += "FAILED   ";
		}
		try{
			cleanUpStatus += "Socket=";
			if(sock != null) sock.close();
			cleanUpStatus += "SUCCESS";
		} catch(IOException e1){
			cleanUpStatus += "FAILED";
		}			
		Log.d(TAG, cleanUpStatus);
	}
	
	/**
	 * BroadcastReceiver attached to the sticky alarm
	 * for battery statistics.  This listener will automatically
	 * send updates to the PC whenever the phone battery level
	 * changes by 1%.
	 */
	public static BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {
			int curLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			
			// Only do this if the battery percentage has changed
			if(prevBatteryLevel != curLevel)
			{
				prevBatteryLevel = curLevel;
				if(scale != 100){
					curLevel = (int) ((float)curLevel / (float)scale);
				}
				
				BlueTextRequest request = new BlueTextRequest(REQUEST.BATTERY_PERCENTAGE, null);
				BlueTextResponse response = new BlueTextResponse(request, curLevel);	
				sendObjectToPC(response);
				Log.d(TAG, "Sending battery level " + curLevel + " to PC.");
			}
		}
	};
}
