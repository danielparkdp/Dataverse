package edu.brown.cs.login;

import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.game_generics.GAME_TYPE;

/**
 * InfoWrapper class to represent a person's data. Used to cache info from db.
 */
public class InfoWrapper {

	// Value indices are as follows: STATUS = 3, STARBUCKS = 4, REDSHIP = 5,
	// BLUESHIP = 6, PURPLESHIP = 7, GOLDSHIP = 8, FIRSTITEM = 9,
	// SECONDITEM = 10, SPEED = 11, ELO = 12, LINKEDLIST = 13, STACKQUEUE = 14,
	// HASHMAP = 15, BINARYTREE = 16, BUILDTREE = 17;

	private String user;
	private String pass;
	private int status;
	private int starbucks;
	private int redship;
	private int blueship;
	private int purpleship;
	private int goldship;
	private int firstitem;
	private int seconditem;
	private int speed;
	private int elo;
	private int linkedlist;
	private int stackqueue;
	private int hashmap;
	private int binarytree;
	private int buildtree;

	private Map<GAME_TYPE, Integer> highScores;

	/**
	 * InfoWrapper constructor.
	 *
	 * @param u
	 *            user
	 * @param p
	 *            pass
	 * @param stat
	 *            status
	 * @param money
	 *            money owned
	 * @param red
	 *            ship owned and or used
	 * @param blue
	 *            ship owned and or used
	 * @param purple
	 *            ship owned and or used
	 * @param gold
	 *            ship owned and or used
	 * @param first
	 *            item owned
	 * @param second
	 *            item owned
	 * @param sp
	 *            speed
	 * @param el
	 *            elo rating
	 * @param ll
	 *            top score on ll game
	 * @param sq
	 *            top score on sq game
	 * @param hash
	 *            top score on hash game
	 * @param bint
	 *            top score on bint game
	 * @param buildt
	 *            top score on buildt game
	 */
	public InfoWrapper(String u, String p, int stat, int money, int red, int blue, int purple, int gold, int first,
			int second, int sp, int el, int ll, int sq, int hash, int bint, int buildt) {
		user = u;
		pass = p;
		status = stat;
		starbucks = money;
		redship = red;
		blueship = blue;
		purpleship = purple;
		goldship = gold;
		firstitem = first;
		seconditem = second;
		speed = sp;
		elo = el;
		linkedlist = ll;
		stackqueue = sq;
		hashmap = hash;
		binarytree = bint;
		buildtree = buildt;
		// initializing high scores to 0.
		this.highScores = new HashMap<>();
		for (GAME_TYPE type : GAME_TYPE.values()) {
			highScores.put(type, 0);
		}
	}

	/**
	 * Access User.
	 *
	 * @return user string
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Access Password.
	 *
	 * @return password string
	 */
	public String getPass() {
		return pass;
	}

	public int getHighScore(GAME_TYPE game) {
		return this.highScores.get(game);
	}

	public void setHighScore(GAME_TYPE game, int newScore) {
		try {
			this.highScores.replace(game, newScore);
		} catch (Exception e) {
			System.out.println("ERROR: For some reason the high scores hashmap isnt being initialized correctly,"
					+ " or you are trying to change the high score of a game that doesnt exit");
		}
	}

	/**
	 * Access money.
	 *
	 * @return starbucks int
	 */
	public int getStarbucks() {
		return starbucks;
	}

	/**
	 * 
	 * @param more
	 * @return updates and returns the number of starbucks someone has.
	 */
	public int increaseStarbucks(int more) {
		this.starbucks += more;
		return this.starbucks;
	}

	/**
	 * Access Status.
	 *
	 * @return status int
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Access red ship status.
	 *
	 * @return red ship owned/used
	 */
	public int getRedship() {
		return redship;
	}

	/**
	 * Access ship status.
	 *
	 * @return ship owned/used
	 */
	public int getBlueship() {
		return blueship;
	}

	/**
	 * Access ship status.
	 *
	 * @return ship owned/used
	 */
	public int getPurpleship() {
		return purpleship;
	}

	/**
	 * Access ship status.
	 *
	 * @return ship owned/used
	 */
	public int getGoldship() {
		return goldship;
	}

	/**
	 * Access item status.
	 *
	 * @return item owned/used
	 */
	public int getGreenship() {
		return firstitem;
	}

	/**
	 * Access item status.
	 *
	 * @return item owned/used
	 */
	public int getPinkship() {
		return seconditem;
	}

	/**
	 * Access speed status.
	 *
	 * @return speed owned/used
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * Access elo status.
	 *
	 * @return elo rating
	 */
	public int getElo() {
		return elo;
	}

	/**
	 * Access game high score.
	 *
	 * @return game score
	 */
	public int getLinkedlist() {
		return linkedlist;
	}

	/**
	 * Access game high score.
	 *
	 * @return game score
	 */
	public int getStackqueue() {
		return stackqueue;
	}

	/**
	 * Access game high score.
	 *
	 * @return game score
	 */
	public int getHashmap() {
		return hashmap;
	}

	/**
	 * Access game high score.
	 *
	 * @return game score
	 */
	public int getBinarytree() {
		return binarytree;
	}

	/**
	 * Access game high score.
	 *
	 * @return game score
	 */
	public int getBuildtree() {
		return buildtree;
	}

	/**
	 * map of rockets access.
	 * 
	 * @return maps of rockets.
	 */
	public Map<String, Integer> rockets() {
		Map<String, Integer> rocketMap = new HashMap<>();
		rocketMap.put("red", redship);
		rocketMap.put("blue", blueship);
		rocketMap.put("gold", goldship);
		rocketMap.put("purple", purpleship);
		rocketMap.put("green", firstitem);
		rocketMap.put("pink", seconditem);
		return rocketMap;
	}

	/**
	 * update rockets.
	 * 
	 * @param rockets
	 */
	public void updateRockets(Map<String, Integer> rockets) {
		redship = rockets.containsKey("red") ? rockets.get("red") : redship;
		blueship = rockets.containsKey("blue") ? rockets.get("blue") : blueship;
		goldship = rockets.containsKey("gold") ? rockets.get("gold") : goldship;
		purpleship = rockets.containsKey("purple") ? rockets.get("purple") : purpleship;
		firstitem = rockets.containsKey("green") ? rockets.get("green") : firstitem;
		seconditem = rockets.containsKey("pink") ? rockets.get("pink") : seconditem;
	}

	/**
	 * Set User.
	 *
	 * @param us
	 *            string
	 */
	public void setUser(String us) {
		user = us;
	}

	/**
	 * Set Password.
	 *
	 * @param pa
	 *            string
	 */
	public void setPass(String pa) {
		pass = pa;
	}

	/**
	 * Set money.
	 *
	 * @param st
	 *            int
	 */
	public void setStarbucks(int st) {
		starbucks = st;
	}

	/**
	 * Set Status.
	 *
	 * @param stat
	 *            int
	 */
	public void setStatus(int stat) {
		status = stat;
	}

	/**
	 * Set item status.
	 *
	 * @param item
	 *            owned/used
	 */
	public void setGreenship(int item) {
		firstitem = item;
	}

	/**
	 * Set item status.
	 *
	 * @param item
	 *            owned/used
	 */
	public void setPinkship(int item) {
		seconditem = item;
	}

	/**
	 * Set speed status.
	 *
	 * @param sp
	 *            owned/used
	 */
	public void setSpeed(int sp) {
		speed = sp;
	}

	/**
	 * Set elo status.
	 *
	 * @param el
	 *            rating
	 */
	public void setElo(int el) {
		elo = el;
	}

	/**
	 * Set game high score.
	 *
	 * @param game
	 *            score
	 */
	public void setLinkedlist(int game) {
		linkedlist = game;
	}

	/**
	 * Set game high score.
	 *
	 * @param game
	 *            score
	 */
	public void setStackqueue(int game) {
		stackqueue = game;
	}

	/**
	 * Set game high score.
	 *
	 * @param game
	 *            score
	 */
	public void setHashmap(int game) {
		hashmap = game;
	}

	/**
	 * Set game high score.
	 *
	 * @param game
	 *            score
	 */
	public void setBinarytree(int game) {
		binarytree = game;
	}

	/**
	 * Set game high score.
	 *
	 * @param game
	 *            score
	 */
	public void setBuildtree(int game) {
		buildtree = game;
	}
}
