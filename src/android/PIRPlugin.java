package com.ecmxpert.pirphonegap;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;


import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PIRPlugin extends CordovaPlugin {
	private Context thisContext;
	private Intent ioioService;
	private Intent broadcastIntent = new Intent("msgIOIO");
	private String interval="5";
	private String dulation="6";
	public int PIRDetect =2;
	private CallbackContext connectionCallbackContext; // for callback startup IOIO
	private CallbackContext connectionCallbackMotion; // for callback Detect Motion sensor
	
	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {

		if (action.equals("ioioStartup")) {
			System.out.println("startup IOIO service");
		//	callbackContext.success("status up");
            		this.ioioStartup(callbackContext); 
            return true;
        }
		if (action.equals("ioiogetdata")) {
            this.ioioGetdata(callbackContext); 
            return true;
        }
        if (action.equals("ioioSendMessage")) {
            this.ioioSendMessage(args.getString(0),args.getString(1),args.getString(2) ,callbackContext); 
            return true;
        }        
        return false;
	}

    // Initialise IOIO service (Called from Javascript)
    private void ioioStartup(CallbackContext callbackContext) {
    	// Initialise the service variables and start it it up
    	//thisContext = this.cordova.getActivity().getApplicationContext();
    	//ioioService = new Intent(thisContext, PIRMotionService.class);
        //thisContext.startService(ioioService); 
        //System.out.println("start service");
        
        // Setup a method to receive messages broadcast from the IOIO
        LocalBroadcastManager.getInstance(thisContext).registerReceiver(
                mMessageReceiver, 
                new IntentFilter("returnIOIOdata")
        ); 
	   // callbackContext.success("status up");
        
        this.connectionCallbackContext = callbackContext;
		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
    	pluginResult.setKeepCallback(true);
    	callbackContext.sendPluginResult(pluginResult);
    	
    	PluginResult result = new PluginResult(PluginResult.Status.OK, interval+":/"+dulation);
        result.setKeepCallback(true);
        connectionCallbackContext.sendPluginResult(result);
         /*
    	cordova.getThreadPool().execute(new Runnable() {
            public void run() {
            	callbackContext.success("status up");
            	
            	while(interval.equalsIgnoreCase("")&& dulation.equalsIgnoreCase("")){
            		try{
            			Thread.sleep(1000);
            		}catch(Exception ex){
            			
            		}
            	}
            	
            	PluginResult result = new PluginResult(PluginResult.Status.OK, interval+":/"+dulation);
            	result.setKeepCallback(true);
            	connectionCallbackContext.sendPluginResult(result);
            	
            }
        });*/
    }
    
    private void ioioGetdata(CallbackContext callbackContext) {
    	String message = String.valueOf(PIRDetect);
    //	System.out.println("PIR Detect :"+message);
    	

    	if (message != null && message.length() > 0) { 
            //callbackContext.success(message);
            
            this.connectionCallbackMotion = callbackContext;
    		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
        	pluginResult.setKeepCallback(true);
        	callbackContext.sendPluginResult(pluginResult);
        	cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                	while(true){
                		PluginResult result = new PluginResult(PluginResult.Status.OK, String.valueOf(PIRDetect));
                    	result.setKeepCallback(true);
                    	connectionCallbackMotion.sendPluginResult(result);
                		try{
                			Thread.sleep(300);
                		}catch(Exception ex){
                			ex.printStackTrace();
                		}
                	}
                }
            });
        	//return true;
        } else {
            callbackContext.error("IOIO.java Expected one non-empty string argument.");
        }
    }
    
    // Receive message from the IOIO device
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		PIRDetect = intent.getIntExtra("PIRDetect", -1);
    	//	System.out.println("mMessageReceiver : "+intent.getStringExtra("setinterval"));
    	//	System.out.println("mMessageReceiver : "+intent.getStringExtra("setdulation"));
    		if(intent.getStringExtra("setinterval")!= null){
    			interval = intent.getStringExtra("setinterval");
    		}
    		if(intent.getStringExtra("setdulation")!= null){
    			dulation =  intent.getStringExtra("setdulation");
    		}
    		
    		
    	}
    };
    
    // Send a message to IOIO service
    private void ioioSendMessage(String command,String dulation,String interval, CallbackContext callbackContext){
    	// Which vars to send  
    	broadcastIntent.putExtra("command", command);
    	broadcastIntent.putExtra("dulation", dulation);
    	broadcastIntent.putExtra("interval", interval);
    	// Send the message to the IOIO
        LocalBroadcastManager.getInstance(thisContext).sendBroadcast(broadcastIntent);
    }
    
    
}
