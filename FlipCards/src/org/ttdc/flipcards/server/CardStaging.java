package org.ttdc.flipcards.server;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class CardStaging {
	@Persistent
	private String word;
	@Persistent
	private String definition;
	@Persistent
	private Date createDate = new Date();
	@Persistent
	private User user;
	@PrimaryKey
	@Persistent
	@Index(name="STAGING_CARD_ID_IDX")
	private String id;
	
	public CardStaging(String id, String word, String definition, User user) {
		this.id = id;
		this.word = word;
		this.definition = definition;
		this.user = user;
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
