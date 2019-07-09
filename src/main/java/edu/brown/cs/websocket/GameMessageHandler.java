package edu.brown.cs.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.JsonObject;

import edu.brown.cs.game_generics.GAME_TYPE;
import edu.brown.cs.game_generics.GameHandler;
import edu.brown.cs.login.InfoWrapper;
import edu.brown.cs.multiplayer.LobbyException;
import edu.brown.cs.multiplayer.MatchmakingQueue;
import edu.brown.cs.multiplayer.MultiplayerLobby;
import edu.brown.cs.multiplayer.Player;

/**
 * contains all the code for responding to messages related to games. Will
 * register these commands with some sort of command manager.
 *
 */
public class GameMessageHandler {

	// all of these things need to be updated when people leave!!!!

	// each session has their own gamehandler
	// doesnt matter if its being accessed by multiple clients at once.
	private ConcurrentMap<Session, GameHandler> clientGameHandlers;

	// map from username to session so we know who is who.
	private Map<String, Session> userSessionMap;

	// map of creators to currently open lobbies.
	private Map<String, MultiplayerLobby> openLobbies;

	// keeps track of players that are "busy" i.e. in game or in lobby
	// maybe this should be concurrent
	private Set<String> busyPlayers;

	// Random Opponent Queue
	private MatchmakingQueue roq;

	/**
	 * GameMessageHandler constructor. Sets up everything needed to handle
	 * game-related messages sent from front-end.
	 * 
	 * @param wm
	 *            wm given
	 */
	public GameMessageHandler(MessageManager wm) {
		this.clientGameHandlers = new ConcurrentHashMap<>();
		this.roq = new MatchmakingQueue(clientGameHandlers);
		this.userSessionMap = new HashMap<>();
		this.openLobbies = new HashMap<>();
		this.busyPlayers = new HashSet<>();
		this.installMessages(wm);
	}

	/**
	 * associates each message with a specific enum value pattern that leads to its
	 * execution.
	 *
	 * @param wm
	 *            a websocket manager that will store these associations.
	 */
	public void installMessages(MessageManager wm) {
		wm.registerMessage(MESSAGE_TYPE.JOIN_ARENA, new JoinArena());
		wm.registerMessage(MESSAGE_TYPE.LEAVE_ARENA, new LeaveArena());
		wm.registerMessage(MESSAGE_TYPE.LOBBY_CREATE, new CreateLobby());
		wm.registerMessage(MESSAGE_TYPE.LOBBY_LEAVE, new LeaveLobby());
		wm.registerMessage(MESSAGE_TYPE.LOBBY_LOAD_GAME, new LoadMultiplayerGame());
		wm.registerMessage(MESSAGE_TYPE.LOBBY_INVITE, new InviteToLobby());
		wm.registerMessage(MESSAGE_TYPE.INVITE_RESPONSE, new InviteResponse());
		wm.registerMessage(MESSAGE_TYPE.LOBBY_LOAD_GAME, new LoadMultiplayerGame());
		wm.registerMessage(MESSAGE_TYPE.GAME_START, new StartGame());
		wm.registerMessage(MESSAGE_TYPE.GAME_ACTION, new GameAction());
		wm.registerMessage(MESSAGE_TYPE.GAME_LEAVE, new LeaveGame());
		wm.registerMessage(MESSAGE_TYPE.RANDOM_OPPONENT, new RandomOpponent());

	}

	/**
	 * Check if session is known to exist.
	 * 
	 * @param session
	 *            to check
	 * @return true or false
	 */
	private boolean checkIfSessionKnown(Session session) {
		if (this.clientGameHandlers.containsKey(session)) {
			return true;
		} else {
			System.out.println("ERROR: User must have started a game before they can make a move or leave the game");
			return false;
		}
	}

	/**
	 * Remove session based on given session.
	 * 
	 * @param sess
	 *            to remove
	 */
	public void removeSession(Session sess) {
		// IMPT: How do we remove a user from a lobby if they terminate their session
		// but we dont know which lobby they were in???
		String userToRemove = "";
		for (String username : this.userSessionMap.keySet()) {
			Session thatUserSession = this.userSessionMap.get(username);
			if (sess.equals(thatUserSession)) {
				userToRemove = username;
			}
		}
		// update values with remove
		this.userSessionMap.remove(userToRemove);
		this.openLobbies.remove(userToRemove);
		this.busyPlayers.remove(userToRemove);
		this.clientGameHandlers.remove(sess);
	}

	/**
	 * Join the Arena message handling. Private class to handle this message.
	 *
	 */
	private class JoinArena implements MessageManager.Message {
		/**
		 * execute joining arena based on received message and sess.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			String usernameInArena = receivedMsg.get("username").getAsString();
			userSessionMap.put(usernameInArena, sess);
		}
	}

	/**
	 * remove a player from being considered "in the arena".
	 *
	 */
	private class LeaveArena implements MessageManager.Message {
		/**
		 * execute removing a player from the arena info.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			String usernameInArena = receivedMsg.get("username").getAsString();
			userSessionMap.remove(usernameInArena);
			// technically this should never trigger
			GameMessageHandler.this.busyPlayers.remove(usernameInArena);
		}
	}

	/**
	 * Creating a lobby handled in this private class.
	 *
	 */
	private class CreateLobby implements MessageManager.Message {
		/**
		 * execute logic when creating a lobby is requested.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// create lobby by the person with this username
			String username = receivedMsg.get("username").getAsString();
			if (openLobbies.containsKey(username)) {
				System.err.println("ERROR: User already has lobby");
				return;
			} else {
				// build lobby
				MultiplayerLobby lobby = new MultiplayerLobby(username, GameMessageHandler.this.userSessionMap);
				GameMessageHandler.this.openLobbies.put(username, lobby);
				// the owner is now considered "busy"
				GameMessageHandler.this.busyPlayers.add(username);
			}
		}
	}

	/**
	 * private class for inviting people to lobby.
	 *
	 */
	private class InviteToLobby implements MessageManager.Message {

		/**
		 * execute method to handle logic of being invited to a lobby.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// owner sends invite to player
			String owner = receivedMsg.get("lobbyOwner").getAsString();
			// the person who is being invited
			String username = receivedMsg.get("invitee").getAsString();
			MultiplayerLobby lobby = GameMessageHandler.this.openLobbies.get(owner);
			lobby.invitePlayer(username, busyPlayers);
		}

	}

	/**
	 * Response to invite message is handled in this private class.
	 *
	 */
	private class InviteResponse implements MessageManager.Message {

		/**
		 * execute for handling when person responds to message invite.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			String owner = receivedMsg.get("lobbyOwner").getAsString();
			String username = receivedMsg.get("username").getAsString();
			boolean accepted = receivedMsg.get("accepted").getAsBoolean();
			MultiplayerLobby lobby = GameMessageHandler.this.openLobbies.get(owner);
			// handle based on whether they accepted or not.
			if (accepted) {
				lobby.acceptInvite(username);
				// the player is now considered "busy"
				GameMessageHandler.this.busyPlayers.add(username);
			} else {
				lobby.declineInvite(username);
			}

		}
	}

	/**
	 * this class just checks if the game can actually be started and if it can be
	 * then it sends the game that was chosen to all users DOES NOT actually start
	 * the game, I wait for a GAME_START command to do that. Also assigns the
	 * returned game handler as the gamehandler of all players.
	 *
	 */
	private class LoadMultiplayerGame implements MessageManager.Message {
		/**
		 * Load the game.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			String owner = receivedMsg.get("lobbyOwner").getAsString();
			MultiplayerLobby lobby = GameMessageHandler.this.openLobbies.get(owner);
			// create and handle loading game.
			try {
				GameHandler gh = lobby.loadIntroScreen();
				// only get here if loaded lobby success
				// assign each player the gamehandler for future move routing
				for (Session player : lobby.getPlayers()) {
					GameMessageHandler.this.clientGameHandlers.put(player, gh);
				}
				// destroy lobby and remove it from list of openLobbies
				lobby.selfDestruct();
				GameMessageHandler.this.openLobbies.remove(owner, lobby);
			} catch (LobbyException e) {
				System.err.println(e.getMessage());
				return;
			}
		}

	}

	/**
	 * There might be a way to exit a lobby. This class handles this.
	 *
	 */
	private class LeaveLobby implements MessageManager.Message {
		/**
		 * execute for exiting a lobby.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			String owner = receivedMsg.get("lobbyOwner").getAsString();
			String leaver = receivedMsg.get("leaver").getAsString();
			MultiplayerLobby lobby = GameMessageHandler.this.openLobbies.get(owner);
			// leaving person might be owner.
			if (owner.equals(leaver)) {
				// none of the players are "busy" any longer
				for (String user : lobby.getPlayerUsernames()) {
					GameMessageHandler.this.busyPlayers.remove(user);
				}
				this.destroyLobby(lobby, owner);
				return;
			} else {
				lobby.leaveLobby(leaver);
				// the player is no longer considered "busy"
				GameMessageHandler.this.busyPlayers.remove(leaver);
			}
		}

		// in the case that the owner leaves
		private void destroyLobby(MultiplayerLobby lobby, String owner) {
			// get rid of people who have this game as their gamehandler
			// although technically this shouldnt be allowed to happen
			// since this is only assigned once the game has started
			for (Session player : lobby.getPlayers()) {
				GameMessageHandler.this.clientGameHandlers.remove(player);
			}
			// remove owner.
			openLobbies.remove(owner);
			lobby.selfDestruct();
			lobby = null;
		}
	}

	/**
	 * Start the game message handling.
	 */
	private class StartGame implements MessageManager.Message {
		/**
		 * execute method for starting the game.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			String username = receivedMsg.get("username").getAsString();
			GameHandler handler;
			// case where person is connecting to a game for the first time
			if (!clientGameHandlers.containsKey(sess)) {
				handler = new GameHandler(sess, username);
				clientGameHandlers.put(sess, handler);
				// this player already has a handler
				// this might be deprecated
			} else {
				handler = clientGameHandlers.get(sess);
			}
			try {
				handler.onStartMessage(receivedMsg.get("gameName").getAsString());
				// this player is now busy
				GameMessageHandler.this.busyPlayers.add(username);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Game action message handling.
	 *
	 */
	private class GameAction implements MessageManager.Message {
		/**
		 * when action is taken from game, this execute handles logic.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// handle input action based on session
			GameHandler handler;
			if (GameMessageHandler.this.checkIfSessionKnown(sess)) {
				handler = GameMessageHandler.this.clientGameHandlers.get(sess);
				try {
					handler.onMoveMessage(receivedMsg.get("actionPayload").getAsJsonObject());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}

	/**
	 * Message manager for leaving a game state.
	 *
	 */
	private class LeaveGame implements MessageManager.Message {
		/**
		 * execute for leaving game.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			// get data from game
			String username = receivedMsg.get("username").getAsString();
			String type = receivedMsg.get("gametype").getAsString();
			int highscore = receivedMsg.get("highscore").getAsInt();

			// handle based on leaving session.
			GameHandler handler;
			if (GameMessageHandler.this.checkIfSessionKnown(sess)) {
				handler = GameMessageHandler.this.clientGameHandlers.get(sess);
				try {
					handler.onLeaveMessage(WebSocket.userMap.get(sess), sess);
					// remove the session and handler from the map. This seems like the safest
					// option now that we have multiplayer games
					// however, this could be easily changed later if we want to have functionality
					// like "play again"
					GameMessageHandler.this.clientGameHandlers.remove(sess);
					// the player is no longer busy
					GameMessageHandler.this.busyPlayers.remove(username);
					handler = null;
					// update data, returning updatescore message
					int hs = this.updateScores(WebSocket.userMap.get(sess), type, highscore);

					JsonObject toRet = new JsonObject();
					toRet.addProperty("type", MESSAGE_TYPE.UPDATESCORE.ordinal());
					toRet.addProperty("value", hs);

					sess.getRemote().sendString(toRet.toString());

				} catch (IOException e) {
					System.err.println("ERROR: leaving game error");
				}
			}
		}

		/**
		 * update scores based on given data.
		 * 
		 * @param info
		 *            info given
		 * @param type
		 *            game type
		 * @param hs
		 *            new high score
		 * @return int of new high score
		 */
		private int updateScores(InfoWrapper info, String type, int hs) {

			int newhs = 0;
			// handle based on game type
			if (type.equals("Choco Chip Links")) {
				// LL game, update values stored in infowrapper
				if (info.getLinkedlist() < hs) {
					info.setLinkedlist(hs);
					newhs = hs;
				} else {
					newhs = info.getLinkedlist();
				}
				if (info.getHighScore(GAME_TYPE.LINKED_LIST) < hs) {
					info.setHighScore(GAME_TYPE.LINKED_LIST, hs);
				}

			} else if (type.equals("Barbe-Queue Rush")) {
				// do the same for SQ game
				if (info.getStackqueue() < hs) {
					info.setStackqueue(hs);
					newhs = hs;
				} else {
					newhs = info.getStackqueue();
				}
				if (info.getHighScore(GAME_TYPE.QUEUE_STACK) < hs) {
					info.setHighScore(GAME_TYPE.QUEUE_STACK, hs);
				}
			} else if (type.equals("Bin-apple Trees")) {
				// BT game
				if (info.getBinarytree() < hs) {
					info.setBinarytree(hs);
					newhs = hs;
				} else {
					newhs = info.getBinarytree();
				}
				if (info.getHighScore(GAME_TYPE.SEARCH_TREE) < hs) {
					info.setHighScore(GAME_TYPE.SEARCH_TREE, hs);
				}
			} else if (type.equals("Build A Tree")) {
				// Build tree game
				if (info.getBuildtree() < hs) {
					info.setBuildtree(hs);
					newhs = hs;
				} else {
					newhs = info.getBuildtree();
				}
				if (info.getHighScore(GAME_TYPE.BUILD_TREE) < hs) {
					info.setHighScore(GAME_TYPE.BUILD_TREE, hs);
				}
			} else if (type.equals("Candy Hash Saga")) {
				// hashmap game
				if (info.getHashmap() < hs) {
					info.setHashmap(hs);
					newhs = hs;
				} else {
					newhs = info.getHashmap();
				}
				if (info.getHighScore(GAME_TYPE.HASHMAP) < hs) {
					info.setHighScore(GAME_TYPE.HASHMAP, hs);
				}
			}
			// return new HS, could be the same as old one
			return newhs;

		}
	}

	/**
	 * private class for random opponent message handling.
	 *
	 */
	private class RandomOpponent implements MessageManager.Message {
		/**
		 * execute for getting random opponent.
		 */
		@Override
		public void execute(JsonObject receivedMsg, Session sess) {
			boolean join = receivedMsg.get("join").getAsBoolean();
			// i technically need the session--> infowrapper map
			InfoWrapper playerInfo = WebSocket.userMap.get(sess);
			Player p = new Player(playerInfo, sess);
			String username = p.getUsername();
			if (join) {
				// marking the player as busy
				GameMessageHandler.this.busyPlayers.add(username);
				GameMessageHandler.this.roq.addPlayer(p);
			} else {
				// remove from queue
				GameMessageHandler.this.busyPlayers.remove(username);
				GameMessageHandler.this.roq.removePlayer(p);
			}
		}

	}

}
