package org.ttdc.flipcards.client;

import java.util.ArrayList;
import java.util.List;

import org.ttdc.flipcards.client.services.LoginService;
import org.ttdc.flipcards.client.services.LoginServiceAsync;
import org.ttdc.flipcards.client.services.StagingCardService;
import org.ttdc.flipcards.client.services.StagingCardServiceAsync;
import org.ttdc.flipcards.client.ui.CardManager;
import org.ttdc.flipcards.client.ui.QuizSelection;
import org.ttdc.flipcards.client.ui.skeleton.CardManager2;
import org.ttdc.flipcards.client.ui.skeleton.Login;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.History;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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

	private static LoginInfo loginInfo = null;
	private VerticalPanel loginPanel = new VerticalPanel();
	private Label loginLabel = new Label(
			"Please sign in to your Google Account to access ¡Enfoca!");
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
	public final static StagingCardServiceAsync stagingCardService = GWT.create(StagingCardService.class);
	
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
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			   @Override
			   public void onValueChange(ValueChangeEvent<String> event) {
			      String historyToken = event.getValue();
			      ViewName viewName;
			      if(historyToken.trim().isEmpty()){
			    	  viewName = ViewName.QUIZ_SELECTION;
			      }
			      else{
			    	  viewName = ViewName.valueOf(historyToken);
			      }
			      switch(viewName){
				      case CARD_MANAGER:
				    	  replaceView(viewName, new CardManager2());
				    	 break;
				      case QUIZ_SELECTION:
				    	  replaceView(viewName, new QuizSelection());
				    	  break;
				      case FLIPCARDS:
				    	  replaceView(ViewName.FLIPCARDS, new QuizSelection());
				    	 break;
				      case RESULT:
				    	  replaceView(ViewName.RESULT, new QuizSelection());
				    	  break;
				      case QUIZ:
				    	  replaceView(ViewName.QUIZ, new QuizSelection());
				    	  break;
				      case DEBUG:
				    	  replaceView(ViewName.DEBUG, new CardManager());
				    	  break;
				      default:
				    	  replaceView(ViewName.QUIZ_SELECTION, new QuizSelection());
				    	  break;
			      }
			      
			      
			      /* parse the history token */
//			      try {
//			         if (historyToken.substring(0, 9).equals("pageIndex")) {
//			            String tabIndexToken = historyToken.substring(9, 10);
//			            int tabIndex = Integer.parseInt(tabIndexToken);
//			            /* select the specified tab panel */
//			            tabPanel.selectTab(tabIndex);
//			         } else {
//			            tabPanel.selectTab(0);
//			         }
//			      } catch (IndexOutOfBoundsException e) {
//			         tabPanel.selectTab(0);
//			      }
			   }
			});
		
		loginService.login(GWT.getHostPageBaseURL(),
				new AsyncCallback<LoginInfo>() {
					public void onFailure(Throwable error) {
					}

					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						if (loginInfo.isLoggedIn()) {
							signOutLink.setHref(loginInfo.getLogoutUrl());
//							RootPanel.get("logout").add(signOutLink);
//							replaceView(new CardManager());
							History.fireCurrentHistoryState();
//							replaceView(ViewName.QUIZ_SELECTION, new QuizSelection());
						} else {
							loadLogin();
						}
					}
				});

	}
	
	public static String getSignOutHref(){
		if(loginInfo != null){
			return loginInfo.getLogoutUrl();
		} else {
			return "";
		}
	}	
	
	private void loadLogin() {
		// Assemble login panel.
		
		RootPanel.get("flipcards").clear();
		RootPanel.get("flipcards").add(new Login(loginInfo.getLoginUrl()));
		
//		signInLink.setHref(loginInfo.getLoginUrl());
//		loginPanel.add(loginLabel);
//		loginPanel.add(signInLink);
//		RootPanel.get("flipcards").clear();
//		RootPanel.get("flipcards").add(loginPanel);
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
	public static void replaceView(ViewName viewName, Widget view){
		History.newItem(viewName.name());
		RootPanel.get("systemError").clear();
		RootPanel.get("flipcards").clear();
		RootPanel.get("flipcards").add(view);
	}
	
	//No history
	public static void replaceView(Widget view){
		RootPanel.get("systemError").clear();
		RootPanel.get("flipcards").clear();
		RootPanel.get("flipcards").add(view);
	}

	public static void loadView(ViewName viewName){
		History.newItem(viewName.name(), true);
	}
			
	

}
