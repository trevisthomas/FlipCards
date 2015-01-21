package org.ttdc.flipcards.client.ui.staging;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.ui.CardView;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class StagingCardView extends Composite {

	private static StagingCardViewUiBinder uiBinder = GWT
			.create(StagingCardViewUiBinder.class);

	interface StagingCardViewUiBinder extends UiBinder<Widget, StagingCardView> {
	}

	@UiField
	HorizontalPanel mainPanel;
	@UiField
	Button deleteButton;
	@UiField
	Button updateButton;	
	@UiField
	Button promoteButon;
	@UiField
	TextBox termTextBox;
	@UiField
	TextBox definitionTextBox;
	@UiField
	Label indexLabel;
	
	private WordPair wordPair;
	
	public StagingCardView(WordPair wordPair) {
		initWidget(uiBinder.createAndBindUi(this));
		this.wordPair = wordPair;
		loadWordPair(wordPair);
	}
	
	void loadWordPair(WordPair wordPair){
		indexLabel.setText(""+wordPair.getDisplayOrder());
//		indexLabel.setText(""+wordPair.getUser());
		termTextBox.setText(wordPair.getWord());
		definitionTextBox.setText(wordPair.getDefinition());
		
	}
	
	@UiHandler("deleteButton")
	void onDeleteButtonClicked(ClickEvent e) {
		performDelete();
	}
	
	@UiHandler("updateButton")
	void onUpdateClicked(ClickEvent e) {
		performUpdate();
	}
	

	@UiHandler("promoteButon")
	void onPromoteClicked(ClickEvent e) {
		performPromote();
	}
	
	@UiHandler("definitionTextBox")
	void onDefinitionTextBoxKeyDown(KeyDownEvent e){
		submitOnEnterHelper(e);
	}
	
	@UiHandler("termTextBox")
	void onTermTextBoxKeyDown(KeyDownEvent e){
		submitOnEnterHelper(e);
	}

	private void submitOnEnterHelper(KeyDownEvent e) {
		if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			performUpdate();
		}
	}
	
	private void performPromote(){
		FlipCards.stagingCardService.promote(wordPair.getId(),
				new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void card) {
						FlipCards.showMessage(wordPair.getWord() + " promoted to active card stack.");
						removeFromParent();
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
	}
	
	private void performDelete(){
		FlipCards.stagingCardService.delete(wordPair.getId(),
				new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void card) {
						FlipCards.showMessage("Staging card delete");
						removeFromParent();
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
	}

	private void performUpdate() {
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
		
		FlipCards.stagingCardService.update(wordPair.getId(), word, definition,
				new AsyncCallback<WordPair>() {
					@Override
					public void onSuccess(WordPair card) {
						loadWordPair(card);
						FlipCards.showMessage("Staging card updated");
					}

					@Override
					public void onFailure(Throwable caught) {
						loadWordPair(wordPair);
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
		
	}

}

