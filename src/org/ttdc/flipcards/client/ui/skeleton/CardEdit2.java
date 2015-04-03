package org.ttdc.flipcards.client.ui.skeleton;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CardEdit2 extends Composite {

	private static CardEdit2UiBinder uiBinder = GWT
			.create(CardEdit2UiBinder.class);

	interface CardEdit2UiBinder extends UiBinder<Widget, CardEdit2> {
	}
	
	private WordPair card;
	
	@UiField
	TextBox termTextBox;
	@UiField
	TextBox definitionTextBox;
	
//	@UiField
//	Button updateButton;
//	@UiField
//	Anchor spanishDictButton;
	@UiField
	Button deleteButton;
	@UiField
	Button activateButton;
	@UiField
	Button deactivateButton;
	@UiField
	Button closeButton;
	@UiField
	HTMLPanel tagMePanel;
	
	private CardEditObserver observer;
	
	
	public interface CardEditObserver{
		void onCardUpdated(WordPair result);
		void onCardDeleted();
		void onCardEditClose(WordPair card);
	}

	public CardEdit2(CardEditObserver observer, WordPair card) {
		this.observer = observer;
		this.card = card;
		initWidget(uiBinder.createAndBindUi(this));
		setVisible(false);
		
//		updateButton.setText("Update");
		deleteButton.setText("Delete");
		activateButton.setText("Activate!");
		deactivateButton.setText("Deactivate");
		closeButton.setText("Close");
		
		termTextBox.getElement().setId("termInput");
		definitionTextBox.getElement().setId("definitionInput");
		
		TextBoxKeyDownHandler handler = new TextBoxKeyDownHandler();
		definitionTextBox.addKeyDownHandler(handler);
		termTextBox.addKeyDownHandler(handler);
		
		deleteButton.setVisible(false);
		activateButton.setVisible(false);
		deactivateButton.setVisible(false);
		
		termTextBox.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if(!termTextBox.getText().trim().toLowerCase().equals(CardEdit2.this.card.getWord())){
					performUpdate();
				} 
			}
		});
		
		definitionTextBox.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if(!definitionTextBox.getText().trim().toLowerCase().equals(CardEdit2.this.card.getDefinition())){
					performUpdate();
				} 
			}
		});

	}
	
//	class MyBlurHandler implements BlurHandler {
//		private TextBox textBox;
//		public MyBlurHandler(final TextBox textBox) {
//			this.textBox = textBox;
//		}
//		
//		@Override
//		public void onBlur(BlurEvent event) {
//			if()
//			
//		}
//	}
	

	private void loadWordPairToUi(final WordPair c) {
		termTextBox.getElement().setPropertyString("placeholder", c.getWord());
		definitionTextBox.getElement().setPropertyString("placeholder", c.getDefinition());
		
		termTextBox.setText(c.getWord());
		definitionTextBox.setText(c.getDefinition());
		
		deleteButton.setEnabled(c.isDeleteAllowed());
		deleteButton.setVisible(!c.isActive());
		
		activateButton.setVisible(!c.isActive());
		deactivateButton.setVisible(c.isActive());
		
		//Load tags
		FlipCards.studyWordsService.getAllTagNames(new AsyncCallback<List<Tag>>() {
			@Override
			public void onSuccess(List<Tag> result) {
				tagMePanel.clear();
				for(Tag tag : result){
					tagMePanel.add(new TagEditor2(observer, card, tag));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}

//	@UiHandler("updateButton")
//	void onUpdateClick(ClickEvent e) {
//		performUpdate();
//	}

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
				observer.onCardUpdated(result); 
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
	

	
	
	@UiHandler("activateButton")
	void onActivateClick(ClickEvent e) {
		FlipCards.studyWordsService.setActiveStatus(card.getId(), true, new AsyncCallback<WordPair>() {
			@Override
			public void onSuccess(WordPair result) {
				refreshCard();
				observer.onCardUpdated(result); 
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage("Server failed to activate");
			}
		});
	}
	
	@UiHandler("deactivateButton")
	void onDeactivateClick(ClickEvent e) {
		new ConfirmationPopup(new ConfirmationPopup.Observer() {
			@Override
			public void onPerformAction() {
				FlipCards.studyWordsService.setActiveStatus(card.getId(), false, new AsyncCallback<WordPair>() {
					@Override
					public void onSuccess(WordPair result) {
						refreshCard();
						observer.onCardUpdated(result); 
					}
					
					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage("Server failed to deactivate");
					}
				});
			}
		} , "Deactivation Confirmation","Are you sure you want to deactivate the card \""+card.getWord()+"\"? Quiz statistics will not be recoverable." );
	}
	
	@UiHandler("deleteButton")
	void onDeleteClick(ClickEvent e) {
		//Web service remove
		new ConfirmationPopup(new ConfirmationPopup.Observer() {
			@Override
			public void onPerformAction() {
				FlipCards.studyWordsService.deleteWordPair(card.getId(), new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if(result == true){
							FlipCards.showMessage("Card deleted");
							observer.onCardDeleted();
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
		} , "Delete Confirmation","Are you sure you want to delete the card \""+card.getWord()+"\"? This action can not be undone." );
		
		
	}

	
	public void hide(){
		setVisible(false);
	}
	
	public void show(){
		refreshCard();
		setVisible(true);
	}

	private void refreshCard() {
		//Go get the fresh card
		FlipCards.studyWordsService.getStudyItem(card.getId(), new AsyncCallback<WordPair>() {
			@Override
			public void onSuccess(WordPair result) {
				CardEdit2.this.card = result;
				loadWordPairToUi(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	@UiHandler("lingueeButton")
	void onLingueeClick(ClickEvent e) {
		Window.open(
				"http://www.linguee.com/english-spanish/search?source=spanish&query="
						+ termTextBox.getText(), "_blank", "");
	}

	@UiHandler("spanishDictButton")
	void onSpanishDictClick(ClickEvent e) {
		Window.open(
				"http://www.spanishdict.com/translate/" + termTextBox.getText(),
				"_blank", "");
	}
	
	@UiHandler("closeButton")
	void onCloseClick(ClickEvent e) {
		hide();
	}
//	@UiHandler("closeAnchor")
//	void onCloseClick(ClickEvent e) {
//		observer.onCardEditClose(card); 
//	}
}
