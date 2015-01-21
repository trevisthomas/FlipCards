package org.ttdc.flipcards.shared;

public class User {
	private String username;
	private String userid;
	
	public User(String id, String name){
		username = name;
		userid = id;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	
}
