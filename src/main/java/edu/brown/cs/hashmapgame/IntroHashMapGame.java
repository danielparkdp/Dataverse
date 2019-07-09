package edu.brown.cs.hashmapgame;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.Game;
import edu.brown.cs.game_generics.IllegalGameStateException;

/**
 * this is the first of two hashmap games that will follow the same schema. This
 * one buckets items based on some characteristic, such as color, shape, size
 *
 * @author camillo_stuff
 *
 */

public class IntroHashMapGame extends Game {

	private int sortingCharacteristic;
	// how many different shapes to have
	private int numOfShapes;
	// the list of shapes that hasnt been sorted yet
	private List<Shape> nonSortedShapes;
	// the list of all the initial shapes created on startup, in case the user
	// wants to start sorting by a different criterion.
	private List<Shape> allInitialShapes;

	// how many different types of each characteristic there are
	private final int NUM_COLORS = 5;
	private final int NUM_SHAPES = 4;
	private final int NUM_SIZES = 3;

	private List<Shape>[] sortingBuckets;

	private static enum Characteristic {
		COLOR, SHAPE, SIZE
	}

	/**
	 * the default sorting axis is set to be colors. If you change this you also
	 * have to change the default initialization of sortingBuckets in setUpGame
	 *
	 * @param numOfShapes
	 *            how many shapes you want to have
	 */
	public IntroHashMapGame(int numOfShapes) {
		this.numOfShapes = numOfShapes;
		this.sortingCharacteristic = 0;
	}

	@Override
	protected void setUpGame() {
		this.sortingBuckets = new List[NUM_COLORS];
		this.allInitialShapes = this.makeRandomShapes(this.numOfShapes);
		this.nonSortedShapes = new ArrayList<>(this.allInitialShapes);
	}

	@Override
	protected boolean validateMove(JsonObject move) {
		// lets pretend this gets a json object for now
		// could also represent it as an int where the first two numbers are the
		// position the item was and the second number is the new position.
		// do i want an index of the shape or do i want the shape characteristics?
		return false;
	}

	@Override
	public JsonObject getDataToDisplay() {
		JsonObject gameState = new JsonObject();
		gameState.addProperty("unsortedShapes", GSON.toJson(this.nonSortedShapes));
		// does other stuff need to be sent back?
		// should i just send back all the buckets every time? will they be
		// displayed?
		return gameState;
	}

	@Override
	protected void updateGameState(JsonObject move) {
		// lets pretend this gets a json object for now
		// put the shape object in the bin specified in the message
		// remove the shape object from the nonSorted list.
		// this.sortingBuckets[bucketNum].add(shape);
		// one of these
		// this.nonSortedShapes.remove(index / Object);
	}

	@Override
	protected int calculateScore(String player) {
		return 1;
	}

	@Override
	protected JsonObject startResponse() {
		JsonObject startResp = new JsonObject();
		startResp.addProperty("allShapes", GSON.toJson(this.allInitialShapes));
		return startResp;
	}

	private List<Shape> makeRandomShapes(int numShapes) {
		List<Shape> shapes = new ArrayList<>();
		for (int i = 0; i < numShapes; i++) {
			shapes.add(new Shape());
		}
		return shapes;
	}

	@SuppressWarnings("unchecked")
	private List<Shape>[] makeCharShapeMapping() throws IllegalGameStateException {
		List<Shape>[] buckets;
		// wouldnt let me do a switch statement on an enum
		if (this.sortingCharacteristic == Characteristic.COLOR.ordinal()) {
			buckets = new List[NUM_COLORS];
		} else if (this.sortingCharacteristic == Characteristic.SHAPE.ordinal()) {
			buckets = new List[NUM_SHAPES];
		} else if (this.sortingCharacteristic == Characteristic.SIZE.ordinal()) {
			buckets = new List[NUM_SIZES];
		} else {
			throw new IllegalGameStateException(
					"The sorting characteristic is not set to be one of the values it can take");
		}
		return buckets;
	}

	private void changeSortingCharacteristic(int code) {
		if (code < 0 || code > Characteristic.values().length) {
			throw new IllegalArgumentException(
					Integer.toString(code) + " is not a valid value for the sorting characteristic. Should be one of "
							+ Characteristic.values().toString());
		}
		this.sortingCharacteristic = code;
		// have to make a new bucket system for the new characteristic
		try {
			this.sortingBuckets = this.makeCharShapeMapping();
		} catch (IllegalGameStateException e) {
			System.out.println(
					"Something is screwed up in the HashMap Game. This error should already have been caught by an IllegalArgumentException");
			return;
		}
		// rest the shapes that havent been sorted yet.
		this.nonSortedShapes = new ArrayList<>(this.allInitialShapes);
	}

	@Override
	public GAME_TYPE getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScore(String player) {
		// TODO Auto-generated method stub
		return 0;
	}

}
