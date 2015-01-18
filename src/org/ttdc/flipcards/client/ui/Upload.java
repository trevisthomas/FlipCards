package org.ttdc.flipcards.client.ui;

import org.ttdc.flipcards.client.FlipCards;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.client.StudyWordsServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class Upload extends Composite {

	private static UploadUiBinder uiBinder = GWT.create(UploadUiBinder.class);

	interface UploadUiBinder extends UiBinder<Widget, Upload> {
	}

	private final StudyWordsServiceAsync studyWordsService = GWT.create(StudyWordsService.class); //Not sure if i should share this or not?
	
	@UiField
	FormPanel formPanel;
	@UiField
	Button closeButton;
	@UiField
	Label errorMessageLabel;
	
//	<g:Label ui:field="errorMessageLabel"></g:Label>
//	<g:Button ui:field="closeButton">Close uploader</g:Button>

	public Upload() {
		initWidget(uiBinder.createAndBindUi(this));
		
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		formPanel.setMethod(FormPanel.METHOD_POST);
		
		studyWordsService.getFileUploadUrl(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				formPanel.setAction(result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				errorMessageLabel.setText(caught.getMessage());
			}
		});
		
		
//		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
//		formPanel.setAction(blobstoreService.createUploadUrl("/flipcards/upload"));
	}
	
	@UiHandler("closeButton")
	void handleCloseButton(ClickEvent e) {
		studyWordsService.assignSelfToUserlessWords(new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				errorMessageLabel.setText(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				FlipCards.showAddWordsView();
			}
		});
		
	}

}
