package com.ecmxpert.pirphonegap;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


public class PIRPlugin extends CordovaPlugin {
	private Context thisContext;

	private String interval="";
	private String dulation="";
	public int PIRDetect =2;

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {

		if (action.equals("ioioStartup")) {
			 callbackContext.success("status up");
            return true;
        }
		if (action.equals("ioiogetdata")) {
             callbackContext.success("status up");
            return true;
        }
    
        return false;
	}

}
