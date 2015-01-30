package org.ttdc.flipcards.client.services;

import java.util.List;

import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.PagedWordPair;
import org.ttdc.flipcards.shared.User;
import org.ttdc.flipcards.shared.WordPair;


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("staging")
public interface StagingCardService  extends RemoteService {
	String getFileUploadUrl() throws NotLoggedInException;
	
	WordPair add(String term, String definition) throws IllegalArgumentException, NotLoggedInException;

	WordPair update(String id, String word, String definition) throws IllegalArgumentException, NotLoggedInException;

	void delete(String id) throws IllegalArgumentException, NotLoggedInException;

	void promote(String id) throws IllegalArgumentException, NotLoggedInException;
	
	List<String> getStagingFriends() throws IllegalArgumentException, NotLoggedInException;

	List<WordPair> getStagedCards(String owner)
			throws IllegalArgumentException, NotLoggedInException;
	
	PagedWordPair getStagedCards(String owner, String cursor)
			throws IllegalArgumentException, NotLoggedInException;
}

