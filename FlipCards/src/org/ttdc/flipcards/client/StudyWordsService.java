package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("studyWords")
public interface StudyWordsService extends RemoteService{
	WordPair addWordPair(String dictionaryId, String word, String definition) throws IllegalArgumentException, NotLoggedInException;
	WordPair updateWordPair(String id, String word, String definition) throws IllegalArgumentException, NotLoggedInException;
	Boolean deleteWordPair(String id) throws IllegalArgumentException, NotLoggedInException;
	List<WordPair> generateQuiz(QuizOptions quizOptions) throws NotLoggedInException;
	void answerQuestion(String id, Boolean correct) throws IllegalArgumentException, NotLoggedInException;
	List<WordPair> getAllWordPairs(String dictionaryId) throws NotLoggedInException;
}


