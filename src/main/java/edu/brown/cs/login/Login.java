package edu.brown.cs.login;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Login class for logging in and out of the game. Manages database entries and
 * updates.
 *
 */
public class Login {

	private Connection conn1;

	// add in order of: user, password, status, starbucks, redship, blueship,
	// purpleship, goldship, firstitem, seconditem, speed, elo, linkedlist,
	// stackqueue, hashmap, binarytree, buildtree.
	private static final int STATUS = 3, STARBUCKS = 4, REDSHIP = 5, BLUESHIP = 6, PURPLESHIP = 7, GOLDSHIP = 8,
			FIRSTITEM = 9, SECONDITEM = 10, SPEED = 11, ELO = 12, LINKEDLIST = 13, STACKQUEUE = 14, HASHMAP = 15,
			BINARYTREE = 16, BUILDTREE = 17;

	// defined values
	private static final int INITIALSPEED = 5;
	private static final int INITIALELO = 1200;
	private static final int INITIALMONEY = 200;

	/**
	 * Constructor for when we get connection to build off of.
	 *
	 * @param file
	 *            connection
	 */
	public Login(String file) {
		assert (file != null);

		// Set up a connection and store it in a field
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: Class not found");
			return;
		}
		String url = "jdbc:sqlite:" + file;
		try {
			// close current connection if not null
			if (conn1 != null) {
				conn1.close();
			}
			// makes file if not found, setting up conn
			conn1 = DriverManager.getConnection(url);
			Statement stat = conn1.createStatement();
			stat.executeUpdate("PRAGMA foreign_keys = ON;");
			stat.close();
		} catch (SQLException e) {
			// catch possible error
			if (conn1 != null) {
				try {
					conn1.close();
				} catch (SQLException e1) {
					System.err.println("ERROR: SQL setup error in login");
				}
			}
			conn1 = null;
			System.err.println("ERROR: SQL setup error in login");
			return;
		}
		// create the table itself.
		this.createTable();

	}

	/**
	 * Create table for login.
	 */
	private void createTable() {
		// set query
		String query;

		// Create a PreparedStatement
		PreparedStatement prep = null;

		try {

			// update to create table
			StringBuilder s = new StringBuilder();

			// add in order of: user, password, status, starbucks, redship, blueship,
			// purpleship, goldship, firstitem, seconditem, speed, elo, linkedlist,
			// stackqueue, hashmap, binarytree, buildtree.
			s.append("CREATE TABLE IF NOT EXISTS \"login\" ( \"user\" TEXT, ");
			s.append("\"password\" TEXT, \"status\" INTEGER, ");

			// money
			s.append("\"starbucks\" INTEGER, ");

			// ship types, 0 for not owned, 1 for owned, 2 for equipped
			s.append("\"redship\" INTEGER, \"blueship\" INTEGER, ");
			s.append("\"purpleship\" INTEGER,  \"goldship\" INTEGER, ");

			// progress of account
			s.append("\"firstitem\" INTEGER,  \"seconditem\" INTEGER, ");
			s.append("\"speed\" INTEGER,  \"elo\" INTEGER, ");

			// game progress
			s.append("\"linkedlist\" INTEGER, \"stackqueue\" INTEGER, ");
			s.append("\"hashmap\" INTEGER, \"binarytree\" INTEGER, ");
			s.append("\"buildtree\" INTEGER, PRIMARY KEY(\"user\"));");

			query = s.toString();

			prep = conn1.prepareStatement(query);

			// Execute the update
			prep.executeUpdate();

			prep.close();

		} catch (SQLException e) {
			System.err.println("ERROR: Create Table sql error");
		} finally {
			try {
				// Close the ResultSet and the PreparedStatement
				if (prep != null) {
					prep.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: Create Table sql error");
			}
		}
	}

	/**
	 * Add new user.
	 *
	 * @param user
	 *            username
	 * @param pass
	 *            password
	 * @return boolean of success or fail
	 */
	public boolean addUser(String user, String pass) {
		// don't allow empty username
		if (user.length() == 0) {
			return false;
		}

		// set query to check if it already exists
		String query = "SELECT * FROM login WHERE user = ?";

		// Create a PreparedStatement
		PreparedStatement prep = null;
		ResultSet rs = null;

		// adding a user
		try {
			if (conn1 == null) {
				System.err.println("Unable to access connection");
				return false;
			}
			prep = conn1.prepareStatement(query);

			prep.setString(1, user);

			rs = prep.executeQuery();

			while (rs.next()) {
				return false;
			}

			prep.close();
			rs.close();

			// now try inserting into database
			query = "INSERT INTO login VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ".concat("?, ?, ?, ?, ?, ?, ?, ?);");

			prep = conn1.prepareStatement(query);

			// base values
			prep.setString(1, user);
			prep.setString(2, encrypt(pass));
			prep.setInt(STATUS, 0);
			prep.setInt(STARBUCKS, INITIALMONEY);
			prep.setInt(REDSHIP, 2);
			prep.setInt(BLUESHIP, 0);
			prep.setInt(PURPLESHIP, 0);
			prep.setInt(GOLDSHIP, 0);
			prep.setInt(FIRSTITEM, 0);
			prep.setInt(SECONDITEM, 0);

			prep.setInt(SPEED, INITIALSPEED);
			prep.setInt(ELO, INITIALELO);
			prep.setInt(LINKEDLIST, 0);
			prep.setInt(STACKQUEUE, 0);
			prep.setInt(HASHMAP, 0);
			prep.setInt(BINARYTREE, 0);
			prep.setInt(BUILDTREE, 0);

			// Execute the update
			prep.executeUpdate();

		} catch (SQLException e) {
			System.err.println("ERROR: AddUser sql error " + e.getMessage());
		} finally {
			try {
				// Close the ResultSet and the PreparedStatement
				if (prep != null) {
					prep.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: AddUser sql error " + e.getMessage());
			}
		}

		return true;

	}

	/**
	 * Validates login and updates login status.
	 *
	 * @param user
	 *            username
	 * @param pass
	 *            password given
	 * @return Wrapper containing the login's info, or null if failed.
	 */
	public InfoWrapper validateLogin(String user, String pass) {

		// set query
		String query = "SELECT * FROM login WHERE user = ?;";

		// Create a PreparedStatement
		PreparedStatement prep = null;
		ResultSet rs = null;
		boolean access = false;

		// base infowrapper
		InfoWrapper wrap = new InfoWrapper("", "", -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

		try {

			prep = conn1.prepareStatement(query);

			prep.setString(1, user);

			// Execute the update
			rs = prep.executeQuery();

			while (rs.next()) {

				// validate the password and the status (ensure not logged in right now)
				if (rs.getString(2).equals(encrypt(pass)) && rs.getInt(STATUS) == 0) {
					access = true;

					// contain user data
					wrap = new InfoWrapper(user, pass, rs.getInt(STATUS), rs.getInt(STARBUCKS), rs.getInt(REDSHIP),
							rs.getInt(BLUESHIP), rs.getInt(PURPLESHIP), rs.getInt(GOLDSHIP), rs.getInt(FIRSTITEM),
							rs.getInt(SECONDITEM), rs.getInt(SPEED), rs.getInt(ELO), rs.getInt(LINKEDLIST),
							rs.getInt(STACKQUEUE), rs.getInt(HASHMAP), rs.getInt(BINARYTREE), rs.getInt(BUILDTREE));
				} else {
					access = false;
				}
			}

			prep.close();
			rs.close();

			if (!access) {
				return wrap;
			}

			// also need to update to know that this user is logged on
			query = "UPDATE login SET status = 1 WHERE user = ?;";

			prep = conn1.prepareStatement(query);

			prep.setString(1, user);

			prep.executeUpdate();

		} catch (SQLException e) {
			System.err.println("ERROR: Validating Login sql error");
		} finally {
			try {
				// Close the ResultSet and the PreparedStatement
				if (prep != null) {
					prep.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: Validating Login sql error");
			}
		}
		return wrap;
	}

	/**
	 * Change the user's username.
	 * 
	 * @param oldinfo
	 *            old data
	 * @param newUser
	 *            new username
	 * @return bool of success or fail
	 */
	public boolean changeUser(InfoWrapper oldinfo, String newUser) {

		if (!addUser(newUser, oldinfo.getPass())) {
			return false;
		}

		// remove the previous row
		String query = "DELETE FROM login WHERE user = ?;";

		// Create a PreparedStatement
		PreparedStatement prep = null;

		try {

			prep = conn1.prepareStatement(query);

			// update all these values
			prep.setString(1, oldinfo.getUser());

			prep.executeUpdate();

			prep.close();

		} catch (SQLException e) {
			System.err.println("ERROR: DELETE User Info error");
			return false;
		} finally {
			try {
				// Close the PreparedStatement
				if (prep != null) {
					prep.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: DELETE User Info error");
				return false;
			}
		}
		// change the username
		oldinfo.setUser(newUser);

		this.updateUserInfo(oldinfo);

		return true;
	}

	/**
	 * change password.
	 * 
	 * @param oldinfo
	 *            old data
	 * @param newPass
	 *            new password
	 * @return boolean success fail
	 */
	public boolean changePass(InfoWrapper oldinfo, String newPass) {

		// update to reset data
		StringBuilder s = new StringBuilder();

		s.append("UPDATE \"login\" SET password = ? ");
		s.append("WHERE user = ?");

		// set query
		String query = s.toString();

		// Create a PreparedStatement
		PreparedStatement prep = null;

		try {

			prep = conn1.prepareStatement(query);

			// update all these values including new pass
			prep.setString(1, encrypt(newPass));
			prep.setString(2, oldinfo.getUser());
			prep.executeUpdate();

			prep.close();

		} catch (SQLException e) {
			System.err.println("ERROR: Update Pass Info error");
			return false;
		} finally {
			try {
				// Close the PreparedStatement
				if (prep != null) {
					prep.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: Update Pass Info error");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updating user's database row when logging out.
	 *
	 * @param userInfo
	 *            info to pass in
	 */
	public void updateUserInfo(InfoWrapper userInfo) {

		// update to create table
		StringBuilder s = new StringBuilder();

		// add in order of: user, password, status, starbucks, redship, blueship,
		// purpleship, goldship, firstitem, seconditem, speed, elo, linkedlist,
		// stackqueue, hashmap, binarytree, buildtree.
		s.append("UPDATE \"login\" SET status = ?, starbucks = ?, redship = ?, ");
		s.append("blueship = ?, purpleship = ?, goldship = ?, firstitem = ?, ");
		s.append("seconditem = ?, speed = ?, elo = ?, linkedlist = ?, ");
		s.append("stackqueue = ?, hashmap = ?, binarytree = ?, buildtree = ? ");
		s.append("WHERE user = ?");

		// set query
		String query = s.toString();

		// Create a PreparedStatement
		PreparedStatement prep = null;

		try {

			prep = conn1.prepareStatement(query);

			// update all these values
			prep.setInt(1, userInfo.getStatus());
			prep.setInt(2, userInfo.getStarbucks());
			prep.setInt(3, userInfo.getRedship());
			prep.setInt(4, userInfo.getBlueship());
			prep.setInt(5, userInfo.getPurpleship());
			prep.setInt(6, userInfo.getGoldship());
			prep.setInt(7, userInfo.getGreenship());
			prep.setInt(8, userInfo.getPinkship());
			prep.setInt(9, userInfo.getSpeed());
			prep.setInt(10, userInfo.getElo());
			prep.setInt(11, userInfo.getLinkedlist());
			prep.setInt(12, userInfo.getStackqueue());
			prep.setInt(13, userInfo.getHashmap());
			prep.setInt(14, userInfo.getBinarytree());
			prep.setInt(15, userInfo.getBuildtree());
			prep.setString(16, userInfo.getUser());

			prep.executeUpdate();

			prep.close();

		} catch (SQLException e) {
			System.err.println("ERROR: Update User Info error");
		} finally {
			try {
				// Close the PreparedStatement
				if (prep != null) {
					prep.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: Update User Info error");
			}
		}
	}

	/**
	 * Logs out by updating status.
	 *
	 * @param user
	 *            username
	 */
	public void logout(String user) {
		// set query
		String query = "UPDATE login SET status = 0 WHERE user = ?;";

		// Create a PreparedStatement
		PreparedStatement prep = null;

		try {

			prep = conn1.prepareStatement(query);

			prep.setString(1, user);

			prep.executeUpdate();

			prep.close();

		} catch (SQLException e) {
			System.err.println("ERROR: Update Logout error");
		} finally {
			try {
				// Close the PreparedStatement
				if (prep != null) {
					prep.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: Update Logout error");
			}
		}
	}

	/**
	 * Method used for testing.
	 * 
	 * @param user
	 *            name to find
	 * @return info of that user
	 */
	public InfoWrapper userRow(String user) {

		// set query
		String query = "SELECT * FROM login WHERE user = ?;";

		// Create a PreparedStatement
		PreparedStatement prep = null;
		ResultSet rs = null;

		// base infowrapper
		InfoWrapper wrap = new InfoWrapper("", "", -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

		try {

			prep = conn1.prepareStatement(query);

			prep.setString(1, user);

			// Execute the update
			rs = prep.executeQuery();

			while (rs.next()) {

				// contain user data
				wrap = new InfoWrapper(user, rs.getString(2), rs.getInt(STATUS), rs.getInt(STARBUCKS),
						rs.getInt(REDSHIP), rs.getInt(BLUESHIP), rs.getInt(PURPLESHIP), rs.getInt(GOLDSHIP),
						rs.getInt(FIRSTITEM), rs.getInt(SECONDITEM), rs.getInt(SPEED), rs.getInt(ELO),
						rs.getInt(LINKEDLIST), rs.getInt(STACKQUEUE), rs.getInt(HASHMAP), rs.getInt(BINARYTREE),
						rs.getInt(BUILDTREE));

			}

			prep.close();
			rs.close();

		} catch (SQLException e) {
			System.err.println("ERROR: Access user sql error");
		} finally {
			try {
				// Close the ResultSet and the PreparedStatement
				if (prep != null) {
					prep.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: Access user sql error");
			}
		}
		return wrap;
	}

	/**
	 * Method used for testing.
	 */
	public void resetTable() {

		// set query
		String query = "DELETE FROM login;";

		// Create a PreparedStatement
		PreparedStatement prep = null;

		try {

			prep = conn1.prepareStatement(query);

			// Execute the update
			prep.executeUpdate();

			prep.close();

		} catch (SQLException e) {
			System.err.println("ERROR: Access user sql error");
		} finally {
			try {
				// Close the ResultSet and the PreparedStatement
				if (prep != null) {
					prep.close();
				}
			} catch (SQLException e) {
				System.err.println("ERROR: Access user sql error");
			}
		}
	}

	/**
	 * close everything when finished, or when resetting.
	 */
	public void clear() {
		try {
			if (conn1 != null) {
				conn1.close();
			}
		} catch (SQLException e) {
			System.err.println("ERROR: closing login conn error");
		}
	}

	/**
	 * Encrypts a user's password using the SHA-256 hashing algorithm, through
	 * Java's MessageDigest class.
	 *
	 * @param password
	 *            password to encrypt.
	 * @return Encrypted password.
	 */
	private static String encrypt(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			StringBuilder encodedPassword = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				encodedPassword.append(hex);
			}
			return encodedPassword.toString();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Unable to load SHA-256");
			return password;

		}

	}

}
