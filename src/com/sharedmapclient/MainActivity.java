package com.sharedmapclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    // Asyntask
    AsyncTask<Void, Void, Void> mRegisterTask;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
		StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);
		setContentView(R.layout.activity_main);
		
	/*	Intent intent=new Intent(this,HomeActivity.class);
   	    startActivity(intent);
   	    finish();*/
   	    StringBuffer lu=null;
		 try {
			 //Lecture du fichier de config si existant
	          FileInputStream input = openFileInput("Config");
	          int value;
	           lu = new StringBuffer();
	          while((value = input.read()) != -1) {
	            lu.append((char)value);
	          }
	          Toast.makeText(this, "tel : "+lu.toString(), Toast.LENGTH_LONG);
	          //fichier config présent donc compte existe => login transparent au user
	          if(input != null) 
	          {
	        	  input.close();
		          
	        	  //Connexion au serveur pour verification du compte
	        	 HttpGet uri = new HttpGet("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/account/get?tel="+lu.toString());    

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
						//Parser document xml
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
							Intent intent=new Intent(this,SaisieNumeroTelActivity.class);
				 			startActivity(intent);
						}
						else {
							String telephone=null;
							Node child = doc.getFirstChild();
							NodeList list = child.getChildNodes();
							for (int i=0;i<list.getLength();i++) {
								if (list.item(i).getNodeName().equals("telephone") ){
									telephone=list.item(i).getFirstChild().getNodeValue();
								}
							}
						
							//Si telephone est nul alors compte non référencé sur le serveur => recréation
							if (telephone == null) {
								Toast.makeText(this, "Votre numéro n'est plus associé compte", Toast.LENGTH_SHORT).show();
								 Intent intent=new Intent(this,SaisieNumeroTelActivity.class);
					 			 startActivity(intent);
							} 
							else {
								 Conf.telUser=telephone;
					        	 Intent intent=new Intent(this,HomeActivity.class);
					        	 startActivity(intent);
							}
						}
					}
					else {
						Toast.makeText(this, "Votre numéro n'est plus associé compte", Toast.LENGTH_SHORT).show();
						Intent intent=new Intent(this,SaisieNumeroTelActivity.class);
			 			startActivity(intent);
					}
	          } 
		 } catch (FileNotFoundException e) {
			 Toast.makeText(this, "Première inscription", Toast.LENGTH_SHORT).show();
			 Intent intent=new Intent(this,SaisieNumeroTelActivity.class);
 			 startActivity(intent);
	     } catch (IOException e) {
	          e.printStackTrace();
	     }

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
