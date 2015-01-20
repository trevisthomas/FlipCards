package org.ttdc.flipcards.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.client.StudyWordsServiceAsync;
import org.ttdc.flipcards.client.ui.staging.StagingManager;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CardManager extends Composite {

	private static final String NONE = "None";

	private static CardManagerUiBinder uiBinder = GWT
			.create(CardManagerUiBinder.class);

	private final StudyWordsServiceAsync studyWordsService = GWT
			.create(StudyWordsService.class);

	interface CardManagerUiBinder extends UiBinder<Widget, CardManager> {
	}

	@UiField
	Button addCardButton;
	@UiField
	TextBox termTextBox;
	@UiField
	TextBox definitionTextBox;
	@UiField
	VerticalPanel cardBrowserPanel;
	@UiField
	Button closeCardManagerButton;
	@UiField
	Anchor tagEditorAnchor;
	@UiField
	Anchor tagFilterAnchor;
	@UiField
	Button goButton;
	@UiField
	ListBox tagListBox;
	@UiField
	HorizontalPanel tagFilterPanel;
	@UiField
	Label dataHeaderLabel;
	
	
	private long lastIndex;

	public CardManager() {
		initWidget(uiBinder.createAndBindUi(this));

		addCardButton.setText("Add Card");
		TextBoxKeyDownHandler keyDownHandler = new TextBoxKeyDownHandler();
		termTextBox.addKeyDownHandler(keyDownHandler);
		definitionTextBox.addKeyDownHandler(keyDownHandler);
		closeCardManagerButton.setText("Close Editor");
		goButton.setText("Apply Filter");
		tagFilterPanel.setVisible(false);
		dataHeaderLabel.setText("Loading...");
		loadWords();
	}

	private void loadWords() {
		studyWordsService.getAllWordPairs(new AsyncCallback<List<WordPair>>() {
			@Override
			public void onSuccess(List<WordPair> result) {
				for (WordPair card : result) {
					lastIndex = card.getDisplayOrder();
					cardBrowserPanel.add(new CardView(card));
				}
				dataHeaderLabel.setText("Loaded " + result.size() + " cards.");
			}

			@Override
			public void onFailure(Throwable caught) {
				dataHeaderLabel.setText("Failed to load.");
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	private void loadWords(String tagId) {
		if(NONE.equals(tagId)){
			loadWords();
			return;
		}
		List<String> tagIds = new ArrayList<>();
		tagIds.add(tagId);
		studyWordsService.getWordPairs(tagIds, new AsyncCallback<List<WordPair>>() {
			@Override
			public void onSuccess(List<WordPair> result) {
				for (WordPair card : result) {
					lastIndex = card.getDisplayOrder();
					cardBrowserPanel.add(new CardView(card));
				}
				dataHeaderLabel.setText("Loaded " + result.size() + " cards.");
			}

			@Override
			public void onFailure(Throwable caught) {
				dataHeaderLabel.setText("Failed to load.");
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}

	private void saveNewCard() {
		String word = termTextBox.getText().trim();
		String definition = definitionTextBox.getText().trim();

		if (word.length() == 0) {
			FlipCards.showErrorMessage("Word can't be blank");
			return;
		}

		if (definition.length() == 0) {
			FlipCards.showErrorMessage("Definition can't be blank");
			return;
		}

		studyWordsService.addWordPair(word, definition,
				new AsyncCallback<WordPair>() {
					@Override
					public void onSuccess(WordPair card) {
						termTextBox.setText("");
						definitionTextBox.setText("");
						termTextBox.setFocus(true);
						card.setDisplayOrder(lastIndex); //So lame.
						cardBrowserPanel.add(new CardView(card));
						FlipCards.showMessage("New card created");
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
	}

	private class TextBoxKeyDownHandler implements KeyDownHandler {
		@Override
		public void onKeyDown(KeyDownEvent event) {
			FlipCards.clearErrorMessage();
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				saveNewCard();
			}
		}
	}
	
	@UiHandler("closeCardManagerButton")
	void onCloseClick(ClickEvent e) {
		FlipCards.replaceView(new QuizSelection());
	}

	@UiHandler("stagingAnchor")
	void onViewStagingClick(ClickEvent e) {
		FlipCards.replaceView(new StagingManager());
	}
	
	@UiHandler("addCardButton")
	void onClick(ClickEvent e) {
		saveNewCard();
	}

	@UiHandler("tagEditorAnchor")
	void onShowTagEditorClick(ClickEvent e) {
		TagManager.show();
	}
	
	@UiHandler("tagFilterAnchor")
	void onToggleTagFilterClick(ClickEvent e) {
		tagFilterPanel.setVisible(!tagFilterPanel.isVisible());
		
		if(tagFilterPanel.isVisible()){
			tagListBox.clear();
			studyWordsService.getAllTagNames(new AsyncCallback<List<Tag>>() {
				
				@Override
				public void onSuccess(List<Tag> result) {
					tagListBox.addItem(NONE, NONE);
					for(Tag tag : result){
						tagListBox.addItem(tag.getTagName(), tag.getTagId());
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					FlipCards.showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
	@UiHandler("tagListBox")
	void onTagFilterChange(ChangeEvent e){
		String selected = tagListBox.getValue(tagListBox.getSelectedIndex());
		//Window.alert("Sel:" +selected);
		dataHeaderLabel.setText("Loading...");
		cardBrowserPanel.clear(); //Show wait icon.
		loadWords(selected);
	}
	
	
	@UiHandler("goButton")
	void onFilterButtonClick(ClickEvent e) {
//		String selected = tagListBox.getValue(tagListBox.getSelectedIndex());
//		Window.alert("Sel:" +selected);
		onTagFilterChange(null);//shrug
	}
}
