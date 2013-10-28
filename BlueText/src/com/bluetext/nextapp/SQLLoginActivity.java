package com.bluetext.nextapp;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;

public class SQLLoginActivity extends AsyncTask<String, String, String>
{
	private final String TAG = "AGG";
	private static String sqlResult = null;
	String phoneNumber;
	String password;
	String IPaddr;
	
	@Override
	protected String doInBackground(String... params) 
	{
		Connection conn = null;
		this.phoneNumber = params[0];
		this.password    = params[1];
		try {									
			// Grab the mySQL driver from libs and get connection to our database
			Class.forName("com.mysql.jdbc.Driver");		
			conn = DriverManager.getConnection("jdbc:mysql://mysql.cs.iastate.edu/db30901", "adm309", "EXbDqudt4");
			
			// Run a select statement to make sure that phoneNumber and password entered are valid
			String verifyUserAndPass = "SELECT * FROM testTable WHERE phoneNumber='" + phoneNumber + "' AND password='" + password + "'";
			ResultSet rs = conn.createStatement().executeQuery(verifyUserAndPass);			
			if(rs.next() == false){
				Log.d(TAG, "Username/password were invalid");
				sqlResult = "Username or password not found.";
				return sqlResult;
			}
			Log.d(TAG, rs.getString("phoneNumber"));
			Log.d(TAG, rs.getString("password"));
			rs.close();		
			
			// If we get this far, lets get the Computer IP for this user
			String getIP = "SELECT IP_Computer FROM testTable WHERE phoneNumber='" + phoneNumber + "'";
			ResultSet rs1 = conn.createStatement().executeQuery(getIP);			
			if(rs1.next() == false){
				Log.d(TAG, "Computer IP was not found for user " + phoneNumber);
				sqlResult = "Log in on computer client and retry.";
				return sqlResult;
			}
			this.IPaddr =  rs1.getString("IP_Computer");
			Log.d(TAG, "Got computer IP: " + IPaddr);
			rs1.close();	
			
			conn.close();
			
			sqlResult = "Login Successful! " + IPaddr;
			return sqlResult;
		} catch (SQLException e){
			sqlResult = "Unable to connect to login database in SQLLoginActivity.";			
			return sqlResult;
		} catch (Exception e) {
			Log.d(TAG, "Unknown exception caught in SQLActivity.");
			Log.d(TAG, e.getMessage() + "\n" + e.getCause());
			sqlResult = "An unknown exception occurred while logging in.";
			return sqlResult;
		} finally{
			Log.d(TAG, sqlResult);
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {}
			}			
		}
	}
	
	protected void onPostExecute(String result){
		MainActivity.checkLogin(sqlResult);
	}
}
