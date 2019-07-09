package edu.brown.cs.hashmapgame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.Game;

/**
 * this will eventually be migrated into the regular hashmap game, just using
 * this as a dev space while mantaining fucntionality of the other one.
 * 
 * @author camillo_stuff
 *
 */
public class HashMapGame extends Game {

	private static enum ACTION_TYPE {
		SHOW_BUCKET, GUESS_TARGET
	}

	// we want these to be primes to give some intuition about actual hashmaps.
	// one will be selected for an instance of a game.
	private final int[] availableMods = { 5, 7, 11 };
	private int mod;

	private int targetNumber;
	private List<Integer>[] buckets;

	private final int NUMBER_RANGE = 100;
	private final int NUM_NUMBERS = 20;
	// a list of all the integers that are in the hashmap, i.e. are available to
	// be chosen as a target.
	private List<Integer> availableNumbers;

	// the bucket number that each user currently has open(i.e. expanded)
	// only one bucket can be viewed at a time per player.
	private Map<String, Integer> playerBuckets;
	private Map<String, Integer> playerScores;
	// keeps track of how many times a player has clicked the wrong bucket since the
	// target has been updated
	private Map<String, Integer> playerMissedStreaks;

	public HashMapGame(Collection<String> playerUsernames) {
		super();
		this.setUpPlayerMaps(playerUsernames);
	}

	@Override
	protected void setUpGame() {
		// must be called in this order!
		this.mod = availableMods[(int) (Math.random() * availableMods.length)];
		this.buckets = new List[mod];
		for (int i = 0; i < mod; i++) {
			this.buckets[i] = new ArrayList<Integer>();
		}
		this.availableNumbers = this.generateRandomNumbers(NUM_NUMBERS);
		this.populateBuckets(this.availableNumbers);
		this.targetNumber = this.getNewTargetNumber();
	}

	/**
	 * sets up a dictionary for each attribute that makes one entry per connected
	 * player.
	 * 
	 * @param numPlayers
	 *            how many players will connect to this game.
	 */
	private void setUpPlayerMaps(Collection<String> playerUsernames) {
		this.playerMissedStreaks = new HashMap<>();
		this.playerBuckets = new HashMap<>();
		this.playerScores = new HashMap<>();
		for (String user : playerUsernames) {
			// use -1 to represent no bucket currently open
			this.playerBuckets.put(user, 0);
			this.playerScores.put(user, 0);
			this.playerMissedStreaks.put(user, 0);
		}

	}

	@Override
	protected boolean validateMove(JsonObject move) {
		if (move == null) {
			return false;
		}
		// first check if the playerID sending the move is actually a player
		String player = move.get("username").getAsString();
		if (!playerScores.containsKey(player)) {
			return false;
		}
		ACTION_TYPE type = ACTION_TYPE.values()[move.get("moveType").getAsInt()];
		if (type == ACTION_TYPE.GUESS_TARGET) {
			int guessedNumber = move.get("guess").getAsInt();
			if (guessedNumber == this.targetNumber) {
				return true;
			} else {
				return false;
			}
		} else if (type == ACTION_TYPE.SHOW_BUCKET) {
			int newBucket = move.get("newBucket").getAsInt();
			if (newBucket >= 0 && newBucket < this.mod) {
				return true;
			} else {
				return false;
			}
		}
		System.out.println("ERROR: The move type " + move.get("type").getAsString() + "was not recognized");
		return false;
	}

	@Override
	public JsonObject getDataToDisplay() {
		JsonObject gameState = new JsonObject();
		gameState.addProperty("target", this.targetNumber);
		JsonObject playerMap = new JsonObject();
		for (String player : this.playerScores.keySet()) {
			JsonObject playerStats = this.makePlayerJson(player);
			playerMap.add(player, playerStats);
		}
		gameState.addProperty("players", GSON.toJson(playerMap));
		return gameState;
	}

	/**
	 * Helper method that creates JsonObjects specific to players
	 * 
	 * @param playerName
	 *            the key in the hashsets used to access this player's info
	 * @return a JsonObject wrapping this information.
	 */
	private JsonObject makePlayerJson(String playerName) {
		JsonObject playerStats = new JsonObject();
		playerStats.addProperty("newScoreVal", this.calculateScore(playerName));
		playerStats.addProperty("score", this.playerScores.get(playerName));
		playerStats.addProperty("currBucketNums", GSON.toJson(buckets[this.playerBuckets.get(playerName)]));
		return playerStats;
	}

	@Override
	protected void updateGameState(JsonObject move) {
		String player = move.get("username").getAsString();
		ACTION_TYPE type = ACTION_TYPE.values()[move.get("moveType").getAsInt()];
		if (type == ACTION_TYPE.GUESS_TARGET) {
			int guessedNumber = move.get("guess").getAsInt();
			// this will technically always be the case since it is already validated
			assert (guessedNumber == this.targetNumber);
			int playerScore = this.playerScores.get(player);
			this.playerScores.put(player, playerScore + this.calculateScore(player));
			this.targetNumber = this.getNewTargetNumber();
			// ONLY reset the player who correctly found the target
			this.playerMissedStreaks.put(player, 0);
		} else if (type == ACTION_TYPE.SHOW_BUCKET) {
			// update the number of times this player has clicked a bucket
			// if they guess the target this is reset so they are only penalized if they
			// open a bucket and then try to open another one
			int clickedBucket = move.get("newBucket").getAsInt();
			// if they clicked a wrong bucket, increase the count
			if (clickedBucket != this.targetNumber % this.mod) {
				int currNumTimesClicked = this.playerMissedStreaks.get(player);
				this.playerMissedStreaks.put(player, currNumTimesClicked + 1);
			}
			this.playerBuckets.put(player, clickedBucket);
		}
	}

	@Override
	protected int calculateScore(String player) {
		// have to add one penalty extra to baseScore because it will always be
		// decreased by one because clicking on a bucket will always decrease it, even
		// if it was the right bucket
		final int baseScore = 100;
		final int minScore = 10;
		final int numWrongMoves = this.playerMissedStreaks.get(player);
		final int penalty = 20 * numWrongMoves;
		return Math.max(minScore, baseScore - penalty);
	}

	@Override
	protected JsonObject startResponse() {
		JsonObject startResp = new JsonObject();
		startResp.addProperty("target", this.targetNumber);
		startResp.addProperty("numBuckets", this.mod);
		return startResp;
	}

	/**
	 * generates a list of random numbers between 0 and the number range.
	 * 
	 * @param howMany
	 *            numbers to generate and put in the list
	 * @return said list.
	 */
	private List<Integer> generateRandomNumbers(int howMany) {
		List<Integer> allTheNumbers = new ArrayList<>();
		for (int i = 0; i < howMany; i++) {
			int newNum = (int) (Math.random() * this.NUMBER_RANGE);
			while (allTheNumbers.contains(newNum)) {
				newNum = (int) (Math.random() * this.NUMBER_RANGE);
			}
			allTheNumbers.add(newNum);
		}

		return allTheNumbers;
	}

	/**
	 * puts the passed in numbers into the list at the appropriate array index based
	 * on its remainder with the mod.
	 *
	 * @param numbers
	 *            a list of integers, such as the ones created by
	 *            generateRandomNumbers
	 */
	private void populateBuckets(List<Integer> numbers) {
		for (Integer i : numbers) {
			int remainder = i % this.mod;
			// shouldnt error since the bucket size is determined by mod
			this.buckets[remainder].add(i);
		}
	}

	private int getNewTargetNumber() {
		int index = (int) (Math.random() * NUM_NUMBERS);
		return availableNumbers.get(index);
	}

	@Override
	public GAME_TYPE getType() {
		return GAME_TYPE.HASHMAP;
	}

	@Override
	public int getScore(String player) {
		return this.playerScores.get(player);
	}

}
