package org.ttdc.flipcards.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StudyWordsServiceImpl extends RemoteServiceServlet implements StudyWordsService{
//	private static List<WordPair> wordPairs = new ArrayList<>();
	private static Map<String, WordPair> wordPairs = new HashMap<>();
	@Override
	public WordPair addWordPair(String word, String definition) throws IllegalArgumentException {
		UUID uuid = java.util.UUID.randomUUID();
		
		WordPair pair = new WordPair(uuid.toString(), word, definition);
		
		System.err.println("Created: " + pair.getId() + ": " + word);
		
		wordPairs.put(pair.getId(), pair);
		return pair;
		
	}
	
	@Override
	public List<WordPair> getAllWordPairs() {
		return new ArrayList<>(wordPairs.values());
	}

	@Override
	public WordPair updateWordPair(String id, String word, String definition)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean deleteWordPair(String id) throws IllegalArgumentException {
		return wordPairs.remove(id) != null;
		
	}
}
