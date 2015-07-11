package com.sharedmapclient;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SaisieNumeroTelActivity extends Activity {

	//Declaration des objets graphiques
	private EditText txtNumTel=null;
	private EditText txtIndicatifTel=null;
	private Button btnValider = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saisie_numero_tel);
		
		//Récuperation des objets graphiques de la vue
		txtNumTel = (EditText) findViewById(R.id.numTel);
		txtIndicatifTel = (EditText) findViewById(R.id.indicatifTel);
		btnValider = (Button) findViewById(R.id.btnValider);
		
		//Listener sur bouton Valider
		btnValider.setOnClickListener(ValiderListener);
		
	}
	
	
	// Listener du bouton envoyer numero de telephone
	private OnClickListener ValiderListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	    	
	    	//Genere un nombre aléatoire à 6 chiffres
	    	Random r = new Random();
	    	int codeSms = 100000 + r.nextInt(999999 - 100000);
	    	
	    	//Envoi un message sur le numero saisi
	    	String numeroTel = txtIndicatifTel.getText().toString() + txtNumTel.getText().toString();
	    	SmsManager manager = SmsManager.getDefault();
	    	manager.sendTextMessage("+" + numeroTel, null, "[SharedMap] Bonjour, Veuillez entrer " +
	    			"le code de vérification suivant pour confirmer votre identité : " + codeSms, null, null);
	    	
	    	//Ecran suivant avec comme paramètre le codeSms généré
	    	Intent intent = new Intent(getBaseContext(), VerifCodeSmsActivity.class);
	    	intent.putExtra("codeSms", Integer.toString(codeSms));
	    	intent.putExtra("numTel", numeroTel);
	    	startActivity(intent);
	    	
	    }
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.saisie_numero_tel, menu);
		return true;
	}

}
