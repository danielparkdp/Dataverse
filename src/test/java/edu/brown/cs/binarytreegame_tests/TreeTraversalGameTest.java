package edu.brown.cs.binarytreegame_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.binarytreegame.BinaryTreeNode;
import edu.brown.cs.binarytreegame.SearchTreeGame;
import edu.brown.cs.game_generics.Game;
import edu.brown.cs.game_generics.InvalidMoveException;

public class TreeTraversalGameTest {

	protected static Gson GSON = new Gson();

	@Test
	/*
	 * game instantiates and sends a start response correctly
	 */
	public void testGameInstantiation() {
		Game game = new SearchTreeGame(new ArrayList<String>());
		assertNotNull(game);
		JsonObject startResponse = game.start();
		assertNotNull(startResponse);
	}

	/*
	 * test moving, right and up
	 */
	@Test
	public void testMakingMoves() throws InvalidMoveException {
		List<String> players = new ArrayList<>();
		players.add("player1");
		Game game = new SearchTreeGame(players);
		JsonObject p1_left = new JsonObject();
		p1_left.addProperty("username", "player1");
		p1_left.addProperty("moveCode", 1);

		game.start();
		// move to left
		game.handleInput(p1_left);
		JsonObject currState = game.getDataToDisplay();
		assertNotNull(currState);
		JsonObject playersMap = GSON.fromJson(currState.get("players").getAsString(), JsonObject.class);
		JsonObject player1 = GSON.fromJson(playersMap.get("player1"), JsonObject.class);
		BinaryTreeNode player1CurrNode = GSON.fromJson(player1.get("currNode").getAsString(), BinaryTreeNode.class);
		// id equality
		assertEquals(player1CurrNode.getID(), "bl");
		game.handleInput(p1_left);
		currState = game.getDataToDisplay();
		playersMap = GSON.fromJson(currState.get("players").getAsString(), JsonObject.class);
		player1 = GSON.fromJson(playersMap.get("player1"), JsonObject.class);
		player1CurrNode = GSON.fromJson(player1.get("currNode").getAsString(), BinaryTreeNode.class);
		// id equality
		assertEquals(player1CurrNode.getID(), "bll");
	}

	/*
	 * testing multiplayer moves
	 */
	@Test
	public void multiplayerMove() throws InvalidMoveException {
		List<String> players = new ArrayList<>();
		players.add("player1");
		players.add("player2");
		Game game = new SearchTreeGame(players);
		JsonObject p1_left = new JsonObject();
		JsonObject p2_left = new JsonObject();
		JsonObject p1_right = new JsonObject();
		JsonObject p2_right = new JsonObject();
		p1_left.addProperty("username", "player1");
		p1_right.addProperty("username", "player1");
		p2_left.addProperty("username", "player2");
		p2_right.addProperty("username", "player2");
		p1_left.addProperty("moveCode", 1);
		p1_right.addProperty("moveCode", 2);
		p2_left.addProperty("moveCode", 1);
		p2_right.addProperty("moveCode", 2);

		game.start();
		// move to left
		game.handleInput(p1_right);
		game.handleInput(p2_right);
		game.handleInput(p2_left);
		game.handleInput(p2_left);
		JsonObject currState = game.getDataToDisplay();
		assertNotNull(currState);
		JsonObject playersMap = GSON.fromJson(currState.get("players").getAsString(), JsonObject.class);
		JsonObject player1 = GSON.fromJson(playersMap.get("player1"), JsonObject.class);
		BinaryTreeNode player1CurrNode = GSON.fromJson(player1.get("currNode").getAsString(), BinaryTreeNode.class);
		JsonObject player2 = GSON.fromJson(playersMap.get("player2"), JsonObject.class);
		BinaryTreeNode player2CurrNode = GSON.fromJson(player2.get("currNode").getAsString(), BinaryTreeNode.class);
		// id equality
		assertEquals(player1CurrNode.getID(), "br");
		assertEquals(player2CurrNode.getID(), "brll");

	}

	/*
	 * moving out of the bounds
	 */
	@Test(expected = InvalidMoveException.class)
	public void exceedingTreeDepth() throws InvalidMoveException {
		List<String> players = new ArrayList<>();
		players.add("player1");
		Game game = new SearchTreeGame(players);
		JsonObject p1_left = new JsonObject();
		p1_left.addProperty("username", "player1");
		p1_left.addProperty("moveCode", 1);
		game.start();
		// move to left
		game.handleInput(p1_left);
		game.handleInput(p1_left);
		game.handleInput(p1_left);
		game.handleInput(p1_left);
	}

	/*
	 * trying to go up to parent at the root node
	 */@Test(expected = InvalidMoveException.class)
	public void cantGoUp() throws InvalidMoveException {
		List<String> players = new ArrayList<>();
		players.add("player1");
		Game game = new SearchTreeGame(players);
		JsonObject p1_up = new JsonObject();
		p1_up.addProperty("username", "player1");
		p1_up.addProperty("moveCode", 3);
		game.start();
		// move to left
		game.handleInput(p1_up);
	}

}
