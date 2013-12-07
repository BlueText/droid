package com.bluetext.nextapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * This activity is a secondary screen to the Android app where the 
 * phone us tethered to the PC.
 * @author Andy Guibert
 */
public class PostLoginActivity extends Activity {	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_login);  
        ServerListener.pla = this;
        Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
    }

    /**
     * If the logout button is pressed on the post login screen
     * this method is called in order to shut down the connections
     * and return the user to the login screen.
     * @param view
     */
    public void doLogout(View view)
	{    	
    	ServerListener.closeStream();
    	MainActivity.getAllContacts = new GetAllContactsActivity().execute();
    	finish();
	}
}
