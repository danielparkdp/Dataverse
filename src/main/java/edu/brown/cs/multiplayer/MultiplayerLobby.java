package edu.brown.cs.multiplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GameHandler;
import edu.brown.cs.websocket.MESSAGE_TYPE;

public class MultiplayerLobby {
	private static final Gson GSON = new Gson();

	private String owner;

	private Map<Session, String> connectedPlayers;

	private List<String> invitedPlayers;

	// map of all usernames to their sessions
	// need this because we dont know which usernames will be invited
	private Map<String, Session> userSessions;

	public MultiplayerLobby(String creator, Map<String, Session> userSessionMap) {
		this.connectedPlayers = new HashMap<>();
		this.invitedPlayers = new ArrayList<>();
		this.owner = creator;
		this.userSessions = userSessionMap;
		this.invitedPlayers.add(creator);
		this.connectedPlayers.put(userSessions.get(creator), creator);
	}

	/**
	 * 
	 * @param username
	 * @param busyPlayers
	 *            gets the list of busyPlayers so it can check if a user is in it
	 *            before inviting them
	 */
	public void invitePlayer(String username, Set<String> busyPlayers) {
		JsonObject invite = new JsonObject();
		// just sends the name of the invitor to the recipient
		invite.addProperty("type", MESSAGE_TYPE.LOBBY_INVITE.ordinal());
		invite.addProperty("owner", this.owner);
		invite.addProperty("invitee", username);
		if (!userSessions.containsKey(username)) {
			System.out.println("ERROR: User is not in the arena");
			invite.addProperty("errorReason", "ERROR: There is no player " + username + " in the arena");
			invite.addProperty("valid", false);
		} else if (busyPlayers.contains(username)) {
			System.out.println("ERROR: User is in game, another lobby, or in random queue");
			invite.addProperty("errorReason", "ERROR: " + username + " is already in another game or lobby!");
			invite.addProperty("valid", false);
		} else if (this.invitedPlayers.size() > 3) {
			System.out.println("ERROR: Max lobby size of 4");
			invite.addProperty("errorReason", "ERROR: Max lobby size of 4");
			invite.addProperty("valid", false);
		} else {
			invite.addProperty("valid", true);
			// add this person to the invited players list
			this.invitedPlayers.add(username);
			Session sess = userSessions.get(username);
			// sending it to the invited person
			try {
				sess.getRemote().sendString(GSON.toJson(invite));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		// sending it back to the person who sent the request (lobby owner)
		Session ownerSess = userSessions.get(this.owner);
		try {
			ownerSess.getRemote().sendString(GSON.toJson(invite));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void acceptInvite(String username) {
		this.joinlobby(username);
	}

	/**
	 * basically does all the code for acceptInvite
	 */
	private void joinlobby(String username) {
		Session sess = userSessions.get(username);
		this.connectedPlayers.put(sess, username);
		JsonObject accept = new JsonObject();
		accept.addProperty("user", username);
		accept.addProperty("accepted", true);
		accept.addProperty("type", MESSAGE_TYPE.INVITE_RESPONSE.ordinal());
		this.sendMessage(accept);
	}

	/**
	 * turning down an invite to a game
	 * 
	 * @param username
	 */
	public void declineInvite(String username) {
		this.invitedPlayers.remove(username);
		JsonObject decline = new JsonObject();
		decline.addProperty("user", username);
		decline.addProperty("accepted", false);
		decline.addProperty("type", MESSAGE_TYPE.INVITE_RESPONSE.ordinal());
		this.sendMessage(decline);
	}

	/**
	 * leaving a lobby. 2 scenarios, either an invited person leaves or the owner
	 * leaves.
	 * 
	 * @param username
	 */
	public void leaveLobby(String username) {
		Session sess = this.userSessions.get(username);
		// removing player from both the invited list and the connected list.
		this.invitedPlayers.remove(username);
		if (!this.connectedPlayers.remove(sess, username)) {
			System.out.println("INFO: Attempting to remove player from lobby who is not in it");
			return;
		}
		// send a message to the lobby owner that this person has left
		Session ownerSess = this.userSessions.get(this.owner);
		JsonObject playerLeftLobby = new JsonObject();
		playerLeftLobby.addProperty("type", MESSAGE_TYPE.LOBBY_LEAVE.ordinal());
		playerLeftLobby.addProperty("leaver", username);
		try {
			ownerSess.getRemote().sendString(GSON.toJson(playerLeftLobby));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * if theres at least one player in the lobby and the number of invited and
	 * connected players are equal. returns a game handler with all these sessions
	 * connected
	 * 
	 * @throws LobbyException
	 */
	public GameHandler loadIntroScreen() throws LobbyException {
		if (connectedPlayers.size() > 1 && connectedPlayers.size() == invitedPlayers.size()) {
			// select a random game type
			MULTIPLAYER_GAMES gameType = this.chooseGame();
			// send a message to all players to display the intro screen?
			// then we wait for a START message
			JsonObject ready = new JsonObject();
			ready.addProperty("ready", true);
			ready.addProperty("gameType", gameType.ordinal());
			ready.addProperty("type", MESSAGE_TYPE.LOBBY_LOAD_GAME.ordinal());
			this.sendMessage(ready);
			return new GameHandler(this.connectedPlayers);
		} else {
			// send a message just to the owner that not everyone is in the lobby yet.
			JsonObject notReady = new JsonObject();
			notReady.addProperty("ready", false);
			notReady.addProperty("type", MESSAGE_TYPE.LOBBY_LOAD_GAME.ordinal());
			Session ownerSession = this.userSessions.get(this.owner);
			try {
				ownerSession.getRemote().sendString(GSON.toJson(notReady));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			throw new LobbyException("ERROR: Starting criteria are not met");
		}

	}

	/**
	 * 
	 * @return all the connected players
	 */
	public Set<Session> getPlayers() {
		return this.connectedPlayers.keySet();
	}

	/**
	 * 
	 * @return the usernames of all connected players
	 */
	public Collection<String> getPlayerUsernames() {
		return this.connectedPlayers.values();
	}

	/**
	 * 
	 * @return an enum val representing one of the three multiplayer games.
	 */
	private MULTIPLAYER_GAMES chooseGame() {
		return MULTIPLAYER_GAMES.values()[new Random().nextInt(MULTIPLAYER_GAMES.values().length)];
	}

	/**
	 * destroy the lobby and let everyone know
	 */
	public void selfDestruct() {
		JsonObject destructMsg = new JsonObject();
		destructMsg.addProperty("type", 66);
		this.sendMessage(destructMsg);
	}

	/**
	 * sends the msg to all players in the lobby
	 * 
	 * @param msg
	 * @throws IOException
	 *             in case something goes wrong with the sessions.
	 */
	private void sendMessage(JsonObject msg) {
		for (Session player : this.connectedPlayers.keySet()) {
			try {
				player.getRemote().sendString(GSON.toJson(msg));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
