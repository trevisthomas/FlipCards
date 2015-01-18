package org.ttdc.flipcards.client.ui;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.client.StudyWordsServiceAsync;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CardManager extends Composite {

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
	Button csvUploadButton;
	@UiField
	Button closeCardManagerButton;
	@UiField
	VerticalPanel uploadCsvPanel;
	
	private int lastIndex;

	public CardManager() {
		initWidget(uiBinder.createAndBindUi(this));

		addCardButton.setText("Add Card");
		csvUploadButton.setText("Upload CSV");
		TextBoxKeyDownHandler keyDownHandler = new TextBoxKeyDownHandler();
		termTextBox.addKeyDownHandler(keyDownHandler);
		definitionTextBox.addKeyDownHandler(keyDownHandler);
		closeCardManagerButton.setText("Close Editor");
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
			}

			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}

	private void saveNewCard() {
		String word = termTextBox.getText().trim();
		String definition = definitionTextBox.getText().trim();

		if (word.length() == 0) {
			FlipCards.showErrorMessage("Word can't be blank");
		}

		if (definition.length() == 0) {
			FlipCards.showErrorMessage("Definition can't be blank");
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

	@UiHandler("addCardButton")
	void onClick(ClickEvent e) {
		saveNewCard();
	}
	@UiHandler("csvUploadButton")
	void onCsvUploadClick(ClickEvent e) {
		uploadCsvPanel.add(new Upload());
	}
	
}
