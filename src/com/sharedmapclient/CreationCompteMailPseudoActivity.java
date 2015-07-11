package com.sharedmapclient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.media.audiofx.PresetReverb;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class CreationCompteMailPseudoActivity extends Activity {

	private EditText txtPseudo=null;
	private EditText txtMail=null;
	private Button btnValider = null;
	private String telNumber=null;
	private String pseudo=null;
	private String email=null;
	private String context=null;
	private String registrationId=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		 
		setContentView(R.layout.activity_creation_compte_mail_pseudo);

		//Récuperation des objets graphiques de la vue
		txtPseudo = (EditText) findViewById(R.id.pseudo);
		txtMail = (EditText) findViewById(R.id.mail);
		btnValider = (Button) findViewById(R.id.btnValider);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			
		    context = extras.getString("context");
		    telNumber = extras.getString("numTel");
		    
			if (context.equals("update")) {
			    pseudo = extras.getString("pseudo");
			    email = extras.getString("email");
			    txtPseudo.setText(pseudo);
			    txtMail.setText(email);
			}
		}
		
		//Listener sur bouton Valider
		btnValider.setOnClickListener(ValiderListener);		
	}
	
	// Listener du bouton envoyer numero de telephone
		private OnClickListener ValiderListener = new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
	    		//Récupération des informations saisies
		    	String mail=txtMail.getText().toString();
		    	String pseudo=txtPseudo.getText().toString();
		    	
		    	if (context.equals("create")) {
			    	//Appel au webservice
		    		HttpPost uri = new HttpPost("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/account/create?tel="+telNumber+"&pseudo="+pseudo+"&email="+mail+"&deviceToken="+registrationId);  
			    	
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
				    	
						String telephone=null;
						Node child = doc.getFirstChild();
						NodeList list = child.getChildNodes();
						for (int i=0;i<list.getLength();i++) {
								if (list.item(i).getNodeName().equals("telephone")) {
									telephone=list.item(i).getFirstChild().getNodeValue();
								}
								if (list.item(i).getNodeName().equals("email")) {
									mail=list.item(i).getFirstChild().getNodeValue();
								}
						}
						
						if (telephone == null) {
							Toast.makeText(CreationCompteMailPseudoActivity.this, "Opération échoué : Veuillez quitter et recommencer l'opération", Toast.LENGTH_LONG).show();
						}
						else {
					    	//si creation OK en base sur le serveur alors creation du fichier de config
							Conf.telUser=telephone;
							Conf.mail=mail;
					    	FileOutputStream output = null;        
					    	try {
					    	  output = openFileOutput("Config", MODE_PRIVATE);
					    	  output.write(telephone.getBytes());
					    	  if(output != null)
					    	    output.close();
					    	  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					    	  SharedPreferences.Editor editor = sharedPref.edit();
					    	  editor.putString("tel", telephone);
					    	  editor.commit();
					    	} catch (FileNotFoundException e) {
					    	  e.printStackTrace();
					    	} catch (IOException e) {
					    	  e.printStackTrace();
					    	}
					    	
					    	//GOTO ecran Accueil (liste notifications)
					    	Intent intent=new Intent(getBaseContext(),HomeActivity.class);
				        	startActivity(intent);
				        	finish();
						}
		    	
		    	}
		    	else if (context.equals("update")) {
			    	//Appel au webservice
		    		HttpPut uri = new HttpPut("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/account/modify?tel="+telNumber+"&pseudo="+pseudo+"&email="+mail+"&deviceToken="+registrationId); 
		    	
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
				    	
						String telephone=null;
						Node child = doc.getFirstChild();
						NodeList list = child.getChildNodes();
						for (int i=0;i<list.getLength();i++) {
								if (list.item(i).getNodeName().equals("telephone")) {
									telephone=list.item(i).getFirstChild().getNodeValue();
								}
								if (list.item(i).getNodeName().equals("email")) {
									mail=list.item(i).getFirstChild().getNodeValue();
								}
						}
						
						if (telephone == null) {
							Toast.makeText(CreationCompteMailPseudoActivity.this, "Opération échoué : Veuillez quitter et recommencer l'opération", Toast.LENGTH_LONG).show();
						}
						else {
					    	//si creation OK en base sur le serveur alors creation du fichier de config
							Conf.telUser=telephone;
							//Conf.mail=mail;
					    	FileOutputStream output = null;        
					    	try {
					    	  output = openFileOutput("Config", MODE_PRIVATE);
					    	  output.write(telephone.getBytes());
					    	  if(output != null)
					    	    output.close();
					    	  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					    	  SharedPreferences.Editor editor = sharedPref.edit();
					    	  editor.putString("tel", telephone);
					    	  editor.commit();
					    	} catch (FileNotFoundException e) {
					    	  e.printStackTrace();
					    	} catch (IOException e) {
					    	  e.printStackTrace();
					    	}
					    	
					    	//GOTO ecran Accueil (liste notifications)
					    	Intent intent=new Intent(getBaseContext(),HomeActivity.class);
				        	startActivity(intent);
				        	finish();
						}
		    	}
		    	  
		    	
		    }
		};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.creation_compte_mail_pseudo, menu);
		return true;
	}

}
