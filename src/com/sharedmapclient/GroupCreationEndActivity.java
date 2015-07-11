package com.sharedmapclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class GroupCreationEndActivity extends Activity {

	private ImageView btnInvit;
	private ImageView btnHome;
	private String groupName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_creation_end);
		btnInvit = (ImageView) findViewById(R.id.btnInvite);
		btnHome = (ImageView) findViewById(R.id.btnAccueil);
		btnHome.setOnClickListener(HomeListener);
	}
		
		// Listener du bouton Cancel
		private OnClickListener HomeListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Ecran back : EventDisplay
		    	Intent intent = new Intent(getBaseContext(), GroupListActivity.class);
		    	startActivity(intent);
		    	finish();
			}
		};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.creation_group_end, menu);
		return true;
	}

}
