package org.ttdc.flipcards.client.ui.staging;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.ui.CardManager;
import org.ttdc.flipcards.client.ui.CardView;
import org.ttdc.flipcards.client.ui.QuizSelection;
import org.ttdc.flipcards.client.ui.Upload;
import org.ttdc.flipcards.shared.User;
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
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.core.java.util.Collections;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class StagingManager extends Composite{

	private static StagingManagerUiBinder uiBinder = GWT
			.create(StagingManagerUiBinder.class);

	interface StagingManagerUiBinder extends UiBinder<Widget, StagingManager> {
	}

	@UiField
	Button uploadButton;
	@UiField
	Button addButton;
	@UiField
	TextBox termTextBox;
	@UiField
	TextBox definitionTextBox;
	@UiField
	VerticalPanel stagingPanel;
	@UiField
	Anchor viewCardEditorAnchor;
	@UiField
	Anchor quizAnchor;
	@UiField
	VerticalPanel uploadPanel;
	@UiField
	Button refreshButton;
	@UiField
	ListBox friendsListBox;
	@UiField
	Button goButton;
	
	
	private int lastIndex = 1;
	
	public StagingManager() {
		initWidget(uiBinder.createAndBindUi(this));
		uploadButton.setText("Upload");
		addButton.setText("Add");
		refreshButton.setText("refresh");
		friendsListBox.addItem("");
		FlipCards.stagingCardService.getStagingFriends(new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> result) {
				for(String friend : result){
					friendsListBox.addItem(friend);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
		
		
		refresh("");
	}

	
	private void refresh(String owner) {
		stagingPanel.clear();
		stagingPanel.add(new Label("Loading..."));
		FlipCards.stagingCardService.getStagedCards(owner, new AsyncCallback<List<WordPair>>() {
			
			@Override
			public void onSuccess(List<WordPair> result) {
				stagingPanel.clear();
				java.util.Collections.reverse(result);
				for(WordPair pair : result){
					lastIndex++;
					stagingPanel.add(new StagingCardView(pair));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
				
			}
		});
		
	}

	
	@UiHandler("friendsListBox")
	void onFriendFilterChanged(ChangeEvent e) {
		refresh(friendsListBox.getValue(friendsListBox.getSelectedIndex()));
	}
	
	@UiHandler("goButton")
	void onGoClick(ClickEvent e) {
		refresh(friendsListBox.getValue(friendsListBox.getSelectedIndex()));
	}
	@UiHandler("uploadButton")
	void onClick(ClickEvent e) {
		uploadPanel.clear();
		uploadPanel.add(new Upload());
	}
	@UiHandler("addButton")
	void onAddClick(ClickEvent e) {
		stageNewCard();
	}
	
	@UiHandler("viewCardEditorAnchor")
	void onViewCardEditorClick(ClickEvent e) {
		FlipCards.replaceView(new CardManager());
	}
	
	@UiHandler("quizAnchor")
	void onCloseClick(ClickEvent e) {
		FlipCards.replaceView(new QuizSelection());
	}
	
	@UiHandler("definitionTextBox")
	void onDefinitionTextBoxKeyDown(KeyDownEvent e){
		submitOnEnterHelper(e);
	}
	
	@UiHandler("termTextBox")
	void onTermTextBoxKeyDown(KeyDownEvent e){
		submitOnEnterHelper(e);
	}

	@UiHandler("refreshButton")
	void onRefreshButtonClicked(ClickEvent e){
		refresh(friendsListBox.getValue(friendsListBox.getSelectedIndex()));
	}
	
	private void submitOnEnterHelper(KeyDownEvent e) {
		if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			stageNewCard();
		}
	}
	
	
	private void stageNewCard(){
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
		
		FlipCards.stagingCardService.add(word, definition,
				new AsyncCallback<WordPair>() {
					@Override
					public void onSuccess(WordPair card) {
						termTextBox.setText("");
						definitionTextBox.setText("");
						termTextBox.setFocus(true);
						card.setDisplayOrder(lastIndex++); //So lame.
						stagingPanel.insert(new StagingCardView(card), 0);
//						stagingPanel.add();
						FlipCards.showMessage("New card created in staging");
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
	}
}
