package org.ttdc.flipcards.client.ui.skeleton;



import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.Tag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TagNameEditor2 extends Composite {

	private static TagNameEditor2UiBinder uiBinder = GWT
			.create(TagNameEditor2UiBinder.class);

	interface TagNameEditor2UiBinder extends UiBinder<Widget, TagNameEditor2> {
	}

	
	private Tag tag;

	@UiField
	Button updateButton;
	@UiField
	Button addButton;
	@UiField
	Button deleteButton;
	@UiField
	TextBox tagNameEditor;
	
	public TagNameEditor2(Tag tag) {
		initWidget(uiBinder.createAndBindUi(this));
		this.tag = tag;
		deleteButton.setText("Delete");
		updateButton.setText("Update");
		addButton.setText("Add");
		
		addButton.setStyleName("button-primary");
		
		refresh(tag);
	}

	private void refresh(final Tag tag) {
		if(tag == null){
			updateButton.setVisible(false);
			deleteButton.setVisible(false);
			addButton.setVisible(true);
		} else {
			updateButton.setVisible(true);
			addButton.setVisible(false);
			deleteButton.setVisible(true);
		}
	
		if(tag != null){
			tagNameEditor.setText(tag.getTagName());
		}
		tagNameEditor.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if(TagNameEditor2.this.tag == null){
					return;
				}
				if(tag.getTagName() != tagNameEditor.getText()){	
					updateButton.addStyleName("button-primary");	
				}
				else {
					updateButton.removeStyleName("button-primary");
				}
				
			}
		});
	}

	public TagNameEditor2() {
		this(null);
	}

	@UiHandler("deleteButton")
	void onClick(ClickEvent e) {
		new ConfirmationPopup(new ConfirmationPopup.Observer() {
			@Override
			public void onPerformAction() {
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
		}, "Warning", "Warning, are you sure? There could be words associated with this tag!");
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
	
	@UiHandler("addButton")
	void onAddClick(ClickEvent e) {
		String value = tagNameEditor.getText().trim();
		if(value.isEmpty()){
			FlipCards.showErrorMessage("Tag can't be blank");
			return;
		}
		
		FlipCards.studyWordsService.createTag(value, new AsyncCallback<Tag>() {
			@Override
			public void onSuccess(Tag result) {
				refresh(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
	}
	

}
