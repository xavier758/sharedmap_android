package com.sharedmapclient;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeNotifAdapter extends BaseAdapter {

	static class ViewHolder{
		ImageView logo;
		TextView txt;
		Button btnAccept;
		Button btnDecline;
	}
	
	LayoutInflater inflater;
	List<Notification> notifs;
	Context ctx;
	
	public HomeNotifAdapter(Context ctx, List<Notification> notifs){
		this.ctx = ctx;
		this.notifs = notifs;
	}
	
	@Override
	public int getCount() {
		return notifs.size();
	}

	@Override
	public Object getItem(int position) {
		return notifs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		// if view is not recycled, inflate
		if(convertView == null)
		{
			inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.activity_home_listelement, parent, false);
			
			holder = new ViewHolder();
			holder.logo = (ImageView)convertView.findViewById(R.id.logoNotif);
			holder.txt = (TextView)convertView.findViewById(R.id.txtNotif);
			holder.btnAccept = (Button)convertView.findViewById(R.id.btnAccept);
			holder.btnDecline = (Button)convertView.findViewById(R.id.btnDecline);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		// update data with concerned object
		String txt = "";
		switch(((Notification)getItem(position)).type)
		{
		case REQUEST:
			holder.logo.setImageDrawable(ctx.getResources().getDrawable(R.drawable.event_request));
			holder.btnAccept.setText("Accepter");
			holder.btnDecline.setText("Refuser");
			holder.btnAccept.setVisibility(View.VISIBLE);
			holder.btnDecline.setVisibility(View.VISIBLE);
			txt = Notification.MSG_REQUEST;
			break;
			
		case MAP_EVENT:
			holder.logo.setImageDrawable(ctx.getResources().getDrawable(R.drawable.map_notif2));
			holder.btnAccept.setVisibility(View.GONE);
			holder.btnDecline.setVisibility(View.GONE);
			txt = Notification.MSG_MAP_EVENT;
			break;
			
		case INVITATION:
			holder.logo.setImageDrawable(ctx.getResources().getDrawable(R.drawable.event_invit));
			holder.btnAccept.setText("Rejoindre");
			holder.btnDecline.setText("Décliner");
			holder.btnAccept.setVisibility(View.VISIBLE);
			holder.btnDecline.setVisibility(View.VISIBLE);
			txt = Notification.MSG_INVITATION;
			break;
		}
		holder.txt.setText(Html.fromHtml(
				"<b><font color='#F05B4F'>#"+((Notification)getItem(position)).hashtag+": "
				+"</font>"+((Notification)getItem(position)).userPseudo+"</b>"+" "
				+txt));
		
		
		
		
		// ###############################"""
		// 			LISTENERS
		// ####################################
		final ViewHolder finHolder = holder;
		final int finPos = position;
		
		finHolder.logo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Notification notif = notifs.get(finPos);
				switch(notif.type)
				{
				case MAP_EVENT:
					Intent intent = new Intent(ctx, MapActivity.class);
					intent.putExtra("hashtag", notif.hashtag);
					ctx.startActivity(intent);
					break;
					
				default:
					
				}
				
			}
		});
		
		finHolder.btnAccept.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Notification notif = notifs.get(finPos);
				HttpPost uri=null;
				switch(notif.type)
				{
				case REQUEST: // accept someone in my group
	    			uri = new HttpPost("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/notification/repondDemande?idDemande="+notif.id+"&reponse=2");    
	    			
					break;
					
				case INVITATION: // join a group!
	    			uri = new HttpPost("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/notification/repondInvitation?idInvitation="+notif.id+"&reponse=2");  
					break;
					
				default: // it won't happen
				}
				
				if (uri != null) {
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
	    				Intent intent = new Intent(ctx, GroupListActivity.class);
						ctx.startActivity(intent);
	    			}
	    			else {
	    				//Toast.makeText(HomeActivity.class, "Echec de l'acceptation", Toast.LENGTH_SHORT).show();
	    			}
				}
    			
			}
		});
		
		finHolder.btnDecline.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Notification notif = notifs.get(finPos);
				HttpPost uri=null;
				switch(notif.type)
				{
				case REQUEST: // refuse someone in my group
					//Appel au web service create group
	    			uri = new HttpPost("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/notification/repondDemande?idDemande="+notif.id+"&reponse=1"); 
					break;
					
				case INVITATION: // decline the invitation to join a group
					uri = new HttpPost("http://"+Conf.IP_ADDRESS+":"+Conf.PORT+"/Serveur/WebServices/notification/repondInvitation?idInvitation="+notif.id+"&reponse=1");
					break;
					
				default: // it won't happen
				}
				
				if (uri != null) {
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
	    				Intent intent = new Intent(ctx, HomeActivity.class);
						ctx.startActivity(intent);
	    			}
	    			else {
	    				//Toast.makeText(HomeActivity.class, "Echec de l'acceptation", Toast.LENGTH_SHORT).show();
	    			}
				}
				
			}
		});
		
		
		
		return convertView;
		
		
		
	}

}
