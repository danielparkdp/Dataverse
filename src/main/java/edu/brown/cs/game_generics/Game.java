package edu.brown.cs.game_generics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public abstract class Game {
	protected int score;
	// the maximum number of time that can be taken (in seconds).
	protected int maxTime;
	// need this to convert nodes to gson objects
	protected static Gson GSON = new Gson();

	public Game() {
		this.maxTime = 60;
	}

	/**
	 * should not be overridden. Instead override setUpGame and startResponse
	 *
	 * @return the start response specific to this game.
	 */
	public JsonObject start() {
		this.score = 0;
		this.setUpGame();
		return this.startResponse();
	}

	public abstract GAME_TYPE getType();

	protected abstract void setUpGame();

	// needs to be overridden
	// if movie is valid return true, else false
	protected abstract boolean validateMove(JsonObject move);

	// can just send back the json object specific to this game
	// has to be public so can be called by gamehandler for example
	public abstract JsonObject getDataToDisplay();

	// change the instance variables associated with the game.
	protected abstract void updateGameState(JsonObject move);

	public void handleInput(JsonObject move) throws InvalidMoveException {

		if (this.validateMove(move)) {
			this.updateGameState(move);
		} else {
			// send back a negative response
			throw new InvalidMoveException("The move " + move + " was not valid");
		}
	}

	/**
	 * use this to get the json object specific to a certain game can be protected
	 * since this is returned by start.
	 *
	 * @return a json object with the start parameters for a certain game
	 */
	protected abstract JsonObject startResponse();

	public int maxTime() {
		return this.maxTime;
	}

	/**
	 * 
	 * @return the score of the requested player
	 */
	public abstract int getScore(String player);

	/**
	 * 
	 * @param player
	 *            the username of the player for whom to calculate score
	 * @return the score of a single correct target guess. Will be added to total
	 *         score.
	 */
	protected abstract int calculateScore(String player);

}
