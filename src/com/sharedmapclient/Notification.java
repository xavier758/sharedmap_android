package com.sharedmapclient;

public class Notification {

	static final String MSG_INVITATION = "souhaite vous inviter � rejoindre le groupe.";
	static final String MSG_REQUEST = "souhaite rejoindre votre groupe.";
	static final String MSG_MAP_EVENT = "a marqu� la carte.";
	
	enum NotifType{
		INVITATION,
		REQUEST,
		MAP_EVENT
	};
	
	String hashtag;
	String userPseudo;
	NotifType type;
	String id;
}
