package edu.brown.cs.linkedlist_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.Game;
import edu.brown.cs.game_generics.InvalidMoveException;
import edu.brown.cs.linkedlistgame.MultiplayerLinkedList;

/**
 * Tests for LinkedList Game class.
 *
 */
public class LinkedListGameTest {

	Game llgame;

	private static final int SIZE = 20;
	private static final int MAX = 100;

	/**
	 * test construction.
	 */
	@Test
	public void testConstructor() {
		Set<String> players = new HashSet<String>();
		players.add("a");
		llgame = new MultiplayerLinkedList(players);

		assertNotNull(llgame);

		players.add("b");
		llgame = new MultiplayerLinkedList(players);

		assertNotNull(llgame);

	}

	/**
	 * test generation of linked lists.
	 */
	@Test
	public void testGenerate() {
		Set<String> players = new HashSet<String>();
		players.add("a");
		players.add("b");
		llgame = new MultiplayerLinkedList(players);
		// calls set up game
		llgame.start();

		List<Integer> list = ((MultiplayerLinkedList) llgame).getList();
		assertEquals(list.size(), SIZE);

		Set<Integer> used = new HashSet<Integer>();
		for (int i : list) {
			assertFalse(used.contains(i));
			used.add(i);
			assertTrue(i > 0);
			assertTrue(i <= MAX);
		}

	}

	/**
	 * test moves within linked list game by players.
	 */
	@Test
	public void testInput() {
		Set<String> players = new HashSet<String>();
		players.add("a");
		players.add("b");
		llgame = new MultiplayerLinkedList(players);
		// calls set up game
		llgame.start();

		JsonObject move = new JsonObject();

		// 1 is left, 2 right, 4 validate
		move.addProperty("moveCode", 2);
		move.addProperty("username", "a");

		Map<String, Integer> pos = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(pos.get("a") == 0);

		try {
			llgame.handleInput(move);
		} catch (InvalidMoveException e) {
			System.err.println("ERROR: invalid move");
		}

		pos = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(pos.get("a") == 1);

		move = new JsonObject();

		// 1 is left, 2 right, 4 validate
		move.addProperty("moveCode", 1);
		move.addProperty("username", "a");

		try {
			llgame.handleInput(move);
		} catch (InvalidMoveException e) {
			System.err.println("ERROR: invalid move");
		}

		pos = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(pos.get("a") == 0);

		move = new JsonObject();

		// 1 is left, 2 right, 4 validate
		move.addProperty("moveCode", 2);
		move.addProperty("username", "b");

		try {
			llgame.handleInput(move);
		} catch (InvalidMoveException e) {
			System.err.println("ERROR: invalid move");
		}

		pos = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(pos.get("a") == 0);
		assertTrue(pos.get("b") == 1);

	}

	/**
	 * test singleplayer.
	 */
	@Test
	public void testSingleplayer() {
		Set<String> players = new HashSet<String>();
		players.add("a");
		llgame = new MultiplayerLinkedList(players);
		// calls set up game
		llgame.start();

		JsonObject move = new JsonObject();

		// 1 is left, 2 right, 4 validate
		move.addProperty("moveCode", 2);
		move.addProperty("username", "a");

		Map<String, Integer> pos = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(pos.get("a") == 0);

		try {
			llgame.handleInput(move);
		} catch (InvalidMoveException e) {
			System.err.println("ERROR: invalid move");
		}

		pos = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(pos.get("a") == 1);

		move = new JsonObject();

		// 1 is left, 2 right, 4 validate
		move.addProperty("moveCode", 1);
		move.addProperty("username", "a");

		try {
			llgame.handleInput(move);
		} catch (InvalidMoveException e) {
			System.err.println("ERROR: invalid move");
		}

		pos = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(pos.get("a") == 0);

	}

	/**
	 * test base scoring.
	 */
	@Test
	public void testScore() {
		Set<String> players = new HashSet<String>();
		players.add("a");
		players.add("b");
		llgame = new MultiplayerLinkedList(players);
		// calls set up game
		llgame.start();

		Map<String, Integer> scores = ((MultiplayerLinkedList) llgame).getPositions();
		assertTrue(scores.get("a") == 0);
		assertTrue(scores.get("b") == 0);

	}

}
