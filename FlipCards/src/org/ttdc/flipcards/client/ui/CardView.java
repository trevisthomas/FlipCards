package org.ttdc.flipcards.client.ui;

import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class CardView extends Composite {

	private static CardViewUiBinder uiBinder = GWT
			.create(CardViewUiBinder.class);

	interface CardViewUiBinder extends UiBinder<Widget, CardView> {
	}

	@UiField
	Label termLabel;
	@UiField
	Label tagsLabel;
	@UiField
	Label definitionLabel;
	@UiField
	Label numberLabel;
	@UiField
	Grid wraperGrid;
	@UiField
	HTMLPanel mainHtmlPanel;
	
	private final WordPair card;
	
	//Current thought is to pass the tag map in as an arg
	public CardView(WordPair card) {
		this.card = card;
		initWidget(uiBinder.createAndBindUi(this));
		loadCardToUi(card);
		//tagsLabel
	}

	private void loadCardToUi(WordPair card) {
		termLabel.setText(card.getWord());
		definitionLabel.setText(card.getDefinition());
		numberLabel.setText(""+card.getDisplayOrder());
	}

	@UiHandler("wraperGrid")
	void onClick(DoubleClickEvent e){
		CardEdit cardEdit = new CardEdit(this, card);
		mainHtmlPanel.clear();
		mainHtmlPanel.add(cardEdit);
	}
	
	public void restore(WordPair card){
		mainHtmlPanel.clear();
		mainHtmlPanel.add(wraperGrid);
		loadCardToUi(card);
	}
	
	public void destroy(){
		removeFromParent();
	}

}
