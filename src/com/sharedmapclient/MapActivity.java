package com.sharedmapclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MapActivity extends FragmentActivity {

	// ########## TEST #########################
	private final LatLng LYON = new LatLng(45.767, 4.833);
	
	// ########## FIN TEST #####################
	
	private final String USER_DATA_SEP = "////";
	private final float MARKER_TAG = (float) 0.99;
	
	GoogleMap map;
	EditText editSearchLocation;
	Button btnSearchLocation;
	Button btnSearchFriends;
	
	List<Marker> markers;
	List<Marker> users;
	Marker userMarker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		 StrictMode.setThreadPolicy(policy);
		
		// ####################################
		// 				INIT
		// ####################################
		// display HASHTAG of the group in the title
		setTitle(getIntent().getStringExtra("hashtag"));
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		markers = new ArrayList<Marker>();
		users = new ArrayList<Marker>();
		
		// show my current position
		map.setMyLocationEnabled(true);
		
		// set custom info window for markers
		map.setInfoWindowAdapter(new InfoWindowAdapter() {
			
			@Override
			public View getInfoWindow(Marker arg0) {
				return null;
			}
			
			@Override
			public View getInfoContents(Marker m) {
				ViewGroup v = (ViewGroup)getLayoutInflater().inflate(R.layout.info_map_marker, null);
				TextView title = (TextView)v.findViewById(R.id.txtTitle);
				TextView desc = (TextView)v.findViewById(R.id.txtDesc);
				TextView lastDate = (TextView)v.findViewById(R.id.txtLastDate);
				ImageView avatar = (ImageView)v.findViewById(R.id.imgAvatar);
				
				
				// CASE EVENT MARKER
				if(m.getAlpha() == MARKER_TAG)
				{
					title.setText(m.getTitle());
					desc.setText(m.getSnippet());
				}
				// CASE USER MARKER
				else
				{
					String[] data = m.getTitle().split(USER_DATA_SEP);
					String pseudo = data[0];
					String email = data[1];
					String hash = UtilMD5.stringToMd5Hex(email);
					
					title.setText(pseudo);
					lastDate.setText(m.getSnippet());
					
					try {
						URL url = new URL("http://www.gravatar.com/avatar/"+hash+"?s=100");
						Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
						avatar.setImageBitmap(bmp);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				
				
				return v;
				
			}
		});
		
		// load markers and users of the group from the server
		loadMarkers();
		loadUsers();
		
		// go to the position of the user
		if(userMarker != null){
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 15));
			userMarker.showInfoWindow();
		}
		
		editSearchLocation = (EditText)findViewById(R.id.editSearchLocation);
		btnSearchLocation = (Button)findViewById(R.id.btnSearchLocation);
		btnSearchFriends = (Button)findViewById(R.id.btnSearchSomeone);
		
		// #######################################
		// 			LISTENERS
		// #######################################
		// long click on map = add a marker
		map.setOnMapLongClickListener(new OnMapLongClickListener() {
		
			@Override
			public void onMapLongClick(LatLng pos)
			{
				MapAddMarkerDialog dlg = new MapAddMarkerDialog();
				dlg.setMarkerPos(pos);
				dlg.show(getSupportFragmentManager(), "marker");
			}
		});
		
		// find the location of a friend
		btnSearchFriends.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				ArrayList<String> friends = new ArrayList<String>();
				for(int i=0; i<users.size(); i++)
				{
					friends.add(users.get(i).getTitle().split(USER_DATA_SEP)[0]);
				}
				MapSearchFriendDialog dlg = MapSearchFriendDialog.newInstance(friends);
				dlg.show(getSupportFragmentManager(), "friends");
			}
		});
		
		// find location by name
		btnSearchLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String address = editSearchLocation.getText().toString();
				if(address.equals("")) { return; }
				
				Geocoder geoc = new Geocoder(getBaseContext());
				try
				{
					List<Address> res = geoc.getFromLocationName(address, 1);
					if(res.size() > 0){
						Address result = res.get(0);
						map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(result.getLatitude(), result.getLongitude())));
					}
					
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	public void onMapAddMarkerDialogPositiveClick(MapAddMarkerDialog dlg)
	{
		// PUSH MARKER TO SERVER
		String request = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/marqueur/addMarqueur?";
		request += "tel="+Conf.telUser;
		request += "&hashtag="+getIntent().getStringExtra("hashtag");
		request += "&desc="+dlg.getMarkerDescription();
		request += "&longitude="+dlg.getMarkerPos().longitude;
		request += "&lattitude="+dlg.getMarkerPos().latitude;
		request += "&fin="+dlg.getExpirationDate().getTimeInMillis();
		
		HttpPost uri = new HttpPost(request.replace(" ", "+"));    
		
		DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse resp=null;
		try {
			resp = client.execute(uri);
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		if(resp.getStatusLine().getStatusCode() != 200) { return; }
		
		// now get the marker data back from the server
		if (resp.getEntity() != null)
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder builder;
	    	Document doc= null;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(resp.getEntity().getContent());
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// and draw the new marker on the map
			drawMarkerOnMap(doc.getFirstChild().getChildNodes());
		}   
	}
	
	public void onMapSearchFriendDialogClick(int friendPositionInList)
	{
		users.get(friendPositionInList).showInfoWindow();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(users.get(friendPositionInList).getPosition(), 15));
	}
	
	private void loadMarkers()
	{
		// GET MARKERS FROM SERVER
		String request = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/marqueur/getMarqueurs?hashtag=";
		request += getIntent().getStringExtra("hashtag");
		request += "&tel="+Conf.telUser;

		HttpGet uri = new HttpGet(request.replace(" ", "+")); 

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse resp=null;
		try {
			resp = client.execute(uri);
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// IF ERROR, we don't display anything on the map
		if(resp.getStatusLine().getStatusCode() != 200) { return; }
		
		if (resp.getEntity() != null)
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder builder;
	    	Document doc= null;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(resp.getEntity().getContent());
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// get all markers from the list and draw them on the map
			NodeList markers = doc.getFirstChild().getChildNodes();
			for(int i=0; i<markers.getLength(); i++)
			{
				drawMarkerOnMap(markers.item(i).getChildNodes());
			}
		}   
	}
	
	private void loadUsers()
	{
		// GET USERS OF THE GROUP FROM SERVER
		String request = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/get?hashtag=";
		request += getIntent().getStringExtra("hashtag");

		HttpGet uri = new HttpGet(request.replace(" ", "+")); 

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse resp=null;
		try {
			resp = client.execute(uri);
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// IF ERROR, we don't display anything on the map
		if(resp.getStatusLine().getStatusCode() != 200) { return; }

		if (resp.getEntity() != null)
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			Document doc= null;
			try {
				builder = factory.newDocumentBuilder();
				doc = builder.parse(resp.getEntity().getContent());
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// get all users from the list and draw them on the map
			NodeList groupData = doc.getFirstChild().getChildNodes();
			for(int i=0; i<groupData.getLength(); i++)
			{
				if(groupData.item(i).getNodeName().equals("invites")){
					drawUserOnMap(groupData.item(i).getChildNodes());
				}
			}
		}   
	}
	
	private void drawMarkerOnMap(NodeList markerData)
	{
		String owner = "";
		String desc = "";
		double lat = 0;
		double longt = 0;
		
		for(int j=0; j<markerData.getLength(); j++)
		{
			if(markerData.item(j).getNodeName().equals("description")){
				desc = markerData.item(j).getFirstChild().getNodeValue();
			}
			else if(markerData.item(j).getNodeName().equals("createur")){
				NodeList ownerData =  markerData.item(j).getChildNodes();
				for(int k=0; k<ownerData.getLength(); k++)
				{
					if(ownerData.item(k).getNodeName().equals("pseudo")){
						owner = ownerData.item(k).getFirstChild().getNodeValue();
						break;
					}
				}
			}
			else if(markerData.item(j).getNodeName().equals("lattitude")){
				lat = Double.parseDouble(markerData.item(j).getFirstChild().getNodeValue());
			}
			else if(markerData.item(j).getNodeName().equals("longitude")){
				longt = Double.parseDouble(markerData.item(j).getFirstChild().getNodeValue());
			}
		}
		
		
		// draw marker on map
		Marker mark = map.addMarker(new MarkerOptions()
				.title("De "+owner)
				.snippet(desc)
				.alpha(MARKER_TAG)
				.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.btn_star_big_on))
				.position(new LatLng(lat, longt)));
		
		// show text above markers without click on it
		mark.showInfoWindow();
		
		// to make difference between event and user markers
		mark.setFlat(true);
		
		// reference marker
		this.markers.add(mark);
	}

	private void drawUserOnMap(NodeList userData)
	{
		String phone = "";
		String email = "";
		String name = "";
		String lastDate = "";
		double lat = 0;
		double longt = 0;
		
		boolean hasPosition = false;
		
		for(int i=0; i<userData.getLength(); i++)
		{
			if(userData.item(i).getNodeName().equals("email")){
				email = userData.item(i).getFirstChild().getNodeValue();
			}
			if(userData.item(i).getNodeName().equals("telephone")){
				phone = userData.item(i).getFirstChild().getNodeValue();
			}
			else if(userData.item(i).getNodeName().equals("pseudo")){
				name = userData.item(i).getFirstChild().getNodeValue();
			}
			else if(userData.item(i).getNodeName().equals("dernierePosition"))
			{
				hasPosition = true;
				NodeList positionData = userData.item(i).getChildNodes();
				for(int j=0; j<positionData.getLength(); j++)
				{
					if(positionData.item(j).getNodeName().equals("lattitude")){
						lat = Double.parseDouble(positionData.item(j).getFirstChild().getNodeValue());
					}
					else if(positionData.item(j).getNodeName().equals("longitude")){
						longt = Double.parseDouble(positionData.item(j).getFirstChild().getNodeValue());
					}
					else if(positionData.item(j).getNodeName().equals("time")){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ");
						Date d = null;
						try {
							d = sdf.parse(positionData.item(j).getFirstChild().getNodeValue());
						} catch (DOMException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						}
						if(d != null){
							sdf = new SimpleDateFormat("dd-MM 'à' HH:mm");
							lastDate = sdf.format(d);
						}
					}
				}
			}
		}
		
		// if we do not know the position of the user, do not draw it
		if(!hasPosition) { return; }
		
		// draw user on map
		Marker mark;
		Bitmap icon;
		
		if(phone.equals(Conf.telUser)){
			icon = ((BitmapDrawable)getResources().getDrawable(R.drawable.map_user_green)).getBitmap();
		}
		else{
			icon = ((BitmapDrawable)getResources().getDrawable(R.drawable.map_user)).getBitmap();
		}
		
		icon = Bitmap.createScaledBitmap(icon, icon.getWidth()/2, icon.getHeight()/2, false);
		
		mark = map.addMarker(new MarkerOptions()
		.title(name+USER_DATA_SEP+email)
		.snippet(lastDate)
		.icon(BitmapDescriptorFactory.fromBitmap(icon))
		.position(new LatLng(lat, longt)));
		
		// reference marker
		this.users.add(mark);
		if(phone.equals(Conf.telUser)){
			userMarker = mark;
		}
	}
	
}
