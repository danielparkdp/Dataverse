package edu.brown.cs.binarytreegame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.Game;

public class SearchTreeGame extends Game {

	private BinaryTree tree;
	private BinaryTreeNode root;
	private BinaryTreeNode targetNode;

	// keeps track of the current node each player is on
	private Map<String, BinaryTreeNode> playerPositions;
	// keeps track of each player's score
	private Map<String, Integer> playerScores;
	// keep track of the most recent move each player made.
	private Map<String, Integer> playerMoves;
	// keeps track of non-optimal moves by the player and has some impact on the
	// score as a result
	private Map<String, Integer> playerInefficientMoves;

	// stores usernames of eveyone in the game.
	private Collection<String> playerUsernames;

	public SearchTreeGame(Collection<String> playerUsernames) {
		super();
		// need to store this here because I should only set up player maps after
		// creating the tree, so I cant call it in the constructor.
		this.playerUsernames = playerUsernames;
	}

	@Override
	protected void setUpGame() {
		this.tree = this.generateTree();
		this.root = this.tree.getRoot();
		this.targetNode = this.setNewTargetNode();
		this.setUpPlayerMaps(this.playerUsernames);
	}

	protected BinaryTree generateTree() {
		// create a random list of numbers in an interval and pass it to a
		// binarytree constructor
		List<Double> randomNums = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			// rounding to 2 decimal places
			double randomNum = Math.round(Math.random() * 100 * 100) / 100;
			// not allowing duplicates
			while (randomNums.contains(randomNum)) {
				randomNum = Math.round(Math.random() * 100 * 100) / 100;
			}
			randomNums.add(randomNum);
		}
		return new BinaryTree(randomNums);
	}

	/**
	 * makes a separate dictionary for score, last move, and current position.
	 * Terrible style but it gets the job done.
	 */
	private void setUpPlayerMaps(Collection<String> playerUsernames) {
		this.playerPositions = new HashMap<>();
		this.playerScores = new HashMap<>();
		this.playerMoves = new HashMap<>();
		this.playerInefficientMoves = new HashMap<>();
		for (String user : playerUsernames) {
			this.playerPositions.put(user, this.root);
			this.playerScores.put(user, 0);
			this.playerMoves.put(user, 0);
			this.playerInefficientMoves.put(user, 0);
		}
	}

	@Override
	protected boolean validateMove(JsonObject move) {
		String player = move.get("username").getAsString();
		BinaryTreeNode currPlayerNode = this.playerPositions.get(player);
		int code = move.get("moveCode").getAsInt();
		// override the last move of this player
		this.playerMoves.put(player, code);
		switch (code) {
		case 0:
			if (this.root != null) {
				return true;
			}
			return false;
		case 1:
			return currPlayerNode.hasLeft();
		case 2:
			return currPlayerNode.hasRight();
		case 3:
			// this is the case for traversing to the parent
			if (currPlayerNode == this.root) {
				return false;
			} else {
				return true;
			}
		case 4:
			// this is the case for guessing a node as the target node
			if (currPlayerNode.equals(this.targetNode)) {
				return true;
			} else {
				return false;
			}
		default:
			throw new IllegalArgumentException("ERROR: The code " + Integer.toString(code) + " was not recognized");
		}
	}

	@Override
	protected void updateGameState(JsonObject move) {
		String player = move.get("username").getAsString();
		BinaryTreeNode currPlayerNode = this.playerPositions.get(player);
		int code = move.get("moveCode").getAsInt();
		// updating inefficiency score
		double val = currPlayerNode.getValue();
		int newInefficiency = this.playerInefficientMoves.get(player) + this.calcInefficiencyScore(val, code);
		this.playerInefficientMoves.put(player, newInefficiency);
		// override the last move of this player
		this.playerMoves.put(player, code);
		switch (code) {
		case 0:
			currPlayerNode = this.root;
			break;
		case 1:
			currPlayerNode = (BinaryTreeNode) currPlayerNode.getLeft();
			break;
		case 2:
			currPlayerNode = (BinaryTreeNode) currPlayerNode.getRight();
			break;
		case 3:
			String currId = currPlayerNode.getID();
			String parentId = currId.substring(0, currId.length() - 1);
			currPlayerNode = tree.getNodeById(parentId);
			break;
		case 4:
			this.targetNode = this.setNewTargetNode();
			int currScore = this.playerScores.get(player);
			this.playerScores.put(player, currScore + this.calculateScore(player));
			// inefficiency score of this player only is reset
			this.playerInefficientMoves.put(player, 0);
			break;
		default:
			throw new IllegalArgumentException("ERROR: The code " + Integer.toString(code) + " was not recognized");
		}
		this.playerPositions.put(player, currPlayerNode);
	}

	@Override
	protected int calculateScore(String player) {
		final int baseScore = 100;
		final int minScore = 10;
		final int numWrongMoves = this.playerInefficientMoves.get(player);
		final int penalty = 20 * numWrongMoves;
		return Math.max(minScore, baseScore - penalty);
	}

	@Override
	/**
	 * will always return a json object with player1, player2 and targetnode.
	 * player1 and player2 will contain currNode, score, and lastClicked
	 */
	public JsonObject getDataToDisplay() {
		JsonObject gameState = new JsonObject();
		// looping through all players in the game (will typically be 2 or 1)
		JsonObject allPlayers = new JsonObject();
		for (String player : this.playerScores.keySet()) {
			JsonObject playerStats = this.makePlayerJson(player);
			allPlayers.add(player, playerStats);
		}
		gameState.addProperty("players", GSON.toJson(allPlayers));
		gameState.addProperty("targetNode", GSON.toJson(this.targetNode));
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
		playerStats.addProperty("lastClicked", this.playerMoves.get(playerName));
		playerStats.addProperty("score", this.playerScores.get(playerName));
		playerStats.addProperty("currNode", GSON.toJson(this.playerPositions.get(playerName)));
		playerStats.addProperty("nextScoreVal", this.calculateScore(playerName));
		return playerStats;
	}

	@Override
	protected JsonObject startResponse() {
		JsonObject startResponse = new JsonObject();
		// this should always be set to root
		startResponse.addProperty("root", GSON.toJson(this.root));
		startResponse.addProperty("targetNode", GSON.toJson(this.targetNode));
		return startResponse;
	}

	/**
	 * calculate the inefficiency score of a single move
	 * 
	 * @nodeSide "l" or "r" for being a left or right child
	 * @return this score
	 */
	private int calcInefficiencyScore(double val, int moveCode) {
		// you are punished for going the wrong direction but not for going up when you
		// shouldnt because
		// always going back to the root should be considered a valid non-penalized
		// strategy imo.
		if (moveCode == 4) {
			return 0;
		}
		double targetVal = this.targetNode.getValue();

		if (val < targetVal && (moveCode == 1)) {
			return 1;
		} else if (val > targetVal && (moveCode == 2)) {
			return 1;
		}
		return 0;
	}

	/**
	 *
	 * @return a node that has been semi-randomly selected from the nodes in the
	 *         tree to be the new target node the target node is the node the user
	 *         is trying to navigate to. right now it is possible to get the same
	 *         target node back.
	 */
	protected BinaryTreeNode setNewTargetNode() {
		if (this.root == null) {
			throw new NullPointerException("tree has not yet been constructed");
		}
		// first randomly select a length of string between 1 and max depth of tree
		// then make a string of that length of random l and r
		// get the node with that id
		int strLength = (int) (Math.random() * this.tree.getDepth()) + 1;
		String id = "b";
		for (int i = 0; i < strLength; i++) {
			int randomBit = (int) (Math.random() * 2);
			if (randomBit == 1) {
				id += "r";
			} else {
				id += "l";
			}
		}
		return this.tree.getNodeById(id);
	}

	@Override
	public GAME_TYPE getType() {
		return GAME_TYPE.SEARCH_TREE;
	}

	@Override
	public int getScore(String player) {
		return this.playerScores.get(player);
	}

}
