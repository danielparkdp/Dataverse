package edu.brown.cs.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.csvparser.CSVParser;
import edu.brown.cs.login.InfoWrapper;
import edu.brown.cs.login.Login;

/**
 * Connection Message Handler for handling all messages related to connecting,
 * logging in, logging out, etc.
 *
 */
public class ConnectionMessageHandler {

	private static final Gson GSON = new Gson();
	private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
	private static int nextId = 0;
	private static final int MINUSERLENGTH = 1;
	private static final int MAXUSERLENGTH = 36;
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLUE = "\u001B[34m";
	private static final int INITIALSPEED = 5;
	private static final int INITIALGUESTELO = 1199;
	private static final int INITIALMONEY = 200;
	private static Login login = new Login("data/final/login.sqlite3");
	private static CSVParser badwords = new CSVParser("data/words/swearWords.csv");
	private static CSVParser guestnames = new CSVParser("data/words/guestNames.csv");
	private static final String GUESTPRIORWORD = "Anonymous ";
	private static Set<String> currGuests = new HashSet<String>();

	public ConnectionMessageHandler(MessageManager wm) {
		this.installMessages(wm);
	}

	/**
	 * associates each message with a specific enum value pattern that leads to its
	 * execution.
	 *
	 * @param wm
	 *            a websocket manager that will store these associations.
	 */
	public void installMessages(MessageManager wm) {
		wm.registerMessage(MESSAGE_TYPE.LOGIN, new LoginAsUser());
		wm.registerMessage(MESSAGE_TYPE.HEARTBEAT, new HeartBeat());
		wm.registerMessage(MESSAGE_TYPE.SIGNUP, new Signup());
		wm.registerMessage(MESSAGE_TYPE.GUEST, new JoinAsGuest());
		wm.registerMessage(MESSAGE_TYPE.LOGOUT, new Logout());
		wm.registerMessage(MESSAGE_TYPE.CHANGEUSER, new ChangeUser());
		wm.registerMessage(MESSAGE_TYPE.CHANGEPASS, new ChangePass());
	}

	/**
	 * Sends a message to a session to forward to a world.
	 *
	 * @param session
	 *            Session to send a message to you.
	 */
	private void forwardMessage(Session session, InfoWrapper loginInfo, boolean showTutorial) {
		JsonObject toSend = new JsonObject();
		// send forward message to update based on data of user
		toSend.addProperty("type", MESSAGE_TYPE.FORWARD.ordinal());
		JsonObject payload = new JsonObject();
		payload.addProperty("username", loginInfo.getUser());
		payload.addProperty("coins", loginInfo.getStarbucks());
		payload.addProperty("speed", loginInfo.getSpeed());
		payload.addProperty("showTutorial", showTutorial);
		payload.addProperty("rockets", GSON.toJson(loginInfo.rockets()));
		toSend.addProperty("payload", payload.toString());
		try {
			session.getRemote().sendString(toSend.toString());
		} catch (IOException e) {
			System.err.println("Error sending message to socket.");
		}
	}

	/**
	 * Heartbeat to keep socket alive.
	 *
	 */
	private class HeartBeat implements MessageManager.Message {
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// not supposed to do anything
			return;
		}
	}

	/**
	 * Change username message handling private class.
	 *
	 */
	private class ChangeUser implements MessageManager.Message {
		/**
		 * execute logic for changing a username.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			JsonObject signup_payload = receivedMsg.get("payload").getAsJsonObject();
			String olduser = signup_payload.get("olduser").getAsString();
			String pass = signup_payload.get("password").getAsString();
			String newuser = signup_payload.get("username").getAsString();
			InfoWrapper info = WebSocket.userMap.get(sess);
			boolean change = false;

			// make sure they gave valid inputs first.
			if (!info.getUser().equals(olduser) || !info.getPass().equals(pass)) {
				try {
					JsonObject error = new JsonObject();
					error.addProperty("type", MESSAGE_TYPE.CHANGEUSER.ordinal());
					error.addProperty("error", "Error: Wrong username/password!");

					sess.getRemote().sendString(error.toString());
				} catch (IOException e) {
					System.err.println("Error changing user/pass");
				}
				return;
			}

			// change username. Ensure everything is clean since username is the key
			if (validUserCheck(newuser) && validUserCheck(info.getUser())) {
				change = login.changeUser(info, newuser);

			}
			// if changed
			if (change) {
				info.setUser(newuser);
				WebSocket.userMap.put(sess, info);
				ConnectionMessageHandler.this.forwardMessage(sess, info, false);
			} else {
				try {
					JsonObject error = new JsonObject();
					error.addProperty("type", MESSAGE_TYPE.CHANGEUSER.ordinal());
					error.addProperty("error", "Error: You can't use that username!");

					sess.getRemote().sendString(error.toString());
				} catch (IOException e) {
					System.err.println("Error changing user/pass");
				}
			}
		}

	}

	/**
	 * Change password message handling private class.
	 *
	 */
	private class ChangePass implements MessageManager.Message {
		/**
		 * execute for changing the password of someone.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// get data.
			JsonObject signup_payload = receivedMsg.get("payload").getAsJsonObject();
			String oldpass = signup_payload.get("oldpass").getAsString();
			String newpass = signup_payload.get("password").getAsString();
			InfoWrapper info = WebSocket.userMap.get(sess);
			String user = info.getUser();
			boolean change = false;

			// ensure old pass given is right.
			if (!info.getPass().equals(oldpass)) {
				try {
					JsonObject error = new JsonObject();
					// error
					error.addProperty("type", MESSAGE_TYPE.CHANGEPASS.ordinal());
					error.addProperty("error", "Error: Wrong password!");

					sess.getRemote().sendString(error.toString());
				} catch (IOException e) {
					System.err.println("Error changing pass");
				}
				return;
			}
			// if guest, can't change pass
			if (user.length() >= GUESTPRIORWORD.length()
					&& user.substring(0, GUESTPRIORWORD.length()).equals(GUESTPRIORWORD)) {
				try {
					JsonObject error = new JsonObject();
					// error
					error.addProperty("type", MESSAGE_TYPE.CHANGEPASS.ordinal());
					error.addProperty("error", "Error: Guest!");

					sess.getRemote().sendString(error.toString());
				} catch (IOException e) {
					System.err.println("Error: Guest");
				}
				return;
			}
			// change pass
			change = login.changePass(info, newpass);
			// update info
			if (change) {
				info.setPass(newpass);
				WebSocket.userMap.put(sess, info);
			}

		}

	}

	/**
	 * Message handling for sign up – private class.
	 *
	 */
	private class Signup implements MessageManager.Message {
		/**
		 * execute for signing up logic, ensuring all inputs are valid.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// get info from message
			JsonObject signup_payload = receivedMsg.get("payload").getAsJsonObject();
			String signup_username = signup_payload.get("username").getAsString();
			String signup_password = signup_payload.get("password").getAsString();
			boolean notTaken = false;
			// make sure the username given is valid
			if (validUserCheck(signup_username)) {
				// add user
				notTaken = login.addUser(signup_username, signup_password);
			}
			// if username not taken
			if (notTaken) {
				// add info by logging in
				InfoWrapper info = login.validateLogin(signup_username, signup_password);
				WebSocket.userMap.put(sess, info);
				ConnectionMessageHandler.this.forwardMessage(sess, info, true);
			} else {
				// taken user
				try {
					JsonObject error = new JsonObject();
					error.addProperty("type", MESSAGE_TYPE.FORWARD.ordinal());
					error.addProperty("error", "Error: You can't use that username!");

					sess.getRemote().sendString(error.toString());
				} catch (IOException e) {
					System.err.println("Error:");
				}
			}
		}

	}

	/**
	 * Login User message handling – private class.
	 *
	 */
	private class LoginAsUser implements MessageManager.Message {
		/**
		 * execute login logic.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			JsonObject payload = receivedMsg.get("payload").getAsJsonObject();
			String username = payload.get("username").getAsString();
			String password = payload.get("password").getAsString();
			// get info based on validating login.
			InfoWrapper info = login.validateLogin(username, password);
			// status will be -1 if failed
			if (info.getStatus() != -1) {
				// since wasn't -1, it was valid.
				WebSocket.userMap.put(sess, info);
				ConnectionMessageHandler.this.forwardMessage(sess, info, false);
			} else {
				try {

					JsonObject error = new JsonObject();
					error.addProperty("type", MESSAGE_TYPE.FORWARD.ordinal());

					error.addProperty("error", "Error: invalid username or password");

					sess.getRemote().sendString(error.toString());
				} catch (IOException e) {
					System.err.println("error" + e.getMessage());
				}
			}
		}
	}

	/**
	 * Joining as a guest message refers to this private class.
	 *
	 */
	private class JoinAsGuest implements MessageManager.Message {
		/**
		 * execute to add a guest user.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// create the random guest name!
			String guest = this.generateGuestName();
			// give guest base data.
			InfoWrapper guestInfo = new InfoWrapper(guest, "", 1, INITIALMONEY, 2, 0, 0, 0, 0, 0, INITIALSPEED,
					INITIALGUESTELO, 0, 0, 0, 0, 0);
			WebSocket.userMap.put(sess, guestInfo);
			ConnectionMessageHandler.this.forwardMessage(sess, guestInfo, true);
		}

		/**
		 * generate a new guest name!
		 * 
		 * @return new name
		 */
		private String generateGuestName() {
			// build string name starting with guest name prior word
			StringBuilder name = new StringBuilder();
			name.append(GUESTPRIORWORD);
			// pick random name in the csv parsed data structure
			int index1 = (int) (Math.random() * guestnames.getWords().size());
			int index2 = (int) (Math.random() * guestnames.getWords().get(index1).length);
			// make guest name
			name.append(guestnames.getWords().get(index1)[index2]);

			StringBuilder check = new StringBuilder();
			check.append(name.toString());
			// make sure you have different ones by appending numbers at end if needed
			int count = 1;
			while (currGuests.contains(check.toString())) {
				check = new StringBuilder();
				check.append(name.toString());
				check.append(count);
				count++;
			}
			// add and return
			currGuests.add(check.toString());

			return check.toString();
		}

	}

	/**
	 * Logout message handling for when the user logs out.
	 *
	 */
	private class Logout implements MessageManager.Message {
		/**
		 * execute for log out.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			InfoWrapper userInfo = WebSocket.userMap.get(sess);
			// if guest, don't logout of database
			if (userInfo.getUser().length() <= GUESTPRIORWORD.length()
					|| !userInfo.getUser().substring(0, GUESTPRIORWORD.length()).equals(GUESTPRIORWORD)) {
				// update user info with new stats that were cached
				login.updateUserInfo(userInfo);
				// logout status
				login.logout(userInfo.getUser());
			} else {
				// remove guest
				currGuests.remove(userInfo.getUser());
			}
			// update maps and confirm
			WebSocket.userMap.remove(sess);
			JsonObject logout = new JsonObject();
			logout.addProperty("type", MESSAGE_TYPE.LOGOUT.ordinal());
			try {
				sess.getRemote().sendString(logout.toString());

			} catch (IOException e) {
				System.err.println("Error sending session");
			}
		}
	}

	/**
	 * check to make sure username is valid.
	 * 
	 * @param user
	 *            given user
	 * @return valid or not
	 */
	private boolean validUserCheck(String user) {
		// length
		if (user.length() < MINUSERLENGTH) {
			return false;
		} else if (user.length() > MAXUSERLENGTH) {
			return false;
		}

		// can't be same as guest
		if (user.length() >= GUESTPRIORWORD.length()
				&& user.substring(0, GUESTPRIORWORD.length()).equals(GUESTPRIORWORD)) {
			return false;
		}

		// can't be in badwords
		for (String[] w : badwords.getWords()) {
			for (String s : w) {
				if (user.contains(s)) {
					return false;
				}
			}
		}
		return true;
	}

}
