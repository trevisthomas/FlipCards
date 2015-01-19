package org.ttdc.flipcards.client.ui;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.Tag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TagNameEditor extends Composite{

	private static TagNameEditorUiBinder uiBinder = GWT
			.create(TagNameEditorUiBinder.class);

	interface TagNameEditorUiBinder extends UiBinder<Widget, TagNameEditor> {
	}
	@UiField
	Button deleteButton;
	@UiField
	Button updateButton;
	@UiField
	TextBox tagNameEditor;
	
	
	private Tag tag;
	
	public TagNameEditor(Tag tag) {
		initWidget(uiBinder.createAndBindUi(this));
		this.tag = tag;
		deleteButton.setText("Delete");
		updateButton.setText("Update");
		tagNameEditor.setText(tag.getTagName());
	}

	@UiHandler("deleteButton")
	void onClick(ClickEvent e) {
//		Dialog.confirm(String,DialogCallback);
		boolean sure = Window.confirm("Warning, are you sure. There could be words associated with this tag?");
		
		if(!sure){
			return;
		}
		
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
	
	@UiHandler("updateButton")
	void onUpdateClick(ClickEvent e) {
		String value = tagNameEditor.getText().trim();
		if(value.isEmpty()){
			FlipCards.showErrorMessage("Tag can't be blank");
			return;
		}
		
		FlipCards.studyWordsService.updateTagName(tag.getTagId(), value, new AsyncCallback<Tag>() {
			@Override
			public void onSuccess(Tag result) {
				tag.setTagName(result.getTagName());
				tagNameEditor.setText(result.getTagName());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
}
