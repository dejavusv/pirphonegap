package com.ecmxpert.pirphonegap;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class PIRPlugin extends CordovaPlugin {
	private Context thisContext;

	private String interval="";
	private String dulation="";
	public int PIRDetect =2;

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {

		if (action.equals("ioioStartup")) {
			System.out.println("startup IOIO service");
            this.ioioStartup(callbackContext); 
            return true;
        }
		if (action.equals("ioiogetdata")) {
            this.ioioGetdata(callbackContext); 
            return true;
        }
    
        return false;
	}

	
    // Initialise IOIO service (Called from Javascript)
    private void ioioStartup(CallbackContext callbackContext) {
    	// Initialise the service variables and start it it up
        
        // Setup a method to receive messages broadcast from the IOIO
	    callbackContext.success("status up");
    }
    
    private void ioioGetdata(CallbackContext callbackContext) {
    	String message = String.valueOf(PIRDetect);
    //	System.out.println("PIR Detect :"+message);
    	

    	if (message != null && message.length() > 0) { 
            callbackContext.success(message);
        	//return true;
        } else {
            callbackContext.error("IOIO.java Expected one non-empty string argument.");
        }
    }
    

    
    
}
