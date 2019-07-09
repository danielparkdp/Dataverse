package edu.brown.cs.multiplayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GameHandler;
import edu.brown.cs.websocket.MESSAGE_TYPE;

/**
 * takes care of storing players who are searching for a random opponent and
 * assigning them a good match. Has its own thread that checks the queue for
 * people to put together.
 * 
 * @author camillo_stuff
 *
 */
public class MatchmakingQueue {
	private static final Gson GSON = new Gson();

	ScheduledExecutorService executor;

	// stores player object, sorted by elo.
	private ConcurrentSkipListSet<Player> usersInQueue = new ConcurrentSkipListSet<>();

	// shared between this and game message handler.
	private ConcurrentMap<Session, GameHandler> clientGameHandlers;

	private boolean running;

	public MatchmakingQueue(ConcurrentMap<Session, GameHandler> clientGameHandlers) {
		this.running = false;
		this.clientGameHandlers = clientGameHandlers;
	}

	public void addPlayer(Player p) {
		if (this.usersInQueue.contains(p)) {
			System.out.println("ERROR: " + p.toString() + " already in queue");
			return;
		}
		this.usersInQueue.add(p);
		// if the matchmaker was off turn it on
		if (!this.running && this.usersInQueue.size() > 1) {
			this.startUpExecutor();
		}
		// send everyone else who is also in the queue an update on how many ppl are in
		// the queue.
		this.sendNumPlayersMessage();
	}

	/**
	 * when a player clicks cancel they are removed from the queue without being
	 * assigned to a game
	 * 
	 * @param p
	 *            the player to be removed.
	 */
	public void removePlayer(Player p) {
		if (!this.usersInQueue.contains(p)) {
			System.out.println(
					"ERROR: Trying to remove a player from the random opponenent matchmaking who is not even in the queue");
			return;
		}
		this.usersInQueue.remove(p);
		this.sendNumPlayersMessage();
	}

	// if nobody is in the arena we should do this
	public void shutDownExecutor() {
		System.out.println("INFO: Shutting down Executor");
		this.executor.shutdown();
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	/**
	 * if there are people in the arena, it should be running
	 */
	public void startUpExecutor() {
		this.startMatchmakingLoop();
		this.running = true;
	}

	private void increaseWaitedTime() {
		for (Player p : usersInQueue) {
			p.updateTimeWaited();
		}
	}

	private void matchPlayers() {
		Set<Player> matchedPlayers = new HashSet<>();
		// assumes ordered from lowest to highest
		for (Player p : this.usersInQueue) {
			// if this player has already been assigned to a game, move on
			if (matchedPlayers.contains(p)) {
				continue;
			}
			// gets the closest player whose elo is greater than p's elo.
			// cant be equal to or else it will just return p
			Player nextHighest = this.usersInQueue.higher(p);
			if (nextHighest == null) {
				// if there is no such player then you can end the search prematurely because
				// there are no players with higher elo
				// basically the same as a break statement
				continue;
			}
			// if the next higehst player's elo is within a threshold of this player's, put
			// them in a game together.
			if (nextHighest.getElo() - p.getElo() < 20 * p.getTimeWaited()) {
				matchedPlayers.add(p);
				matchedPlayers.add(nextHighest);
				this.startMatchedPlayers(p, nextHighest);
			}
		}
		// remove all players that were matched
		this.usersInQueue.removeAll(matchedPlayers);
		// update the num players
		this.sendNumPlayersMessage();
	}

	private void startMatchedPlayers(Player p1, Player p2) {
		GameHandler handler = this.makeNewGameHandler(p1, p2);
		// assign the gamehandler to both people
		this.clientGameHandlers.put(p1.getSession(), handler);
		this.clientGameHandlers.put(p2.getSession(), handler);
		// start the game(should this go in its own thread)?
		System.out.println("Starting a game between " + p1.toString() + " and " + p2.toString());
		// handler.onStartMessage("Bin-apple Trees");
		this.sendLoadGameMessage(p1.getSession(), p2.getSession());
	}

	/**
	 * starts a new thread which is responsible for checking the list of people,
	 * recomputing their score, and removing people from the queue and putting them
	 * into game.
	 */
	private void startMatchmakingLoop() {
		Runnable findOpponents = new Runnable() {
			public void run() {
				try {
					MatchmakingQueue.this.increaseWaitedTime();
					MatchmakingQueue.this.matchPlayers();
					// if theres less than 2 people in queue shut down the matchmaking until more
					// ppl join
					if (MatchmakingQueue.this.usersInQueue.size() < 2) {
						MatchmakingQueue.this.shutDownExecutor();
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
		System.out.println("INFO: Starting up executor for matchmaking");
		// have to make a new executor after shutting down the old one.
		this.executor = Executors.newScheduledThreadPool(1);
		this.executor.scheduleAtFixedRate(findOpponents, 1, 1, TimeUnit.SECONDS);
	}

	private GameHandler makeNewGameHandler(Player p1, Player p2) {
		Map<Session, String> twoPlayerMap = new HashMap<>();
		twoPlayerMap.put(p1.getSession(), p1.getUsername());
		twoPlayerMap.put(p2.getSession(), p2.getUsername());
		return new GameHandler(twoPlayerMap);
	}

	/**
	 * sends a message to all the players in the queue to inform them how many
	 * people are in the queue This way they can gage if they will find a match
	 * soon.
	 */
	private void sendNumPlayersMessage() {
		int numPeople = this.usersInQueue.size();
		JsonObject msg = new JsonObject();
		msg.addProperty("type", MESSAGE_TYPE.RANDOM_OPPONENT.ordinal());
		msg.addProperty("numPlayers", numPeople);
		for (Player p : this.usersInQueue) {
			Session sess = p.getSession();
			sess.getRemote().sendStringByFuture(GSON.toJson(msg));
		}
	}

	private void sendLoadGameMessage(Session sess1, Session sess2) {
		MULTIPLAYER_GAMES gameType = this.chooseGame();
		JsonObject ready = new JsonObject();
		ready.addProperty("ready", true);
		ready.addProperty("gameType", gameType.ordinal());
		ready.addProperty("type", MESSAGE_TYPE.LOBBY_LOAD_GAME.ordinal());
		System.out.println("INFO: Sending LOAD Message from Queue. Awaiting START message");
		sess1.getRemote().sendStringByFuture(GSON.toJson(ready));
		sess2.getRemote().sendStringByFuture(GSON.toJson(ready));
	}

	/**
	 * 
	 * @return an enum representing one of the three multiplayer games.
	 */
	private MULTIPLAYER_GAMES chooseGame() {
		return MULTIPLAYER_GAMES.values()[new Random().nextInt(MULTIPLAYER_GAMES.values().length)];
	}

}
