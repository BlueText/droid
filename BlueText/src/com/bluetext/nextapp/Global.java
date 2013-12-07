package com.bluetext.nextapp;

import java.util.HashMap;
import bigsky.Contact;

/**
 * Public class for storing session persistant information
 * @author Andy Guibert
 */
public class Global {
	public static HashMap<String, Contact> numberToContact = new HashMap<String, Contact>();
}
