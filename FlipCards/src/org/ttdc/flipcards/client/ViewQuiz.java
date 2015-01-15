package org.ttdc.flipcards.client;

import java.util.List;

import org.ttdc.flipcards.client.ui.FlipCard;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewQuiz extends VerticalPanel{
	private final StudyWordsServiceAsync studyWordsService = GWT.create(StudyWordsService.class); //Not sure if i should share this or not?
	
	private final Label errorLabel = new Label();
	
	public ViewQuiz(QuizOptions options) {
		add(errorLabel);
		errorLabel.setVisible(false);
		add(new Label("Quiz Options: " + options.getSize()));
		
		final VerticalPanel test = new VerticalPanel();
		
		add(test);
		
		studyWordsService.generateQuiz(options, new AsyncCallback<List<WordPair>>() {
			@Override
			public void onSuccess(List<WordPair> result) {
				for(WordPair pair : result){
					test.add(new Label(pair.getWord()));
//					test.add(new FlipCard(pair));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showError(caught.getMessage());
			}
		});
	}
	
	void showError(String errorMessage){
		errorLabel.setText(errorMessage);
		errorLabel.setVisible(true);
	}
}
