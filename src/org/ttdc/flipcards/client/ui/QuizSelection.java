package org.ttdc.flipcards.client.ui;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.CardOrder;
import org.ttdc.flipcards.shared.CardSide;
import org.ttdc.flipcards.shared.QuizOptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
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
	Button editCardsButton;

	public QuizSelection() {
		initWidget(uiBinder.createAndBindUi(this));
		goButton.setText("Go!");
		editCardsButton.setText("Edit Cards");
		
		cardCountListBox.addItem("10");
		cardCountListBox.addItem("20");
		cardCountListBox.addItem("30");
		cardCountListBox.addItem("40");
		cardCountListBox.addItem("50");
		cardCountListBox.addItem("100");
		cardCountListBox.addItem("All");
		cardCountListBox.setSelectedIndex(1);
		
		orderListBox.addItem(CardOrder.RANDOM.toString(), CardOrder.RANDOM.name());
		orderListBox.addItem(CardOrder.EASIEST.toString(), CardOrder.EASIEST.name());
		orderListBox.addItem(CardOrder.HARDEST.toString(), CardOrder.HARDEST.name());
		orderListBox.addItem(CardOrder.LEAST_STUDIED.toString(), CardOrder.LEAST_STUDIED.name());
		orderListBox.addItem(CardOrder.LATEST_ADDED.toString(), CardOrder.LATEST_ADDED.name());
		
		cardSideTerm.setText(CardSide.TERM.toString());
		cardSideDefinition.setText(CardSide.DEFINITION.toString());
		cardSideRandom.setText(CardSide.RANDOM.toString());
		
	}
	
	@UiHandler("editCardsButton")
	void onEditCardsClick(ClickEvent e) {
		FlipCards.replaceView(new CardManager());
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
		
		FlipCards.replaceView(new FlipCard(options));
		
//		Window.alert("Quizit byotch " + side + " ORder:" + options.getCardOrder());
	}
	
	
}
