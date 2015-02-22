package org.ttdc.flipcards.client.ui.skeleton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LoadingMessageWidget extends Composite {

	private static LoadingMessageWidgetUiBinder uiBinder = GWT
			.create(LoadingMessageWidgetUiBinder.class);

	interface LoadingMessageWidgetUiBinder extends
			UiBinder<Widget, LoadingMessageWidget> {
	}

	public LoadingMessageWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
