package org.ttdc.flipcards.client.ui.staging;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.ui.CardManager;
import org.ttdc.flipcards.client.ui.CardView;
import org.ttdc.flipcards.client.ui.Upload;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
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
	Anchor closeAnchor;
	@UiField
	VerticalPanel uploadPanel;
	@UiField
	Button refreshButton;
	
	
	private int lastIndex = 1;
	
	public StagingManager() {
		initWidget(uiBinder.createAndBindUi(this));
		uploadButton.setText("Upload");
		addButton.setText("Add");
		refreshButton.setText("refresh");
		refresh();
	}

	
	private void refresh() {
		stagingPanel.clear();
		stagingPanel.add(new Label("Loading..."));
		FlipCards.stagingCardService.getStagedCards(new AsyncCallback<List<WordPair>>() {
			
			@Override
			public void onSuccess(List<WordPair> result) {
				stagingPanel.clear();
				for(WordPair pair : result){
					stagingPanel.add(new StagingCardView(pair));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
				
			}
		});
		
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
	
	@UiHandler("closeAnchor")
	void onCloseClick(ClickEvent e) {
		FlipCards.replaceView(new CardManager());
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
		refresh();
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
						card.setDisplayOrder(lastIndex); //So lame.
						stagingPanel.add(new StagingCardView(card));
						FlipCards.showMessage("New card created in staging");
					}

					@Override
					public void onFailure(Throwable caught) {
						FlipCards.showErrorMessage(caught.getMessage());
					}
				});
	}
}
