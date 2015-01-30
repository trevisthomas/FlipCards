package org.ttdc.flipcards.client.ui.staging;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.ui.CardManager;
import org.ttdc.flipcards.client.ui.CardView;
import org.ttdc.flipcards.client.ui.QuizSelection;
import org.ttdc.flipcards.client.ui.Upload;
import org.ttdc.flipcards.shared.PagedWordPair;
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
import org.ttdc.flipcards.shared.Constants;

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
	@UiField
	Anchor lingueeButton;
	@UiField
	Anchor spanishDictButton;
	@UiField
	Button prevButton;
	@UiField
	Button nextButton;
	@UiField
	Label resultSizeLabel;
	
	private long totalCardCount = -1;
	private Stack<String> cursorStack = new Stack<>();
	private String owner = ""; //Self
	private int pageNum = 1;
	public StagingManager() {
		cursorStack.push("");
		initWidget(uiBinder.createAndBindUi(this));
		prevButton.setText("Prev");
		nextButton.setText("Next");
		uploadButton.setText("Upload");
		addButton.setText("Add");
		refreshButton.setText("refresh");
		friendsListBox.addItem("");
		lingueeButton.setText("Linguee");
		spanishDictButton.setText("Spanish Dict");
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
		
		
		refresh("","");
	}

	
	private void refresh(String owner, String cursorString) {
		stagingPanel.clear();
		stagingPanel.add(new Label("Loading..."));
		fetchCards(owner, cursorString);
		
	}


	private void fetchCards(String owner, String cursorString) {
		FlipCards.stagingCardService.getStagedCards(owner, cursorString, new AsyncCallback<PagedWordPair>() {
			
			@Override
			public void onSuccess(PagedWordPair result) {
				if(result.getTotalCardCount() != -1){
					totalCardCount = result.getTotalCardCount();
				}
				resultSizeLabel.setText(totalCardCount + " cards found.");
				stagingPanel.clear();
				
				if(cursorStack.size() == 1){
					prevButton.setEnabled(false);
				}
				else{
					prevButton.setEnabled(true);
				}
				
				
				int offset = (pageNum - 1) * Constants.PAGE_SIZE;// assumes page size of 50
				
//				if(((pageNum + 1) * PAGE_SIZE) > totalCardCount){
				if(offset + Constants.PAGE_SIZE < totalCardCount){
					nextButton.setEnabled(true);
				}
				else{
					nextButton.setEnabled(false);
				}
				
				
//				List<WordPair> reverseList = new ArrayList<>(result.getWordPair());
				cursorStack.push(result.getCursorString());
//				java.util.Collections.reverse(reverseList);
				
				
				for(WordPair pair : result.getWordPair()){
					pair.setDisplayOrder(offset + pair.getDisplayOrder());
					stagingPanel.add(new StagingCardView(pair));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
				
			}
		});
	}
	
	@UiHandler("nextButton")
	void onNextClick(ClickEvent e){
		pageNum++;
		refresh(owner, cursorStack.peek());
	}
	
	@UiHandler("prevButton")
	void onPrevClick(ClickEvent e){
		pageNum--;
		cursorStack.pop();//Next
		cursorStack.pop();//Current
		refresh(owner, cursorStack.peek());
		
	}
	

	@UiHandler("lingueeButton")
	void onLingueeClick(ClickEvent e) {
		Window.open("http://www.linguee.com/english-spanish/search?source=spanish&query="+termTextBox.getText(), "_blank", "");
	}
	
	@UiHandler("spanishDictButton")
	void onSpanishDictClick(ClickEvent e) {
		Window.open("http://www.spanishdict.com/translate/"+termTextBox.getText(), "_blank", "");
	}
	
	@UiHandler("friendsListBox")
	void onFriendFilterChanged(ChangeEvent e) {
		owner = friendsListBox.getValue(friendsListBox.getSelectedIndex());
		pageNum = 1;
		totalCardCount = 0;
		cursorStack.clear();
		cursorStack.push("");
		refresh(owner, "");
	}
	
	@UiHandler("goButton")
	void onGoClick(ClickEvent e) {
		owner = friendsListBox.getValue(friendsListBox.getSelectedIndex());
		cursorStack.clear();
		refresh(owner, "");
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
		refresh(owner, cursorStack.elementAt(cursorStack.size()-2));
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
		
		FlipCards.stagingCardService.add(word.toLowerCase(), definition.toLowerCase(),
				new AsyncCallback<WordPair>() {
					@Override
					public void onSuccess(WordPair card) {
						termTextBox.setText("");
						definitionTextBox.setText("");
						termTextBox.setFocus(true);
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
