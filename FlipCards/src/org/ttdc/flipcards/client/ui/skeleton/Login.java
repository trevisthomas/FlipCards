package org.ttdc.flipcards.client.ui.skeleton;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Login extends Composite {

	private static LoginUiBinder uiBinder = GWT.create(LoginUiBinder.class);

	interface LoginUiBinder extends UiBinder<Widget, Login> {
	}

	@UiField
	Anchor signInAnchor;
	@UiField
	Anchor signInAnchor2;
	
	public Login(String signInHref) {
		initWidget(uiBinder.createAndBindUi(this));
		signInAnchor.setHref(signInHref);
		signInAnchor2.setHref(signInHref);
		
	}

}
