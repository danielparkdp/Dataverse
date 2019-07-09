package edu.brown.cs.shop;

import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.login.InfoWrapper;

public class ShopHandler {

	private InfoWrapper info1;

	private static final String GREEN = "Sequel Green";
	private static final String PINK = "The PHPink";
	private static final String SPEED = "Speed Upgrade";
	private static final String PURPLE = "The Purple Python";
	private static final String BLUE = "The Blue Java";
	private static final String GOLD = "GOLD++";

	private static final int SPEEDUPGRADE = 5;

	private static final Map<String, Integer> prices = new HashMap<String, Integer>() {
		private static final long serialVersionUID = -3431060567443404758L;

		{
			put(GREEN, 50);
			put(PINK, 50);
			put(SPEED, 100);
			put(PURPLE, 200);
			put(BLUE, 200);
			put(GOLD, 1000);
		}
	};

	public ShopHandler(InfoWrapper input) {
		info1 = input;
	}

	/**
	 * Validates purchase.
	 *
	 * @return the amount expected to have after purchase. If negative, not valid.
	 */
	public boolean canBuy(String item) {
		int starbucks = info1.getStarbucks();

		if (!prices.containsKey(item) || prices.get(item) > starbucks) {
			return false;
		}

		if (item.equals(GREEN)) {
			if (info1.getGreenship() != 0) {
				return false;
			}
		} else if (item.equals(PINK)) {
			if (info1.getPinkship() != 0) {
				return false;
			}
		} else if (item.equals(SPEED)) {
			if (info1.getSpeed() >= 20) {
				return false;
			}
		} else if (item.equals(PURPLE)) {
			if (info1.getPurpleship() != 0) {
				return false;
			}
		} else if (item.equals(BLUE)) {
			if (info1.getBlueship() != 0) {
				return false;
			}
		} else if (item.equals(GOLD)) {
			if (info1.getGoldship() != 0) {
				return false;
			}
		}
		return true;
	}

	public InfoWrapper buy(String item) {
		info1.setStarbucks(info1.getStarbucks() - prices.get(item));

		if (item.equals(GREEN)) {
			Map<String, Integer> rockets = new HashMap<String, Integer>();
			rockets.put("red", info1.getRedship());
			rockets.put("blue", info1.getBlueship());
			rockets.put("purple", info1.getPurpleship());
			rockets.put("green", 1);
			rockets.put("pink", info1.getPinkship());
			rockets.put("gold", info1.getGoldship());
			info1.updateRockets(rockets);
		} else if (item.equals(PINK)) {
			Map<String, Integer> rockets = new HashMap<String, Integer>();
			rockets.put("red", info1.getRedship());
			rockets.put("blue", info1.getBlueship());
			rockets.put("purple", info1.getPurpleship());
			rockets.put("green", info1.getGreenship());
			rockets.put("pink", 1);
			rockets.put("gold", info1.getGoldship());
			info1.updateRockets(rockets);
		} else if (item.equals(SPEED)) {
			info1.setSpeed(info1.getSpeed() + SPEEDUPGRADE);
		} else if (item.equals(PURPLE)) {
			Map<String, Integer> rockets = new HashMap<String, Integer>();
			rockets.put("red", info1.getRedship());
			rockets.put("blue", info1.getBlueship());
			rockets.put("purple", 1);
			rockets.put("green", info1.getGreenship());
			rockets.put("pink", info1.getPinkship());
			rockets.put("gold", info1.getGoldship());
			info1.updateRockets(rockets);
		} else if (item.equals(BLUE)) {
			Map<String, Integer> rockets = new HashMap<String, Integer>();
			rockets.put("red", info1.getRedship());
			rockets.put("blue", 1);
			rockets.put("purple", info1.getPurpleship());
			rockets.put("green", info1.getGreenship());
			rockets.put("pink", info1.getPinkship());
			rockets.put("gold", info1.getGoldship());
			info1.updateRockets(rockets);
		} else if (item.equals(GOLD)) {
			Map<String, Integer> rockets = new HashMap<String, Integer>();
			rockets.put("red", info1.getRedship());
			rockets.put("blue", info1.getBlueship());
			rockets.put("purple", info1.getPurpleship());
			rockets.put("green", info1.getGreenship());
			rockets.put("pink", info1.getPinkship());
			rockets.put("gold", 1);
			info1.updateRockets(rockets);
		}

		return info1;
	}

}
