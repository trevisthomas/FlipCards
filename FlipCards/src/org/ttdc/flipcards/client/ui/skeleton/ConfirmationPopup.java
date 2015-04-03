package org.ttdc.flipcards.client.ui.skeleton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class ConfirmationPopup extends Composite {

	private static ConfirmationPopupUiBinder uiBinder = GWT
			.create(ConfirmationPopupUiBinder.class);

	interface ConfirmationPopupUiBinder extends
			UiBinder<Widget, ConfirmationPopup> {
	}
	
	public interface Observer{
		void onPerformAction();
	}
	
	private Observer observer;
	private PopupPanel popup;

	@UiField
	Label messageLabel;
	
	@UiField
	Label titleLabel;
	
	@UiField
	HTMLPanel okCancelPanel;
	
	@UiField
	HTMLPanel okOnlyPanel;

	public ConfirmationPopup(String title, String message) {
		this(null,title,message);
	}
	public ConfirmationPopup(Observer observer, String title, String message) {
		initWidget(uiBinder.createAndBindUi(this));
		this.observer = observer;
		popup = new PopupPanel(true);
		messageLabel.setText(message);
		titleLabel.setText(title);
		
		popup.add(this);
		popup.setGlassEnabled(true);
		popup.setAnimationEnabled(true);
		
		if(observer == null){
			okOnlyPanel.setVisible(true);
			okCancelPanel.setVisible(false);
		}
		else {
			okOnlyPanel.setVisible(false);
			okCancelPanel.setVisible(true);
		}
		
		popup.center();
		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
//				event.
				// TODO Auto-generated method stub
				
			}
		});
		
		popup.show();
	}

	@UiHandler("okButton")
	void onOkClick(ClickEvent e) {
		observer.onPerformAction();
		popup.hide();
	}
	
	@UiHandler("cancelButton")
	void onCancelClick(ClickEvent e) {
		popup.hide();
	}
	@UiHandler("onOnlyButton")
	void onOkOnlyClick(ClickEvent e) {
		popup.hide();
	}
	
}
