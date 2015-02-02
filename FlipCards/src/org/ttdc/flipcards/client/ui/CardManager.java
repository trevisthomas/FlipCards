package org.ttdc.flipcards.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.client.StudyWordsServiceAsync;
import org.ttdc.flipcards.client.ui.staging.StagingManager;
import org.ttdc.flipcards.shared.CardOrder;
import org.ttdc.flipcards.shared.Constants;
import org.ttdc.flipcards.shared.ItemFilter;
import org.ttdc.flipcards.shared.PagedWordPair;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.util.PreventSpuriousRebuilds;

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
	Anchor quizAnchor;
	@UiField
	Anchor tagEditorAnchor;
	@UiField
	Anchor tagFilterAnchor;
	@UiField
	Button goButton;
	@UiField
	ListBox tagListBox;
	@UiField
	ListBox ownerListBox;
	@UiField
	ListBox filterListBox;
	@UiField
	HorizontalPanel tagFilterPanel;
	@UiField
	Label dataHeaderLabel;
	@UiField
	Button refreshButton;
//	@UiField
//	HTMLPanel paginationPanel;
//	@UiField
//	Anchor pagePrevAnchor;
//	@UiField
//	Anchor pageNextAnchor;
	@UiField
	Button migrateButton;
	
	@UiField
	Button prevButton;
	@UiField
	Button nextButton;
	@UiField
	Anchor lingueeButton;
	@UiField
	Anchor spanishDictButton;
	
	private long lastIndex;
	private String lastSelectedTag = NONE;
	private String lastSelectedOwner = NONE;
	private ItemFilter lastSelectedFilter = ItemFilter.INACTIVE;
	private int currentPage = 1;
//	private int availablePages = -1;
	private int cardsPerPage = 50;
	
	private long totalCardCount = -1;
	private Stack<String> cursorStack = new Stack<>();
	

	public CardManager() {
		initWidget(uiBinder.createAndBindUi(this));

		migrateButton.setVisible(false);
		
		addCardButton.setText("Add Card");
		TextBoxKeyDownHandler keyDownHandler = new TextBoxKeyDownHandler();
		termTextBox.addKeyDownHandler(keyDownHandler);
		definitionTextBox.addKeyDownHandler(keyDownHandler);
		
		goButton.setText("Apply Filter");
		tagFilterPanel.setVisible(false);
		dataHeaderLabel.setText("Loading...");
		refreshButton.setText("Refresh");
		loadWords();
	}

//	private void loadWords() {
//		studyWordsService.getAllWordPairs(new AsyncCallback<List<WordPair>>() {
//			@Override
//			public void onSuccess(List<WordPair> result) {
//				for (WordPair card : result) {
//					lastIndex = card.getDisplayOrder();
//					cardBrowserPanel.add(new CardView(card));
//				}
//				dataHeaderLabel.setText("Loaded " + result.size() + " cards.");
//			}
//
//			@Override
//			public void onFailure(Throwable caught) {
//				dataHeaderLabel.setText("Failed to load.");
//				FlipCards.showErrorMessage(caught.getMessage());
//			}
//		});
//	}
	
	private void loadWords(){
		loadWords("");
	}
	private void loadWords(String cursorString) {
		cursorStack.clear();
		cursorStack.push("");
		dataHeaderLabel.setText("Loading...");
		cardBrowserPanel.clear();
		currentPage = 1;
		refresh(cursorString);
	}

	private void refresh(String cursorString) {
		List<String> tagIds = new ArrayList<>();
		if(!NONE.equals(lastSelectedTag)){
			tagIds.add(lastSelectedTag);
		}
		List<String> owners = new ArrayList<>();
		if(!NONE.equals(lastSelectedOwner)){
			owners.add(lastSelectedOwner);
		}
		
		studyWordsService.getWordPairs(tagIds, owners, lastSelectedFilter, currentPage, cardsPerPage,  new AsyncCallback<PagedWordPair>() {
			@Override
			public void onSuccess(PagedWordPair result) {
				cardBrowserPanel.clear();
				Window.scrollTo (0 ,0);
				if(result.getTotalCardCount() != -1){
					totalCardCount = result.getTotalCardCount();
					dataHeaderLabel.setText("Loaded " + result.getTotalCardCount() + " cards.");
				}
				if(cursorStack.size() == 1){
					prevButton.setEnabled(false);
				}
				else{
					prevButton.setEnabled(true);
				}
				int offset = (currentPage - 1) * cardsPerPage;
				if(offset + cardsPerPage < totalCardCount){
					nextButton.setEnabled(true);
				}
				else{
					nextButton.setEnabled(false);
				}
				cursorStack.push("fat fakeness");
				
				List<WordPair> wordPairs = result.getWordPair();
				int count = 1;
				for (WordPair card : wordPairs) {
					card.setDisplayOrder((cardsPerPage * (currentPage - 1)) + count++);
					cardBrowserPanel.add(new CardView(card));
				}
				
			}

			@Override
			public void onFailure(Throwable caught) {
				dataHeaderLabel.setText("Failed to load.");
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
	
//	protected void setupPaginatedPanel() {
//		paginationPanel.setVisible(availablePages > 1);
//		pagePrevAnchor.setEnabled(currentPage > 1);
//		if(pagePrevAnchor.isEnabled()){
//			pagePrevAnchor.removeStyleName("disabled");
//		}
//		else{
//			pagePrevAnchor.addStyleName("disabled");
//		}
//		pageNextAnchor.setEnabled(currentPage < availablePages);
//		if(pageNextAnchor.isEnabled()){
//			pageNextAnchor.removeStyleName("disabled");
//		}
//		else{
//			pageNextAnchor.addStyleName("disabled");
//		}
//	}

//	private void loadPage(){
//		studyWordsService.getWordPairsForPage(currentPage, new AsyncCallback<List<WordPair>>() {
//			@Override
//			public void onSuccess(List<WordPair> wordPairs) {
//				cardBrowserPanel.clear();
//				setupPaginatedPanel();
//				for (WordPair card : wordPairs) {
//					cardBrowserPanel.add(new CardView(card));
//				}			
//			}
//			
//			@Override
//			public void onFailure(Throwable caught) {
//				dataHeaderLabel.setText("Error loading page");
//				FlipCards.showErrorMessage(caught.getMessage());
//			}
//		});
//	}

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
						cardBrowserPanel.insert(new CardView(card), 0);
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
	
	@UiHandler("migrateButton")
	void onMigrate(ClickEvent e){
		FlipCards.stagingCardService.migrateToStudyItemSchema(new AsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				Window.alert("Done: " + result);
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Failed");
			}
		});
	}
	
	@UiHandler("nextButton")
	void onNextClick(ClickEvent e){
		currentPage++;
		refresh(cursorStack.peek());
	}
	
	@UiHandler("prevButton")
	void onPrevClick(ClickEvent e){
		currentPage--;
		cursorStack.pop();//Next
		cursorStack.pop();//Current
		refresh(cursorStack.peek());
		
	}
	
	@UiHandler("refreshButton")
	void onRefreshClick(ClickEvent e) {
		loadWords("");
	}
	
	@UiHandler("quizAnchor")
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
	
//	@UiHandler("pagePrevAnchor")
//	void onPagePrevClick(ClickEvent e) {
//		currentPage--;
////		loadPage();
//	}
//	
//	@UiHandler("pageNextAnchor")
//	void onPageNextClick(ClickEvent e) {
//		currentPage++;
////		loadPage();
//	}
	
	@UiHandler("lingueeButton")
	void onLingueeClick(ClickEvent e) {
		Window.open("http://www.linguee.com/english-spanish/search?source=spanish&query="+termTextBox.getText(), "_blank", "");
	}
	
	@UiHandler("spanishDictButton")
	void onSpanishDictClick(ClickEvent e) {
		Window.open("http://www.spanishdict.com/translate/"+termTextBox.getText(), "_blank", "");
	}
	
	@UiHandler("tagFilterAnchor")
	void onToggleTagFilterClick(ClickEvent e) {
		tagFilterPanel.setVisible(!tagFilterPanel.isVisible());
		
		if(tagFilterPanel.isVisible()){
			tagListBox.clear();
			studyWordsService.getAllTagNames(new AsyncCallback<List<Tag>>() {
				
				@Override
				public void onSuccess(List<Tag> result) {
					tagListBox.clear();
					tagListBox.addItem("All", NONE);
					for(Tag tag : result){
						tagListBox.addItem(tag.getTagName(), tag.getTagId());
						if(tag.getTagId().equals(lastSelectedTag)){
							tagListBox.setSelectedIndex(tagListBox.getItemCount() - 1);
						}
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					FlipCards.showErrorMessage(caught.getMessage());
				}
			});
			
			studyWordsService.getStudyFriends(new AsyncCallback<List<String>>() {
				
				@Override
				public void onSuccess(List<String> result) {
					ownerListBox.clear();
					ownerListBox.addItem("All", NONE);
					for(String owner : result){
						ownerListBox.addItem(owner, owner);
					}
//					ownerListBox.setSelectedIndex(1);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					FlipCards.showErrorMessage(caught.getMessage());
				}
			});
			filterListBox.clear();
			addItemFilterItem(ItemFilter.ACTIVE);
			addItemFilterItem(ItemFilter.INACTIVE);
			addItemFilterItem(ItemFilter.BOTH);
		}
	}

	private void addItemFilterItem(ItemFilter item) {
		filterListBox.addItem(item.toString(), item.name());
		if(item.equals(lastSelectedFilter)){
			filterListBox.setSelectedIndex(filterListBox.getItemCount() - 1);
		}
	}
	
	@UiHandler("tagListBox")
	void onTagFilterChange(ChangeEvent e){
		lastSelectedTag = tagListBox.getValue(tagListBox.getSelectedIndex());
		//Window.alert("Sel:" +selected);
		dataHeaderLabel.setText("Loading...");
		cardBrowserPanel.clear(); //Show wait icon.
		loadWords();
	}
	
	@UiHandler("ownerListBox")
	void onOwnerFilterChange(ChangeEvent e){
		lastSelectedOwner = ownerListBox.getValue(ownerListBox.getSelectedIndex());
		//Window.alert("Sel:" +selected);
		dataHeaderLabel.setText("Loading...");
		cardBrowserPanel.clear(); //Show wait icon.
		loadWords();
	}
	
	@UiHandler("filterListBox")
	void onFilterChange(ChangeEvent e){
		lastSelectedFilter = ItemFilter.valueOf(filterListBox.getValue(filterListBox.getSelectedIndex()));
		dataHeaderLabel.setText("Loading...");
		cardBrowserPanel.clear(); //Show wait icon.
		loadWords();
	}
	
	
	
	@UiHandler("goButton")
	void onFilterButtonClick(ClickEvent e) {
//		String selected = tagListBox.getValue(tagListBox.getSelectedIndex());
//		Window.alert("Sel:" +selected);
		onTagFilterChange(null);//shrug
	}
}