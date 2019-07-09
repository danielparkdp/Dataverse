package edu.brown.cs.websocket;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.JsonObject;

/**
 * modeled after the commandmanager from past projects. you reigster commands
 * with it and then process them. Helps factor out code from one gigantic switch
 * statement.
 *
 */
public class MessageManager {

	// goes from enum type code to the correct message class
	private Map<MESSAGE_TYPE, Message> trafficDirector;

	/**
	 * constructor for message manager.
	 */
	public MessageManager() {
		this.trafficDirector = new HashMap<>();

	}

	/**
	 * register the message received.
	 * 
	 * @param msgCode
	 *            code of message type
	 * @param msg
	 *            message itself
	 */
	public void registerMessage(MESSAGE_TYPE msgCode, Message msg) {
		this.trafficDirector.put(msgCode, msg);
	}

	/**
	 * process the received message.
	 * 
	 * @param receivedMsg
	 *            received message
	 * @param sess
	 *            session sent
	 */
	public void processMessage(JsonObject receivedMsg, Session sess) {
		MESSAGE_TYPE type = MESSAGE_TYPE.values()[receivedMsg.get("type").getAsInt()];
		if (type != MESSAGE_TYPE.HEARTBEAT) {
			System.out.println("RECEIVED MESSAGE: " + receivedMsg);
		}
		Message msgHandler = this.trafficDirector.get(type);
		if (msgHandler != null) {
			msgHandler.execute(receivedMsg, sess);
		} else {
			System.err.println("ERROR: Type not recognized!");
		}
	}

	/**
	 * a contract all messages have to follow.
	 */
	public interface Message {
		/**
		 * 
		 * @param receivedMsg
		 *            the parsed json from the frontend
		 * @param sess
		 *            the session sending the message.
		 */
		void execute(JsonObject receivedMsg, Session sess);
	}

}
