package com.bluetext.nextapp;


import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SQLLoginActivity extends AsyncTask<String, String, String>
{
	private final String TAG = "AGG";
	private static String sqlResult = null;
	
	@Override
	protected String doInBackground(String... params) 
	{
		Connection conn = null;
		try {									
			Class.forName("com.mysql.jdbc.Driver");
		
			conn = DriverManager.getConnection("jdbc:mysql://mysql.cs.iastate.edu/db30901", "adm309", "EXbDqudt4");
			
			ResultSet rs = conn.createStatement().executeQuery(params[0]);
			
			if(rs.next() == false){
				Log.d(TAG, "Username/password were invalid");
				sqlResult = "Username or password not found.";
				return sqlResult;
			}
			Log.d(TAG, rs.getString("phoneNumber"));
			Log.d(TAG, rs.getString("password"));
			rs.close();		
			
			conn.close();
			
			Log.d(TAG, "Login successful!");
			sqlResult = "Login Successful!";
			return sqlResult;
		} catch (SQLException e){
			sqlResult = "Unable to connect to login database.\nLoginFailed";
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
	
	protected void onPostExecute(String result){
		MainActivity.checkLogin(sqlResult);
	}
}
