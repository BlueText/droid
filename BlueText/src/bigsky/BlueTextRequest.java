package bigsky;

import java.io.Serializable;

/**
 * Request object for communication between PC and android
 * @author Andrew
 */
public class BlueTextRequest implements Serializable{

	private static final long serialVersionUID = -4688360178870423350L;
		
	/**
	 * Requests used to ask for data from the phone:
	 *  <LI><B>PING:</B> Tests the connection between phone and computer
	 *  <LI><B>CONTACT_CHAT_HISTORY:</B> Asks for ArrayList of TextMessages 
	 *    representing chat history with that user or users.
	 *  <LI><B>CONTACT_PICTURE:</B> Not completed yet
	 *  <LI><B>BATTERY_PERCENTAGE:</B> Asks for phone battery level (0 - 100)
	 *  <LI><B>SUBMIT_NEW_CONTACT:</B> Send a Contact object that has been
	 *    created in PC client and should be added to phone contacts.
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
	
	/**
	 * Validate the Contact c that this request was constructed with
	 * @return true = INVALID contact   false = VALID contact
	 */
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
