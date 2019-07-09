package edu.brown.cs.linkedlistgame;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;

/**
 * Linked List Game's class for back-end of game. Multiplayer focused.
 *
 */
public class MultiplayerLinkedList extends LinkedListGame {

	private List<Integer> cookies1;
	private int target1;

	// keeps track of the current node each player is on
	private Map<String, Integer> playerPositions;
	// keeps track of each player's score
	private Map<String, Integer> playerScores;
	// keep track of the most recent move each player made.
	private Map<String, Integer> playerMoves;
	// keeps track of non-optimal moves by the player and has some impact on the
	// score as a result
	private Map<String, Integer> playerInefficientMoves;

	private static final int SIZE = 20;
	private static final int MAX = 100;

	private static final int BASESCORE = 50;
	private static final int MINSCORE = 10;
	private static final int DECREASESCORE = 10;

	/**
	 * Constructor for LinkedListGame's back-end class.
	 */
	public MultiplayerLinkedList(Collection<String> playerUsernames) {
		super();
		this.setUpPlayerMaps(playerUsernames);
	}

	/**
	 * makes a separate dictionary for score, last move, and current position.
	 */
	private void setUpPlayerMaps(Collection<String> playerUsernames) {
		this.playerPositions = new HashMap<>();
		this.playerScores = new HashMap<>();
		this.playerMoves = new HashMap<>();
		this.playerInefficientMoves = new HashMap<>();
		for (String user : playerUsernames) {
			this.playerPositions.put(user, 0);
			this.playerScores.put(user, 0);
			this.playerMoves.put(user, 0);
			this.playerInefficientMoves.put(user, 0);
		}
	}

	/**
	 * Set up game, method extended from Game class.
	 */
	@Override
	protected void setUpGame() {
		cookies1 = new LinkedList<Integer>();
		target1 = (int) (Math.random() * SIZE);

		this.generate();
	}

	/**
	 * generate cookies randomly, from 1 to MAX.
	 */
	private void generate() {
		Set<Integer> taken = new HashSet<Integer>();
		int curr = 0;

		for (int i = 0; i < SIZE; i++) {
			curr = (int) (Math.random() * MAX + 1);
			while (taken.contains(curr)) {
				curr = (int) (Math.random() * MAX + 1);
			}
			taken.add(curr);
			cookies1.add(curr);
		}
	}

	/**
	 * validate the move sent in.
	 */
	@Override
	protected boolean validateMove(JsonObject move) {
		int code = move.get("moveCode").getAsInt();
		// if it is a valid code
		if (code == 4 || code == 1 || code == 2) {
			return true;
		}
		return false;
	}

	/**
	 * update game state by managing behavior based on input.
	 */
	@Override
	protected void updateGameState(JsonObject move) {
		String player = move.get("username").getAsString();
		int code = move.get("moveCode").getAsInt();
		this.playerMoves.put(player, code);

		switch (code) {
		case 4:
			// enter
			this.validate(player);
			break;
		case 1:
			// left
			this.left(player);
			break;
		case 2:
			// right
			this.right(player);
			break;
		default:
			throw new IllegalArgumentException("ERROR: The code " + Integer.toString(code) + " was not recognized");
		}
	}

	/**
	 * get the data needed for displaying in front-end. will always return a json
	 * object with score, current,left,right.
	 */
	@Override
	public JsonObject getDataToDisplay() {
		JsonObject gameState = new JsonObject();
		JsonObject allPlayers = new JsonObject();
		// add all players info to one object
		for (String player : this.playerScores.keySet()) {
			JsonObject playerStats = this.makePlayerJson(player);
			allPlayers.add(player, playerStats);
		}
		// return updated players info and target.
		gameState.addProperty("players", GSON.toJson(allPlayers));
		gameState.addProperty("target", cookies1.get(target1));

		return gameState;
	}

	/**
	 * Helper method that creates JsonObjects specific to players.
	 *
	 * @param playerName
	 *            the key in the maps used to access this player's info
	 * @return a JsonObject wrapping this info.
	 */
	private JsonObject makePlayerJson(String playerName) {
		JsonObject playerStats = new JsonObject();
		playerStats.addProperty("lastClicked", playerMoves.get(playerName));
		playerStats.addProperty("nextScoreVal", this.calculateScore(playerName));
		playerStats.addProperty("score", playerScores.get(playerName));
		playerStats.addProperty("currNode", GSON.toJson(cookies1.get(playerPositions.get(playerName))));
		return playerStats;
	}

	/**
	 * response at start of game.
	 */
	@Override
	protected JsonObject startResponse() {
		JsonObject gameState = new JsonObject();
		// initial data
		gameState.addProperty("startNode", cookies1.get(0));
		gameState.addProperty("targetNode", cookies1.get(target1));

		return gameState;
	}

	private void left(String player) {
		int loc = playerPositions.get(player);

		// move left
		if (loc - 1 >= 0) {
			loc -= 1;
		} else {
			loc = SIZE - 1;
		}

		playerPositions.put(player, loc);
	}

	private void right(String player) {
		int loc = playerPositions.get(player);

		// move right
		if (loc + 1 < SIZE) {
			loc += 1;
		} else {
			loc = 0;
		}

		playerPositions.put(player, loc);
	}

	private void validate(String player) {
		int loc = playerPositions.get(player);
		int sc = playerScores.get(player);

		// check correct
		if (loc == target1) {
			sc += this.calculateScore(player);
			target1 = (int) (Math.random() * SIZE);
			// update
			playerScores.put(player, sc);

			for (String s : playerInefficientMoves.keySet()) {
				playerInefficientMoves.put(s, 0);
			}

		} else {
			playerInefficientMoves.put(player, playerInefficientMoves.get(player) + 1);
		}
	}

	/**
	 * clear the cookies.
	 */
	@Override
	public void clear() {
		cookies1.clear();
	}

	/**
	 * get type.
	 */
	@Override
	public GAME_TYPE getType() {
		return GAME_TYPE.LINKED_LIST;
	}

	/**
	 * get score.
	 */
	@Override
	public int getScore(String player) {
		return this.playerScores.get(player);
	}

	/**
	 * calculate score to add.
	 */
	@Override
	protected int calculateScore(String player) {
		return Math.max(MINSCORE, BASESCORE - (playerInefficientMoves.get(player) * DECREASESCORE));
	}

	/**
	 * Used for testing.
	 * 
	 * @return cookies1.
	 */
	public List<Integer> getList() {
		return ImmutableList.copyOf(cookies1);
	}

	/**
	 * Used for testing.
	 * 
	 * @return map.
	 */
	public Map<String, Integer> getPositions() {
		return ImmutableMap.copyOf(playerPositions);
	}

	/**
	 * Used for testing.
	 * 
	 * @return map.
	 */
	public Map<String, Integer> getScores() {
		return ImmutableMap.copyOf(playerScores);
	}

}