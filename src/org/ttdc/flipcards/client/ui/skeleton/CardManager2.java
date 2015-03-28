package org.ttdc.flipcards.client.ui.skeleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.ViewName;
import org.ttdc.flipcards.client.ui.CardView;
import org.ttdc.flipcards.client.ui.skeleton.TagManagerPopup.TagManagerPopupUiBinder;
import org.ttdc.flipcards.shared.AutoCompleteWordPairList;
import org.ttdc.flipcards.shared.ItemFilter;
import org.ttdc.flipcards.shared.PagedWordPair;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;












import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CardManager2 extends Composite implements CardView2.CardViewOwner, TagManagerPopup.Observer {

	private static CardManager2UiBinder uiBinder = GWT
			.create(CardManager2UiBinder.class);
	
	
	
	@UiField
	TextBox termTextBox;
	@UiField
	TextBox definitionTextBox;
	@UiField
	Button addCardButton;
	@UiField
	Button spanishDictButton;
	@UiField
	Button clearButton;
	@UiField
	ListBox	filterListBox;
	@UiField
	Label dataDetailLabel;
	@UiField
	HTMLPanel cardBrowserPanel;
	@UiField
	HTMLPanel autoCompletePanel;
	@UiField
	Button prevButton;
	@UiField
	Button nextButton;
	@UiField 
	Anchor logoffAnchor;
	@UiField
	Anchor migrate;
	@UiField
	FlowPanel tagFilterPanel;
	
	
	private static final String NONE = "None";
	
	private long lastIndex;
	private String lastSelectedTag = NONE;
	private String lastSelectedOwner = NONE;
	private ItemFilter lastSelectedFilter = ItemFilter.INACTIVE;
	private int currentPage = 1;
	// private int availablePages = -1;
	private int cardsPerPage = 50;
	private int sequence = 0;

	private long totalCardCount = -1;
	private Stack<String> cursorStack = new Stack<>();
	private Set<CardEdit2> cardEditors = new HashSet<>();
	private Set<CardEdit2> autoCompleteEditors = new HashSet<>();
	
	Map<String, CheckBox> filterCheckBoxesMap = new HashMap<>();

	interface CardManager2UiBinder extends UiBinder<Widget, CardManager2> {
	}

	public CardManager2() {
		initWidget(uiBinder.createAndBindUi(this));
		
		termTextBox.getElement().setId("termInput");
		termTextBox.getElement().setPropertyString("placeholder", "spanish");
		definitionTextBox.getElement().setId("definitionInput");
		definitionTextBox.getElement().setPropertyString("placeholder", "english");
		addCardButton.setText("add card");
		spanishDictButton.setText("lookup");
		clearButton.setText("clear");
		
		TextBoxKeyDownHandler keyDownHandler = new TextBoxKeyDownHandler();
		termTextBox.addKeyDownHandler(keyDownHandler);
		definitionTextBox.addKeyDownHandler(keyDownHandler);
		
		filterListBox.getElement().setId("filterInput");
		
		dataDetailLabel.setText("Loading...");
		
		addItemFilterItem(ItemFilter.BOTH);
		addItemFilterItem(ItemFilter.ACTIVE);
		addItemFilterItem(ItemFilter.INACTIVE);
		logoffAnchor.setHref(FlipCards.getSignOutHref());
		
//		setupFilterCheckboxes(true);
		
		FlipCards.studyWordsService.getStudyFriends(new AsyncCallback<List<String>>() {
			@Override
			public void onSuccess(List<String> result) {
//				filterListBox.clear();
//				ownerListBox.addItem("All", NONE);
//				for (String owner : result) {
//					ownerListBox.addItem(owner, owner);
//				}
//				// ownerListBox.setSelectedIndex(1);
//				
				for (String owner : result) {
					filterListBox.addItem(owner);
				}
				
			}

			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
		
		reloadTags();
		
		loadWords("");
		
//		filterListBox.addItem(item.toString(), item.name());
		
	}
	private void reloadTags() {
		tagFilterPanel.clear();
		tagFilterPanel.add(new Label("Loading..."));
		FlipCards.studyWordsService.getAllTagNames(new AsyncCallback<List<Tag>>() {
			
			@Override
			public void onSuccess(List<Tag> result) {
				tagFilterPanel.clear();
				for(Tag tag : result){
					CheckBox checkBox = new CheckBox();
					checkBox.setText(tag.getTagName());
					tagFilterPanel.add(checkBox);
					filterCheckBoxesMap.put(tag.getTagId(), checkBox);
					checkBox.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							loadWords();
						}
					});
				}
				
				Anchor editTagAnchor = new Anchor("edit");
				tagFilterPanel.add(editTagAnchor);
				editTagAnchor.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						new TagManagerPopup(CardManager2.this);							
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
				
			}
		});
	}
	@Override
	public void onClosePopup() {
		reloadTags();
	}
	
//	@UiHandler("allCheckbox")
//	void onCheckAllClicked(ClickEvent e){
//		setupFilterCheckboxes();
//	}

//	private void setupFilterCheckboxes(boolean enable) {
//		if (enable) {
//			filterListBox.removeStyleName("disabled");
//			filterListBox.setEnabled(true);
//		} else {
//			filterListBox.addStyleName("disabled");
//			filterListBox.setEnabled(false);
//		}
//	}

	
	private void addItemFilterItem(ItemFilter item) {
		filterListBox.addItem(item.toString(), item.name());
		if (item.equals(lastSelectedFilter)) {
			filterListBox.setSelectedIndex(filterListBox.getItemCount() - 1);
		}
	}	
	
	private void loadWords() {
		loadWords("");
	}
	
	private void loadWords(String cursorString) {
		cursorStack.clear();
		cursorStack.push("");
		dataDetailLabel.setText("Loading...");
		cardBrowserPanel.clear();
		cardBrowserPanel.add(new LoadingMessageWidget());
		currentPage = 1;
		refresh(cursorString);
	}

	private void refresh(String cursorString) {
		List<String> tagIds = new ArrayList<>();
//		if (!NONE.equals(lastSelectedTag)) {
//			tagIds.add(lastSelectedTag);
//			filterListBox.setEnabled(false);
//		} else {
//			filterListBox.setEnabled(true);
//		}
		
		//Apply the filter checks only if all is not selected
		for(String tagId : filterCheckBoxesMap.keySet()){
			if(filterCheckBoxesMap.get(tagId).getValue()){
				tagIds.add(tagId);
			}
		}	
		
		
		List<String> owners = new ArrayList<>();
		if (!NONE.equals(lastSelectedOwner)) {
			owners.add(lastSelectedOwner);
		}
		
		

		FlipCards.studyWordsService.getWordPairs(tagIds, owners, lastSelectedFilter,
				currentPage, cardsPerPage, new AsyncCallback<PagedWordPair>() {
					@Override
					public void onSuccess(PagedWordPair result) {
						cardBrowserPanel.clear();
						cardEditors.clear();
						Window.scrollTo(0, 0);
						if (result.getTotalCardCount() != -1) {
							totalCardCount = result.getTotalCardCount();
							dataDetailLabel.setText("Loaded "
									+ result.getTotalCardCount() + " cards.");
						}
						if (cursorStack.size() == 1) {
							prevButton.setEnabled(false);
						} else {
							prevButton.setEnabled(true);
						}
						int offset = (currentPage - 1) * cardsPerPage;
						if (offset + cardsPerPage < totalCardCount) {
							nextButton.setEnabled(true);
						} else {
							nextButton.setEnabled(false);
						}
						cursorStack.push("fat fakeness");

						List<WordPair> wordPairs = result.getWordPair();
						int count = 1;
						for (WordPair card : wordPairs) {
							card.setDisplayOrder((cardsPerPage * (currentPage - 1))
									+ count++);
							CardView2 viewer = new CardView2(card, CardManager2.this);
							cardBrowserPanel.add(viewer);
							cardBrowserPanel.add(viewer.getSeperator());
							cardBrowserPanel.add(viewer.getEditor());
							cardEditors.add(viewer.getEditor());
							
						}

					}

					@Override
					public void onFailure(Throwable caught) {
						dataDetailLabel.setText("Failed to load.");
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
	}
	
	@UiHandler("filterListBox")
	void onFilterChange(ChangeEvent e) {
		String selected = filterListBox.getValue(filterListBox.getSelectedIndex());
		lastSelectedOwner = NONE;
		boolean isItem = false;
		for(ItemFilter itemFilter : Arrays.asList(ItemFilter.values())){
			if(itemFilter.name().equals(selected)){
				isItem = true;
			}
		}
		
		if(isItem){
			lastSelectedFilter = ItemFilter.valueOf(selected);
		} else {
			lastSelectedFilter = ItemFilter.INACTIVE;
			lastSelectedOwner = selected;
		}
		
		dataDetailLabel.setText("Loading...");
		cardBrowserPanel.clear(); // Show wait icon.
		loadWords();
	}
	
	@UiHandler("termTextBox")
	void onTermKeyDown(KeyUpEvent e) {
		if (e.getNativeKeyCode() != KeyCodes.KEY_ENTER) {
			String text = termTextBox.getText().trim().toLowerCase();
			if (!text.isEmpty()) {
				performAutoComplete(text);
			} else {
				sequence = -1; //If any are inflight, screw them.
				clearAutoComplete();
			}
		}
	}
	@UiHandler("termTextBox")
	void onTermBlurHandler(BlurEvent e){
		if(termTextBox.getText().trim().isEmpty()){
			sequence = -1; //If any are inflight, screw them.
			clearAutoComplete();
		}
	}

//	private int pageNumber = 1;
//	private int pageNumberStudyCards = 1;
	@UiHandler("migrate")
	void onMigrate(ClickEvent event){
		FlipCards.showErrorMessage("Starting migration");
		performMigrate(0, 1);
		performMigrate(1, 1);
		performMigrate(2, 1);
		performMigrate(3, 1);
	}


	private void performMigrate(final int table, final int pageNumber) {
		FlipCards.studyWordsService.migrate(table, pageNumber, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(Boolean result) {
				if(result){
					FlipCards.addErrorMessage("Done table: " + table);
				} else{
					FlipCards.addErrorMessage("Finished table: "+table+" page: " + pageNumber);
//					pageNumber++;
					performMigrate(table, pageNumber+1);
				}
			}
		});
	}

	private void clearAutoComplete() {
		autoCompletePanel.clear();
		autoCompletePanel.setVisible(false);
	}

	private void performAutoComplete(String qstr) {
		List<String> owners = new ArrayList<>();
//		if (!NONE.equals(lastSelectedOwner)) {
//			owners.add(lastSelectedOwner);
//		}
		
		sequence++;
		FlipCards.studyWordsService.getAutoCompleteWordPairs(owners, sequence, qstr,
				new AsyncCallback<AutoCompleteWordPairList>() {
					@Override
					public void onSuccess(AutoCompleteWordPairList result) {
						if(sequence != result.getSequence()){
							return; //This result is not for the most recient call.
						}
						autoCompletePanel.clear();
						if (result.getWordPairs().size() == 0) {
							autoCompletePanel.setVisible(false);
						} else {
							autoCompletePanel.setVisible(true);
							autoCompleteEditors.clear();

							for (WordPair pair : result.getWordPairs()) {
//								autoCompletePanel.add(new CardView(pair));
								CardView2 viewer = new CardView2(pair, CardManager2.this);
								autoCompletePanel.add(viewer);
								autoCompletePanel.add(viewer.getSeperator());
								autoCompletePanel.add(viewer.getEditor());
								
								autoCompleteEditors.add(viewer.getEditor());
								
								viewer.refresh();//This will load the detailed info.
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
						autoCompletePanel.setVisible(false);
					}
				});

	}
	
	@UiHandler("nextButton")
	void onNextClick(ClickEvent e) {
		currentPage++;
		refresh(cursorStack.peek());
	}

	@UiHandler("prevButton")
	void onPrevClick(ClickEvent e) {
		currentPage--;
		cursorStack.pop();// Next
		cursorStack.pop();// Current
		refresh(cursorStack.peek());

	}
	
	@UiHandler("clearButton")
	void onClearClick(ClickEvent e) {
		termTextBox.setText("");
		definitionTextBox.setText("");
		clearAutoComplete();
		termTextBox.setFocus(true);
	}
	
	@UiHandler("addCardButton")
	void onClick(ClickEvent e) {
		saveNewCard();
	}
	
	@UiHandler("spanishDictButton")
	void onSpanishDictClick(ClickEvent e) {
		Window.open(
				"http://www.spanishdict.com/translate/" + termTextBox.getText(),
				"_blank", "");
	}
	
	@Override
	public void removeCard(CardView2 viewer) {
		cardEditors.remove(viewer.getEditor());
		viewer.getEditor().removeFromParent();
		viewer.removeFromParent();
		viewer.getSeperator().removeFromParent();
	}
	
	@Override
	public void hideEditor(CardView2 viewer) {
		viewer.getEditor().hide();
	}
	
	@Override
	public void showEditor(CardView2 viewer) {
		for(CardEdit2 editor : cardEditors){
			editor.hide();
		}
		
		for(CardEdit2 editor : autoCompleteEditors){
			editor.hide();
		}
		
		viewer.getEditor().show();
	}
	
	private void saveNewCard() {
		String word = termTextBox.getText().trim().toLowerCase();
		String definition = definitionTextBox.getText().trim().toLowerCase();

		if (word.length() == 0) {
			FlipCards.showErrorMessage("Word can't be blank");
			return;
		}

		if (definition.length() == 0) {
			FlipCards.showErrorMessage("Definition can't be blank");
			return;
		}

		FlipCards.studyWordsService.addWordPair(word, definition,
				new AsyncCallback<WordPair>() {
					@Override
					public void onSuccess(WordPair card) {
						termTextBox.setText("");
						definitionTextBox.setText("");
						termTextBox.setFocus(true);
//						card.setDisplayOrder(lastIndex); // So lame.
						
						//This madness is because there is no insert for HTMLPanel like there was for VerticalPanel (but it's a table which doesnt work with the new css)
						List<Widget> widgets = evacuate(cardBrowserPanel);
						
						CardView2 viewer = new CardView2(card, CardManager2.this);
						cardBrowserPanel.add(viewer);
						cardBrowserPanel.add(new Seperator());
						cardBrowserPanel.add(viewer.getEditor());
						cardEditors.add(viewer.getEditor());
						
						
						for(Widget w : widgets){
							cardBrowserPanel.add(w);
						}
						
						
						FlipCards.showMessage("New card created");
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
	}
	
	private List<Widget> evacuate(HTMLPanel dest){
		int ct = dest.getWidgetCount();
		List<Widget> widgets = new ArrayList<>();
		for(int i = 0 ; i < ct ; i ++){
			widgets.add(dest.getWidget(i));
		}
		dest.clear();
		
		return widgets;
//		CardView2 viewer = new CardView2(card, CardManager2.this);
//		cardBrowserPanel.add(viewer);
//		cardBrowserPanel.add(new Seperator());
//		cardBrowserPanel.add(viewer.getEditor());
//		cardEditors.add(viewer.getEditor());
//		dest.add(child)
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
	
	@UiHandler("quizAnchor")
	void onCloseClick(ClickEvent e) {
			FlipCards.loadView(ViewName.QUIZ_SELECTION);
	}

}
