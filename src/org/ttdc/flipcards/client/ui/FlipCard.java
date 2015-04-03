package org.ttdc.flipcards.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.client.StudyWordsServiceAsync;
import org.ttdc.flipcards.client.ViewName;
import org.ttdc.flipcards.client.ui.skeleton.CardEdit2;
import org.ttdc.flipcards.client.ui.skeleton.ConfirmationPopup;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FlipCard extends Composite implements CardEdit2.CardEditObserver{
	interface MyUiBinder extends UiBinder<Widget, FlipCard> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	private final StudyWordsServiceAsync studyWordsService = GWT
			.create(StudyWordsService.class); // Not sure if i should share this
												// or not?
//	private NumberFormat formatter = new NumberFormat("#0.0000");
	
	@UiField
	Label wordLabel;
	@UiField
	Label definitionLabel;
	@UiField
	Anchor surrenderAnchor;
	@UiField
	FlexTable debugDumpFlexTable;
	@UiField
	Anchor debugAnchor; 
//	@UiField
//	Anchor lingueeAnchor;
//	@UiField
//	Anchor spanishDictAnchor;
//	@UiField
//	HTMLPanel definePanel;
	@UiField
	Anchor editAnchor;
	@UiField
	HTMLPanel editCardPanel;
	@UiField
	Label yesDiv;
	@UiField
	Label noDiv;
	@UiField
	Anchor logoffAnchor;
	
	
//	http://www.linguee.com/english-spanish/search?source=spanish&query=facile
//		http://www.spanishdict.com/translate/amor

	private List<WordPair> wordPairs;
	private int currentIndex = 0;
	private WordPair currentPair;
	private final QuizOptions options;
	boolean spanishFirst = false;
	private long timeStarted;
	private long duration;
	private static final long TIMEOUT = 30*1000;
	private Timer t;
	
	
	private List<WordPair> incorrectWordPairs = new ArrayList<WordPair>();

	public FlipCard(QuizOptions options, List<WordPair> wordPairList) {
		this.options = options;
		initWidget(uiBinder.createAndBindUi(this));
		
		yesDiv.setVisible(false);
		noDiv.setVisible(false);
		wordLabel.setVisible(true);
		wordLabel.setText("Loading...");
		definitionLabel.setVisible(false);
		surrenderAnchor.setText("I Surrender");
		debugDumpFlexTable.setVisible(false);
		debugAnchor.setText("debug");
		editAnchor.setText("Edit");
//		lingueeAnchor.setText("Linguee");
//		spanishDictAnchor.setText("Spanish Dict");
//		lingueeAnchor.setTarget("_blank");
//		spanishDictAnchor.setTarget("_blank");
//		definePanel.setVisible(false);
		
		logoffAnchor.setHref(FlipCards.getSignOutHref());
		
		if(wordPairList != null){
			wordPairs = wordPairList;
			debugDump(wordPairs);
			nextWord();
			return;
		} else {
		studyWordsService.generateQuiz(options,
				new AsyncCallback<List<WordPair>>() {
					@Override
					public void onSuccess(List<WordPair> result) {
						wordPairs = result;
						debugDump(wordPairs);
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
		debugDumpFlexTable.setText(row, 3, "Correct");
		debugDumpFlexTable.setText(row, 4, "Difficulty");
		debugDumpFlexTable.setText(row, 5, "AverageTime");
		
		for(WordPair word : words){
			row++;
			debugDumpFlexTable.setText(row, 0, word.getWord());
			debugDumpFlexTable.setText(row, 1, word.getDefinition());
			debugDumpFlexTable.setText(row, 2, "" +word.getTestedCount());
			debugDumpFlexTable.setText(row, 3, "" +word.getCorrectCount());
			debugDumpFlexTable.setText(row, 4, NumberFormat.getFormat("#0.000000").format(word.getDifficulty()));
			debugDumpFlexTable.setText(row, 5, ""+word.getAverageTime()+"ms");
		}
	}
	
	public FlipCard(QuizOptions options) {
		this(options, null);
	}

	
	private void nextWord() {
		yesDiv.setVisible(false);
		noDiv.setVisible(false);
		if (currentIndex >= wordPairs.size()) {
			showScore();
		} else {
			
//			definePanel.setVisible(false);
			
			currentPair = wordPairs.get(currentIndex++); 
			definitionLabel.setVisible(false);
			wordLabel.setVisible(true);
			loadWordPair();
		}
	}

	private void loadWordPair() {
		t = new Timer() {
			@Override
			public void run() {
				handleFlipButton(null);
				new ConfirmationPopup(null, "Time's Up!", "The time allowed for a response has expired.");
			}
		};
		t.schedule((int) TIMEOUT);
		    
		timeStarted = System.currentTimeMillis();
		duration = 0;
		switch(options.getCardSide()){
			case TERM:{
				spanishFirst = true;
				break;
			}
			case RANDOM:{
				spanishFirst = Random.nextBoolean();
				break;
			}
			case DEFINITION:{
				spanishFirst = false;
				break;
			}
		}
			
		if(spanishFirst){
			wordLabel.setText(currentPair.getWord());
			definitionLabel.setText(currentPair.getDefinition());
		}
		else {
			wordLabel.setText(currentPair.getDefinition());
			definitionLabel.setText(currentPair.getWord());
		}
		
//		spanishDictAnchor.setHref("http://www.spanishdict.com/translate/"+currentPair.getWord());
//		lingueeAnchor.setHref("http://www.linguee.com/english-spanish/search?source=spanish&query="+currentPair.getWord());
	}
	
	@UiHandler("wordLabel")
	void handleFlipWordLabel(ClickEvent e) {
		handleFlipButton(e);
	}
	@UiHandler("definitionLabel")
	void handleFlipDefinitionLabel(ClickEvent e) {
		handleFlipButton(e);
	}
	

	private void showScore() {
		//test
//		showError("Asked: "+wordPairs.size() +"<br> Correct: " + (wordPairs.size() - incorrectWordPairs.size()) + " <BR>Score: "+ ((double)(wordPairs.size() - incorrectWordPairs.size()) / (double)wordPairs.size()) * 100 +"%" );
		FlipCards.replaceView(ViewName.RESULT, new Results(options, wordPairs, incorrectWordPairs, currentIndex));
	}

//	@UiHandler("flipButton")
	void handleFlipButton(ClickEvent e) {
		cancelTimer();
		wordLabel.setVisible(!wordLabel.isVisible());
		definitionLabel.setVisible(!definitionLabel.isVisible());
		yesDiv.setVisible(true);
		noDiv.setVisible(true);
//		definePanel.setVisible(true);
		editCardPanel.clear();
		
		duration = System.currentTimeMillis() - timeStarted;
		if(duration > TIMEOUT){
			duration = TIMEOUT;
		}
	}

	
	
	@UiHandler("editAnchor")
	void handleEditAnchorClick(ClickEvent e) {
		editCardPanel.clear();
		CardEdit2 ce = new CardEdit2(this, currentPair);
		ce.show();
		editCardPanel.add(ce);
	}
	
	@UiHandler("noDiv")
	void handleNoDiv(ClickEvent e) {
		incorrectWordPairs.add(currentPair);
		updateWordScore(false);
		nextWord();
	}
	
	@UiHandler("yesDiv")
	void handleYesDiv(ClickEvent e) {
		updateWordScore(true);
		nextWord();
	}
	
	@UiHandler("debugAnchor")
	void debugClicked(ClickEvent e){
		debugDumpFlexTable.setVisible(!debugDumpFlexTable.isVisible());
	}
	
	@UiHandler("surrenderAnchor")
	void surrenderAnchorClicked(ClickEvent e) {
		cancelTimer();
		FlipCards.replaceView(ViewName.RESULT, new Results(options, wordPairs, incorrectWordPairs, currentIndex-1));
	}

	private void cancelTimer() {
		if(t != null){
			t.cancel();
		}
	}

	private void updateWordScore(boolean correct) {
		
		studyWordsService.answerQuestion(currentPair.getId(), duration, correct, new AsyncCallback<Void> (){
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
	
	@Override
	public void onCardDeleted() {
		nextWord();
		editCardPanel.clear();
	}
	
	@Override
	public void onCardUpdated(WordPair result) {
		currentPair = result; //Hmn
		loadWordPair();
		editCardPanel.clear();
	}
	
	@Override
	public void onCardEditClose(WordPair card) {
		editCardPanel.clear();
	}
	
	
	
}
