package com.sharedmapclient;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;

// here is the OnRecieve methode which will be called when boot completed
public class BootCompleted extends BroadcastReceiver{

	static boolean started = false;
	
     @Override
     public void onReceive(Context context, Intent intent) {
	 //we double check here for only boot complete event
	 if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
	   {
	     //here we start the service             
	     Intent serviceIntent = new Intent(context, LocationService.class);
	     if (!started){
	    	 context.startService(serviceIntent);
	    	 started = true;
	     }
	   }
     }
}