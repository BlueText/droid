package bigsky;

import java.io.Serializable;

public class TextMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2147500987192903441L;
	private Contact sender;
	private Contact receiver;
	private String content;
	
	public TextMessage(Contact start_sender, Contact start_receiver, String start_content){
		sender = start_sender;
		receiver = start_receiver;
		content = start_content;
	}
	
	public void setSender(Contact con){
		sender = con;
	}
	
	public void setReceiver(Contact con){
		receiver = con;
	}
	
	public void setContent(String con){
		content = con;
	}
	
	public Contact getSender(){
		return sender;
	}
	
	public Contact getReceiver(){
		return receiver;
	}
	
	public String getContent(){
		return content;
	}

}
