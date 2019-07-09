package edu.brown.cs.multiplayer;

public class LobbyException extends Exception {

	private String msg;

	public LobbyException(String error) {
		this.msg = error;
	}

	@Override
	public String getMessage() {
		return this.msg;
	}

}
