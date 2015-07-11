package com.sharedmapclient;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private EditText txtPseudo;
	private EditText txtMail;
	private Button btnValider;
	private String registrationId=null; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		//register to the push service
		final String SENDER_ID = "894315775614";
		
		Context ctx = getApplicationContext();
		final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... arg0) {
	            String msg = "";
	            try {
	                registrationId = gcm.register(SENDER_ID);
	                msg = "Device registered, registration id=" + registrationId;
	                Log.d("lalala", registrationId);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;			
			}
	    }.execute(null, null, null);
			    
		txtPseudo = (EditText) findViewById(R.id.pseudo);
		txtMail = (EditText) findViewById(R.id.mail);
		btnValider = (Button) findViewById(R.id.btnValider);
		
		btnValider.setOnClickListener(ValiderListener);
		
		HttpGet uri = new HttpGet("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/account/get?tel="+Conf.telUser);    

    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse resp=null;
		try {						
			resp = client.execute(uri);
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		if (resp.getEntity() != null) {
			//Parser document
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
			//si aucun document renvoyé alors creation de compte
			if (doc.getChildNodes().getLength() == 0) {
				Toast.makeText(this, "Erreur de récupération des informations personnelles", Toast.LENGTH_SHORT).show();
			}
			else {
				String pseudo=null;
				String email=null;
				Node child = doc.getFirstChild();
				NodeList list = child.getChildNodes();
				for (int i=0;i<list.getLength();i++) {
					if (list.item(i).getNodeName().equals("pseudo")) {
						pseudo=list.item(i).getFirstChild().getNodeValue();
					} 
					else if (list.item(i).getNodeName().equals("email")) {
						email=list.item(i).getFirstChild().getNodeValue();
					}
				}
				txtPseudo.setText(pseudo);
				txtMail.setText(email);
			}
		}
		
		
	}
	
	// Listener du bouton Next
		private OnClickListener ValiderListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
					    	
			HttpPut uri = new HttpPut("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/account/modify?tel="+Conf.telUser+"&pseudo="+txtPseudo.getText().toString()+"&email="+txtMail.getText().toString()+"&deviceToken="+registrationId); 
	    	
	    	DefaultHttpClient client = new DefaultHttpClient();
	    	HttpResponse resp=null;
			try {
				resp = client.execute(uri);
			} catch (ClientProtocolException e2) {
				e2.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}

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
	    	
			if (resp.getStatusLine().getStatusCode() == 200) {
				Toast.makeText(getBaseContext(), "Modifications prises en compte", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getBaseContext(), "Echec des modifications", Toast.LENGTH_SHORT).show();
			}
				
				   
			}
		};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
