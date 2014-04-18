package com.bluetext.nextapp;

import java.util.HashMap;
import bigsky.Contact;

/**
 * Public class for storing session persistant information
 * @author Andy Guibert
 */
public class Global {
	public static HashMap<String, Contact> numberToContact = new HashMap<String, Contact>();
	
	// DB info for ISU database
//	public final static String DATABASE_URL = "jdbc:mysql://mysql.cs.iastate.edu/db30901";
//	public final static String DATABASE_USERNAME = "adm309";
//	public final static String DATABASE_PASSWORD = "EXbDqudt4";
//	public final static String DATABASE_TABLENAME = "testTable";
	
	// DB info fo AWS RDS instance
	public final static String DATABASE_URL = "jdbc:mysql://bluetextdb.c2e4o2jtraid.us-west-2.rds.amazonaws.com:3306/blueTextDB";
	public final static String DATABASE_USERNAME = "aguibert";
	public final static String DATABASE_PASSWORD = "bluetextpass";
	public final static String DATABASE_TABLENAME = "testTable";
}
