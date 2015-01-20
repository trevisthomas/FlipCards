package org.ttdc.flipcards.client.services;

import java.util.List;

import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StagingCardServiceAsync {
	void add(String term, String definition, AsyncCallback<WordPair> callback);

	void update(String id, String word, String definition,
			AsyncCallback<WordPair> asyncCallback);

	void delete(String id, AsyncCallback<Void> asyncCallback);

	void promote(String id, AsyncCallback<Void> asyncCallback);

	void getStagedCards(AsyncCallback<List<WordPair>> callback);

	void getFileUploadUrl(AsyncCallback<String> callback);
	

}
