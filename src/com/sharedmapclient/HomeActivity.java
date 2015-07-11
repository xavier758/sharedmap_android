package com.sharedmapclient;

import java.io.IOException;
import java.util.ArrayList;

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
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity {

	ListView notifView;
	ImageView btnGroupAccess;
	TextView titleNotif;
	TextView txtNoNotif;
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Context ctx = getBaseContext();
		
		if (!BootCompleted.started){
		     Intent serviceIntent = new Intent(ctx, LocationService.class);
		     ctx.startService(serviceIntent);
		     BootCompleted.started = true;
		}
		
		setContentView(R.layout.activity_home);
		ArrayList<Notification> notifs = new ArrayList<Notification>();
		Notification nt;
		//Conf.telUser="33699850720";
		HttpGet uri = new HttpGet("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/notification/getNotifications?tel="+Conf.telUser);

		DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse resp=null;
    	try {
			resp = client.execute(uri);
    	}
		catch (ErrorManager error) {
			error.printError(resp, getApplicationContext());
		} catch (IOException e) {
			e.printStackTrace();
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
				String pseudo=null;
				String nodeId = null;
				Node child = doc.getFirstChild();
				NodeList listNotif = child.getChildNodes();
				
				for (int i=0;i<listNotif.getLength();i++) {
					
					NodeList listTypeNotif = listNotif.item(i).getChildNodes();
					  if (listNotif.item(i).getNodeName().equals("notificationMarqueur")) {
						  
						  for (int m=0;m<listTypeNotif.getLength();m++) {
							  if (listTypeNotif.item(m).getNodeName().equals("marqueur")) {

								  NodeList listCreateurGroup = listTypeNotif.item(m).getChildNodes();
								  for (int j=0;j<listCreateurGroup.getLength();j++) {
										
									  if (listCreateurGroup.item(j).getNodeName().equals("createur")) {
										  
										  NodeList listUser = listCreateurGroup.item(j).getChildNodes();
											for (int k=0;k<listUser.getLength();k++) {
													
												if (listUser.item(k).getNodeName().equals("pseudo")) {
													pseudo=listUser.item(k).getFirstChild().getNodeValue();
												}
										  	}
									  }
									  
									  if (listCreateurGroup.item(j).getNodeName().equals("group")) {
										  NodeList listGroup = listCreateurGroup.item(j).getChildNodes();
											for (int k=0;k<listGroup.getLength();k++) {
													
												if (listGroup.item(k).getNodeName().equals("hashtag")) {
													groupName=listGroup.item(k).getFirstChild().getNodeValue();
												}
										  	}
									  }
							  	}
							  }
						  }
						  	nt = new Notification();
							nt.userPseudo = pseudo;
							nt.hashtag = groupName;
							nt.type = Notification.NotifType.MAP_EVENT;
							notifs.add(nt);
						  
					  }
					  else if (listNotif.item(i).getNodeName().equals("demande")) {
						  
						  for (int m=0;m<listTypeNotif.getLength();m++) {
							  
							  if (listTypeNotif.item(m).getNodeName().equals("id")) {
								  nodeId=listTypeNotif.item(m).getFirstChild().getNodeValue();
							  }
							  else if (listTypeNotif.item(m).getNodeName().equals("demandeur")) {

								  NodeList listDemandeurGroup = listTypeNotif.item(m).getChildNodes();
								  for (int j=0;j<listDemandeurGroup.getLength();j++) {
										
										  if (listDemandeurGroup.item(j).getNodeName().equals("pseudo")) {
											  
												pseudo=listDemandeurGroup.item(j).getFirstChild().getNodeValue();
										  }
							  		}
							  }
							  else if (listTypeNotif.item(m).getNodeName().equals("group")) {

								  NodeList listDemandeurGroup = listTypeNotif.item(m).getChildNodes();
								  for (int j=0;j<listDemandeurGroup.getLength();j++) {
									  
									if (listDemandeurGroup.item(j).getNodeName().equals("hashtag")) {
										groupName=listDemandeurGroup.item(j).getFirstChild().getNodeValue();
									}
							  	}
							 }
						  }
						  	nt = new Notification();
						  	nt.id=nodeId;
							nt.userPseudo = pseudo;
							nt.hashtag = groupName;
							nt.type = Notification.NotifType.REQUEST;
							notifs.add(nt);
					  }
					  else if (listNotif.item(i).getNodeName().equals("invitation")) {
						  
						  for (int m=0;m<listTypeNotif.getLength();m++) {
							  
							  if (listTypeNotif.item(m).getNodeName().equals("id")) {
								  nodeId=listTypeNotif.item(m).getFirstChild().getNodeValue();
							  }
							  else if (listTypeNotif.item(m).getNodeName().equals("group")) {

								  NodeList listGroup = listTypeNotif.item(m).getChildNodes();
								  for (int j=0;j<listGroup.getLength();j++) {
										
									  if (listGroup.item(j).getNodeName().equals("hashtag")) {
										  groupName=listGroup.item(j).getFirstChild().getNodeValue();
									  }
									  else if (listGroup.item(j).getNodeName().equals("proprietaire")) {
										  
										  NodeList listUser = listGroup.item(j).getChildNodes();
										  for (int k=0;k<listUser.getLength();k++) {
											  
											if (listUser.item(k).getNodeName().equals("pseudo")) {
												pseudo=listUser.item(k).getFirstChild().getNodeValue();
											}
											
									  	}
									  }
								  }
							  }
						  }
						  	nt = new Notification();
						  	nt.id=nodeId;
							nt.userPseudo = pseudo;
							nt.hashtag = groupName;
							nt.type = Notification.NotifType.INVITATION;
							notifs.add(nt);
					  }
					
					
				}	
			}
		}
		
		// ############### TEST #####################
		
		/*nt.userPseudo = "AbdessKiff_OL";
		nt.hashtag = "#VousLesCopains";
		nt.type = Notification.NotifType.INVITATION;
		notifs.add(nt);
		
		nt = new Notification();
		nt.userPseudo = "DidierSuper";
		nt.hashtag = "#BolossesAuSki";
		nt.type = Notification.NotifType.MAP_EVENT;
		notifs.add(nt);
		
		nt = new Notification();
		nt.userPseudo = "Barack.O";
		nt.hashtag = "#Famille";
		nt.type = Notification.NotifType.REQUEST;
		notifs.add(nt);*/
		
		//Conf.telUser = "0601020304";
		
		// ############### FIN TEST #################
		
		ListAdapter adapter = new HomeNotifAdapter(this, notifs);
		notifView = (ListView)findViewById(R.id.listNotif);
		notifView.setAdapter(adapter);
		
		btnGroupAccess = (ImageView)findViewById(R.id.btnGroupAccess);
		btnGroupAccess.setOnClickListener(GroupAccessListener);
		
		titleNotif = (TextView)findViewById(R.id.titleNotif);
		titleNotif.setText("Notifications ("+notifs.size()+")");
		
		// if there are notifs, hide text "no notif"
		if(notifs.size() > 0)
		{
			txtNoNotif = (TextView)findViewById(R.id.txtNoNotif);
			txtNoNotif.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	private OnClickListener GroupAccessListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(), GroupListActivity.class);
			startActivity(intent);
		}
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
	    	startActivity(intent);
	    	return true;
		default:
			return true;
		}
	}

}
