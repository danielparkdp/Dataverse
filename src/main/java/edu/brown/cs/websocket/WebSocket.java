package edu.brown.cs.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.login.InfoWrapper;
import edu.brown.cs.login.Login;

/**
 * WebSocket class to handle the general websocket behavior. Manages general
 * message handling using its MessageManager, but specifics are in individual
 * classes.
 */
@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class WebSocket {

	private static final Gson GSON = new Gson();
	// keep track of sessions connected
	private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
	private static int nextId = 0;
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLUE = "\u001B[34m";
	private static Login login = new Login("data/final/login.sqlite3");

	// this is package visisble since all the handlers also need to see and modify
	// it.
	static Map<Session, InfoWrapper> userMap = new HashMap<>();

	// message manager and the different kinds of message classes.
	private MessageManager man = new MessageManager();
	private GameMessageHandler gmh = new GameMessageHandler(man);
	private ConnectionMessageHandler cmh = new ConnectionMessageHandler(man);
	private CustomizationMessageHandler custommh = new CustomizationMessageHandler(man);

	/**
	 * Handles networking for this program. isn't actually called...
	 */
	public WebSocket() {
	}

	/**
	 * on connected, use the session and add all necessary info to our handlers.
	 * 
	 * @param session
	 *            session connected
	 */
	@OnWebSocketConnect
	public void connected(Session session) {
		// add session and create json to send
		sessions.add(session);
		JsonObject json = new JsonObject();
		json.addProperty("type", MESSAGE_TYPE.CONNECT.ordinal());
		json.addProperty("id", nextId);
		try {
			session.getRemote().sendString(GSON.toJson(json));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println(ANSI_GREEN + "New client connected with id " + nextId + ANSI_RESET);
		nextId++;
	}

	/**
	 * close session, handle given statuses and reasons.
	 * 
	 * @param session
	 *            session close
	 * @param status
	 *            status given
	 * @param reason
	 *            reason given
	 */
	@OnWebSocketClose
	public void close(Session session, int status, String reason) {
		// logs a user out on termination of the web socket session
		sessions.remove(session);
		if (!userMap.containsKey(session) || userMap.get(session) == null) {
			this.gmh.removeSession(session);
			return;
		}
		// get session and handle response
		InfoWrapper info = userMap.get(session);
		info.setStatus(0);
		// remove values
		login.updateUserInfo(info);
		userMap.remove(session);
		System.out.println(ANSI_RED + "Client has disconnected" + ANSI_RESET);
		this.gmh.removeSession(session);
	}

	/**
	 * Handler that gets called when a message is recieved from the frontend.
	 *
	 * @param session
	 *            session sending the message
	 * @param message
	 *            message that is recieved.
	 */
	@OnWebSocketMessage
	public void message(Session session, String message) {
		JsonObject receivedMsg = GSON.fromJson(message, JsonObject.class);
		this.man.processMessage(receivedMsg, session);
	}

}
