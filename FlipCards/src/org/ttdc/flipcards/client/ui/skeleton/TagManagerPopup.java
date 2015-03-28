package org.ttdc.flipcards.client.ui.skeleton;

import java.util.List;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.ui.TagNameEditor;
import org.ttdc.flipcards.shared.Tag;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.java.swing.plaf.windows.resources.windows;

public class TagManagerPopup extends Composite {

	private static TagManagerPopupUiBinder uiBinder = GWT
			.create(TagManagerPopupUiBinder.class);

	private final Observer observer;
	private PopupPanel popup;
	
	@UiField
	HTMLPanel tagNamesPanel;
	@UiField
	Anchor createTagAnchor;
	@UiField
	Button okButton;
	@UiField
	Anchor addNewTag;
	
	interface TagManagerPopupUiBinder extends UiBinder<Widget, TagManagerPopup> {
	}
	
	public interface Observer{
		void onClosePopup();
	}

	public TagManagerPopup(Observer observer) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.observer = observer;
		popup = new PopupPanel(true);
		
		popup.add(this);
		popup.setGlassEnabled(true);
		popup.setAnimationEnabled(true);
		
		popup.center();
		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				TagManagerPopup.this.observer.onClosePopup();
			}
		});
		
		FlipCards.studyWordsService.getAllTagNames(new AsyncCallback<List<Tag>>() {
			@Override
			public void onSuccess(List<Tag> result) {
				for(Tag tag : result){
					tagNamesPanel.add(new TagNameEditor2(tag));
				}
				popup.center();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
				
			}
		});
		
		popup.show();
		
	}
	
	@UiHandler("addNewTag")
	void onClickAddNew(ClickEvent e) {
		tagNamesPanel.add(new TagNameEditor2());
	}

	@UiHandler("okButton")
	void onClick(ClickEvent e) {
		popup.hide();
	}
	
}
