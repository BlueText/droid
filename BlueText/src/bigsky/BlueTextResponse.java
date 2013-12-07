package bigsky;

import java.io.Serializable;
import java.util.ArrayList;

import bigsky.BlueTextRequest.REQUEST;

/**
 * Response object for communication between PC and Android
 * @author Andrew
 *
 */
public class BlueTextResponse implements Serializable{

	private static final long serialVersionUID = -6724604542837571918L;

	private final BlueTextRequest originalRequest;
	private final REQUEST requestType;
	private ArrayList<TextMessage> chatHistory;
	private int batteryLevel;
	private byte[] contactPicture;
	private String contactImageString;
	private boolean useImage;
	private final String NO_IMG = "NO_IMG";
	
	/**
	 * A Response must be created with the original BlueTextRequest and
	 * additional arguments depending on what the original request is:
	 * <TABLE>
		  <THEAD>
		    <tr><th>REQUEST</th><th>Objects...</th></tr>
		  </THEAD>
		  <tbody>
		     <tr><td>CONTACT_CHAT_HISTORY</td>  <td>0: ArrayList{TextMessage}</td></tr>
		     <tr><td>BATTERY_PERCENTAGE</td>    <td>0: Integer</td></tr>
		     <tr><td>CONTACT_PICTURE</td>       <td>0: Boolean 1: obj[0] ? Byte[] : String</td></tr>
		  </tbody>
	 * </TABLE>
	 * @param origReq
	 * @param objs
	 */
	@SuppressWarnings("unchecked")
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
			if(objs[1] == null){
				contactPicture = null;
				contactImageString = NO_IMG;
			}
			else if(useImage){
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
	
	/**
	 * Retrieve the chat history of this response.
	 * NOTE: Must check requetType before accessing this method
	 * @return The chat history contained in this response.
	 * @throws RuntimeException if original request was not for CONTACT_CHAT_HISTORY
	 */
	public ArrayList<TextMessage> getChatHistory(){
		if(BlueTextRequest.REQUEST.CONTACT_CHAT_HISTORY != requestType){
			throw new RuntimeException("Original request was not for chat history.");
		}
		return this.chatHistory;
	}
	
	/**
	 * Get the original BlueTextRequest that this object is a response to.
	 */
	public BlueTextRequest getOriginalRequest(){
		return this.originalRequest;
	}
	
	/**
	 * Retrieve the battery level of the phone.
	 * NOTE: Must check requetType before accessing this method.
	 * @return The battery level of the phone (1-100)
	 * @throws RuntimeException if original request was not for BATTERY_PERCENTAGE
	 */
	public int getBatteryLevel(){
		if(BlueTextRequest.REQUEST.BATTERY_PERCENTAGE != requestType){
			throw new RuntimeException("Original request was not for battery level.");
		}
		return batteryLevel;
	}
	
	/**
	 * Get the image resource for the contact's picture.
	 * NOTE: Must check requetType before accessing this method.
	 * @return 1: If a photo was located for the contact, returns byte[]<br>
	 * 2: If a facebook profile picture was located, returns String<br>
	 * 3: If no photo is associated with this contact, returns null<br>
	 * @throws RuntimeException if original request was not for CONTACT_PICTURE
	 */
	public Object getImageResource(){
		if(BlueTextRequest.REQUEST.CONTACT_PICTURE != requestType){
			throw new RuntimeException("Original request was not for contact picture.");
		}
		if(useImage){
			return contactPicture;
		}
		else{
			return contactImageString;
		}
	}
}
