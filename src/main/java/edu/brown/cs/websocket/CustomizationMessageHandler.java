package edu.brown.cs.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.login.InfoWrapper;
import edu.brown.cs.shop.ShopHandler;

/**
 * Customization Message Handler class for handling all messages related to
 * customizing the account's items, rockets, etc. and the shop.
 */
public class CustomizationMessageHandler {

	private static final Gson GSON = new Gson();

	public CustomizationMessageHandler(MessageManager wm) {
		installMessages(wm);
	}

	/**
	 * associates each message with a specific enum value pattern that leads to its
	 * execution.
	 *
	 * @param wm
	 *            a websocket manager that will store these associations.
	 */
	public void installMessages(MessageManager wm) {
		wm.registerMessage(MESSAGE_TYPE.ROCKET_CHANGE, new ChangeRocket());
		wm.registerMessage(MESSAGE_TYPE.SHOP, new Shop());
		wm.registerMessage(MESSAGE_TYPE.CANBUY, new BuyConfirm());
	}

	/**
	 * Message for changing the rocket is handled in this private class. Sends back
	 * a message where needed.
	 *
	 */
	private class ChangeRocket implements MessageManager.Message {

		/**
		 * method to receive, handle, and return the message to the sender. Just sends
		 * back the received message in this case.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			try {
				JsonObject rocket_payload = receivedMsg.get("payload").getAsJsonObject();
				String rocket = rocket_payload.get("color").getAsString();
				String prev = rocket_payload.get("prev").getAsString();
				// check how we should update rocket
				if (!rocket.equals(prev)) {
					Map<String, Integer> rockets = new HashMap<>();
					rockets.put(rocket, 2);
					rockets.put(prev, 1);
					// update in infowrapper
					WebSocket.userMap.get(sess).updateRockets(rockets);
				}

				sess.getRemote().sendString(receivedMsg.toString());
			} catch (IOException e) {
				System.err.println("Error");
			}
		}
	}

	/**
	 * Private class to handle message where confirming if user can buy.
	 *
	 */
	private class BuyConfirm implements MessageManager.Message {

		/**
		 * method to receive, handle, and return the message to the sender. takes item
		 * and confirms if user can buy or not.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			JsonObject rocket_payload = receivedMsg.get("payload").getAsJsonObject();
			String item = rocket_payload.get("item").getAsString();
			// get data and make shop handler
			InfoWrapper wrap = WebSocket.userMap.get(sess);
			ShopHandler shop = new ShopHandler(wrap);

			// check if user can buy
			boolean valid = shop.canBuy(item);
			JsonObject toSend = new JsonObject();
			// send message back with result
			toSend.addProperty("type", MESSAGE_TYPE.CANBUY.ordinal());
			toSend.addProperty("valid", valid);
			try {
				sess.getRemote().sendString(toSend.toString());
			} catch (IOException e) {
				System.err.println("Error sending message to socket.");
			}

		}

	}

	/**
	 * Sends a message to a session to forward to a world.
	 *
	 * @param session
	 *            Session to send a message to you.
	 */
	private void forwardMessage(boolean rebuy, Session session, InfoWrapper loginInfo) {
		// used for sending back buy message with updated data for user
		JsonObject toSend = new JsonObject();
		toSend.addProperty("type", MESSAGE_TYPE.BUY.ordinal());
		JsonObject payload = new JsonObject();
		// updated values needed
		payload.addProperty("username", loginInfo.getUser());
		payload.addProperty("coins", loginInfo.getStarbucks());
		payload.addProperty("speed", loginInfo.getSpeed());
		payload.addProperty("rockets", GSON.toJson(loginInfo.rockets()));
		toSend.addProperty("payload", payload.toString());
		try {
			session.getRemote().sendString(toSend.toString());
		} catch (IOException e) {
			System.err.println("Error sending message to socket.");
		}
		// send back if can still buy again as well, to update shop.
		JsonObject send = new JsonObject();
		send.addProperty("type", MESSAGE_TYPE.CANBUY.ordinal());
		send.addProperty("valid", rebuy);
		try {
			session.getRemote().sendString(send.toString());
		} catch (IOException e) {
			System.err.println("Error sending message to socket.");
		}
	}

	/**
	 * The user is actually shopping and purchasing the item. Private class to
	 * handle this.
	 *
	 */
	private class Shop implements MessageManager.Message {

		/**
		 * message handler, takes in item and processes the purchase, sending back
		 * updated info.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			JsonObject rocket_payload = receivedMsg.get("payload").getAsJsonObject();
			String item = rocket_payload.get("item").getAsString();
			// get info and shophandler
			InfoWrapper wrap = WebSocket.userMap.get(sess);
			ShopHandler shop = new ShopHandler(wrap);
			// recheck to see if can buy
			if (shop.canBuy(item)) {
				// Handle to buy item
				wrap = shop.buy(item);

				WebSocket.userMap.put(sess, wrap);
				CustomizationMessageHandler.this.forwardMessage(shop.canBuy(item), sess, wrap);
			} else {
				// Handle if can't buy as well
				try {
					JsonObject error = new JsonObject();
					error.addProperty("type", MESSAGE_TYPE.CANBUY.ordinal());
					error.addProperty("error", "Error: You can't purchase this!");

					sess.getRemote().sendString(error.toString());
				} catch (IOException e) {
					System.err.println("bad");
				}
			}

		}

	}

}
