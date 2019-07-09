package multiplayer_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;

import edu.brown.cs.multiplayer.MultiplayerLobby;
import edu.brown.cs.websocket.WebSocket;

public class LobbyTest {

	private MultiplayerLobby lobby;

	@Before
	public void SetUpLobby() throws URISyntaxException {
		WebSocket fakeWebSocket = new WebSocket();
		Map<String, Session> fakeUsers = new HashMap<>();
		// Session ownerSess = new WebSocketSession(new
		// WebSocketClient(),null,null,null);
		// Session fakeSess1 = new ClSession();
		// Session fakeSess2 = new WebSocketSession(new WebSocketClient(), null, null,
		// null);
		fakeUsers.put("joe", null);
		// fakeUsers.put("bill", fakeSess1);
		fakeUsers.put("bob", null);
		this.lobby = new MultiplayerLobby("joe", fakeUsers);
	}

	@Test
	public void makeNewLobby() {
		assertNotNull(lobby);
		assertEquals(lobby.getPlayerUsernames().toArray()[0], "joe");
	}

	@Test
	public void inviteAndAccept() {
		// no busy players
		Set<String> busyPlayers = new HashSet<>();
		try {
			lobby.invitePlayer("bill", busyPlayers);
		} catch (NullPointerException e) {
			// dont do anything, this is expected since im not giving it real sessions to
			// send back to
		}
		// players should only have lobby owner
		Collection<String> acceptedPlayerUsernames = new HashSet<>();
		acceptedPlayerUsernames.add("joe");
		assertEquals(new HashSet<>(lobby.getPlayerUsernames()), acceptedPlayerUsernames);
		try {
			lobby.acceptInvite("bill");
		} catch (NullPointerException e) {
			// ignore
		}
		acceptedPlayerUsernames.remove("joe");
		acceptedPlayerUsernames.add("bill");
		assertEquals(new HashSet<>(lobby.getPlayerUsernames()), acceptedPlayerUsernames);
	}

	@Test
	public void inviteAndDecline() {
		// no busy players
		Set<String> busyPlayers = new HashSet<>();
		try {
			lobby.invitePlayer("bill", busyPlayers);
		} catch (NullPointerException e) {
			// dont do anything, this is expected since im not giving it real sessions to
			// send back to
		}
		// players should only have lobby owner
		Collection<String> acceptedPlayerUsernames = new HashSet<>();
		acceptedPlayerUsernames.add("joe");
		assertEquals(new HashSet<>(lobby.getPlayerUsernames()), acceptedPlayerUsernames);
		try {
			lobby.declineInvite("bill");
		} catch (NullPointerException e) {
			// ignore
		}
		assertEquals(new HashSet<>(lobby.getPlayerUsernames()), acceptedPlayerUsernames);
	}

	@Test
	public void inviteBusyPlayer() {
		Set<String> busyPlayers = new HashSet<>();
		// bill is busy
		busyPlayers.add("bill");
		try {
			lobby.invitePlayer("bill", busyPlayers);
		} catch (NullPointerException e) {
			// dont do anything, this is expected since im not giving it real sessions to
			// send back to
		}
		// dont want to write an accessor for invited players that will only be used by
		// tests...
		assertTrue(!lobby.getPlayerUsernames().contains("bill"));

	}

	@Test
	public void ownerLeavesLobby() {
		try {
			this.lobby.leaveLobby("joe");
		} catch (NullPointerException e) {
			// ignore
		}
		assertTrue(this.lobby.getPlayerUsernames().size() == 0);

	}

	@Test
	public void playerLeavesLobby() {
		// no busy players
		Set<String> busyPlayers = new HashSet<>();
		try {
			lobby.invitePlayer("bill", busyPlayers);
		} catch (NullPointerException e) {
			// dont do anything, this is expected since im not giving it real sessions to
			// send back to
		}
		try {
			lobby.acceptInvite("bill");
		} catch (NullPointerException e) {
			// ignore
		}
		try {
			this.lobby.leaveLobby("bill");
		} catch (NullPointerException e) {
			// ignore
		}
		assertTrue(this.lobby.getPlayerUsernames().size() == 0);

	}

	@Test
	/**
	 * terminating a lobby after if it is empty or owner leaves
	 */
	public void destroyLobby() {
		try {
			this.lobby.selfDestruct();
		} catch (NullPointerException e) {
			// ignore
		}

	}

}
