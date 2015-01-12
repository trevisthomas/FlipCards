package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("studyWords")
public interface StudyWordsService extends RemoteService{
	WordPair addWordPair(String word, String definition) throws IllegalArgumentException;
	WordPair updateWordPair(String id, String word, String definition) throws IllegalArgumentException;
	Boolean deleteWordPair(String id) throws IllegalArgumentException;
	List<WordPair> getAllWordPairs();
}


