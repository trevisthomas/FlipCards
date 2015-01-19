package org.ttdc.flipcards.client.ui;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.Tag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TagNameEditor extends Composite{

	private static TagNameEditorUiBinder uiBinder = GWT
			.create(TagNameEditorUiBinder.class);

	interface TagNameEditorUiBinder extends UiBinder<Widget, TagNameEditor> {
	}
	@UiField
	Button deleteButton;
	@UiField
	Label tagNameLabel;
	
	private Tag tag;
	
	public TagNameEditor(Tag tag) {
		initWidget(uiBinder.createAndBindUi(this));
		this.tag = tag;
		deleteButton.setText("X");
		tagNameLabel.setText(tag.getTagName());
	}

	@UiHandler("deleteButton")
	void onClick(ClickEvent e) {
		FlipCards.studyWordsService.deleteTagName(tag.getTagId(), new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				removeFromParent();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
}
