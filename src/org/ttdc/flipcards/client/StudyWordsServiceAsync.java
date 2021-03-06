package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.shared.AutoCompleteWordPairList;
import org.ttdc.flipcards.shared.CardOrder;
import org.ttdc.flipcards.shared.ItemFilter;
import org.ttdc.flipcards.shared.PagedWordPair;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;









import com.google.gwt.user.client.rpc.AsyncCallback;

//Nice, got an error when this didnt exist and then the red dot auto created it for me.
public interface StudyWordsServiceAsync {
	void addWordPair(String word, String definition, List<String> tagIds, AsyncCallback<WordPair> callback);

	void getWordPairs(List<String> tagIds, List<String> users,
			ItemFilter filter, CardOrder cardOrder, int pageNumber,
			int perPage, AsyncCallback<PagedWordPair> callback);

	void updateWordPair(String id, String word, String definition,
			String example, AsyncCallback<WordPair> callback);

	void deleteWordPair(String id, AsyncCallback<Boolean> callback);

	void generateQuiz(QuizOptions quizOptions,
			AsyncCallback<List<WordPair>> callback);

	void getFileUploadUrl(AsyncCallback<String> callback);

	void assignSelfToUserlessWords(AsyncCallback<Void> callback);

	void getAllTagNames(AsyncCallback<List<Tag>> callback);

	void applyTag(String tagId, String cardId, AsyncCallback<Void> callback);

	void createTag(String name, AsyncCallback<Tag> callback);

	void deleteTagName(String tagId, AsyncCallback<Void> callback);

	void deTag(String tagId, String cardId, AsyncCallback<Void> callback);

	void updateTagName(String tagId, String name, AsyncCallback<Tag> callback);

	void getStudyFriends(AsyncCallback<List<String>> callback);

	void setActiveStatus(String id, boolean active,
			AsyncCallback<WordPair> callback);

	void getWordPairsForPage(int pageNumber,
			AsyncCallback<List<WordPair>> callback);

	void getAutoCompleteWordPairs(List<String> owners, int sequence, String qstr,
			AsyncCallback<AutoCompleteWordPairList> callback);

	void getStudyItem(String studyItemId, AsyncCallback<WordPair> callback);

	void answerQuestion(String id, long duration, Boolean correct,
			AsyncCallback<Void> callback);

	void migrate(int table, int pageNumber, AsyncCallback<Boolean> callback);
	
}
