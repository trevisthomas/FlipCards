package org.ttdc.flipcards.server;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PersistantWordPair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5053123430650344749L;

	@Persistent
	private String word;
	@Persistent
	private String definition;
	@Persistent
	private Date createDate = new Date();
	@Persistent
	private User user;
	@Persistent
	private String dictionaryId;
	@PrimaryKey
	@Persistent
	private String id;

	public PersistantWordPair() {

	}

	public String getId() {
		return id;
	}

	public PersistantWordPair(String id, String word, String definition,
			String dictionaryId, User user) {
		this.id = id;
		this.word = word;
		this.definition = definition;
		this.dictionaryId = dictionaryId;
		this.user = user;
	}

	// public WordPair(String word, String definition) {
	// this.id = id;
	// this.word = word;
	// this.definition = definition;
	// }

	public String getWord() {
		return word;
	}

	public String getDefinition() {
		return definition;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(String dictionaryId) {
		this.dictionaryId = dictionaryId;
	}
}
