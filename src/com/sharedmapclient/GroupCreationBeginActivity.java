package com.sharedmapclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;



public class GroupCreationBeginActivity extends Activity {

	private EditText txtGroupName;
	private EditText txtDescription;
	private EditText txtPwd;
	private ImageView btnNext;
	private ImageView btnCancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_creation_begin);
		StringBuffer lu=null;
		
		txtGroupName = (EditText) findViewById(R.id.editNomGroupe);
		txtDescription = (EditText) findViewById(R.id.editDescr);
		txtPwd = (EditText) findViewById(R.id.editPwd);
		
		btnNext = (ImageView) findViewById(R.id.buttonNext);
		btnCancel = (ImageView) findViewById(R.id.buttonCancel);
		
		btnNext.setOnClickListener(ValiderListenerNext);
		btnCancel.setOnClickListener(ValiderListenerCancel);
		
	}
	
	// Listener du bouton Next
	private OnClickListener ValiderListenerNext = new OnClickListener() {
		@Override
		public void onClick(View v) {
				    	
			//Récupération des informations saisies
			String groupName = txtGroupName.getText().toString();
			String description = txtDescription.getText().toString();
			String pwd = txtPwd.getText().toString();
		
//			Intent intent = new Intent(getBaseContext(), GroupCreationEndActivity.class);
//	    	startActivity(intent);
//	    	finish();
	    	
	    	if (groupName.equals("") || pwd.equals("")) {
	    		Toast.makeText(GroupCreationBeginActivity.this, "Le nom du groupe et le mot de passe sont obligatoires" ,Toast.LENGTH_SHORT).show();
	    	} else {
	    		if(description.equals("")) {
	    			showDialog( 0 );
	    		}
	    		else {
	    			
	    			//Tester le nom existant
	    			String get = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/get?&hashtag="+groupName;
	    			HttpGet uriGetName = new HttpGet(get.replace(" ", "+"));
	    			
	    			DefaultHttpClient clientGetName = new DefaultHttpClient();
	    			HttpResponse respGetName=null;
					try {
						respGetName = clientGetName.execute(uriGetName);
					} catch (ClientProtocolException e2) {
						e2.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					if (respGetName.getEntity()!=null) {
						Toast.makeText(GroupCreationBeginActivity.this, "Erreur de création de groupe : Ce nom existe déjà", Toast.LENGTH_SHORT).show();
						
					}else{
		    			//Appel au web service create group
		    			String post = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/create?desc="+description+"&hashtag="+groupName+"&password="+pwd+"&tel="+Conf.telUser;
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
		    			
	    				Intent intent = new Intent(getBaseContext(), GroupCreationEndActivity.class);
    			    	startActivity(intent);
    			    	finish();
					}
	    			

	    		}
	    	}
	    	
			
			   
		}
	};
	
	@Override
	protected Dialog onCreateDialog( int id ) 
	{
		return 
		new AlertDialog.Builder( this )
        	.setTitle( "Attention" )
        	.setMessage("Continuez sans la description?")
        	.setPositiveButton( "OK", new DialogButtonClickHandler() )
        	.setNegativeButton("Cancel", new DialogButtonClickHandler())
        	.create();
	}
	
	public class DialogButtonClickHandler implements DialogInterface.OnClickListener
	{
		@Override
		public void onClick( DialogInterface dialog, int clicked )
		{
			switch( clicked )
			{
				case DialogInterface.BUTTON_POSITIVE:
					String groupName = txtGroupName.getText().toString();
					String description = txtDescription.getText().toString();
					String pwd = txtPwd.getText().toString();
					
					//Tester le nom existant
	    			String get = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/get?&hashtag="+groupName;
	    			HttpGet uriGetName = new HttpGet(get.replace(" ", "+"));
	    			
	    			DefaultHttpClient clientGetName = new DefaultHttpClient();
	    			HttpResponse respGetName=null;
					try {
						respGetName = clientGetName.execute(uriGetName);
					} catch (ClientProtocolException e2) {
						e2.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					if (respGetName.getEntity()!=null) {
						Toast.makeText(GroupCreationBeginActivity.this, "Erreur de création de groupe : Ce nom a existé", Toast.LENGTH_SHORT).show();
						
					}else{
		    			//Appel au web service create group
		    			String post = "http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/create?desc="+description+"&hashtag="+groupName+"&password="+pwd+"&tel="+Conf.telUser;
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
		    				Intent intent = new Intent(getBaseContext(), GroupCreationEndActivity.class);
	    			    	startActivity(intent);
	    			    	finish();
		    			}
		    			else {
		    				Toast.makeText(GroupCreationBeginActivity.this, "Erreur de création de groupe : Veuillez recommencer l'opération", Toast.LENGTH_SHORT).show();
		    			}
					}
	    			

//	    			Intent intent = new Intent(getBaseContext(), GroupCreationEndActivity.class);
//	    	    	startActivity(intent);
//	    	    	finish();
					break;
					
				case DialogInterface.BUTTON_NEGATIVE:
					
					break;
			}
		}
	}
	
	// Listener du bouton Cancel
	private OnClickListener ValiderListenerCancel = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//Ecran back : EventDisplay
			GroupCreationBeginActivity.this.finish();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.creation_group_begin, menu);
		return true;
	}

}
