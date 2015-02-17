package org.ttdc.flipcards.client.ui.skeleton;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TagEditor2 extends Composite{

	private final WordPair pair;
	private final Tag tag;
	private static TagEditorUiBinder uiBinder = GWT
			.create(TagEditorUiBinder.class);

	interface TagEditorUiBinder extends UiBinder<Widget, TagEditor2> {
	}

	@UiField
	CheckBox checkBox;
	
	private CardEdit2.CardEditObserver observer;

	public TagEditor2(CardEdit2.CardEditObserver observer, WordPair pair, Tag tag) {
		initWidget(uiBinder.createAndBindUi(this));
		this.observer = observer;
		this.pair = pair;
		this.tag = tag;
		checkBox.setText(tag.getTagName());
		if(pair.getTags().contains(tag)){
			checkBox.setValue(true);
		}
		
		//Set the check box to checked if the word pair is tagged to this tagName
	}

	@UiHandler("checkBox")
	void onClick(ClickEvent e) {
		boolean isChecked = checkBox.getValue();	
		
		if(isChecked){
			//Tag
			tag();
		} else {
			//DeTag
			deTag();
		}
		
	}

	private void tag() {
		FlipCards.studyWordsService.applyTag(tag.getTagId(), pair.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				checkBox.setValue(true);
				pair.getTags().add(tag); //These are a hack really. I could just reload the damned thing when the editor is restored.
				observer.onCardUpdated(pair);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
		
	}

	private void deTag() {
		FlipCards.studyWordsService.deTag(tag.getTagId(), pair.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				checkBox.setValue(false);
				pair.getTags().remove(tag);
				observer.onCardUpdated(pair);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlipCards.showErrorMessage(caught.getMessage());
			}
		});
		
	}

}
