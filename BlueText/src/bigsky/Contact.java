package bigsky;

import java.io.Serializable;

public class Contact implements Serializable {

	private static final long serialVersionUID = 1394646218153182251L;
	private String first_name;
	private String last_name;
	private String phone_number;
	private String second_phone;

	public Contact(String start_first_name, String start_last_name, String start_phone_number, String start_second_phone) {
		if (start_first_name == null || start_first_name.equals("")){
			throw new IllegalArgumentException();
		}
		if (start_last_name == null){
			start_last_name = "";
		}
		if (start_phone_number == null || start_phone_number.length() < 3){
			throw new IllegalArgumentException();
		}
		if (start_second_phone == null){
			start_second_phone = "";
		}
		first_name = start_first_name;
		last_name = start_last_name;
		phone_number = start_phone_number;
		second_phone = start_second_phone;
	}

	public void setFirstName(String name) {
		if (name.equals("") || name == null){
			throw new IllegalArgumentException();
		}
		first_name = name;
	}

	public void setLastName(String name) {
		if (name == null){
			name = "";
		}
		last_name = name;
	}

	public void setPhoneNumber(String number){
		if (number.equals("") || number == null){
			throw new IllegalArgumentException();
		}
		phone_number = number;
	}

	public void setSecondPhone(String number){
		second_phone = number;
	}

	public String getFirstName(){
		return first_name;
	}

	public String getLastName(){
		return last_name;
	}

	public String getPhoneNumber(){
		return phone_number;
	}

	public String getSecondPhone(){
		return second_phone;
	}

}