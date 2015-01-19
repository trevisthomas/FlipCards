package org.ttdc.flipcards.client.ui;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
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
	private CardView cardView;
		
	interface CardEditUiBinder extends UiBinder<Widget, CardEdit> {
	}

	@UiField
	Button updateButton;
	@UiField
	Button deleteButton;
	@UiField 
	TextBox termTextBox;
	@UiField
	FlowPanel tagMeFlowPanel;
	@UiField 
	TextBox definitionTextBox;
	@UiField
	Anchor closeAnchor;
	

	public CardEdit(CardView cardView, WordPair c) {
		initWidget(uiBinder.createAndBindUi(this));
		this.card = c;
		this.cardView = cardView;
		updateButton.setText("Update");
		deleteButton.setText("Delete");
		closeAnchor.setText("close");
		termTextBox.setText(card.getWord());
		definitionTextBox.setText(card.getDefinition());
		
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
				cardView.restore(result); 
			}
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@UiHandler("closeAnchor")
	void onCloseClick(ClickEvent e) {
		cardView.restore(card); 
	}
	
	@UiHandler("deleteButton")
	void onDeleteClick(ClickEvent e) {
		//Web service remove
		FlipCards.studyWordsService.deleteWordPair(card.getId(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				if(result == true){
					FlipCards.showMessage("Card deleted");
					cardView.destroy();
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
