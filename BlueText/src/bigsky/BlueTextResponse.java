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
	
	public BlueTextResponse(BlueTextRequest origReq, Object... objs){
		this.originalRequest = origReq;
		this.requestType = origReq.getRequest();
		
		if(BlueTextRequest.REQUEST.CONTACT_CHAT_HISTORY == requestType)
		{
			chatHistory = (ArrayList<TextMessage>) objs[0];
		}
		else if(BlueTextRequest.REQUEST.BATTERY_PERCENTAGE == requestType){
			batteryLevel = (Integer) objs[0];
		}
	}
	
	public ArrayList<TextMessage> getChatHistory(){
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
}
