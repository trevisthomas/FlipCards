package org.ttdc.flipcards.server;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class TagAssociation implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 2739153469922337943L;
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	@Persistent
	private String tagId;
	@Persistent
	private String cardId;
	@Persistent
	private String cardOwner;
	
	//TREVIS: Once this thing is working, see if you can remove User!
	
	public TagAssociation(String cardOwner, String tagId, String cardId){
		this.tagId = tagId;
		this.cardId = cardId;
		this.cardOwner = cardOwner;
	}
	
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	public String getCardId() {
		return cardId;
	}
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getCardOwner() {
		return cardOwner;
	}

	public void setCardOwner(String cardOwner) {
		this.cardOwner = cardOwner;
	}
	
	
	
}
