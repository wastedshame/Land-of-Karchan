/*-------------------------------------------------------------------------
svninfo: $Id$
Maarten's Mud, WWW-based MUD using MYSQL
Copyright (C) 1998  Maarten van Leunen

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Maarten van Leunen
Appelhof 27
5345 KA Oss
Nederland
Europe
maarten_l@yahoo.com
-------------------------------------------------------------------------*/

package mmud.characters;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import mmud.Constants;
import mmud.MudException;
import mmud.Sex;
import mmud.database.Database;
import mmud.database.MudDatabaseException;
import mmud.rooms.Area;
import mmud.rooms.Room;
import simkin.Executable;
import simkin.ExecutableContext;
import simkin.ExecutableIterator;
import simkin.FieldNotSupportedException;
import simkin.MethodNotSupportedException;

/**
 * Collection class containing all persons at the moment active in the game. Can
 * contain not only users, but also bots and the like. As long as the base class
 * is Person.
 * 
 * @see mmud.characters.Person
 */
public final class Persons implements Executable
{
	private static Vector<Person> thePersons = new Vector<Person>();

	public static Persons create()
	{
		return new Persons();
	}

	/**
	 * Returns a string describing the contents.
	 */
	public static String getDescription()
	{
		if (thePersons == null)
		{
			throw new RuntimeException("Persons is null!");
		}
		return "Persons amount (Capacity) = " + thePersons.size() + "("
				+ thePersons.capacity() + ")<BR>";
	}

	/**
	 * Get the number of characters that are cached. Both NPCs as PCs.
	 * 
	 * @param int, containing the amount of characters in the cache.
	 */
	public static int getSize()
	{
		return thePersons.size();
	}

	/**
	 * Initialise this object by retrieving all persons from the database that
	 * are playing the game.
	 * 
	 * @see mmud.database.Database#getPersons
	 */
	public static void init() throws MudException
	{
		Logger.getLogger("mmud").finer("");
		thePersons = Database.getPersons();
	}

	/**
	 * Default constructor.
	 */
	public Persons()
	{
		Logger.getLogger("mmud").finer("");
	}

	/**
	 * retrieve the character from the list of characters currently active in
	 * the game.
	 * 
	 * @param aName
	 *            name of the character to search for.
	 * @return Person object containing all relevant information of the
	 *         character. Will return null pointer if character not active in
	 *         the game.
	 */
	public static Person retrievePerson(String aName)
	{
		if (thePersons == null)
		{
			throw new RuntimeException("thePersons vector was null.");
		}
		Logger.getLogger("mmud").finer("aName=" + aName);
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar != null)
					&& (myChar.getName().compareToIgnoreCase(aName) == 0))
			{
				return myChar;
			}
		}
		return null;
	}

	/**
	 * clean all characters from the list of active players that have been
	 * inactive for more than an hour.
	 * 
	 * @throws MudException
	 */
	public static void removeIdleUsers() throws MudException
	{
		if (thePersons == null)
		{
			throw new RuntimeException("thePersons vector was null.");
		}
		Logger.getLogger("mmud").finer("");
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar != null) && (myChar instanceof User))
			{
				User myUser = (User) myChar;
				if (myUser.isIdleTooLong())
				{
					Persons.sendMessage(myUser,
							"%SNAME fade%VERB2 slowly from existence.<BR>\r\n");
					try
					{
						deactivateUser(myUser);
						Database.writeLog(myUser.getName(), "idled out.");
					} catch (MudDatabaseException e)
					{
						Constants.logger.throwing("mmud.characters.Persons",
								"removeIdleUsers", e);
						// ignore it, so we cannot remove idle users, so what.
					}
				}
			}
		}
	}

	/**
	 * activate a character
	 * 
	 * @throws PersonException
	 *             if something is wrong
	 * @param aName
	 *            String containing the name of the Person
	 * @param aPassword
	 *            String containing the password of the Person
	 * @param anAddress
	 *            String containing the ip address (more likely the hostname) of
	 *            the user logging in.
	 * @param aCookie
	 *            String containing the session password. It is possible that
	 *            this is "null", if the user was not logged in before.
	 * @return User containing all information. If the class would not be user
	 *         but a Person, it means that it is not a valid player in the game,
	 *         but more likely a bot.
	 */
	public static User activateUser(String aName, String aPassword,
			String anAddress, String aCookie) throws PersonException,
			MudException
	{
		Logger.getLogger("mmud").finer(
				"aName=" + aName + ",aPassword=" + aPassword + ",aCookie="
						+ aCookie);
		Person myChar = retrievePerson(aName);
		if ((myChar != null) && (!(myChar instanceof User)))
		{
			// found, but no user
			Logger.getLogger("mmud").info("thrown: " + Constants.NOTAUSERERROR);
			throw new NotAUserException();
		}
		User myUser = (myChar == null ? null : (User) myChar);
		if (myUser == null)
		{
			myUser = Database.getUser(aName, aPassword);
			if (myUser == null)
			{
				Logger.getLogger("mmud").info(
						"thrown: " + Constants.USERNOTFOUNDERROR);
				throw new UserNotFoundException();
			}
		} else
		{
			if (myUser.getPassword() == null)
			{
				User tempUser = Database.getActiveUser(aName, aPassword);
				if ((tempUser == null) || (!tempUser.verifyPassword(aPassword)))
				{
					Logger.getLogger("mmud").info(
							"thrown: " + Constants.PWDINCORRECTERROR);
					throw new PwdIncorrectException();
				}
				myUser.setPassword(aPassword);
			}
			if ((aCookie != null)
					&& (!aCookie.equals(myUser.getSessionPassword()))
					&& !aCookie.equals(""))
			{
				Logger.getLogger("mmud").info(
						"thrown: " + Constants.MULTIUSERERROR);
				throw new MultiUserException();
			}
			if (!myUser.verifyPassword(aPassword))
			{
				Logger.getLogger("mmud").info(
						"thrown: " + Constants.PWDINCORRECTERROR);
				throw new PwdIncorrectException();
			}
			Logger.getLogger("mmud").info(
					"thrown: " + Constants.USERALREADYACTIVEERROR);
			throw new UserAlreadyActiveException();
		}

		if (!myUser.verifyPassword(aPassword))
		{
			Logger.getLogger("mmud").info(
					"thrown: " + Constants.PWDINCORRECTERROR);
			throw new PwdIncorrectException();
		}
		// everything seems to be okay
		myUser.setAddress(anAddress);
		Database.activateUser(myUser);
		myUser.activate();
		thePersons.addElement(myUser);
		return myUser;
	}

	/**
	 * deactivate a character (usually because someone typed quit.)
	 * 
	 * @param aUser
	 *            the player to be deactivated
	 * @throws PersonException
	 *             if something is wrong
	 */
	public static void deactivateUser(User aUser) throws PersonException,
			MudDatabaseException
	{
		Logger.getLogger("mmud").finer("aUser=" + aUser);
		Database.deactivateUser(aUser);
		thePersons.remove(aUser);
	}

	/**
	 * create a new character
	 * 
	 * @throws PersonException
	 *             if something is wrong
	 * @param aName
	 *            the name of the character
	 * @param aPassword
	 *            the password of the character
	 * @param anAddress
	 *            the address of the computer connecting
	 * @param aRealName
	 *            the real name of the person behind the keyboard.
	 * @param aEmail
	 *            an email address of the person
	 * @param aTitle
	 *            the title of the character
	 * @param aRace
	 *            the race of the character
	 * @param aSex
	 *            the gender of the character (male or female)
	 * @param aAge
	 *            the age of the character (young, very young, old, very old,
	 *            etc.)
	 * @param aLength
	 *            the length of the character (ex. tall)
	 * @param aWidth
	 *            the width of the character (ex. athletic)
	 * @param aComplexion
	 *            the complexion of the character (ex. dark-skinned)
	 * @param aEyes
	 *            the eye colour of the character (ex. blue-eyed)
	 * @param aFace
	 *            the face of the character (ex. dimple-faced)
	 * @param aHair
	 *            the hair of the character (ex. black-haired)
	 * @param aBeard
	 *            the beard of the character (ex. with ponytail)
	 * @param aArms
	 *            the arms of the character (ex. long-armed)
	 * @param aLegs
	 *            the legs of the character (ex. long-legged)
	 * @param aCookie
	 *            the sessionpassword
	 * @return User object
	 */
	public static User createUser(String aName, String aPassword,
			String anAddress, String aRealName, String aEmail, String aTitle,
			String aRace, Sex aSex, String aAge, String aLength, String aWidth,
			String aComplexion, String aEyes, String aFace, String aHair,
			String aBeard, String aArms, String aLegs, String aCookie)
			throws PersonException, MudException
	{
		Logger.getLogger("mmud").finer(
				"aName=" + aName + ",aPassword=" + aPassword + ",anAddress="
						+ anAddress + ",aRealName=" + aRealName + ",aEmail="
						+ aEmail + ",aTitle=" + aTitle + ",aRace=" + aRace
						+ ",aSex=" + aSex + ",aAge=" + aAge + ",aLength="
						+ aLength + ",aWidth=" + aWidth + ",aComplexion="
						+ aComplexion + ",aEyes=" + aEyes + ",aFace=" + aFace
						+ ",aHair=" + aHair + ",aBeard=" + aBeard + ",aArms="
						+ aArms + ",aLegs=" + aLegs + ",aCookie=" + aCookie);
		if (Database.existsUser(aName))
		{
			Logger.getLogger("mmud").info(
					"thrown: " + Constants.USERALREADYEXISTSERROR);
			throw new UserAlreadyExistsException();
		}
		// everything seems to be okay
		User myUser = new User(aName, aPassword, anAddress, aRealName, aEmail,
				aTitle, aRace, aSex, aAge, aLength, aWidth, aComplexion, aEyes,
				aFace, aHair, aBeard, aArms, aLegs, aCookie);
		Database.createUser(myUser);
		myUser.activate();
		thePersons.addElement(myUser);
		return myUser;
	}

	/**
	 * Returns a description of everyone visible in a room.
	 * 
	 * @param aRoom
	 *            the room of which the description is required.
	 * @param aUser
	 *            the user (in most cases we want a description of everyone in
	 *            the room, except ourselves.
	 * @return String containing the description of everyone visible in the
	 *         room.
	 * @throws MudException
	 *             if the room is not correct
	 */
	public static String descriptionOfPersonsInRoom(Room aRoom, User aUser)
			throws MudException
	{
		Logger.getLogger("mmud").finer("aRoom=" + aRoom + ",aUser=" + aUser);
		String myOutput = new String();

		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar.getRoom() == aRoom) && (myChar != aUser))
			{
				myOutput = myOutput + "A " + myChar.getRace()
						+ " called <A HREF=\""
						+ aUser.getUrl("look+at+" + myChar.getName()) + "\">"
						+ myChar.getName() + "</A> is here.<BR>\r\n";
			}
		}
		return myOutput;
	}

	/**
	 * paging all users
	 * 
	 * @param aMessage
	 *            message to be sent to all users.
	 */
	public static void sendWall(String aMessage)
	{
		Logger.getLogger("mmud").finer("aMessage=" + aMessage);
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			myChar.writeMessage(aMessage);
		}
	}

	/**
	 * paging all guild members. If the Guild provided is null, no guild message
	 * is sent.
	 * 
	 * @param aGuild
	 *            the guild to which the user must belong
	 * @param aMessage
	 *            message to be sent to all users.
	 * @param aPerson
	 *            the person sending the guild message.
	 */
	public static void sendGuildMessage(Person aPerson, Guild aGuild,
			String aMessage) throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aGuild=" + aGuild + ",aMessage=" + aMessage);
		if (aGuild == null)
		{
			// no messages if no guild is provided
			return;
		}
		for (int i = 0; i < thePersons.size(); i++)
		{
			if (thePersons.elementAt(i) instanceof User)
			{
				// it has to be a user, that is the only
				// one that can be the member of a guild.
				User myChar = (User) thePersons.elementAt(i);
				if (aGuild.equals(myChar.getGuild()))
				{
					// only write the message if the proper guild member
					myChar.writeMessage("<FONT COLOR=green>[guild] " + aMessage
							+ "</FONT>");
				}
			}
		}
	}

	/**
	 * character communication method to everyone in the room. The message is
	 * parsed, based on who is sending the message.
	 * 
	 * @param aPerson
	 *            the person who is the source of the message.
	 * @param aMessage
	 *            the message
	 * @throws MudException
	 *             if the room is not correct
	 * @see mmud.characters.Person#writeMessage(mmud.characters.Person,java.lang.String)
	 */
	public static void sendMessage(Person aPerson, String aMessage)
			throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson + ",aMessage=" + aMessage);
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar.getRoom() == aPerson.getRoom()))
			{
				myChar.writeMessage(aPerson, aMessage);
			}
		}
	}

	/**
	 * room communication method to everyone in the room. The message is not
	 * parsed. Bear in mind that this method should only be used for
	 * communication about environmental issues. If the communication originates
	 * from a User/Person, you should use sendMessage(aPerson, aMessage).
	 * Otherwise the Ignore functionality will be omitted.
	 * 
	 * @param aRoom
	 *            the room that is to display the message. If the room is null,
	 *            do not do anything.
	 * @param aMessage
	 *            the message
	 * @throws MudException
	 *             if the room is not correct
	 * @see mmud.characters.Person#writeMessage(java.lang.String)
	 */
	public static void sendMessage(Room aRoom, String aMessage)
			throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aRoom=" + aRoom + ",aMessage=" + aMessage);
		if (aRoom == null)
		{
			return;
		}
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar.getRoom() == aRoom))
			{
				myChar.writeMessage(aMessage);
			}
		}
	}

	/**
	 * room communication method to everyone in the room. The message is not
	 * parsed.
	 * 
	 * @param aPerson
	 *            the person that wishes to communicate in a room.
	 * @param aRoom
	 *            the room that is to display the message. If the room is null,
	 *            do not do anything.
	 * @param aMessage
	 *            the message
	 * @throws MudException
	 *             if the room is not correct
	 * @see mmud.characters.Person#writeMessage(java.lang.String)
	 */
	public static void sendMessage(Person aPerson, Room aRoom, String aMessage)
			throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aRoom=" + aRoom + ",aMessage=" + aMessage);
		if (aRoom == null)
		{
			return;
		}
		if (aPerson == null)
		{
			throw new RuntimeException("person is null");
		}
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar.getRoom() == aRoom))
			{
				myChar.writeMessage(aPerson, aMessage);
			}
		}
	}

	/**
	 * character communication method to everyone in the room excluded the
	 * person mentioned in the parameters. The message is parsed, based on who
	 * is sending the message.
	 * 
	 * @param aPerson
	 *            the person who is the source of the message.
	 * @param aMessage
	 *            the message
	 * @throws MudException
	 *             if the room is not correct
	 * @see mmud.characters.Person#writeMessage(mmud.characters.Person,java.lang.String)
	 */
	public static void sendMessageExcl(Person aPerson, String aMessage)
			throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson + ",aMessage=" + aMessage);
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar.getRoom() == aPerson.getRoom()) && myChar != aPerson)
			{
				myChar.writeMessage(aPerson, aMessage);
			}
		}
	}

	/**
	 * character communication method to everyone in the room. The first person
	 * is the source of the message. The second person is the target of the
	 * message. The message is parsed based on the source and target.
	 * 
	 * @param aPerson
	 *            the person doing the communicatin'.
	 * @param aSecondPerson
	 *            the person communicated to.
	 * @param aMessage
	 *            the message to be sent
	 * @throws MudException
	 *             if the room is not correct
	 * @see mmud.characters.Person#writeMessage(mmud.characters.Person,
	 *      mmud.characters.Person,java.lang.String)
	 */
	public static void sendMessage(Person aPerson, Person aSecondPerson,
			String aMessage) throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson + ",aSecondPerson=" + aSecondPerson
						+ ",aMessage=" + aMessage);
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar.getRoom() == aPerson.getRoom()))
			{
				myChar.writeMessage(aPerson, aSecondPerson, aMessage);
			}
		}
	}

	/**
	 * character communication method to everyone in the room except to the two
	 * persons mentioned in the header. The message is parsed based on the
	 * source and target.
	 * 
	 * @param aPerson
	 *            the person doing the communicatin'.
	 * @param aSecondPerson
	 *            the person communicated to.
	 * @param aMessage
	 *            the message to be sent
	 * @throws MudException
	 *             if the room is not correct
	 * @see mmud.characters.Person#writeMessage(mmud.characters.Person,
	 *      mmud.characters.Person,java.lang.String)
	 */
	public static void sendMessageExcl(Person aPerson, Person aSecondPerson,
			String aMessage) throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson + ",aSecondPerson=" + aSecondPerson
						+ ",aMessage=" + aMessage);
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if ((myChar.getRoom() == aPerson.getRoom()) && myChar != aPerson
					&& myChar != aSecondPerson)
			{
				myChar.writeMessage(aPerson, aSecondPerson, aMessage);
			}
		}
	}

	/**
	 * returns a list of persons currently playing the game
	 * 
	 * @return String containing who's who.
	 */
	public static String getWhoList() throws MudException
	{
		Logger.getLogger("mmud").finer("");
		String myString = "<UL>";
		int count = 0;
		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if (myChar instanceof User)
			{
				User myUser = (User) myChar;
				Area myArea = myChar.getRoom().getArea();
				myString += "<LI>" + myUser.getName() + ", "
						+ myUser.getTitle();
				if (!myArea.getName().equals("Main"))
				{
					myString += " in " + myArea.getShortDescription();
				}
				myString += (myUser.isaSleep() ? ", sleeping " : " ")
						+ myUser.getIdleTime() + "\r\n";
				count++;
			}
		}
		myString = "<H2>List of All Users</H2><I>There are " + count
				+ " persons active in the game.</I><P>" + myString + "</UL>";
		return myString;
	}

	/**
	 * Standard tostring implementation.
	 * 
	 * @return String containing the characters in the list.
	 */
	@Override
	public String toString()
	{
		String myOutput = new String();

		for (int i = 0; i < thePersons.size(); i++)
		{
			Person myChar = (Person) thePersons.elementAt(i);
			if (myChar != null)
			{
				myOutput = myOutput + myChar + ",";
			}
		}
		return myOutput;
	}

	public void setValue(String field_name, String attrib_name, Object value,
			ExecutableContext ctxt) throws FieldNotSupportedException
	{
		Logger.getLogger("mmud").finer(
				"field_name=" + field_name + ", atttrib_name=" + attrib_name
						+ ", value=" + value + "[" + value.getClass() + "]");
		throw new FieldNotSupportedException(field_name + " not found.");
	}

	public void setValueAt(Object array_index, String attrib_name,
			Object value, ExecutableContext ctxt)
	{
		Logger.getLogger("mmud").finer(
				"array_index=" + array_index + ", atttrib_name=" + attrib_name
						+ ", value=" + value);
	}

	public ExecutableIterator createIterator()
	{
		Logger.getLogger("mmud").finer("");
		return null;
	}

	public ExecutableIterator createIterator(String qualifier)
	{
		Logger.getLogger("mmud").finer("qualifier=" + qualifier);
		return createIterator();
	}

	public Hashtable getAttributes()
	{
		Logger.getLogger("mmud").finer("");
		return null;
	}

	public Hashtable getInstanceVariables()
	{
		Logger.getLogger("mmud").finer("");
		return null;
	}

	public String getSource(String location)
	{
		Logger.getLogger("mmud").finer("location=" + location);
		return null;
	}

	public Object getValue(String field_name, String attrib_name,
			ExecutableContext ctxt) throws FieldNotSupportedException
	{
		Logger.getLogger("mmud").finer(
				"field_name=" + field_name + ", atttrib_name=" + attrib_name);
		throw new FieldNotSupportedException(field_name + " not found.");
	}

	public Object getValueAt(Object array_index, String attrib_name,
			ExecutableContext ctxt)
	{
		Logger.getLogger("mmud").finer(
				"array_index=" + array_index + ", atttrib_name=" + attrib_name);
		return null;
	}

	public Object method(String method_name, Object[] arguments,
			ExecutableContext ctxt) throws MethodNotSupportedException
	{
		Logger.getLogger("mmud").finer(
				"method_name=" + method_name + ", arguments=" + arguments);
		if (method_name.equals("find"))
		{
			if (arguments.length == 1)
			{
				if (!(arguments[0] instanceof String))
				{
					throw new MethodNotSupportedException(method_name
							+ " does not contain a String as argument.");
				}
				return retrievePerson((String) arguments[0]);
			}
		}
		throw new MethodNotSupportedException(method_name + " not found.");
	}

	/**
	 * Returns the iterator so we can cycle through all available players.
	 * 
	 * @return Iterator for cycling through all available players.
	 */
	public static Iterator getIterator()
	{
		return thePersons.iterator();
	}

	/**
	 * Retrieve a person whose name corresponds to aName. The person can either
	 * be playing or not (this can be checked with the isActive() method) and
	 * can be a bot or mob or user or whatever.
	 * 
	 * @see Person#isActive()
	 * @param aName
	 *            the name of the character to be retrieved
	 * @return Person object containing hopefully all necessary stuff.
	 */
	public static Person getPerson(String aName) throws MudException
	{
		Person toChar2 = Persons.retrievePerson(aName);
		if (toChar2 == null)
		{
			// the person is not playing the game...
			// we'll need to look him up "outside"
			return Database.getUser(aName, null);
		}
		return toChar2;
	}

}
