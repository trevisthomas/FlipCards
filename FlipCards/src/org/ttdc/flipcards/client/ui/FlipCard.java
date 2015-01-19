package org.ttdc.flipcards.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.client.StudyWordsServiceAsync;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class FlipCard extends Composite {
	interface MyUiBinder extends UiBinder<Widget, FlipCard> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private final StudyWordsServiceAsync studyWordsService = GWT
			.create(StudyWordsService.class); // Not sure if i should share this
												// or not?

	@UiField
	Label wordLabel;
	@UiField
	Label definitionLabel;
	@UiField
	Button yesButton;
	@UiField
	Button noButton;
	@UiField
	Button flipButton;
	@UiField
	Anchor surrenderAnchor;
	@UiField
	FlexTable debugDumpFlexTable;

	private List<WordPair> wordPairs;
	private int currentIndex = 0;
	private WordPair currentPair;
	private final QuizOptions options;
	
	private List<WordPair> incorrectWordPairs = new ArrayList<WordPair>();

	public FlipCard(QuizOptions options, List<WordPair> wordPairList) {
		this.options = options;
		initWidget(uiBinder.createAndBindUi(this));
		
		yesButton.setEnabled(false);
		noButton.setEnabled(false);
		flipButton.setEnabled(false);
		wordLabel.setVisible(true);
		wordLabel.setText("Loading...");
		definitionLabel.setVisible(false);
		surrenderAnchor.setText("I Surrender");

//		yesButton.setStylePrimaryName("fixme");
		
		if(wordPairList != null){
			wordPairs = wordPairList;
			nextWord();
			return;
		}
		else {
		studyWordsService.generateQuiz(options,
				new AsyncCallback<List<WordPair>>() {
					@Override
					public void onSuccess(List<WordPair> result) {
						wordPairs = result;
//						debugDump(wordPairs);
						nextWord();
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
		}
	}
	
	//A debug method
	private void debugDump(List<WordPair> words){
		int row = 0;
		debugDumpFlexTable.setText(row, 0, "Word");
		debugDumpFlexTable.setText(row, 1, "Definition");
		debugDumpFlexTable.setText(row, 2, "Tested Count");
		
		for(WordPair word : words){
			row++;
			debugDumpFlexTable.setText(row, 0, word.getWord());
			debugDumpFlexTable.setText(row, 1, word.getDefinition());
			debugDumpFlexTable.setText(row, 2, "" +word.getTestedCount());
		}
	}
	
	public FlipCard(QuizOptions options) {
		this(options, null);
	}

	
	private void nextWord() {
		yesButton.setEnabled(false);
		noButton.setEnabled(false);
		flipButton.setEnabled(true);
		if (currentIndex >= wordPairs.size()) {
//			Window.alert("You're done.");
			showScore();
		} else {
			currentPair = wordPairs.get(currentIndex++); 
			definitionLabel.setVisible(false);
			wordLabel.setVisible(true);
			wordLabel.setText(currentPair.getWord());
			definitionLabel.setText(currentPair.getDefinition());
		}
	}

	private void showScore() {
		//test
//		showError("Asked: "+wordPairs.size() +"<br> Correct: " + (wordPairs.size() - incorrectWordPairs.size()) + " <BR>Score: "+ ((double)(wordPairs.size() - incorrectWordPairs.size()) / (double)wordPairs.size()) * 100 +"%" );
		FlipCards.replaceView(new Results(options, wordPairs, incorrectWordPairs, currentIndex));
	}

	@UiHandler("flipButton")
	void handleFlipButton(ClickEvent e) {
		// Window.alert("Hello, AJAX");
		wordLabel.setVisible(!wordLabel.isVisible());
		definitionLabel.setVisible(!definitionLabel.isVisible());
		yesButton.setEnabled(true);
		noButton.setEnabled(true);
	}

	@UiHandler("noButton")
	void handleNoButton(ClickEvent e) {
		incorrectWordPairs.add(currentPair);
		updateWordScore(false);
		nextWord();
	}

	@UiHandler("yesButton")
	void handleYesButton(ClickEvent e) {
		updateWordScore(true);
		nextWord();
	}
	
	@UiHandler("surrenderAnchor")
	void surrenderAnchorClicked(ClickEvent e) {
		FlipCards.replaceView(new Results(options, wordPairs, incorrectWordPairs, currentIndex-1));
	}

	private void updateWordScore(boolean correct) {
		studyWordsService.answerQuestion(currentPair.getId(), correct, new AsyncCallback<Void> (){
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				//Nothing to do?
			}
		});
	}
}
