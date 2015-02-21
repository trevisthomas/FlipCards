package org.ttdc.flipcards.client.ui.skeleton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TimesUpPopup extends Composite {

	private static TimesUpPopupUiBinder uiBinder = GWT
			.create(TimesUpPopupUiBinder.class);

	interface TimesUpPopupUiBinder extends UiBinder<Widget, TimesUpPopup> {
	}

	public TimesUpPopup() {
		initWidget(uiBinder.createAndBindUi(this));
	}

}
