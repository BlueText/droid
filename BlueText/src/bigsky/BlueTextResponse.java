package bigsky;

import java.io.Serializable;
import java.util.ArrayList;

import bigsky.BlueTextRequest.REQUEST;

public class BlueTextResponse implements Serializable{

	private static final long serialVersionUID = -6724604542837571918L;

	private final BlueTextRequest originalRequest;
	private final REQUEST requestType;
	private ArrayList<TextMessage> chatHistory;
	private int batteryLevel;
	private byte[] contactPicture;
	private String contactImageString;
	private boolean useImage;
	
	public BlueTextResponse(BlueTextRequest origReq, Object... objs){
		this.originalRequest = origReq;
		this.requestType = origReq.getRequest();
		
		if(BlueTextRequest.REQUEST.CONTACT_CHAT_HISTORY == requestType){
			chatHistory = (ArrayList<TextMessage>) objs[0];
		}
		else if(BlueTextRequest.REQUEST.BATTERY_PERCENTAGE == requestType){
			batteryLevel = (Integer) objs[0];
		}
		else if(BlueTextRequest.REQUEST.CONTACT_PICTURE == requestType){
			useImage = ((Boolean) objs[0]).booleanValue();
			if(useImage){
				contactPicture = (byte[]) objs[1];
			}
			else{
				contactImageString = (String) objs[1];
			}
		}
		else{
			throw new RuntimeException("Unsupported request on this response.");
		}
	}
	
	public ArrayList<TextMessage> getChatHistory(){
		if(BlueTextRequest.REQUEST.CONTACT_CHAT_HISTORY != requestType){
			throw new RuntimeException("Original request was not for chat history.");
		}
		return this.chatHistory;
	}
	
	public BlueTextRequest getOriginalRequest(){
		return this.originalRequest;
	}
	
	public int getBatteryLevel(){
		if(BlueTextRequest.REQUEST.BATTERY_PERCENTAGE != requestType){
			throw new RuntimeException("Original request was not for battery level.");
		}
		return batteryLevel;
	}
	
	public Object getImageResource(){
		if(useImage){
			return contactPicture;
		}
		else{
			return contactImageString;
		}
	}
}
