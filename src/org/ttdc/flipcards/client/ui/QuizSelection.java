package org.ttdc.flipcards.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.ViewName;
import org.ttdc.flipcards.client.ui.staging.StagingManager;
import org.ttdc.flipcards.shared.CardOrder;
import org.ttdc.flipcards.shared.CardSide;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.Tag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
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
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class QuizSelection extends Composite {

	private static QuizSelectionUiBinder uiBinder = GWT
			.create(QuizSelectionUiBinder.class);

	interface QuizSelectionUiBinder extends UiBinder<Widget, QuizSelection> {}

	@UiField
	Button goButton;
	@UiField
	ListBox cardCountListBox;
	@UiField
	ListBox orderListBox;
	@UiField
	RadioButton cardSideTerm;
	@UiField
	RadioButton cardSideDefinition;
	@UiField
	RadioButton cardSideRandom;
	@UiField
	Anchor editCardsButton;
	@UiField
	FlowPanel tagFilterPanel;
	@UiField
	CheckBox allCheckbox;
	
	Map<String, CheckBox> filterCheckBoxesMap = new HashMap<>();
	

	public QuizSelection() {
		initWidget(uiBinder.createAndBindUi(this));
		
		cardCountListBox.getElement().setId("cardCountInput");
		orderListBox.getElement().setId("orderInput");
		
		
		goButton.setText("Go!");
		editCardsButton.setText("Edit Cards");
		allCheckbox.setValue(true);
		
		cardCountListBox.addItem("10");
		cardCountListBox.addItem("20");
		cardCountListBox.addItem("30");
		cardCountListBox.addItem("40");
		cardCountListBox.addItem("50");
		cardCountListBox.addItem("100");
		cardCountListBox.addItem("All");
		cardCountListBox.setSelectedIndex(1);
		
		for(CardOrder co : CardOrder.values()){
			orderListBox.addItem(co.toString(), co.name());
		}
		
//		orderListBox.addItem(CardOrder.RANDOM.toString(), CardOrder.RANDOM.name());
//		orderListBox.addItem(CardOrder.EASIEST.toString(), CardOrder.EASIEST.name());
//		orderListBox.addItem(CardOrder.HARDEST.toString(), CardOrder.HARDEST.name());
//		orderListBox.addItem(CardOrder.LEAST_STUDIED.toString(), CardOrder.LEAST_STUDIED.name());
//		orderListBox.addItem(CardOrder.LATEST_ADDED.toString(), CardOrder.LATEST_ADDED.name());
		
		cardSideTerm.setText(CardSide.TERM.toString());
		cardSideDefinition.setText(CardSide.DEFINITION.toString());
		cardSideRandom.setText(CardSide.RANDOM.toString());
		
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
				}
				setupFilterCheckboxes();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
				
			}
		});
	}
	
	@UiHandler("editCardsButton")
	void onEditCardsClick(ClickEvent e) {
//		FlipCards.replaceView(new CardManager());
		FlipCards.loadView(ViewName.CARD_MANAGER);
	}
	
	@UiHandler("allCheckbox")
	void onCheckAllClicked(ClickEvent e){
		setupFilterCheckboxes();
	}

	private void setupFilterCheckboxes() {
		for(String tagId : filterCheckBoxesMap.keySet()){
			filterCheckBoxesMap.get(tagId).setEnabled(!allCheckbox.getValue());
		}
	}

	@UiHandler("goButton")
	void onClick(ClickEvent e) {
		QuizOptions options = new QuizOptions();
		
		CardSide side = null;
		side = (cardSideTerm.getValue() ? CardSide.TERM : side);
		side = (cardSideDefinition.getValue() ? CardSide.DEFINITION : side);
		side = (cardSideRandom.getValue() ? CardSide.RANDOM : side);
		
		options.setCardSide(side);
		options.setCardOrder(CardOrder.valueOf(orderListBox.getValue(orderListBox.getSelectedIndex())));
		
		if(cardCountListBox.getSelectedIndex() == cardCountListBox.getItemCount() - 1){
			options.setSize(-1);
		}	
		else{
			options.setSize(Integer.parseInt(cardCountListBox.getValue(cardCountListBox.getSelectedIndex())));
		}
		
		//Apply the filter checks only if all is not selected
		if(!allCheckbox.getValue()){
			for(String tagId : filterCheckBoxesMap.keySet()){
				if(filterCheckBoxesMap.get(tagId).getValue()){
					options.getTagIds().add(tagId);
				}
			}	
		}
		
		FlipCards.replaceView(ViewName.FLIPCARDS, new FlipCard(options));
		
//		Window.alert("Quizit byotch " + side + " ORder:" + options.getCardOrder());
	}
	
	
}
