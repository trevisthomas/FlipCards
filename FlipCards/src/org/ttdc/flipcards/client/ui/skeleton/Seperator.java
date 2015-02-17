package org.ttdc.flipcards.client.ui.skeleton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Seperator extends Composite {

	private static CardEdit2UiBinder uiBinder = GWT
			.create(CardEdit2UiBinder.class);

	interface CardEdit2UiBinder extends UiBinder<Widget, Seperator> {
	}

	public Seperator() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
