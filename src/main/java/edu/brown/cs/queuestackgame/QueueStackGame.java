package edu.brown.cs.queuestackgame;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.Game;

/**
 * Backend class for the QS game, handles multiplayer game logic for building,
 * creating, and validating burgers.
 *
 */
public class QueueStackGame extends Game {

	private int score1;
	private List<String> burger1;
	private String[] possibleVals1;
	private boolean stack1;
	private int repeat1;
	private int badmove;

	private static final int MAX = 4;
	private static final int MIN = 1;
	private static final int MOSTREPEAT = 3;

	private static final int BASESCORE = 20;
	private static final int MINSCORE = 20;
	private static final int DECREASESCORE = 10;

	/**
	 * constructor, can add instantiations here if needed.
	 */
	public QueueStackGame() {

	}

	/**
	 * generate a burger.
	 */
	private void generate() {

		// clear burger first, to make new one
		burger1.clear();

		// choose a random size of burger
		int size = (int) (Math.random() * (MAX - MIN + 1) + MIN);

		// build burger
		burger1.add("burgTop");

		for (int i = 0; i < size; i++) {
			burger1.add(possibleVals1[(int) (Math.random() * possibleVals1.length)]);
		}

		burger1.add("burgBtm");
		// random stack/queue, but if has repeated a lot, changes.
		if (Math.random() > 0.5) {
			if (!stack1) {
				stack1 = true;
				repeat1 = 0;
			}
			repeat1 += 1;
		} else {
			if (stack1) {
				stack1 = false;
				repeat1 = 0;
			}
			repeat1 -= 1;
		}

		if (repeat1 > MOSTREPEAT || repeat1 < (-1 * MOSTREPEAT)) {
			stack1 = !stack1;
			repeat1 = 0;
		}
	}

	/**
	 * Set up Game by generating everything.
	 */
	@Override
	protected void setUpGame() {

		score1 = 0;
		burger1 = new ArrayList<String>();
		possibleVals1 = new String[4];
		possibleVals1[0] = "tomato";
		possibleVals1[1] = "lettuce";
		possibleVals1[2] = "cheese";
		possibleVals1[3] = "meat";

		// false for queue, true for stack1
		stack1 = false;

		repeat1 = 0;

		badmove = 0;

		this.generate();
	}

	/**
	 * Validate the move given.
	 */
	@Override
	protected boolean validateMove(JsonObject move) {
		JsonArray code = move.getAsJsonArray("burger");
		// must be right size
		if (code.size() > MAX + 2 || code.size() < MIN + 2) {
			return false;
		}

		List<String> input = new ArrayList<String>();
		// validate the burger sent in.
		for (int i = 0; i < code.size(); i++) {
			input.add(code.get(i).getAsString());
		}

		return this.validate(input);

	}

	/**
	 * Update game based on move.
	 */
	@Override
	protected void updateGameState(JsonObject move) {
		// create new burger.
		this.generate();
	}

	/**
	 * calculate score
	 */
	@Override
	protected int calculateScore(String player) {
		return Math.max(MINSCORE, BASESCORE * (burger1.size() - 2) - (badmove * DECREASESCORE));
	}

	/**
	 * give data to display up front.
	 */
	@Override
	public JsonObject getDataToDisplay() {
		// get score to give
		JsonObject gameState = new JsonObject();
		gameState.addProperty("score", score1);

		String[] burg = new String[burger1.size()];
		// give new burger
		for (int i = 0; i < burger1.size(); i++) {
			System.out.println(burger1.get(i));
			burg[i] = burger1.get(i);
			System.out.println(burg[i]);
		}

		gameState.addProperty("order", GSON.toJson(burg));
		gameState.addProperty("nextScoreVal", this.calculateScore(""));

		// give stack queue
		if (stack1) {
			gameState.addProperty("stackqueue", "Stack");
		} else {
			gameState.addProperty("stackqueue", "Queue");
		}

		return gameState;
	}

	/**
	 * response needed to send on start.
	 */
	@Override
	protected JsonObject startResponse() {
		// on start, give data
		JsonObject gameState = new JsonObject();
		gameState.addProperty("score", score1);
		// starting data
		String[] burg = new String[burger1.size()];

		for (int i = 0; i < burger1.size(); i++) {
			burg[i] = burger1.get(i);
		}

		gameState.addProperty("order", GSON.toJson(burg));

		if (stack1) {
			gameState.addProperty("stackqueue", "Stack");
		} else {
			gameState.addProperty("stackqueue", "Queue");
		}

		return gameState;
	}

	/**
	 * Check to make sure everything is valid in move.
	 *
	 * @param input
	 *            input string
	 * @return boolean valid or not
	 */
	private boolean validate(List<String> input) {
		boolean correct = true;
		// validate burger
		int size = burger1.size();

		if (input.size() != size) {
			correct = false;
		} else {
			// make sure everythings right
			if (!stack1) {
				for (int i = 0; i < size; i++) {
					if (!input.get(i).equals(burger1.get(size - 1 - i))) {
						correct = false;
					}
				}
			} else {
				for (int i = 0; i < size; i++) {
					if (!input.get(i).equals(burger1.get(i))) {
						correct = false;
					}
				}
			}

		}
		// correct or not
		if (correct) {
			score1 += this.calculateScore("player");

			badmove = 0;
		} else {
			badmove++;
		}

		return correct;

	}

	/**
	 * return score.
	 *
	 * @return int score.
	 */
	public int getScore() {
		return score1;
	}

	/**
	 * game type.
	 */
	@Override
	public GAME_TYPE getType() {
		return GAME_TYPE.QUEUE_STACK;
	}

	/**
	 * get score.
	 */
	@Override
	public int getScore(String player) {
		return score1;
	}

	/**
	 * testing method.
	 */
	public List<String> getBurger() {
		return ImmutableList.copyOf(burger1);
	}

	/**
	 * testing method.
	 */
	public boolean getStack() {
		return stack1;
	}
}
