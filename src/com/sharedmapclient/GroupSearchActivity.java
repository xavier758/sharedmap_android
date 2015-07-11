package com.sharedmapclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupSearchActivity extends Activity {

	ImageButton btnSearch;
	ImageView btnJoinGroup;
	TextView txtJoinGroup;
	ImageView btnAskJoin;
	TextView txtAskJoin;
	EditText editSearch;
	TextView txtNotFound;
	EditText editPwd;
	TextView editAskJoin;
	TextView txtGroupAlreadyJoined;
	
	TextView hashtag;
	TextView desc;
	TextView participant;
	
	
	MapGroup group = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_search);
	
		
		// find components
		btnSearch = (ImageButton)findViewById(R.id.btnSearchEvent);
		btnJoinGroup = (ImageView)findViewById(R.id.btnJoinEvent);
		txtJoinGroup = (TextView)findViewById(R.id.txtJoinGroup);
		btnAskJoin = (ImageView)findViewById(R.id.btnAskJoin);
		txtAskJoin = (TextView)findViewById(R.id.txtBtnAskJoin);
		editSearch = (EditText)findViewById(R.id.editTxtSearch);
		txtNotFound = (TextView)findViewById(R.id.txtEventNotFound);
		editPwd = (EditText)findViewById(R.id.editPassword);
		editAskJoin = (TextView)findViewById(R.id.txtAskJoin);
		txtGroupAlreadyJoined = (TextView)findViewById(R.id.txtEventAlreadyJoined);
		
		hashtag = (TextView)findViewById(R.id.hashtag);
		desc = (TextView)findViewById(R.id.desc);
		participant = (TextView)findViewById(R.id.participant);
		
		btnSearch.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_search));
		
		// Listeners
		btnSearch.setOnClickListener(ButtonListener);
		btnJoinGroup.setOnClickListener(ButtonListener);
		btnAskJoin.setOnClickListener(ButtonListener);
		
		// hide components
		txtNotFound.setVisibility(View.GONE);
		editPwd.setVisibility(View.GONE);
		btnJoinGroup.setVisibility(View.GONE);
		txtJoinGroup.setVisibility(View.GONE);
		editAskJoin.setVisibility(View.GONE);
		btnAskJoin.setVisibility(View.GONE);
		txtAskJoin.setVisibility(View.GONE);
		txtGroupAlreadyJoined.setVisibility(View.GONE);
		
		hashtag.setVisibility(View.GONE);
		desc.setVisibility(View.GONE);
		participant.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_search, menu);
		return true;
	}
	
	private OnClickListener ButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.btnSearchEvent:
				if(editSearch.getText().toString().equals("")){ return; }
				
				// hide components
				txtNotFound.setVisibility(View.GONE);
				editPwd.setVisibility(View.GONE);
				btnJoinGroup.setVisibility(View.GONE);
				txtJoinGroup.setVisibility(View.GONE);
				editAskJoin.setVisibility(View.GONE);
				btnAskJoin.setVisibility(View.GONE);
				txtAskJoin.setVisibility(View.GONE);
				txtGroupAlreadyJoined.setVisibility(View.GONE);
				
				hashtag.setVisibility(View.GONE);
				desc.setVisibility(View.GONE);
				participant.setVisibility(View.GONE);
				
				HttpGet uri = new HttpGet("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/get?&hashtag="+editSearch.getText().toString());

				DefaultHttpClient client = new DefaultHttpClient();
		    	HttpResponse resp=null;
				try {
					resp = client.execute(uri);
				} catch (ClientProtocolException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				if (resp.getEntity()!=null) {
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
					
					if (doc.getChildNodes().getLength() > 0) {
						String groupName=null;
						String description=null;
						int nbP=0;
						Node child = doc.getFirstChild();
						NodeList list = child.getChildNodes();
						for (int i=0;i<list.getLength();i++) {
								if (list.item(i).getNodeName().equals("description")) {
									description=list.item(i).getFirstChild().getNodeValue();
								}
								if (list.item(i).getNodeName().equals("hashtag")) {
									groupName=list.item(i).getFirstChild().getNodeValue();
								}
								if (list.item(i).getNodeName().equals("invites")) {
									nbP+=1;
								}
						}
							group = new MapGroup();
							group.hashtag = groupName;
							group.desc = description;
							group.nbParticipant=nbP;
							// display buttons to join the group
							editPwd.setVisibility(View.VISIBLE);
							btnJoinGroup.setVisibility(View.VISIBLE);
							txtJoinGroup.setVisibility(View.VISIBLE);
							editAskJoin.setVisibility(View.VISIBLE);
							btnAskJoin.setVisibility(View.VISIBLE);
							txtAskJoin.setVisibility(View.VISIBLE);

							// update group data
							hashtag.setText(group.hashtag);
							desc.setText(group.desc);
							participant.setText(group.nbParticipant+" participant"
									+(group.nbParticipant>1 ? "s" : ""));

							// show group
							hashtag.setVisibility(View.VISIBLE);
							desc.setVisibility(View.VISIBLE);
							participant.setVisibility(View.VISIBLE);
							
					}
					else {
						txtNotFound.setVisibility(View.VISIBLE);
					}
					
				}
				else {
					txtNotFound.setVisibility(View.VISIBLE);
				}
				
				
				//######### TEST ###################
				/*group = new MapGroup();
				group.hashtag = "#test";
				group.desc = "Un super event de test !!!";
				
				if(!editSearch.getText().toString().equals("test"))
				{
					txtNotFound.setVisibility(View.VISIBLE);
				}
				// ######### FIN TEST ##############
				
				else
				{
					// display buttons to join the group
					editPwd.setVisibility(View.VISIBLE);
					btnJoinGroup.setVisibility(View.VISIBLE);
					editAskJoin.setVisibility(View.VISIBLE);
					btnAskJoin.setVisibility(View.VISIBLE);

					// update group data
					hashtag.setText(group.hashtag);
					desc.setText(group.desc);
					participant.setText(group.nbParticipant+" participant"
							+(group.nbParticipant>1 ? " s" : ""));

					// show group
					hashtag.setVisibility(View.VISIBLE);
					desc.setVisibility(View.VISIBLE);
					participant.setVisibility(View.VISIBLE);
				}*/
				
				break;
				
			case R.id.btnJoinEvent:
				
				HttpPost uriJoin = new HttpPost("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/joinGroup?password="+editPwd.getText().toString()+"&tel="+Conf.telUser+"&hashtag="+hashtag.getText().toString());
				
				client = new DefaultHttpClient();
		    	resp=null;
				try {
					resp = client.execute(uriJoin);
				} catch (ClientProtocolException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
				if (resp.getStatusLine().getStatusCode() == 200) {
					
					BufferedReader br = null;
					StringBuilder sb = new StringBuilder();
					String line;
					try {
						InputStream retour = resp.getEntity().getContent();
						br = new BufferedReader(new InputStreamReader(retour));
						while ((line = br.readLine()) != null) {
							sb.append(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (br != null) {
							try {
								br.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					if (sb.toString().equals("false")) {
						Toast.makeText(GroupSearchActivity.this, "Vous avez déjà fait une demande pour rejoindre ce groupe ou vous avez déjà été invité", Toast.LENGTH_LONG).show();
					}
					else {
						Intent intent = new Intent(getBaseContext(), GroupListActivity.class);
						startActivity(intent);
						finish();
					}
					
				}
				else {
					Toast.makeText(GroupSearchActivity.this, "Echec de la demande", Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.btnAskJoin:
				
				HttpPost uriAskJoin = new HttpPost("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/notification/demande?tel="+Conf.telUser+"&hashtag="+hashtag.getText().toString());
				
				client = new DefaultHttpClient();
		    	resp=null;
				try {
					resp = client.execute(uriAskJoin);
				} catch (ClientProtocolException e2) {
					e2.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
				if (resp.getStatusLine().getStatusCode() == 200) {
					
					BufferedReader br = null;
					StringBuilder sb = new StringBuilder();
					String line;
					try {
						InputStream retour = resp.getEntity().getContent();
						br = new BufferedReader(new InputStreamReader(retour));
						while ((line = br.readLine()) != null) {
							sb.append(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (br != null) {
							try {
								br.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					if (sb.toString().equals("false")) {
						Toast.makeText(GroupSearchActivity.this, "Vous avez déjà fait une demande pour rejoindre ce groupe ou vous avez déjà été invité", Toast.LENGTH_LONG).show();
					}
					else {
						Toast.makeText(GroupSearchActivity.this, "Demande envoyé au propriétaire du groupe", Toast.LENGTH_LONG).show();
					}
					
				}
				else {
					Toast.makeText(GroupSearchActivity.this, "Echec de la demande", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			
		}
	};

}
