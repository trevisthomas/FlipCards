package org.ttdc.flipcards.client.ui;

import java.util.List;

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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TagManager extends Composite{

	private static TagEditorUiBinder uiBinder = GWT
			.create(TagEditorUiBinder.class);

	interface TagEditorUiBinder extends UiBinder<Widget, TagManager> {
	}

	@UiField
	Button createTagButton;
	@UiField
	Button closeEditorButton;
	@UiField
	TextBox tagNameTextBox;
	@UiField
	VerticalPanel tagNamesPanel;
	

	final static DialogBox dialogBox = new DialogBox();
	public TagManager() {
		initWidget(uiBinder.createAndBindUi(this));
		createTagButton.setText("Create Tag");
		closeEditorButton.setText("Close");
		
		FlipCards.studyWordsService.getAllTagNames(new AsyncCallback<List<Tag>>() {
			
			@Override
			public void onSuccess(List<Tag> result) {
				for(Tag tag : result){
					tagNamesPanel.add(new TagNameEditor(tag));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
				
			}
		});
	}

	@UiHandler("createTagButton")
	void onClick(ClickEvent e) {
		String name = tagNameTextBox.getText();
		if(name.length() == 0){
			FlipCards.showErrorMessage("Empty tags are the devil.");
			return;
		}
		FlipCards.studyWordsService.createTag(name, new AsyncCallback<Tag>() {
			@Override
			public void onSuccess(Tag tag) {
				FlipCards.showMessage("Tag named \""+tag.getTagName()+"\" created");
				tagNameTextBox.setText("");
				tagNamesPanel.add(new TagNameEditor(tag));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
	@UiHandler("closeEditorButton")
	void onCloseClick(ClickEvent e) {
		dialogBox.hide();
		dialogBox.clear();
	}
	

	public static void show() {
		dialogBox.add(new TagManager());
		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);
		dialogBox.center();
	}

}
