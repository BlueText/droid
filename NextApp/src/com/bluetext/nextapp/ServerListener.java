package com.bluetext.nextapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import bigsky.TextMessage;

public class ServerListener extends AsyncTask<Integer, Void, Socket>
{
	Socket sock;
	int port;
	ObjectInputStream fromPC;
	ObjectOutputStream toPC;
	private final String TAG = "AGG";
	
	protected Socket doInBackground(Integer... params)
	{
		Log.d(TAG, "Creating socket on port: " + params[0]);
		this.port = params[0];
		
		try{
			sock = new Socket("206.127.186.13", 1300); // andy's PC
		}catch(Exception e){
			Log.d(TAG, "Error in serverListener ctor: " + e.getMessage());
		}
		
		try{
			fromPC = new ObjectInputStream(sock.getInputStream());
			toPC = new ObjectOutputStream(sock.getOutputStream());
		}catch(Exception e){
			Log.d(TAG, "Error creating ObjectStreams ctor: " + e.getMessage());
		}
		
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
    		Log.d(TAG, "SMS failed.");
    		return -1;
    	}
    }
}
