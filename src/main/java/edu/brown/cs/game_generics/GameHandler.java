package edu.brown.cs.game_generics;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.binarytreegame.BuildATreeGame;
import edu.brown.cs.binarytreegame.SearchTreeGame;
import edu.brown.cs.hashmapgame.HashMapGame;
import edu.brown.cs.linkedlistgame.MultiplayerLinkedList;
import edu.brown.cs.login.InfoWrapper;
import edu.brown.cs.queuestackgame.QueueStackGame;
import edu.brown.cs.websocket.MESSAGE_TYPE;

public class GameHandler {

	private static final Gson GSON = new Gson();

	private Map<Session, String> connectedPlayers;
	private Set<Session> connectedPlayerSet;
	private int numPlayers;
	private Game currGame;
	// used for generating a random game
	private static final Random RANDOM = new Random();

	public GameHandler(Session session, String username) {
		this.numPlayers = 1;
		this.connectedPlayers = new HashMap<>();
		this.connectedPlayers.put(session, username);
		this.connectedPlayerSet = new HashSet<>();
		this.connectedPlayerSet.add(session);
	}

	public GameHandler(Map<Session, String> sessions) {
		this.connectedPlayers = sessions;
		this.numPlayers = sessions.size();
		this.connectedPlayerSet = this.connectedPlayers.keySet();
	}

	/**
	 * Sets the current game, starts the game, retrieves the response object related
	 * to starting the game and sends it back to the client that requested it.
	 *
	 * @param gameName
	 *            the name of the game
	 * @throws IOException
	 *             the websocket handler will have to catch this
	 */
	public void onStartMessage(String gameName) throws IOException {
		// if(gameType.equals(GAME_TYPE.RANDOM)) {

		// }
		try {
			this.currGame = this.setCurrGame(gameName, this.numPlayers);
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			// this should never happen if we have game names hardcoded correctly
			return;
		}
		JsonObject payload = this.currGame.start();
		payload.addProperty("maxTime", currGame.maxTime());
		payload.addProperty("players", GSON.toJson(this.connectedPlayers.values()));
		JsonObject wrappedPayload = new JsonObject();
		wrappedPayload.addProperty("type", MESSAGE_TYPE.GAME_START.ordinal());
		wrappedPayload.add("payload", payload);
		this.sendMessage(wrappedPayload);
	}

	/**
	 *
	 * @param userInput
	 *            whatever the user input game is to this specific game, such as an
	 *            encoded keystroke or a click event
	 * @throws IOException
	 *             handled by websocket
	 */
	public void onMoveMessage(JsonObject userInput) throws IOException {
		if (currGame == null) {
			throw new NullPointerException("The current game has not yet been set with a START message");
		}
		JsonObject payload;
		try {
			currGame.handleInput(userInput);
			payload = this.currGame.getDataToDisplay();
			payload.addProperty("valid", true);
			payload.addProperty("errorMessage", "None");
			// in case the move isnt valid
		} catch (InvalidMoveException e) {
			// we send back the current node, the boolean false, the error message,
			// the current target and the score
			payload = this.currGame.getDataToDisplay();
			payload.addProperty("valid", false);
			payload.addProperty("errorMessage", e.getMessage());
		}
		// adding the player who actually made the move to the payload.
		String userWhoMoved = userInput.get("username").getAsString();
		payload.addProperty("userWhoMoved", userWhoMoved);
		// wrapping the payload in another json object that stores type.
		JsonObject wrappedPayload = new JsonObject();
		wrappedPayload.addProperty("type", MESSAGE_TYPE.GAME_ACTION.ordinal());
		wrappedPayload.add("payload", payload);
		this.sendMessage(wrappedPayload);
	}

	/**
	 * Sets the current game to null and send information to the frontend containing
	 * (minimally) the score.
	 *
	 * @throws IOException
	 *             which is handled by websocket class
	 * @user receives an infowrapper so it can update the infowrapper starbucks
	 *       based on score and highscore
	 * @sess the session which sent the leave request and to which it should be sent
	 *       back to.
	 */
	public void onLeaveMessage(InfoWrapper user, Session sess) throws IOException {
		GAME_TYPE gameType = this.currGame.getType();
		// send back score, whether it was a highscore, and how many starbucks were
		// earned
		JsonObject endGameStats = new JsonObject();
		String username = this.connectedPlayers.get(sess);
		int score = this.currGame.getScore(username);
		// updating player elo
		int currElo = user.getElo();
		user.setElo(currElo + this.eloChange(score));
		// calculating funds to be added
		int newStarbucks = (int) (score / 50);
		int totalStarbucks = user.increaseStarbucks(newStarbucks);
		int highScore = user.getHighScore(gameType);
		if (score > highScore) {
			user.setHighScore(gameType, score);
			highScore = score;
			// a little extra boost for getting a high score
			totalStarbucks = user.increaseStarbucks(5);
		}
		// adding the computed metrics to the json object.
		endGameStats.addProperty("starbucks", totalStarbucks);
		endGameStats.addProperty("score", score);
		endGameStats.addProperty("highScore", highScore);

		// wrapping the payload in another json object that stores type.
		JsonObject wrappedPayload = new JsonObject();
		wrappedPayload.addProperty("type", MESSAGE_TYPE.GAME_LEAVE.ordinal());
		wrappedPayload.add("payload", endGameStats);
		// only want to send leave back to the person who sent it
		sess.getRemote().sendStringByFuture(GSON.toJson(wrappedPayload));
		// reset the current game to none.

	}

	/**
	 * helper method to change the current game
	 *
	 * @param gameName
	 *            the name of the game.
	 * @return a subclass of Game.
	 */
	private Game setCurrGame(String gameName, int numPlayers) {
		switch (gameName) {
		case "Bin-apple Trees":
			System.out.println("INFO: Game set to Binary Tree Game");
			return new SearchTreeGame(this.connectedPlayers.values());
		case "Choco Chip Links":
			System.out.println("INFO: Game set to Linked List Game");
			// uncomment when linkedlist game actually extends Game.
			return new MultiplayerLinkedList(this.connectedPlayers.values());
		case "Barbe-Queue Rush":
			System.out.println("INFO: Game set to Barbe-Queue Rush");
			return new QueueStackGame();
		case "Candy Hash Saga":
			System.out.println("INFO: Game set to Hashmap Game");
			return new HashMapGame(this.connectedPlayers.values());
		case "Build A Tree":
			System.out.println("INFO: Game set to Build A Tree Game");
			return new BuildATreeGame();
		default:
			throw new IllegalArgumentException("The game name " + gameName + " was not recognized");
		}
	}

	/**
	 * change your elo based on how you performed in the game. The change in elo is
	 * calculated based on the score you achieved and how many players were in the
	 * game Note: this means that lobby and singleplayer games currently affect elo.
	 * 
	 * @param score
	 * @return
	 */
	private int eloChange(int score) {
		int averageScore;
		if (currGame instanceof SearchTreeGame) {
			averageScore = 600;
		} else if (currGame instanceof HashMapGame) {
			averageScore = 800;
		} else if (currGame instanceof MultiplayerLinkedList) {
			averageScore = 500;
		} else {
			// dont want single player games to affect elo
			averageScore = score;
		}
		// normalize based on how many players are in the game.
		averageScore = averageScore / this.numPlayers;
		// if you perform well, your elo goes up, otherwise it goes down
		int scoreDiff = score - averageScore;
		return (scoreDiff / 10);
	}

	private void sendMessage(JsonObject msg) throws IOException {
		for (Session player : this.connectedPlayerSet) {
			player.getRemote().sendStringByFuture(GSON.toJson(msg));
		}
	}

}
