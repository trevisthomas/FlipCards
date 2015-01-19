package org.ttdc.flipcards.server;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PersistantTagName{
	@PrimaryKey
	@Persistent
	private String tagId;
	@Persistent
	private String tagName;
	@Persistent
	private User user;
	
	public PersistantTagName() {
	}
	
	public PersistantTagName(User user, String tagId, String tagName){
		this.tagId = tagId;
		this.tagName = tagName;
		this.user = user;
	}
	
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
