package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.shared.AutoCompleteWordPairList;
import org.ttdc.flipcards.shared.CardOrder;
import org.ttdc.flipcards.shared.ItemFilter;
import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.PagedWordPair;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("studyWords")
public interface StudyWordsService extends RemoteService{
	WordPair addWordPair(String word, String definition) throws IllegalArgumentException, NotLoggedInException;
	WordPair updateWordPair(String id, String word, String definition) throws IllegalArgumentException, NotLoggedInException;
	Boolean deleteWordPair(String id) throws IllegalArgumentException, NotLoggedInException;
	List<WordPair> generateQuiz(QuizOptions quizOptions) throws NotLoggedInException;
	String getFileUploadUrl() throws NotLoggedInException;
	void assignSelfToUserlessWords() throws NotLoggedInException;
	
	List<Tag> getAllTagNames() throws IllegalArgumentException, NotLoggedInException;
	Tag createTag(String name) throws IllegalArgumentException, NotLoggedInException;
	void deleteTagName(String tagId) throws IllegalArgumentException, NotLoggedInException;
	Tag updateTagName(String tagId, String name) throws IllegalArgumentException, NotLoggedInException;
	void deTag(String tagId, String cardId) throws IllegalArgumentException, NotLoggedInException;
	void applyTag(String tagId, String cardId) throws IllegalArgumentException, NotLoggedInException;
	List<String> getStudyFriends() throws IllegalArgumentException,
			NotLoggedInException;
	WordPair setActiveStatus(String id, boolean active)
			throws IllegalArgumentException, NotLoggedInException;
	List<WordPair> getWordPairsForPage(int pageNumber)
			throws IllegalArgumentException, NotLoggedInException;
	
	PagedWordPair getWordPairs(List<String> tagIds, List<String> users,
			ItemFilter filter, CardOrder cardOrder, int pageNumber, int perPage)
			throws IllegalArgumentException, NotLoggedInException;
	AutoCompleteWordPairList getAutoCompleteWordPairs(List<String> owners, int sequence,
			String qstr) throws IllegalArgumentException, NotLoggedInException;
	WordPair getStudyItem(String studyItemId) throws IllegalArgumentException, NotLoggedInException;
	void answerQuestion(String id, long duration, Boolean correct)
			throws IllegalArgumentException, NotLoggedInException;
	boolean migrate(int table, int pageNumber) throws NotLoggedInException;

}


