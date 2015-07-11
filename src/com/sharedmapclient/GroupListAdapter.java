package com.sharedmapclient;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupListAdapter extends BaseAdapter {

	static class ViewHolder{
		TextView hashtag;
		TextView desc;
		TextView participant;
		ImageView logo;
		ImageView crown;
		ImageView mapButton;
	}
	
	LayoutInflater inflater;
	List<MapGroup> groups;
	Context ctx;
	
	public GroupListAdapter(Context ctx, List<MapGroup> events){
		this.ctx = ctx;
		this.groups = events;
	}
	
	@Override
	public int getCount() {
		return groups.size();
	}

	@Override
	public Object getItem(int position) {
		return groups.get(position);
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
			convertView = inflater.inflate(R.layout.activity_group_listelement, parent, false);
			
			holder = new ViewHolder();
			holder.hashtag = (TextView)convertView.findViewById(R.id.hashtag);
			holder.desc = (TextView)convertView.findViewById(R.id.desc);
			holder.participant = (TextView)convertView.findViewById(R.id.participant);
			holder.logo = (ImageView)convertView.findViewById(R.id.eventLogo);
			holder.crown = (ImageView)convertView.findViewById(R.id.crown);
			holder.mapButton = (ImageView)convertView.findViewById(R.id.imgMapAccess);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		// update data with concerned object
		holder.hashtag.setText(((MapGroup)getItem(position)).hashtag);
		holder.desc.setText(((MapGroup)getItem(position)).desc);
		holder.participant.setText(((MapGroup)getItem(position)).nbParticipant+" participant"
						+(((MapGroup)getItem(position)).nbParticipant > 1 ? "s" : ""));
		
		// logo indication about GPS activity for this group
		Resources r = ctx.getResources();
		if(((MapGroup)getItem(position)).isGPSactive){
			holder.logo.setImageDrawable(r.getDrawable(R.drawable.geoloc_green));
		}
		else{
			holder.logo.setImageDrawable(r.getDrawable(R.drawable.geoloc_red));
		}
		
		// show if we are admin or not of the group
		if(((MapGroup)getItem(position)).isMine){
			holder.crown.setVisibility(View.VISIBLE);
		}
		else{
			holder.crown.setVisibility(View.INVISIBLE);
		}
	
		// ###########################################
		// 				LISTENERS
		// ###########################################
		final ViewHolder finHolder = holder;
		final int finPos = position;
		
		// 1. Map access (arrow on the right side)
		finHolder.mapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, MapActivity.class);
				intent.putExtra("hashtag", groups.get(finPos).hashtag);
				ctx.startActivity(intent);
			}
		});
		
		// 2. Activate or deactivate the GPS (on the logo)
		finHolder.logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				groups.get(finPos).isGPSactive = !groups.get(finPos).isGPSactive;
				if(groups.get(finPos).isGPSactive){
					finHolder.logo.setImageDrawable(ctx.getResources().getDrawable(R.drawable.geoloc_green));
				}
				else{
					finHolder.logo.setImageDrawable(ctx.getResources().getDrawable(R.drawable.geoloc_red));
				}
			}
		});
		
		return convertView;
	}
	
}
