package com.sharedmapclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.widget.Toast;

/**
 * 
 * @author SharedMap
 *
 */
@SuppressWarnings("serial")
public class ErrorManager extends ClientProtocolException {
	
	/**
	 * Attributs
	 */
	
	Toast toast;
	
	Context context;
	
	private String message = "";
	
	private int duree = Toast.LENGTH_SHORT;
	
	/**
	 * Affichage d'un message en fonction du status de la réponse
	 * 
	 * @param response
	 */
	
	public void printError(HttpResponse response, Context context)
	{		
		 
		int status = response.getStatusLine().getStatusCode();
		
		switch(status)
		{
			case 118 : this.message = "Délai imparti à l'opération dépassé";
					   this.toast = Toast.makeText(context, this.message, this.duree);
			           this.toast.show();
				break;
			case 200 : this.message = "Requête traitée avec succès";
					   this.toast = Toast.makeText(context, this.message, this.duree);
					   this.toast.show();
				break;

			case 400 : this.message = "La syntaxe de la requête est erronée";
					   this.toast = Toast.makeText(context, this.message, this.duree);
					   this.toast.show();
				break; 
			case 404 : this.message = "Ressource non trouvée";
					   this.toast = Toast.makeText(context, this.message, this.duree);
					   this.toast.show();
				break;
			case 500 : this.message = "Erreur interne du serveur";
				       this.toast = Toast.makeText(context, this.message, this.duree);
				       this.toast.show();
				break;
		
		}		
		
	}

}
