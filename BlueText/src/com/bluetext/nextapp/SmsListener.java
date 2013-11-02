package com.bluetext.nextapp;

import java.util.LinkedList;
import java.util.Queue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import bigsky.Contact;
import bigsky.TextMessage;

public class SmsListener extends BroadcastReceiver
{
    @SuppressWarnings("unused")
	private SharedPreferences preferences;
    private final String TAG = "AGG";
    private static ServerListener servListener = null;
    Queue<TextMessage> messageQueue = new LinkedList<TextMessage>();
    Contact andy = new Contact("Andy", "Guibert", "15072542815", null);

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] messages = null;
            String messageSender;
            TextMessage curMsg;
            Contact from;
            if (bundle != null){
                try{
                    Object[] pduArray = (Object[]) bundle.get("pdus");
                    messages = new SmsMessage[pduArray.length];
                    for(int i=0; i<messages.length; i++){
                        messages[i] = SmsMessage.createFromPdu((byte[])pduArray[i]);
                        messageSender = messages[i].getOriginatingAddress();
                        messageSender = messageSender.replace("+", "").replace("(", "").replace(")", "").replace(" " , "");
                        String msgBody = messages[i].getMessageBody();
                        from = new Contact(null, null, messageSender, null);
                        Log.d(TAG, "Got msg: " + msgBody);
                        Log.d(TAG, "Sender: " + messageSender);                        
                        curMsg = new TextMessage(null, from, msgBody);
                        servListener.sendMsgToPC(curMsg);                        
                    }
                }catch(Exception e){
                	Log.d(TAG, "Error in SMSListener: " + e.getMessage());
                }
            }
        }
    }
    
    public static void setServerListener(ServerListener listener)
    {
    	servListener = listener;
    }    
}
