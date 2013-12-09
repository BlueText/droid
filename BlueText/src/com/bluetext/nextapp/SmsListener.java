package com.bluetext.nextapp;

import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import bigsky.Contact;
import bigsky.TextMessage;

/**
 * Sms listner that gets attached to the phone's inbox when the 
 * ServerListener establishes a connection witht the PC.
 * @author Andy Guibert
 */
public class SmsListener extends BroadcastReceiver
{
    private final String TAG = "AGG";
    private static ServerListener servListener = null;
    private static ConcurrentLinkedQueue<TextMessage> messageQueue = new ConcurrentLinkedQueue<TextMessage>();

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
                        messageSender = formatPhoneNumber(messages[i].getOriginatingAddress().toCharArray());
                        String msgBody = messages[i].getMessageBody();
                        from = Global.numberToContact.get(messageSender);
                        if(from == null){
                        	from = new Contact(messageSender, "", messageSender, "");
                        	Global.numberToContact.put(messageSender, from);
                        	if(servListener != null){
                        		ServerListener.sendObjectToPC(from);
                        	}
                        }
                        Log.d(TAG, "Got msg: " + msgBody);
                        Log.d(TAG, "Sender: " + from.getFirstName() + " " + from.getLastName());                        
                        curMsg = new TextMessage(from, MainActivity.userContact, msgBody);
                        if(servListener != null){
                        	while(!messageQueue.isEmpty()){
                        		Log.d(TAG, "Sending queued message to PC.");
                        		ServerListener.sendObjectToPC(messageQueue.remove());
                        	}
                        	ServerListener.sendObjectToPC(curMsg);
                        }  
                        else{
                        	messageQueue.add(curMsg);
                        }
                    }
                }catch(Exception e){
                	Log.d(TAG, "Error in SMSListener: " + e.getMessage());
                }
            }
        }
    }
    
    public static void setServerListener(ServerListener listener){
    	servListener = listener;
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
