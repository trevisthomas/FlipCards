package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.client.ui.FlipCard;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewQuizConfigure  extends VerticalPanel{
	private final StudyWordsServiceAsync studyWordsService = GWT.create(StudyWordsService.class); //Not sure if i should share this or not?
	
	private final Label errorLabel = new Label();
	
	ListBox howManyWordsListBox = new ListBox();
	
	public ViewQuizConfigure() {
		add(errorLabel);
		errorLabel.setVisible(false);
		setWidth("400px");
		add(new Label("Quiz Options"));
		
		FlexTable formTable = new FlexTable();
		Button startTestButton = new Button("Start Quiz");
		
		howManyWordsListBox.addItem("10");
		howManyWordsListBox.addItem("20");
		howManyWordsListBox.addItem("30");
		howManyWordsListBox.addItem("40");
		howManyWordsListBox.addItem("50");
		howManyWordsListBox.addItem("100");
		howManyWordsListBox.addItem("All");
		
		
		int row = 0;
		formTable.setText(row, 0, "How many words?");
		formTable.setWidget(row, 1, howManyWordsListBox);
		row++;
		
		final VerticalPanel test = new VerticalPanel();
		
		add(formTable);
		add(startTestButton);
		
		add(test);
		
		startTestButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				QuizOptions options = new QuizOptions();
				if(howManyWordsListBox.getSelectedIndex() == howManyWordsListBox.getItemCount() - 1){
					options.setSize(-1);
				}	
				else{
					options.setSize(Integer.parseInt(howManyWordsListBox.getValue(howManyWordsListBox.getSelectedIndex())));
				}
				
				//TODO real dict id!
				options.setDictionaryId(ViewAddWords.DICTIONARY_ID_DEFAULT);
				FlipCards.replaceView(new FlipCard(options));
				
			}
		});
		
		studyWordsService.getAllWordPairs(ViewAddWords.DICTIONARY_ID_DEFAULT, new AsyncCallback<List<WordPair>>() {
			@Override
			public void onSuccess(List<WordPair> result) {
				for(WordPair pair : result){
					test.add(new Label(pair.getWord()));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showError(caught.getMessage());
			}
		});
	}
	
	void showError(String errorMessage){
		errorLabel.setText(errorMessage);
		errorLabel.setVisible(true);
	}
}
