package org.ttdc.flipcards.client.ui;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.Tag;
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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CardEdit extends Composite {

	private static CardEditUiBinder uiBinder = GWT
			.create(CardEditUiBinder.class);
	
	private WordPair card;
	private CardEditObserver cardView;
		
	interface CardEditUiBinder extends UiBinder<Widget, CardEdit> {
	}

	@UiField
	Button updateButton;
	@UiField
	Button deleteButton;
	@UiField
	Button activateButton;
	@UiField
	Button deactivateButton;
	
	@UiField 
	TextBox termTextBox;
	@UiField
	FlowPanel tagMeFlowPanel;
	@UiField 
	TextBox definitionTextBox;
	@UiField
	Anchor closeAnchor;
	
	interface CardEditObserver{
		void onCardUpdated(WordPair result);
		void onCardDeleted();
		void onCardEditClose(WordPair card);
	}

	public CardEdit(CardEditObserver cardView, WordPair c) {
		initWidget(uiBinder.createAndBindUi(this));
		this.card = c;
		this.cardView = cardView;
		updateButton.setText("Update");
		deleteButton.setText("Delete");
		activateButton.setText("Activate!");
		deactivateButton.setText("Deactivate");
		closeAnchor.setText("close");
		termTextBox.setText(card.getWord());
		definitionTextBox.setText(card.getDefinition());
		TextBoxKeyDownHandler handler = new TextBoxKeyDownHandler();
		definitionTextBox.addKeyDownHandler(handler);
		termTextBox.addKeyDownHandler(handler);
		
		deleteButton.setEnabled(c.isDeleteAllowed());
		deleteButton.setVisible(!c.isActive());
		
		activateButton.setVisible(!c.isActive());
		deactivateButton.setVisible(c.isActive());
		
		//Load tags
		FlipCards.studyWordsService.getAllTagNames(new AsyncCallback<List<Tag>>() {
			@Override
			public void onSuccess(List<Tag> result) {
				for(Tag tag : result){
					tagMeFlowPanel.add(new TagEditor(card, tag));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});

	}

	@UiHandler("updateButton")
	void onUpdateClick(ClickEvent e) {
		performUpdate();
	}

	private void performUpdate() {
		String word = termTextBox.getText().trim();
		String definition = definitionTextBox.getText().trim();

		if (word.length() == 0) {
			FlipCards.showErrorMessage("Word can't be blank");
		}

		if (definition.length() == 0) {
			FlipCards.showErrorMessage("Definition can't be blank");
		}
		
		FlipCards.studyWordsService.updateWordPair(card.getId(), word, definition, new AsyncCallback<WordPair>() {
			@Override
			public void onSuccess(WordPair result) {
				FlipCards.showMessage("Card updated");
				result.setDisplayOrder(card.getDisplayOrder()); //This is probably dumb.
				cardView.onCardUpdated(result); 
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
				performUpdate();
			}
		}
	}
	
	@UiHandler("closeAnchor")
	void onCloseClick(ClickEvent e) {
		cardView.onCardEditClose(card); 
	}
	
	
	@UiHandler("activateButton")
	void onActivateClick(ClickEvent e) {
		FlipCards.studyWordsService.setActiveStatus(card.getId(), true, new AsyncCallback<WordPair>() {
			@Override
			public void onSuccess(WordPair result) {
				cardView.onCardUpdated(result); 
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage("Server failed to activate");
			}
		});
	}
	
	@UiHandler("deactivateButton")
	void onDeactivateClick(ClickEvent e) {
		FlipCards.studyWordsService.setActiveStatus(card.getId(), false, new AsyncCallback<WordPair>() {
			@Override
			public void onSuccess(WordPair result) {
				cardView.onCardUpdated(result); 
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage("Server failed to deactivate");
			}
		});
	}
	
	@UiHandler("deleteButton")
	void onDeleteClick(ClickEvent e) {
		//Web service remove
		FlipCards.studyWordsService.deleteWordPair(card.getId(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				if(result == true){
					FlipCards.showMessage("Card deleted");
					cardView.onCardDeleted();
				}
				else {
					FlipCards.showErrorMessage("Server failed to remove");
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}


}
