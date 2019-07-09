package edu.brown.cs.linkedlistgame;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.Game;

/**
 * Linked List Game's class for back-end of game. Deprecated(See
 * MultiplayerLinkedListGame)
 *
 */
public class LinkedListGame extends Game {

	private List<Integer> cookies1;
	private int location1;
	private int score1;
	private int target1;

	// stores the button that was pressed by the most recent handleInput
	// this is a QOL change for the frontend and has nothing to do with the
	// backend.
	private int lastMoveCode;

	private static final int SIZE = 20;
	private static final int MAX = 100;

	/**
	 * Constructor for LinkedListGame's back-end class.
	 */
	public LinkedListGame() {
		lastMoveCode = -1;
	}

	/**
	 * Set up game, method extended from Game class.
	 */
	@Override
	protected void setUpGame() {
		cookies1 = new LinkedList<Integer>();
		location1 = 0;
		score1 = 0;
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

		lastMoveCode = code;

		if (code == 4 || code == 1 || code == 2) {
			return true;
		}
		return false;
	}

	/**
	 * update game state by managing behavior.
	 */
	@Override
	protected void updateGameState(JsonObject move) {
		int code = move.get("moveCode").getAsInt();
		switch (code) {
		case 4:
			this.validate();
			break;
		case 1:
			this.left();
			break;
		case 2:
			this.right();
			break;
		default:
			throw new IllegalArgumentException("ERROR: The code " + Integer.toString(code) + " was not recognized");
		}
	}

	/**
	 * get the data needed for displaying in front-end.
	 */
	@Override
	public JsonObject getDataToDisplay() {
		JsonObject gameState = new JsonObject();
		gameState.addProperty("score", score1);
		gameState.addProperty("curr", cookies1.get(location1));
		gameState.addProperty("target", cookies1.get(target1));
		gameState.addProperty("lastClicked", lastMoveCode);

		return gameState;
	}

	/**
	 * response at start of game.
	 */
	@Override
	protected JsonObject startResponse() {
		JsonObject gameState = new JsonObject();
		gameState.addProperty("startNode", cookies1.get(location1));
		gameState.addProperty("targetNode", cookies1.get(target1));

		return gameState;
	}

	/**
	 * calculate score.
	 */
	@Override
	protected int calculateScore(String player) {
		return 1;
	}

	private void left() {
		// move left
		if (location1 - 1 >= 0) {
			location1 -= 1;
		} else {
			location1 = SIZE - 1;
		}
	}

	private void right() {
		// move right
		if (location1 + 1 < SIZE) {
			location1 += 1;
		} else {
			location1 = 0;
		}
	}

	private void validate() {
		if (location1 == target1) {
			score1++;
			this.score++;
			target1 = (int) (Math.random() * SIZE);

		}
	}

	/**
	 * clear the cookies.
	 */
	public void clear() {
		cookies1.clear();
		score1 = 0;
		this.score = 0;
	}

	/**
	 * Game type access.
	 */
	@Override
	public GAME_TYPE getType() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * get score.
	 */
	@Override
	public int getScore(String player) {
		// TODO Auto-generated method stub
		return 0;
	}

}
