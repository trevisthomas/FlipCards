package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewAddWords extends VerticalPanel{
	private FlexTable wordsFlexTable;
	private final Label errorMessageLabel = new Label();
	private final Button addWordButton = new Button("Add");
	private final Button studyButton = new Button("Study");
	
	private final TextBox wordTextBox = new TextBox();
	private final TextBox definitionTextBox = new TextBox();
	
	private int row = 0;
	
	private final StudyWordsServiceAsync studyWordsService = GWT.create(StudyWordsService.class); //Not sure if i should share this or not?
	
	public ViewAddWords() {
		final VerticalPanel addWordsOuterPanel = new VerticalPanel(); 
		final HorizontalPanel newPairPanel = new HorizontalPanel();
		
		newPairPanel.add(wordTextBox);
		newPairPanel.add(definitionTextBox);
		
		addWordsOuterPanel.add(errorMessageLabel);
		addWordsOuterPanel.add(newPairPanel);
		HorizontalPanel buttonPanel = new HorizontalPanel();
		buttonPanel.add(studyButton);
		buttonPanel.add(addWordButton);
		addWordsOuterPanel.add(buttonPanel);
		
		
		TextBoxKeyDownHandler keyDownHandler = new TextBoxKeyDownHandler();
		
		wordTextBox.addKeyDownHandler(keyDownHandler);
		definitionTextBox.addKeyDownHandler(keyDownHandler);
		
		studyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FlipCards.showStudyView();
			}
		});
		
		addWordButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addWord();
			}
		});
		
		wordsFlexTable = new FlexTable();
		
		wordsFlexTable.setText(row, 0, "Word");
		wordsFlexTable.setText(row, 1, "Definition");
		wordsFlexTable.setText(row, 2, "Remove");
		
		add(addWordsOuterPanel);
		add(wordsFlexTable);
		
		loadWords();
	}
	
	private void loadWords() {
		studyWordsService.getAllWordPairs(new AsyncCallback<List<WordPair>>() {
			
			@Override
			public void onSuccess(List<WordPair> result) {
				for(WordPair pair : result){
					addWordPair(pair);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				errorMessageLabel.setText(caught.getMessage());
				errorMessageLabel.setVisible(true);
			}
		});
	}
	
	private void addWordPair(WordPair result) {
		row++;
		wordsFlexTable.setText(row, 0, result.getWord());
		wordsFlexTable.setText(row, 1, result.getDefinition());
		wordsFlexTable.setWidget(row, 2, new RemoveButton(row, result.getId()));
	}

	private void addWord() {
		try {
			add(wordTextBox.getText().trim(), definitionTextBox.getText().trim());
			wordTextBox.setText("");
			definitionTextBox.setText("");
			wordTextBox.setFocus(true);
		} catch (ValidationException e) {
			showError(e.getMessage());
		}
	}

	private void showError(String message) {
		errorMessageLabel.setText(message);
		errorMessageLabel.setVisible(true);
	}
	
	private class TextBoxKeyDownHandler implements KeyDownHandler{
		@Override
		public void onKeyDown(KeyDownEvent event) {
			errorMessageLabel.setVisible(false);
			if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
				addWord();
			}
		}
	}
	
	
	private class RemoveButton extends Button{
		public RemoveButton(final int row, final String id) {
			setText("X");
			addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//Web service remove
					studyWordsService.deleteWordPair(id, new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {
							if(result == true){
								wordsFlexTable.removeRow(row);
							}
							else {
								showError("Server failed to remove");
							}
						}
						@Override
						public void onFailure(Throwable caught) {
							showError("Caught exception" + caught.getMessage());
						}
					});
				}
			});
		}
	}
	
	
	private void add(String word, String definition) throws ValidationException{
		if(word.length() == 0){
			throw new ValidationException("Word is bad");
		}
		
		if(definition.length() == 0){
			throw new ValidationException("Definition is bad");	
		}
		
		
		studyWordsService.addWordPair(word, definition, new AsyncCallback<WordPair>() {
			
			@Override
			public void onSuccess(WordPair result) {
				addWordPair(result);
			}

			
			
			@Override
			public void onFailure(Throwable caught) {
				errorMessageLabel.setText(caught.getMessage());
				errorMessageLabel.setVisible(true);
			}
		});
	}

	
	
}
