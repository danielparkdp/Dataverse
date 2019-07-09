package edu.brown.cs.multiplayer;

import java.util.Objects;

import org.eclipse.jetty.websocket.api.Session;

import edu.brown.cs.login.InfoWrapper;

/**
 * one of these is made each time a player joins the arena.
 * 
 * @author camillo_stuff
 *
 */
public class Player implements Comparable<Player> {

	private String username;
	private int elo;
	private int timeWaited;
	private Session sess;

	// should i make a player object with an infowrapper?

	//
	public Player(InfoWrapper allInfo, Session s) {
		this.username = allInfo.getUser();
		this.elo = allInfo.getElo() + (int) (Math.random() * 100);
		this.timeWaited = 0;
		this.sess = s;
	}

	public int getElo() {
		return this.elo;
	}

	public String getUsername() {
		return this.username;
	}

	public int getTimeWaited() {
		return this.timeWaited;
	}

	public Session getSession() {
		return this.sess;
	}

	public void updateTimeWaited() {
		this.timeWaited++;
	}

	@Override
	public int compareTo(Player other) {
		if (other.getUsername().equals(this.username)) {
			return 0;
		} else if (other.getElo() > this.elo) {
			return -1;
		} else {
			return 1;
		}
	}

	@Override
	public String toString() {
		return "Player " + this.username + ", ELO: " + Integer.toString(this.elo) + ", TIME: "
				+ Integer.toString(this.timeWaited);
	}

	@Override
	/**
	 * this isnt being called, a concurrentskiplistset uses compareTo rather than
	 * equals
	 */
	public boolean equals(Object o) {
		return this.username.equals(((Player) o).getUsername());
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}

}
