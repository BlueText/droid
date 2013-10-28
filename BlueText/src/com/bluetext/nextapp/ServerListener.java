package com.bluetext.nextapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import bigsky.TextMessage;


/**
 * First argument must be IP address<br>
 * Second argument must be the port to create the socket on<br>
 * (Both must be strings)
 * @author Andrew
 */
public class ServerListener extends AsyncTask<String, Void, Socket>
{
	Socket sock;
	int port;
	String ipAddress;
	ObjectInputStream fromPC;
	ObjectOutputStream toPC;
	private final String TAG = "AGG";
	
	protected Socket doInBackground(String... params)
	{
		Log.d(TAG, "Using IP: " + params[0]);
		Log.d(TAG, "Creating socket on port: " + params[1]);
		this.ipAddress = params[0];	
		this.port = Integer.parseInt(params[1]);
		
		try{
			sock = new Socket(this.ipAddress, this.port);
		}catch(Exception e){
			Log.d(TAG, "Error in serverListener ctor: " + e.getMessage());
		}
		
		try{
			fromPC = new ObjectInputStream(sock.getInputStream());
			toPC = new ObjectOutputStream(sock.getOutputStream());
		}catch(Exception e){
			Log.d(TAG, "Error creating ObjectStreams ctor: " + e.getMessage());
		}
		
		// Initiate the SMS listener on the phone
		SmsListener.setServListener(this);
		Log.d(TAG, "Listening for messages from PC forever...");
		
		int sendCode = 0;
		TextMessage txtMsg = null;
		while(fromPC != null && sendCode == 0)
		{
			try {
				txtMsg = (TextMessage) fromPC.readObject();
				if(txtMsg != null){
					sendCode = phoneSendText(txtMsg);
				}				
			} catch (Exception e) {	
				Log.d(TAG, "Socket was broken, closing the port");
				return sock;
			}
		}
		return sock;
	}
	
	public void sendMsgToPC(TextMessage msg)
	{
		Log.d(TAG, "Sending msg to PC: " + msg.getContent());
		try {
			toPC.writeObject(msg);
		} catch (IOException e) {
			Log.d(TAG, "Error in sendMsgToPC: " + e.getMessage());
		}		
	}
	
	private int phoneSendText(TextMessage msg)
    {
    	try{
    		SmsManager man = SmsManager.getDefault();
    		man.sendTextMessage(msg.getReceiver().getPhoneNumber(), null, msg.getContent(), null, null);
    		Log.d(TAG, "SMS sent to " + msg.getReceiver().getFirstName() + "!");
    		return 0;
    	} catch (Exception e){
    		Log.d(TAG, "SMS delivery failed.");
    		return -1;
    	}
    }
}
