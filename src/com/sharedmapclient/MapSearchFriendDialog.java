package com.sharedmapclient;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MapSearchFriendDialog extends DialogFragment {

	MapActivity map;
	ListView listView;
	
	public static MapSearchFriendDialog newInstance(ArrayList<String> friends)
	{
		MapSearchFriendDialog dlg = new MapSearchFriendDialog();
		Bundle args = new Bundle();
		args.putStringArrayList("friends", friends);
		dlg.setArguments(args);
		return dlg;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_map_friends, container);
		listView = (ListView)view.findViewById(R.id.listFriends);
		ListAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getArguments().getStringArrayList("friends"));
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				map.onMapSearchFriendDialogClick(position);
				dismiss();
			}
		});
		
		getDialog().setTitle("Localiser un ami");
		return view;
		
	}
	
	@Override
	public void onAttach(Activity act)
	{
		super.onAttach(act);
		map = (MapActivity) act;
	}
	
}
