package com.bluetext.nextapp;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PostLoginActivity extends Activity {	
	private static final String TAG = "AGG";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_login);  
        ServerListener.pla = this;
        Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
    }

    public void doLogout(View view)
	{    	
    	ServerListener.pla = null;
    	try {
			if(ServerListener.fromPC != null)
				ServerListener.fromPC.close();
    	} catch(IOException e) {}
    	try{
			if(ServerListener.toPC != null)
				ServerListener.toPC.close();
		} catch (IOException e) {}
    	MainActivity.getAllContacts = new GetAllContactsActivity().execute();
    	finish();
	}
}
