package edu.brown.cs.logintest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.google.common.io.Files;

import edu.brown.cs.login.InfoWrapper;
import edu.brown.cs.login.Login;

/**
 * Tests for login class.
 *
 */
public class LoginTest {

	Login login;

	/**
	 * test construction.
	 */
	@Test
	public void testConstructor() {
		// to add

		String file = "data/final/test.sqlite3";

		login = new Login(file);

		// check to see if it is a file
		File f = new File(file);
		assertTrue(!(!f.isFile() || f.isDirectory() || !Files.getFileExtension(file).equals("sqlite3")));
		assertNotNull(login);

		login.resetTable();
		login.clear();
	}

	/**
	 * test add user.
	 */
	@Test
	public void testAddUser() {
		String file = "data/final/test.sqlite3";

		login = new Login(file);

		// to Add
		assertTrue(login.addUser("testuser", "testpass"));

		InfoWrapper info = login.userRow("testuser");

		assertTrue(info.getUser().equals("testuser"));
		assertTrue(info.getPass().equals(encrypt("testpass")));
		assertEquals(info.getStatus(), 0);

		// ensure can't add 2 same
		assertFalse(login.addUser("testuser", "testpass"));

		// ensure can add multiple though, without modifying previous
		assertTrue(login.addUser("testuser2", "testpass2"));

		info = login.userRow("testuser");

		assertTrue(info.getUser().equals("testuser"));
		assertTrue(info.getPass().equals(encrypt("testpass")));

		info = login.userRow("testuser2");

		assertTrue(info.getUser().equals("testuser2"));
		assertTrue(info.getPass().equals(encrypt("testpass2")));

		assertTrue(login.addUser(" ", " "));
		assertFalse(login.addUser("", ""));

		login.resetTable();
		login.clear();
	}

	/**
	 * test validate login.
	 */
	@Test
	public void testValidateLogin() {
		String file = "data/final/test.sqlite3";

		login = new Login(file);

		// to Add
		assertTrue(login.addUser("testuser", "testpass"));

		// ensure can add multiple though, without modifying previous
		assertTrue(login.addUser("testuser2", "testpass2"));

		// status will be -1 if validate fails.

		InfoWrapper info = login.validateLogin("testuser", "testpass");
		assertEquals(info.getStatus(), 0);

		// already logged in
		info = login.validateLogin("testuser", "testpass");
		assertEquals(info.getStatus(), -1);

		// wrong pass
		info = login.validateLogin("testuser2", "testpass");
		assertEquals(info.getStatus(), -1);

		login.resetTable();
		login.clear();
	}

	/**
	 * Test changing username password.
	 */
	@Test
	public void testChangeUserPass() {
		String file = "data/final/test.sqlite3";

		login = new Login(file);

		// to Add
		assertTrue(login.addUser("a", "a"));

		// ensure can add multiple
		assertTrue(login.addUser("b", "b"));

		InfoWrapper info = login.validateLogin("a", "a");

		info.setPinkship(1);
		info.setLinkedlist(100);

		assertFalse(login.changeUser(info, "b"));
		assertTrue(login.changeUser(info, "auser"));

		InfoWrapper second = login.userRow("a");
		assertEquals(second.getStatus(), -1);

		second = login.userRow("auser");
		assertEquals(second.getStatus(), 0);
		assertEquals(second.getPinkship(), 1);
		assertEquals(second.getLinkedlist(), 100);

		// reset password
		assertTrue(second.getPass().equals(encrypt("a")));

		assertTrue(login.changePass(second, "apass"));

		assertFalse(login.userRow("auser").getPass().equals(encrypt("a")));

		assertTrue(login.userRow("auser").getPass().equals(encrypt("apass")));

		login.resetTable();
		login.clear();
	}

	/**
	 * test updating at logout.
	 */
	@Test
	public void testLogoutUpdate() {
		String file = "data/final/test.sqlite3";

		login = new Login(file);

		// to Add
		assertTrue(login.addUser("d", "d"));

		// ensure can add multiple
		assertTrue(login.addUser("e", "e"));

		InfoWrapper info = login.validateLogin("d", "d");

		assertEquals(login.userRow("d").getStatus(), 1);
		assertEquals(login.validateLogin("d", "d").getStatus(), -1);

		login.logout("d");

		info = login.validateLogin("d", "d");

		assertEquals(info.getStatus(), 0);

		info.setPinkship(1);
		info.setLinkedlist(100);

		login.updateUserInfo(info);
		login.logout("d");

		info = login.userRow("d");

		assertEquals(info.getPinkship(), 1);
		assertEquals(info.getLinkedlist(), 100);
		assertEquals(info.getStatus(), 0);

		login.resetTable();
		login.clear();
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
