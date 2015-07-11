package com.sharedmapclient;

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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class VerifCodeSmsActivity extends Activity {

	private EditText txtCodeVerif=null;
	private Button btnValider = null;
	private String codeSms=null;
	private String telNumber=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verif_code_sms);
		
		//Récuperation du code généré de l'activité précédente 
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    codeSms = extras.getString("codeSms");
		    telNumber = extras.getString("numTel");
		}
		Toast.makeText(VerifCodeSmsActivity.this, "Code : "+codeSms, Toast.LENGTH_SHORT).show();
		//Récuperation des objets graphiques de la vue
		txtCodeVerif = (EditText) findViewById(R.id.codeVerif);
		btnValider = (Button) findViewById(R.id.btnValider);
		
		//Listener sur bouton Valider
		btnValider.setOnClickListener(ValiderListener);
		
	}
	
	// Listener du bouton envoyer numero de telephone
		private OnClickListener ValiderListener = new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	//Récupération du code saisi
		    	String codeSaisi=txtCodeVerif.getText().toString();
		    	
		    	//Vérifie si le code saisi est conforme à celui reçu par l'utilisateur
		    	if (codeSaisi.equals(codeSms)) {
		    	
		    		HttpGet uri = new HttpGet("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/account/get?tel="+telNumber);    

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
							Intent intent=new Intent(getBaseContext(),CreationCompteMailPseudoActivity.class);
					    	intent.putExtra("numTel", telNumber);
					    	intent.putExtra("context", "create");
							startActivity(intent);
							finish();
							
						}
						else {
							String pseudo=null;
							String telephone=null;
							String email=null;
							Node child = doc.getFirstChild();
							NodeList list = child.getChildNodes();
							for (int i=0;i<list.getLength();i++) {
									if (list.item(i).getNodeName().equals("pseudo")) {
										pseudo=list.item(i).getFirstChild().getNodeValue();
									} 
									else if (list.item(i).getNodeName().equals("telephone")) {
										telephone=list.item(i).getFirstChild().getNodeValue();
									}
									else if (list.item(i).getNodeName().equals("email")) {
										email=list.item(i).getFirstChild().getNodeValue();
									}
							}
							
							//Passer les paramètres à la vue suivante 
							//Verification si numero déja associé à un compte (update ou create)
				    		Intent intent=new Intent(getBaseContext(),CreationCompteMailPseudoActivity.class);
					    	
				    		if (telephone == null) {
						    	intent.putExtra("numTel", telNumber);
						    	intent.putExtra("context", "create");
					    	}
					    	else {
						    	intent.putExtra("numTel", telephone);
						    	intent.putExtra("pseudo", pseudo);
						    	intent.putExtra("email", email);
						    	intent.putExtra("context", "update");
					    	}
							startActivity(intent);
							finish();
				    	}
					}
		    		else {
		    			Intent intent=new Intent(getBaseContext(),CreationCompteMailPseudoActivity.class);
				    	intent.putExtra("numTel", telNumber);
				    	intent.putExtra("context", "create");
						startActivity(intent);
						finish();
		    		}
		    	}
		    	else {
		    		Toast.makeText(VerifCodeSmsActivity.this, "Code saisi incorrect", Toast.LENGTH_SHORT).show();
		    	}
		    }
		};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.verif_code_sms, menu);
		return true;
	}

}
