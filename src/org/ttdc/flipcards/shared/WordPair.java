package org.ttdc.flipcards.shared;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WordPair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5138759608971399534L;

	@Persistent
	private String word;
	@Persistent
	private String definition;
	@Persistent
	private Date createDate;

	private int testedCount;
	private int correctCount;

	@PrimaryKey
//	@Persistent(valueStrategy = IdGeneratorStrategy.UUIDSTRING)
	@Persistent
	private String id;

	public WordPair() {

	}

	public String getId() {
		return id;
	}

	public WordPair(String id, String word, String definition) {
		this.id = id;
		this.word = word;
		this.definition = definition;
	}
	
//	public WordPair(String word, String definition) {
//		this.id = id;
//		this.word = word;
//		this.definition = definition;
//	}


	public String getWord() {
		return word;
	}

	public String getDefinition() {
		return definition;
	}

	public int getTestedCount() {
		return testedCount;
	}

	public void setTestedCount(int testedCount) {
		this.testedCount = testedCount;
	}

	public int getCorrectCount() {
		return correctCount;
	}

	public void setCorrectCount(int correctCount) {
		this.correctCount = correctCount;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

}
