package bigsky;

import java.io.Serializable;

public class BlueTextRequest implements Serializable{

	private static final long serialVersionUID = -4688360178870423350L;
		
	/**
	 * <pre>Requests used to ask for data from the phone:
	 *  - PING: Tests the connection between phone and computer
	 *  - CONTACT_CHAT_HISTORY: Asks for ArrayList of TextMessages 
	 *    representing chat history with that user or users.
	 *  - CONTACT_PICTURE: Not completed yet
	 *  - BATTERY_PERCENTAGE: Asks for phone battery level (0 - 100)
	 *  - SUBMIT_NEW_CONTACT: Send a Contact object that has been
	 *    created in PC client and should be added to phone contacts.
	 * </pre>
	 */
	public enum REQUEST {
		PING,
		CONTACT_CHAT_HISTORY,
		CONTACT_PICTURE,
		BATTERY_PERCENTAGE,
		SUBMIT_NEW_CONTACT
	}
	
	private final REQUEST request;
	private final Contact contact;
	
	/**
	 * <pre>Constructor for a request.
	 */
	public BlueTextRequest(REQUEST rq, Contact c)
	{
		this.request = rq;
		
		this.contact = c;
		if(checkContactArg(rq, c)){
			throw new RuntimeException("Contact provided was invalid.");
		}
	}
	
	public REQUEST getRequest(){
		return this.request;
	}
	
	public Contact getContact(){
		return this.contact;
	}
	
	private boolean checkContactArg(REQUEST r, Contact c){
		if(r == REQUEST.CONTACT_CHAT_HISTORY
		|| r == REQUEST.CONTACT_PICTURE
		|| r == REQUEST.SUBMIT_NEW_CONTACT){
			if(c == null || c.getPhoneNumber() == null){
				return true;
			}
			return false;
		}
		else{
			return false;
		}
	}
}
