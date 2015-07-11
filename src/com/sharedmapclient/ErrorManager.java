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
	 * Affichage d'un message en fonction du status de la r�ponse
	 * 
	 * @param response
	 */
	
	public void printError(HttpResponse response, Context context)
	{		
		 
		int status = response.getStatusLine().getStatusCode();
		
		switch(status)
		{
			case 118 : this.message = "D�lai imparti � l'op�ration d�pass�";
					   this.toast = Toast.makeText(context, this.message, this.duree);
			           this.toast.show();
				break;
			case 200 : this.message = "Requ�te trait�e avec succ�s";
					   this.toast = Toast.makeText(context, this.message, this.duree);
					   this.toast.show();
				break;

			case 400 : this.message = "La syntaxe de la requ�te est erron�e";
					   this.toast = Toast.makeText(context, this.message, this.duree);
					   this.toast.show();
				break; 
			case 404 : this.message = "Ressource non trouv�e";
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
