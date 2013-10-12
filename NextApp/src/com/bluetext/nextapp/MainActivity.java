package com.bluetext.nextapp;

import java.net.Socket;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	private final String TAG = "AGG";
	AsyncTask<Integer, Void, Socket> task;
	Socket sock = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		task = new ServerListener().execute(Integer.valueOf(1301));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void connectToServer(View view){
		if(sock == null){
			try {
				sock = task.get();
				Log.d(TAG, "Got socket ok");
			} catch (Exception e){
				Log.e(TAG, "Error in serverListener ctor: " + e.getMessage());
			}
		}
		else{
			Log.d(TAG, "Already connected to socket.");
		}
    }
	
}
