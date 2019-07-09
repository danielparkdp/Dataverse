package edu.brown.cs.queuestack_test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.Game;
import edu.brown.cs.game_generics.InvalidMoveException;
import edu.brown.cs.queuestackgame.QueueStackGame;

/**
 * Tests for LinkedList Game class.
 *
 */
public class QueueStackGameTest {

	Game qsgame;

	private static final int MAX = 4;
	private static final int MIN = 1;

	/**
	 * test construction.
	 */
	@Test
	public void testConstructor() {
		qsgame = new QueueStackGame();

		assertNotNull(qsgame);

	}

	/**
	 * test generation of burgers.
	 */
	@Test
	public void testGenerate() {

		qsgame = new QueueStackGame();
		qsgame.start();

		assertTrue(((QueueStackGame) qsgame).getBurger().size() >= MIN + 2);
		assertTrue(((QueueStackGame) qsgame).getBurger().size() <= MAX + 2);
	}

	/**
	 * test score.
	 */
	@Test
	public void testValidateAndScore() {

		qsgame = new QueueStackGame();
		qsgame.start();
		assertTrue(((QueueStackGame) qsgame).getScore() == 0);

		List<String> burg = ((QueueStackGame) qsgame).getBurger();

		JsonObject js = new JsonObject();

		JsonArray ar = new JsonArray();

		if (((QueueStackGame) qsgame).getStack()) {
			for (int i = 0; i < burg.size(); i++) {
				ar.add(burg.get(i));
			}
		} else {
			for (int i = burg.size() - 1; i >= 0; i--) {
				ar.add(burg.get(i));
			}
		}

		js.add("burger", ar);

		try {
			qsgame.handleInput(js);
		} catch (InvalidMoveException e) {
			System.err.println("ERROR:burger wrong");
		}

		assertTrue(((QueueStackGame) qsgame).getScore() > 0);

	}

}
