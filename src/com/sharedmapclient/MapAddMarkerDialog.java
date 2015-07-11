package com.sharedmapclient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MapAddMarkerDialog extends DialogFragment {

	private MapActivity map;
	private LatLng pos;
	
	private EditText editDesc;
	private TextView txtDuration;
	private ImageView btnDurationLess;
	private ImageView btnDurationMore;
	
	private List<Pair<Integer, String>> durations; // number of hours, label
	private int durationIndex = 1;
	
	@SuppressWarnings("unchecked")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		durations = new ArrayList<Pair<Integer, String>>();
		durations.add(new Pair(2, "2 heures"));
		durations.add(new Pair(2*24, "2 jours"));
		durations.add(new Pair(7*24, "1 semaine"));
		durations.add(new Pair(30*24, "1 mois"));
		durations.add(new Pair(365*24, "1 an"));
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_map_add_marker, null);
		builder.setView(view)
			.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
	            @Override
				public void onClick(DialogInterface dialog, int id) {
	                // Send the positive button event back to the host activity
	                map.onMapAddMarkerDialogPositiveClick(MapAddMarkerDialog.this);
	            }
	        })
	        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// just dismiss
	            }
	        });
		
		
		editDesc = (EditText)view.findViewById(R.id.editTxtMarker);
		txtDuration = (TextView)view.findViewById(R.id.txtDuration);
		btnDurationLess = (ImageView)view.findViewById(R.id.btnDurationLess);
		btnDurationMore = (ImageView)view.findViewById(R.id.btnDurationMore);
		
		btnDurationLess.setOnClickListener(durationListener);
		btnDurationMore.setOnClickListener(durationListener);
		
		btnDurationLess.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_minus));
		btnDurationMore.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_plus));
		
		txtDuration.setText(durations.get(durationIndex).second);
		
		return builder.create();
	}
	
	private OnClickListener durationListener = new OnClickListener() {
		
		@Override
		public void onClick(View v)
		{
			switch(v.getId())
			{
			case R.id.btnDurationLess:
				if(durationIndex <= 0) { return; }
				durationIndex--;
				txtDuration.setText(durations.get(durationIndex).second);
				break;
				
			case R.id.btnDurationMore:
				if(durationIndex >= durations.size()-1) { return; }
				durationIndex++;
				txtDuration.setText(durations.get(durationIndex).second);
				break;
			}
			
		}
	};
	
	@Override
	public void onAttach(Activity act)
	{
		super.onAttach(act);
		map = (MapActivity) act;
	}
	
	public void setMarkerPos(LatLng pos){
		this.pos = pos;
	}
	
	public LatLng getMarkerPos(){
		return this.pos;
	}
	
	public String getMarkerDescription(){
		return (editDesc.getText().toString() != null && !editDesc.getText().toString().equals("")) ?
				editDesc.getText().toString() : "None";
	}
	
	public Calendar getExpirationDate(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, durations.get(durationIndex).first);
		return cal;
	}
}
