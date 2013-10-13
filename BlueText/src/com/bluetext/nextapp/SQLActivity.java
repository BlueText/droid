package com.bluetext.nextapp;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import android.os.AsyncTask;
import android.util.Log;

public class SQLActivity extends AsyncTask<String, String, Integer>
{
	private final String TAG = "AGG";

	@Override
	protected Integer doInBackground(String... params) 
	{
		Connection conn = null;
		try {									
			Class.forName("com.mysql.jdbc.Driver");
		
			conn = DriverManager.getConnection("jdbc:mysql://mysql.cs.iastate.edu/db30901", "adm309", "EXbDqudt4");
			
			ResultSet rs = conn.createStatement().executeQuery(params[0]);
			//stmt.executeUpdate("insert into testTable (phoneNumber, password) values ('1231234567','mypassword')");
			
			if(rs.next() == false){
				Log.d(TAG, "Username/password were invalid");
				return Integer.valueOf(-1);
			}
			Log.d(TAG, rs.getString("phoneNumber"));
			Log.d(TAG, rs.getString("password"));
			rs.close();		
			
			conn.close();
			
			Log.d(TAG, "Login successful!");
			return Integer.valueOf(0);
		} catch (Exception e) {
			return Integer.valueOf(-1);
		} finally{
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {}
			}			
		}
	}
}
