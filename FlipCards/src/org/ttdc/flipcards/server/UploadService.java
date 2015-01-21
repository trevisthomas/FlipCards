package org.ttdc.flipcards.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.WordPair;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UploadService extends HttpServlet {
	private BlobstoreService blobstoreService = BlobstoreServiceFactory
			.getBlobstoreService();

	private static final Logger LOG = Logger
			.getLogger(StudyWordsServiceImpl.class.getName());
	
	/**
	 * This is a really bad hack that i put into place so that the upload service can 
	 * see a real user... wait i had an idea.
	 */
	public static User lastUser;

	
//	@Override
//    public void doGet(HttpServletRequest req, HttpServletResponse resp)
//            throws IOException {
//        UserService userService = UserServiceFactory.getUserService();
//
//        String thisURL = req.getRequestURI();
//
//        resp.setContentType("text/html");
//        if (req.getUserPrincipal() != null) {
//            resp.getWriter().println("<p>Hello, " +
//                                     req.getUserPrincipal().getName() +
//                                     "!  You can <a href=\"" +
//                                     userService.createLogoutURL(thisURL) +
//                                     "\">sign out</a>.</p>");
//        } else {
//            resp.getWriter().println("<p>Please <a href=\"" +
//                                     userService.createLoginURL(thisURL) +
//                                     "\">sign in</a>.</p>");
//        }
//    }

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		long MAX_LINE_COUNT = 1024 * 1024; // Make static private final or
											// whatever. //More than a million
											// lines? Probably something really
											// bad happened.

		Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
		List<BlobKey> blobKeys = blobs.get("myFile");

		// if (blobKeys == null || blobKeys.isEmpty()) {
		// res.sendRedirect("/");
		// } else {
		// res.sendRedirect("/serve?blob-key=" +
		// blobKeys.get(0).getKeyString());
		// }
		//
		// blobstoreService.serve(blobKey, res);

		if (blobKeys == null || blobKeys.isEmpty()) {
			// Fail Log error and freak out
			LOG.severe("File upload failed");
			return;
		}

		LOG.info("File upload received. Parsing");

		try {
			List<WordPair> wordPairs = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new BlobstoreInputStream(blobKeys.get(0))));
			int lineCount = 0;
			String line;
			String definition;
			String term;
			while ((line = reader.readLine()) != null
					&& lineCount++ < MAX_LINE_COUNT) {

//				String definition = line.substring(line.indexOf("\",\"") + 3,
//						line.length() - 1);
//				String term = line.substring(1, line.indexOf("\",\""));
				try{
					if(line.contains("\t")){ //If there is a tab (like google docks tsv) use that
						String[] pairArray = line.split("\t");
						
						definition = ""+pairArray[1];
						term = ""+pairArray[0];
					} else {
						//Presumes "","" format (like that study blue.  If you need it without the quote you gotta make changes.
						definition = line.substring(line.indexOf("\",\"") + 3,
								line.length() - 1);
						term = line.substring(1, line.indexOf("\",\""));
						
					}
					WordPair wp = new WordPair(null, term.trim(), definition.trim());
					wordPairs.add(wp);
				}
				catch (Exception e) {
					LOG.log(Level.WARNING, "badness on line: " + line);
				}
				
				
			}
			LOG.info("File upload parsed. Found: " + wordPairs.size()
					+ " word pairs. Saving to datastore");
			
			
			// If we got here, i guess we processed the file ok.
			for (WordPair pair : wordPairs) {
//				service.addWordPair(pair.getWord(), pair.getDefinition());
				
				writeWordPair(pair.getWord(), pair.getDefinition());
			}
			
			LOG.info("Words saved.");

		} catch (IllegalArgumentException e) {
			LOG.severe("Failed to write word pair. " + e);
		} 
//		catch (NotLoggedInException e) {
//			LOG.severe("Failed to write word pair. " + e);
//		} 
		finally {
			blobstoreService.delete(blobKeys.get(0));
		}

	}

	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
	
	private void writeWordPair(String word, String definition){
		UUID uuid = java.util.UUID.randomUUID();
		PersistenceManager pm = getPersistenceManager();
		
		try{
			StagingCardServiceImpl.checkDoesTermExist(word, lastUser);
		} catch (Exception e) {
			LOG.log(Level.WARNING, word + " already exists.");
			return;
		}
		
		try {
			CardStaging pair = new CardStaging(uuid.toString(), word, definition, lastUser.getEmail());
			pm.makePersistent(pair);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
		} finally {
			pm.close();
		}
	}
	
	
}