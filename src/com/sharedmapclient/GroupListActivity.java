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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupListActivity extends Activity {

	TextView txtNoGroup;
	ListView listView;
	ImageView btnSearch;
	ImageView btnCreate;
	String telNumber;
	Context ctx;
	
	@Override
	protected void onResume() {
		super.onResume();
		
		setContentView(R.layout.activity_group_list);
		
		ctx = getBaseContext();
		
		ArrayList<MapGroup> events = new ArrayList<MapGroup>();
		Log.d("lala",Conf.telUser);
		HttpGet uri = new HttpGet("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/group/getGroups?tel="+Conf.telUser);

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
				String password=null;
				boolean proprio=false;
				int nbP=0;
				Node child = doc.getFirstChild();
				NodeList list = child.getChildNodes();
				for (int i=0;i<list.getLength();i++) {
					NodeList listGroups = list.item(i).getChildNodes();
					nbP=0;
					  for (int j=0;j<listGroups.getLength();j++) {
						  
								if (listGroups.item(j).getNodeName().equals("invites")) {
									nbP+=1;
								}
						  
							  	if (listGroups.item(j).getNodeName().equals("description")) {
									description=listGroups.item(j).getFirstChild().getNodeValue();
								}
							    else if (listGroups.item(j).getNodeName().equals("hashtag")) {
									groupName=listGroups.item(j).getFirstChild().getNodeValue();
								}
								else if (listGroups.item(j).getNodeName().equals("password")) {
									password=listGroups.item(j).getFirstChild().getNodeValue();
								}
								else if (listGroups.item(j).getNodeName().equals("proprietaire")) {
									NodeList listProprio = listGroups.item(j).getChildNodes();
									  for (int k=0;k<listProprio.getLength();k++) {
										  if (listProprio.item(k).getNodeName().equals("telephone")) {
												if (Conf.telUser.equals(listProprio.item(k).getFirstChild().getNodeValue())) {
													proprio = true;
												}
												else {
													proprio = false;
												}
											}
									  }
								}
							  
					  }
					 
					  	MapGroup ev = new MapGroup();
						ev.hashtag = groupName;
						ev.desc = description;
						ev.nbParticipant = nbP;
						ev.isGPSactive = true;
						ev.isMine = proprio;
						ev.password = password;
						events.add(ev);
				}
				
				
				
			}
			else {
				Toast.makeText(this, "Vous participez à aucun groupe", Toast.LENGTH_SHORT).show();
			}
			
		}
		else {
			Toast.makeText(this, "Vous participez à aucun groupe", Toast.LENGTH_SHORT).show();
		}
		
		
		
		// ########### TEST ################ 
		
		/*MapGroup ev = new MapGroup();
		ev.hashtag = "#Famille";
		ev.desc = "Les descriptions ca m'ennuie";
		ev.nbParticipant = 8;
		ev.isGPSactive = true;
		ev.isMine = true;
		ev.password = "mdptrob1";
		
		events.add(ev);
		
		ev = new MapGroup();
		ev.hashtag = "#AdaVoilaLesDalton";
		ev.desc = "Merci Joe. Ca marchait aussi avec les fraises.";
		ev.nbParticipant = 4;
		
		events.add(ev);
		
		
		ev = new MapGroup();
		ev.hashtag = "#BolossesAuSki";
		ev.desc = "Groupe pour pas se perdre pdt cette semaine de ouf à Tignes !";
		ev.nbParticipant = 13;
		ev.isGPSactive = true;
		
		events.add(ev);*/
		
		// ########### FIN TEST ############
		
		
		txtNoGroup = (TextView)findViewById(R.id.txtNoGroup);
		if(events.size() > 0){
			txtNoGroup.setVisibility(View.GONE);
		}
		
		ListAdapter adapter = new GroupListAdapter(this, events);
		listView = (ListView) findViewById(R.id.listEvents);
		listView.setAdapter(adapter);
		
		// Edit the group (long click on the description)
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent = new Intent(GroupListActivity.this, GroupEditionActivity.class);
				intent.putExtra("group", (MapGroup)listView.getItemAtPosition(position));
				startActivity(intent);
				return false;
			}
		});
		
		btnSearch = (ImageView) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(BtnListener);
		
		btnCreate = (ImageView) findViewById(R.id.btnCreate);
		btnCreate.setOnClickListener(BtnListener);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_events, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(GroupListActivity.this, SettingsActivity.class);
	    	startActivity(intent);
	    	return true;
		default:
			return true;
		}
	}

	
	private OnClickListener BtnListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent intent = null;
			switch(v.getId())
			{
			case R.id.btnCreate:
				intent = new Intent(getBaseContext(), GroupCreationBeginActivity.class);
				break;
			case R.id.btnSearch:
				intent = new Intent(getBaseContext(), GroupSearchActivity.class);
				break;
			}

			startActivity(intent);
		}
	};
}
