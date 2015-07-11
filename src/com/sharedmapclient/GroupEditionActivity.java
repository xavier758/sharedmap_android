package com.sharedmapclient;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupEditionActivity extends Activity {

	private TextView txtTitle;
	private TextView txtBtnQuit;
	private TextView txtBtnValidate;
	private TextView txtBtnFire;
	private TextView txtDesc;
	private TextView txtPwd;
	private EditText editDesc;
	private EditText editPwd;
	private ImageView btnValidate;
	private ImageView btnQuit;
	private ImageView btnInvite;
	private ImageView btnFireMembers;
	
	private MapGroup groupEdited;
	
	protected CharSequence _nameSequence[] ;	
	protected boolean[] _selections;
	protected CharSequence _nameSequenceParticipant[] ;	
	protected boolean[] _selectionsParticipant;
	private HashMap<String,String> listContacts = new HashMap<String,String>(); 
	private HashMap<String,String> listContactsMatchName = new HashMap<String,String>();
	private HashMap<String,String> listParticipantContacts;
	private ArrayList<String> contactsFiltre = new ArrayList<String>();
	private ArrayList<String> contacts = new ArrayList<String>();
    private ContentResolver contentResolver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_edition);
		
		groupEdited = (MapGroup) getIntent().getSerializableExtra("group");
		
		// INIT: find components
		txtTitle = (TextView)findViewById(R.id.titleGroup);
		txtBtnFire = (TextView)findViewById(R.id.txtBtnFire);
		txtBtnQuit = (TextView)findViewById(R.id.txtBtnQuit);
		txtBtnValidate = (TextView)findViewById(R.id.txtBtnValidate);
		txtDesc = (TextView) findViewById(R.id.txtDesc);
		txtPwd = (TextView) findViewById(R.id.txtPassword);
		
		editDesc = (EditText) findViewById(R.id.editDesc);
		editPwd = (EditText) findViewById(R.id.editPassword);
		
		btnValidate = (ImageView) findViewById(R.id.btnValidate);
		btnInvite = (ImageView) findViewById(R.id.btnInvite);
		btnFireMembers = (ImageView) findViewById(R.id.btnFireMembers);
		btnQuit = (ImageView) findViewById(R.id.btnQuit);
		
		txtTitle.setText("Groupe #"+groupEdited.hashtag);
		
		// if we are admin of the group, display full edition form
		if(groupEdited.isMine)
		{
			// if admin wants to quit the group, he deletes it
			txtBtnQuit.setText("Supprimer");

			editDesc.setText(groupEdited.desc);
			editPwd.setText(groupEdited.password);
		}
		else
		{
			txtBtnQuit.setText("Quitter");
			
			btnFireMembers.setVisibility(View.GONE);
			txtBtnFire.setVisibility(View.GONE);
			btnValidate.setVisibility(View.GONE);
			txtBtnValidate.setVisibility(View.GONE);
			
			txtDesc.setVisibility(View.GONE);
			txtPwd.setVisibility(View.GONE);
			editDesc.setVisibility(View.GONE);
			editPwd.setVisibility(View.GONE);
		}
		
		
		// #######################################
		//			LISTENERS
		// #######################################
		btnValidate.setOnClickListener(buttonListener);
		btnQuit.setOnClickListener(buttonListener);
		btnInvite.setOnClickListener(buttonListener);
		btnFireMembers.setOnClickListener(buttonListener);
		
		
		//Récupération de tous les contacts
        contentResolver = getContentResolver();    
        Cursor numbercursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null, null);
        if (numbercursor.getCount() > 0){
	        while (numbercursor.moveToNext()) {
	    		String name =numbercursor.getString(numbercursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
	    		//listNames.add(name);
	        	String phoneNumber = numbercursor.getString(numbercursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	        	phoneNumber = phoneNumber.replaceAll("\\s", "");
	        	//Supprime 1er caractère ("+") du numero
	        	String numSansPlus = phoneNumber.substring(1,phoneNumber.length());
		    	//HashMap pour retrouver numéro a partir d'un nom (key)
	        	listContacts.put(name, numSansPlus);
	        	//HashMap pour retrouver nom a partir d'un numéro (key)
	        	listContactsMatchName.put(numSansPlus, name);
	        	//ArrayList à envoyer au serveur
	        	contacts.add(numSansPlus);
	        }
        }
		
		
	}
	
	private OnClickListener buttonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v)
		{
			Intent intent;
			
			switch(v.getId())
			{
			case R.id.btnValidate:
				
				String description = editDesc.getText().toString();
				String pwd = editPwd.getText().toString();
				
				if (pwd.equals("")) {
		    		Toast.makeText(GroupEditionActivity.this, "Le mot de passe est obligatoire" ,Toast.LENGTH_SHORT).show();
				}else{
					String put = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/modify?&hashtag="+groupEdited.hashtag+"&description="+description+"&password="+pwd;
					HttpPut uriPut = new HttpPut(put.replace(" ", "+"));
	    			
	    			DefaultHttpClient clientPut = new DefaultHttpClient();
	    	    	HttpResponse respPut=null;
	    			try {
	    				respPut = clientPut.execute(uriPut);
	    			} catch (ClientProtocolException e2) {
	    				e2.printStackTrace();
	    			} catch (IOException e2) {
	    				e2.printStackTrace();
	    			}
	    			
	    			if (respPut.getStatusLine().getStatusCode() == 200) {
	    				intent = new Intent(getBaseContext(), GroupListActivity.class);
	    				startActivity(intent);
	    				finish();
	    			}
	    			else {
	    				Toast.makeText(GroupEditionActivity.this, "Erreur de création de groupe : Veuillez recommencer l'opération", Toast.LENGTH_SHORT).show();
	    			}
				}	    	

				break;
				
			case R.id.btnQuit:
				
				if(groupEdited.isMine) // If you are admin
				{
					String delete = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/delete?&hashtag="+groupEdited.hashtag;
					HttpDelete uri = new HttpDelete(delete.replace(" ", "+"));    
	    			
	    			DefaultHttpClient client = new DefaultHttpClient();
	    	    	HttpResponse resp=null;
	    			try {
	    				resp = client.execute(uri);
	    			} catch (ClientProtocolException e2) {
	    				e2.printStackTrace();
	    			} catch (IOException e2) {
	    				e2.printStackTrace();
	    			}
	    			
	    			if (resp.getStatusLine().getStatusCode() == 200) {
	    				intent = new Intent(getBaseContext(), GroupListActivity.class);
	    				startActivity(intent);
	    				finish();
	    				break;
	    			}
	    			else {
	    				Toast.makeText(GroupEditionActivity.this, "Erreur de suppression de groupe : Veuillez recommencer l'opération", Toast.LENGTH_SHORT).show();
	    			}
				
				}else{ // if you aren't admin
					
					String delete = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/removeUser?&hashtag="+groupEdited.hashtag+"&telephone="+Conf.telUser;
					HttpDelete uri = new HttpDelete(delete.replace(" ", "+"));    
	    			
	    			DefaultHttpClient client = new DefaultHttpClient();
	    	    	HttpResponse resp=null;
	    			try {
	    				resp = client.execute(uri);
	    			} catch (ClientProtocolException e2) {
	    				e2.printStackTrace();
	    			} catch (IOException e2) {
	    				e2.printStackTrace();
	    			}
	    			
	    			if (resp.getStatusLine().getStatusCode() == 200) {
	    				intent = new Intent(getBaseContext(), GroupListActivity.class);
	    				startActivity(intent);
	    				finish();
	    				break;
	    			}
	    			else {
	    				Toast.makeText(GroupEditionActivity.this, "Erreur de quit de groupe : Veuillez recommencer l'opération", Toast.LENGTH_SHORT).show();
	    			}
				}

				
			case R.id.btnInvite:
				
				List<String> listNames = new ArrayList<String>();
				
				//XML Parsing
			    Document document = null;
			    DocumentBuilderFactory fabrique = null;
			    try {
				    fabrique = DocumentBuilderFactory.newInstance();
				    DocumentBuilder builder = fabrique.newDocumentBuilder();
				    document = builder.newDocument();
				    Element racine = (Element) document.createElement("repertoire");
				    document.appendChild(racine);
				    for (String unContact : contacts) {
				    	Element telephone = (Element) document.createElement("telephone");
					    telephone.appendChild(document.createTextNode(unContact));
					    racine.appendChild(telephone);
				    }
				
			    } catch (Exception e) {
			    e.printStackTrace();
			    }

			    
		    	//Transformer contenu du fichier xml en string
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer=null;
				try {
					transformer = tf.newTransformer();
				} catch (TransformerConfigurationException e2) {
					e2.printStackTrace();
				}
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				StringWriter writer = new StringWriter();
				try {
					transformer.transform(new DOMSource(document), new StreamResult(writer));
				} catch (TransformerException e1) {
					e1.printStackTrace();
				}
				String xmlFile = writer.getBuffer().toString().replaceAll("\n|\r", "");
			    
			    //Appel au web service 
				String post = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/repertoire/getFriends?hashtag="+groupEdited.hashtag;
				HttpPost uri = new HttpPost(post.replace(" ", "+"));    
				
				DefaultHttpClient client = new DefaultHttpClient();
		    	HttpResponse resp=null;
				try {
					//Attache le fichier xml en paramètre sous forme de string
					StringEntity input = new StringEntity(xmlFile);
					input.setContentType("text/xml");
					uri.setEntity(input);
					resp = client.execute(uri);
				} catch (ClientProtocolException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
				
				//Récupération de la réponse
				if (resp.getEntity() != null) {
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
					
					if (doc.getChildNodes().getLength() == 0) {				
						
						Toast.makeText(GroupEditionActivity.this, "Echec de la récupération des contacts", Toast.LENGTH_SHORT).show();
					}
					else {
						//Récupération des contacts ayant installé l'appli
						String telephone=null;
						Node child = doc.getFirstChild();
						NodeList list = child.getChildNodes();
						
						String length = Integer.toString(list.getLength());

						for (int i=0;i<list.getLength();i++) {
							  NodeList listUsers = list.item(i).getChildNodes();
							    
							  
							  for (int j=0;j<listUsers.getLength();j++) {
								  if (listUsers.item(j).getNodeName().equals("telephone")) {

										telephone=listUsers.item(j).getFirstChild().getNodeValue();
										contactsFiltre.add(telephone);
									    
										//Ajoute nom correspondant au numero à la liste des noms
							
										String name = listContactsMatchName.get(telephone);
										listNames.add(name);
	
										
									}  
							  }
								
						}
						//Tri de la liste des noms pour affichage 
						if (!contactsFiltre.isEmpty()) {
							Collections.sort(listNames,String.CASE_INSENSITIVE_ORDER);
					        _nameSequence = listNames.toArray(new CharSequence[listNames.size()]);
					        _selections =  new boolean[ _nameSequence.length ];
						}
						showDialog( 0 );
					}
				}
				else {
					Toast.makeText(GroupEditionActivity.this, "Echec de la récupération des contacts", Toast.LENGTH_SHORT).show();
				} 
				
				break;
				
				
			case R.id.btnFireMembers:
				
				List<String> listParticipantNames = new ArrayList<String>();
				listParticipantContacts = new HashMap<String,String>();
				
				String get = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/getUsers?hashtag="+groupEdited.hashtag;
				HttpGet uriGetUsers = new HttpGet(get.replace(" ", "+"));

				DefaultHttpClient clientGetUsers = new DefaultHttpClient();
		    	HttpResponse respGetUsers=null;
				try {
					respGetUsers = clientGetUsers.execute(uriGetUsers);
				} catch (ClientProtocolException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				if (respGetUsers.getEntity()!=null) {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    	DocumentBuilder builder;
			    	Document doc= null;
					try {
						builder = factory.newDocumentBuilder();
						doc = builder.parse(respGetUsers.getEntity().getContent());
					} catch (ParserConfigurationException e1) {
						e1.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if (doc.getChildNodes().getLength() > 0) {
						String telephone=null;
						String pseudo=null;
						Node child = doc.getFirstChild();
						NodeList list = child.getChildNodes();
						for (int i=0;i<list.getLength();i++) {
							NodeList listGroups = list.item(i).getChildNodes();
							  for (int j=0;j<listGroups.getLength();j++) {
								  if (listGroups.item(j).getNodeName().equals("pseudo")) {
										pseudo=listGroups.item(j).getFirstChild().getNodeValue();
								  }
								  if (listGroups.item(j).getNodeName().equals("telephone")) {
										telephone=listGroups.item(j).getFirstChild().getNodeValue();								
								  }
								  
							  }
							  if (!telephone.equals(Conf.telUser)) {
								listParticipantNames.add(pseudo);
								listParticipantContacts.put(pseudo, telephone);
							  }
						} 
					
					}else {
						Toast.makeText(GroupEditionActivity.this, "Pas de participants", Toast.LENGTH_SHORT).show();
					}
								
				}else {
							Toast.makeText(GroupEditionActivity.this, "Pas de participants", Toast.LENGTH_SHORT).show();
				}

				//Tri de la liste des noms pour affichage 
				if (!listParticipantNames.isEmpty()) {
					Collections.sort(listParticipantNames,String.CASE_INSENSITIVE_ORDER);
			        _nameSequenceParticipant = listParticipantNames.toArray(new CharSequence[listParticipantNames.size()]);
			        _selectionsParticipant =  new boolean[ _nameSequenceParticipant.length ];
				}
				showDialog( 2 );
				
				
				break;
			}
			
		}
	};
	
	protected Dialog onCreateDialog( int id ) 
	{	
		if (id==0){
			return 	new AlertDialog.Builder( this )
        		.setTitle( "PhoneBook" )
        		.setMultiChoiceItems( _nameSequence, _selections, new DialogSelectionClickHandler() )
        		.setPositiveButton( "OK", new DialogButtonClickHandler() )
        		.setNegativeButton("Cancel", new DialogButtonClickHandler())
        		.create();
		}else{
			return 	new AlertDialog.Builder( this )
    		.setTitle( "ParticipantList" )
    		.setMultiChoiceItems( _nameSequenceParticipant, _selectionsParticipant, new DialogSelectionClickHandler() )
    		.setNeutralButton( "Remove", new DialogButtonClickHandler() )
    		.setNegativeButton("Cancel", new DialogButtonClickHandler())
    		.create();
		}
	}
	
	
	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener
	{
		public void onClick( DialogInterface dialog, int clicked, boolean selected )
		{
			
		}
	}
	

	public class DialogButtonClickHandler implements DialogInterface.OnClickListener
	{
		public void onClick( DialogInterface dialog, int clicked )
		{
			Intent intent;
			switch( clicked )
			{
				case DialogInterface.BUTTON_POSITIVE:
					sendSelectedNames();
					break;
				
				case DialogInterface.BUTTON_NEUTRAL:
					deleteSelectedNames();
					break;
					
				case DialogInterface.BUTTON_NEGATIVE:
					break;
			}
		}
	}
	
	protected void deleteSelectedNames(){
		String requeteTel = "";
		
		//Récupération des contacts sélectionnés
		for( int i = 0; i < _nameSequenceParticipant.length; i++ ){
			if (_selectionsParticipant[i]==true){
				
			String pseudo = (String) _nameSequenceParticipant[i];
			String number = listParticipantContacts.get(pseudo);
			
			requeteTel = requeteTel + "&telephone=" + number;
			}
		}
		Toast.makeText(getBaseContext(), requeteTel, Toast.LENGTH_LONG).show();
		System.out.println(requeteTel);
		String delete = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/removeUser?hashtag="+groupEdited.hashtag+requeteTel;
		HttpDelete uri = new HttpDelete(delete.replace(" ", "+"));
		
		/*Toast.makeText(getBaseContext(), delete, Toast.LENGTH_LONG).show();
		System.out.println(delete);*/
		
		DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse resp=null;
		try {
			resp = client.execute(uri);
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		if (resp.getStatusLine().getStatusCode() == 200) {
			Toast.makeText(GroupEditionActivity.this, "Les membres sélectionnés ont bien été retirés du groupe", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getBaseContext(), GroupListActivity.class);
			startActivity(intent);
			finish();
		}
		else {
			Toast.makeText(GroupEditionActivity.this, "Erreur de suppression de participant : Veuillez recommencer l'opération", Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void sendSelectedNames() {
		String requeteTel = "";
		
		//Récupération des contacts sélectionnés
		for( int i = 0; i < _nameSequence.length; i++ ){
			if (_selections[i]==true){
				
			String name = (String) _nameSequence[i];
			String number = listContacts.get(name);
			
			requeteTel = requeteTel + "&telFriend=" + number;
			}
		}
		
		String post = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/notification/inviteFriends?tel="+Conf.telUser+"&hashtag="+groupEdited.hashtag+requeteTel;
		HttpPost uri = new HttpPost(post.replace(" ", "+"));
		DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse resp=null;
		try {
			resp = client.execute(uri);
		} catch (ClientProtocolException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		if (resp.getStatusLine().getStatusCode() == 200) {
			Intent intent = new Intent(getBaseContext(), GroupListActivity.class);
			startActivity(intent);
			finish();
			Toast.makeText(GroupEditionActivity.this, "Les contacts sélectionnés ont bien été invités", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(GroupEditionActivity.this, "Echec de la récupération des contacts", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edition_group_begin, menu);
		return true;
	}

}
