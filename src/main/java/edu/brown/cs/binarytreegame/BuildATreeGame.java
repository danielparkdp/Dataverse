package edu.brown.cs.binarytreegame;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.Game;

/**
 * in this game you will start with an initial root node. You will then receive
 * additional nodes and you must place them in the correct position in the tree.
 * The goal is to correctly add as many nodes as possible to the tree within the
 * time limit.
 *
 * @author camillo_stuff
 *
 */
public class BuildATreeGame extends Game {

	private final int VALUE_RANGE = 100;

	private BinaryTree tree;
	private BinaryTreeNode root;

	// keeps track of the number the user is trying to put in the correct position
	// in the tree.
	private double currDisplayedNumber;

	// all those nodes which have been correctly guessed and are thus displayed in
	// the frontend.
	private List<BinaryTreeNode> displayedNodes;

	private final int height = 5;

	private int wrongClicks;

	@Override
	protected void setUpGame() {
		// make a new tree rooted at a random number somewhere in the middle of the
		// possible number range
		this.tree = this.generateTree(this.height);
		this.root = tree.getRoot();
		this.displayedNodes = new ArrayList<>();
		this.displayedNodes.add(root);
		this.currDisplayedNumber = this.chooseValueFromTree();
		this.wrongClicks = 0;
	}

	@Override
	protected boolean validateMove(JsonObject move) {
		String id = move.get("id").getAsString();
		BinaryTreeNode nodeAtID = tree.getNodeById(id);
		// TODO: Change this to something more meaningful.
		assert (nodeAtID != null);
		if (nodeAtID.getValue() == this.currDisplayedNumber) {
			return true;
		} else {
			this.wrongClicks += 1;
			return false;
		}
	}

	@Override
	public JsonObject getDataToDisplay() {
		// sends back the newest node to display(or potentially the whole list) and
		// the new node number
		// this will have to be parsed again in the frontend
		JsonObject payload = new JsonObject();
		String newNode;
		// if the tree is complete then the game is over.
		if (this.displayedNodes.isEmpty()) {
			newNode = "GAME IS OVER";

		} else {
			newNode = GSON.toJson(this.displayedNodes.get(this.displayedNodes.size() - 1));
		}
		payload.addProperty("currValue", this.currDisplayedNumber);
		payload.addProperty("newNodeToDisp", newNode);
		payload.addProperty("score", this.score);
		payload.addProperty("nextScoreVal", this.calculateScore(""));
		return payload;
	}

	@Override
	protected void updateGameState(JsonObject move) {
		// just adds the node to the displayed nodes.
		String id = move.get("id").getAsString();
		BinaryTreeNode nodeAtID = tree.getNodeById(id);
		this.displayedNodes.add(nodeAtID);
		// make a new target number
		this.currDisplayedNumber = this.chooseValueFromTree();
		this.score += this.calculateScore("");
		// reset this to 0 when a correct node is found
		this.wrongClicks = 0;
	}

	@Override
	protected int calculateScore(String player) {
		final int baseScore = 50;
		final int minScore = 10;
		int penalty = 15 * this.wrongClicks;
		return Math.max(minScore, baseScore - penalty);
	}

	/**
	 * send back root node and the first value
	 */
	@Override
	protected JsonObject startResponse() {
		JsonObject startResp = new JsonObject();
		startResp.addProperty("rootValue", this.root.getValue());
		startResp.addProperty("currValue", this.currDisplayedNumber);
		return startResp;
	}

	/**
	 * creates a complete tree of height h with random numbers
	 *
	 * @return the tree
	 */
	public BinaryTree generateTree(int height) {
		List<Double> randomNums = new ArrayList<>();
		int numNodes = (int) (Math.pow(2, height)) - 1;
		for (int i = 0; i < numNodes; i++) {
			double randomNum = Math.round(Math.random() * 100 * 100) / 100;
			// no duplicate numbers
			while (randomNums.contains(randomNum)) {
				randomNum = Math.round(Math.random() * 100 * 100) / 100;
			}
			randomNums.add(randomNum);
		}
		return new BinaryTree(randomNums);
	}

	/**
	 * generates a random number that will be presented to the user to put into the
	 * correct spot in the tree.
	 *
	 * @param min
	 *            the minimum random value
	 * @param max
	 *            the maximum random value
	 * @return the generated double
	 */
	private double generateRandomValue(int min, int max) {
		double number = (Math.random() * max * 100) / 100 + min;
		return number;
	}

	/**
	 * selects a node randomly from the tree given that the parent is already
	 * displayed
	 *
	 * @return the value associated with that node.
	 */
	private double chooseValueFromTree() {
		// first add all available values to this list.
		// then choose one randomly
		List<Double> availableNumbers = new ArrayList<>();
		// traverse all displayed nodes and add the values of their immediate
		// children
		for (BinaryTreeNode n : this.displayedNodes) {
			String id = n.getID();
			if (id.length() < this.height) {
				BinaryTreeNode left = this.tree.getNodeById(id + "l");
				// dont want to generate the number of a node that is both the child of a
				// displayed node but is also already displayed
				if (!this.displayedNodes.contains(left)) {
					double leftVal = left.getValue();
					availableNumbers.add(leftVal);
				}
				// dont want to generate the number of a node that is both the child of a
				// displayed node but is also already displayed
				BinaryTreeNode right = this.tree.getNodeById(id + "r");
				if (!this.displayedNodes.contains(right)) {
					double rightVal = right.getValue();
					availableNumbers.add(rightVal);
				}
			}
		}
		// now choose a value randomly
		int randomIndex = (int) (Math.random() * availableNumbers.size());
		return availableNumbers.get(randomIndex);
	}

	@Override
	public GAME_TYPE getType() {
		return GAME_TYPE.BUILD_TREE;
	}

	@Override
	public int getScore(String player) {
		return this.score;
	}

}
