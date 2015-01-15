package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.user.client.rpc.AsyncCallback;

//Nice, got an error when this didnt exist and then the red dot auto created it for me.
public interface StudyWordsServiceAsync {
	void addWordPair(String word, String definition, AsyncCallback<WordPair> callback);

	void getAllWordPairs(AsyncCallback<List<WordPair>> callback);

	void updateWordPair(String id, String word, String definition,
			AsyncCallback<WordPair> callback);

	void deleteWordPair(String id, AsyncCallback<Boolean> callback);

	void generateQuiz(QuizOptions quizOptions,
			AsyncCallback<List<WordPair>> callback);

	void answerQuestion(String id, Boolean correct, AsyncCallback<Void> callback);
}
