package gametests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Test;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.Game;
import edu.brown.cs.game_generics.InvalidMoveException;
import edu.brown.cs.hashmapgame.HashMapGame;

/**
 * intended to test basic generic game functionality. Hard to do without also
 * testing specifics of game because Game class is abstract.
 * 
 * @author camillo_stuff
 *
 */
public class GenericsTest {

	@Test(expected = InvalidMoveException.class)
	public void makeInvalidMove() throws InvalidMoveException {
		// have to instantiate an actual class
		Game g = new HashMapGame(new ArrayList<String>());
		g.handleInput(null);
	}

	@Test
	public void setUpGame() {
		Game g = new HashMapGame(new ArrayList<String>());
		g.start();
		assertNotNull(g.maxTime());
		assertEquals(g.getType(), GAME_TYPE.HASHMAP);
	}

}
