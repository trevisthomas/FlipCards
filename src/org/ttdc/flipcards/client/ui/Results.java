package org.ttdc.flipcards.client.ui;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.client.StudyWordsServiceAsync;
import org.ttdc.flipcards.client.ViewName;
import org.ttdc.flipcards.client.ui.FlipCard.MyUiBinder;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class Results extends Composite {
	interface MyUiBinder extends UiBinder<Widget, Results> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private final StudyWordsServiceAsync studyWordsService = GWT
			.create(StudyWordsService.class); // Not sure if i should share this
												// or not?

	@UiField
	Label asked;
	@UiField
	Label correct;
	@UiField
	Label score;
	@UiField
	Label unanswered;
	@UiField
	Button studyWrongWordsButton;
	@UiField
	Anchor newQuizButton;
	
	
	private final QuizOptions options;
	private List<WordPair> wordPairs;
	private List<WordPair> incorrectWordPairs;
	
	public Results(QuizOptions options, List<WordPair> wordPairs, List<WordPair> incorrectWordPairs, int questionsAsked) {
		initWidget(uiBinder.createAndBindUi(this));
		this.options = options;
		this.wordPairs = wordPairs;
		this.incorrectWordPairs = incorrectWordPairs;
		
		asked.setText("Asked: "+questionsAsked);
		correct.setText("Correct: "+(questionsAsked - incorrectWordPairs.size()));
//		score.setText("Score: "+(((double)((wordPairs.size() - questionsAsked) - incorrectWordPairs.size()) / (double)wordPairs.size()) * 100) +"%");
		
		score.setText("Score: "+(((double)(questionsAsked - incorrectWordPairs.size()) / (double)questionsAsked) * 100) +"%");
		if( wordPairs.size() - questionsAsked > 0){
			unanswered.setText("Unanswered: " + (wordPairs.size() - questionsAsked));
		}
		
		if(incorrectWordPairs.size() == 0){
			studyWrongWordsButton.setVisible(false);
		}
	}
	
	@UiHandler("studyWrongWordsButton")
	void handleNoButton(ClickEvent e) {
		FlipCards.replaceView(ViewName.FLIPCARDS, new FlipCard(options, incorrectWordPairs));
	}
	
	@UiHandler("newQuizButton")
	void handleNewQuiz(ClickEvent e) {
		FlipCards.loadView(ViewName.QUIZ_SELECTION);
	}
	
	@UiHandler("editCardsButton")
	void handleEditCards(ClickEvent e) {
		FlipCards.loadView(ViewName.CARD_MANAGER);
	}
	

}
