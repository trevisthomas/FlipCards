package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewQuiz extends VerticalPanel{
	private final StudyWordsServiceAsync studyWordsService = GWT.create(StudyWordsService.class); //Not sure if i should share this or not?
	
	private final Label errorLabel = new Label();
	
	public ViewQuiz() {
		add(errorLabel);
		errorLabel.setVisible(false);
		add(new Label("Quiz Options"));
		
		final VerticalPanel test = new VerticalPanel();
		
		add(test);
		
		studyWordsService.getAllWordPairs(new AsyncCallback<List<WordPair>>() {
			
			@Override
			public void onSuccess(List<WordPair> result) {
				for(WordPair pair : result){
					test.add(new Label(pair.getWord()));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				errorLabel.setText(caught.getMessage());
				errorLabel.setVisible(true);
			}
		});
	}
}
