package com.bluetext.nextapp;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Async task for uploading a given IP into the MySQL table
 * @author Andrew
 */
public class PutIPActivity extends AsyncTask<String, String, String>
{
	private final String TAG = "AGG";
	private static String sqlResult = null;
	
	@Override
	protected String doInBackground(String... params) 
	{
		Connection conn = null;
		try {						
			// Get the mySQL driver jar and open a connection to the database
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://mysql.cs.iastate.edu/db30901", "adm309", "EXbDqudt4");
			
			// Get the phone IP and store it into the database
			String phone_IP = InetAddress.getLocalHost().getHostAddress();			
			String sqlString = "UPDATE testTable SET IP_Phone='" + phone_IP + "' WHERE phoneNumber='" + params[0] + "'";			
			if(conn.createStatement().executeUpdate(sqlString) != 1){
				throw new SQLException("Expected only 1 row to be updated");
			}
						
			sqlResult = "Login Successful!";
			return sqlResult;
		} catch (SQLException e){
			sqlResult = "Unable to connect to login database in PutIPActivity.";
			Log.d(TAG, sqlResult);
			return sqlResult;
		} catch (Exception e) {
			Log.d(TAG, "Unknown exception caught in SQLActivity.");
			Log.d(TAG, e.getMessage() + "\n" + e.getCause());
			sqlResult = "An unknown exception occurred while logging in.";
			return sqlResult;
		} finally{
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {}
			}			
		}
	}

}
