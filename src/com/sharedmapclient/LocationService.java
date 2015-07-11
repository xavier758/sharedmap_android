package com.sharedmapclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;

public class LocationService extends Service
{
	 private LocationManager			locationMgr				= null;
	 private LocationListener		onLocationChange	= new LocationListener()
	 {
		 @Override
		 public void onStatusChanged(String provider, int status, Bundle extras)
		 {
		 }
		
		 @Override
		 public void onProviderEnabled(String provider)
		 {
		 }
		
		 @Override
		 public void onProviderDisabled(String provider)
		 {
		 }
		
		 @Override
		 public void onLocationChanged(Location location)
		 {
			 Context ctx = getApplicationContext();
			 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			 String tel = prefs.getString("tel","");
			 Double latitude = location.getLatitude();
			 Double longitude = location.getLongitude();
		 
			String request = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/position/setPosition?";
			request += "tel="+tel;
			request += "&lattitude="+latitude;
			request += "&longitude="+longitude;
			request += "&date="+Calendar.getInstance().getTimeInMillis();
	
			HttpPut uri = new HttpPut(request); 
	
			try {
				new DefaultHttpClient().execute(uri);
			} catch (ClientProtocolException e2) {
				e2.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		 }
	 };
	 @Override
	 public IBinder onBind(Intent arg0)
	 {
	 return null;
	 }
	
	 @Override
	 public void onCreate()
	 {
		 StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		 StrictMode.setThreadPolicy(policy);
		 locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		 locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000,
		 0, onLocationChange);
		 locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 0,
		 onLocationChange);
		 super.onCreate();
	 }
	
	 @Override
	 public int onStartCommand(Intent intent, int flags, int startId)
	 {
	
	 return super.onStartCommand(intent, flags, startId);
	 }
	
	 @Override
	 public void onDestroy()
	 {
	 super.onDestroy();
	 locationMgr.removeUpdates(onLocationChange);
	 }

}