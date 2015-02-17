package org.ttdc.flipcards.client.ui.skeleton;

import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class CardView2 extends Composite implements CardEdit2.CardEditObserver {

	private static CardView2UiBinder uiBinder = GWT
			.create(CardView2UiBinder.class);
	

	interface CardView2UiBinder extends UiBinder<Widget, CardView2> {
	}
	
	interface CardViewOwner{
		void showEditor(CardView2 viewer);
		void hideEditor(CardView2 viewer);
		void removeCard(CardView2 viewer);
	}
	
	private WordPair card;
	private CardEdit2 editor;
	private CardViewOwner owner;
	private Seperator seperator;
	
	@UiField
	Anchor editAnchor;
	@UiField
	Label termLabel;
	@UiField
	Label definitionLabel;
	@UiField
	Label tagsLabel;
	@UiField
	Label numberLabel;
	@UiField 
	HTMLPanel cardRow;

	public CardView2(WordPair card, CardViewOwner owner) {
		initWidget(uiBinder.createAndBindUi(this));
		
		loadCardToUi(card);
		this.owner = owner;
		editor = new CardEdit2(this, card);
		seperator = new Seperator();
	}
	
	public CardEdit2 getEditor(){
		return editor;
	}
	
	@Override
	public void onCardDeleted() {
		owner.removeCard(this);
		
	}
	
	@Override
	public void onCardEditClose(WordPair card) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onCardUpdated(WordPair result) {
		loadCardToUi(result);
	}
	
	private void loadCardToUi(WordPair card) {
		this.card = card;
		if(card.isActive()){
			cardRow.addStyleName("tt-active");
		} else {
			cardRow.removeStyleName("tt-active");
		}
		termLabel.setText(card.getWord());
		definitionLabel.setText(card.getDefinition());
		if(card.getDisplayOrder() != 0){
			numberLabel.setText(""+card.getDisplayOrder());
		} else {
			numberLabel.getElement().setInnerHTML("&nbsp;");
		}
		
		if(card.getTags().size() > 0){
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			for(Tag tag : card.getTags()){
				builder.append(tag.getTagName());
				boolean last = card.getTags().get(card.getTags().size() - 1).equals(tag);
				if(!last){
					builder.append(", ");
				}
			}
			builder.append(")");
			tagsLabel.setText(builder.toString());
		} else {
			tagsLabel.getElement().setInnerHTML("&nbsp;");
//			tagsLabel.setText("");
		}
	}
	
	@UiHandler("editAnchor")
	void onEditClick(ClickEvent e){ 
//		editor.show();
		
		owner.showEditor(this);
	}

	public Widget getSeperator() {
		return seperator;
	}
}
