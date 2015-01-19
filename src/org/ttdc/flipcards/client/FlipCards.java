package org.ttdc.flipcards.client;

import java.util.ArrayList;
import java.util.List;

import org.ttdc.flipcards.client.services.LoginService;
import org.ttdc.flipcards.client.services.LoginServiceAsync;
import org.ttdc.flipcards.client.ui.CardManager;
import org.ttdc.flipcards.client.ui.QuizSelection;
import org.ttdc.flipcards.shared.FieldVerifier;
import org.ttdc.flipcards.shared.LoginInfo;
import org.ttdc.flipcards.shared.WordPair;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class FlipCards implements EntryPoint {

	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label(
			"Please sign in to your Google Account to access �Enfoca!");
	private Anchor signInLink = new Anchor("Sign In");
	private Anchor signOutLink = new Anchor("Sign Out");

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	public final static StudyWordsServiceAsync studyWordsService = GWT.create(StudyWordsService.class);
	
	public static void showErrorMessage(String message){
		RootPanel.get("systemError").clear();
		RootPanel.get("systemError").add(new Label(message));
	}
	
	public static void showMessage(String message) {
		//set the style?
		RootPanel.get("systemError").clear();
		RootPanel.get("systemError").add(new Label(message));
	}

	
	public static void clearErrorMessage(){
		RootPanel.get("systemError").clear();
	}

	public void onModuleLoad() {
		// Check login status using login service.
		LoginServiceAsync loginService = GWT.create(LoginService.class);
		loginService.login(GWT.getHostPageBaseURL(),
				new AsyncCallback<LoginInfo>() {
					public void onFailure(Throwable error) {
					}

					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						if (loginInfo.isLoggedIn()) {
							signOutLink.setHref(loginInfo.getLogoutUrl());
							RootPanel.get("logout").add(signOutLink);
//							replaceView(new CardManager());
							replaceView(new QuizSelection());
						} else {
							loadLogin();
						}
					}
				});

	}

	
	private void loadLogin() {
		// Assemble login panel.
		signInLink.setHref(loginInfo.getLoginUrl());
		loginPanel.add(loginLabel);
		loginPanel.add(signInLink);
		RootPanel.get("flipcards").clear();
		RootPanel.get("flipcards").add(loginPanel);
	}

//	public static void showAddWordsView() {
//		RootPanel.get("flipcards").clear();
//		RootPanel.get("flipcards").add(new ViewAddWords());
//	}

//	public static void showStudyView() {
//		RootPanel.get("flipcards").clear();
//		RootPanel.get("flipcards").add(new ViewQuizConfigure());
//	}
//	
	public static void replaceView(Widget view){
		RootPanel.get("flipcards").clear();
		RootPanel.get("flipcards").add(view);
	}

	

}