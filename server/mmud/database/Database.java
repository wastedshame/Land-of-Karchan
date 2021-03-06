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
package mmud.database;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import mmud.Constants;
import mmud.MudException;
import mmud.Sex;
import mmud.UserCommandInfo;
import mmud.boards.Board;
import mmud.boards.BoardFormatEnum;
import mmud.characters.CharacterFactory;
import mmud.characters.Guild;
import mmud.characters.GuildFactory;
import mmud.characters.GuildRank;
import mmud.characters.Macro;
import mmud.characters.Person;
import mmud.characters.Persons;
import mmud.characters.User;
import mmud.characters.UserNotFoundException;
import mmud.rooms.Area;
import mmud.rooms.Room;
import mmud.rooms.RoomNotFoundException;
import mmud.rooms.Rooms;

/**
 * The central class that takes care of all the commands that are to be
 * transmitted to the MySQL database server. This database class is basically
 * the class that effectively hides the database side of things and only
 * provides standard mud functions to the rest of the mud.
 */
public class Database
{

	private static Connection theConnection = null;
	private static String sqlSetGuild = "update mm_guilds " + "set title = ?, "
			+ "minguildlevel = ?, " + "guilddescription= ?, "
			+ "guildurl = ?, logonmessage = ?, bossname = ?, "
			+ "active = ? where name = ?";
	private static String sqlGetGuild = "select * from mm_guilds "
			+ "where name = ?";
	private static String sqlGetGuildRanks = "select * from mm_guildranks "
			+ "where guildname = ?";
	private static String sqlAddGuildRank = "replace into mm_guildranks "
			+ "(title, guildlevel, guildname) " + "values(?, ?, ?)";
	private static String sqlDelGuildRank = "delete from mm_guildranks "
			+ "where guildname = ? and guildlevel = ?";
	private static String sqlGetIgnoreList = "select * from mm_ignore "
			+ "where toperson = ?";
	private static String sqlAddIgnore = "replace into mm_ignore "
			+ "(fromperson, toperson) " + "values(?, ?)";
	private static String sqlDelIgnore = "delete from mm_ignore "
			+ "where fromperson = ? and toperson = ?";
	private static String sqlGetGuildMembers = "select name from mm_usertable where guild = ?";
	private static String sqlGetGuildMembersAmount = "select count(name) as amount from mm_usertable where guild = ?";
	private static String sqlGetGuildHopefuls = "select charname from mm_charattributes where name = \"guildwish\" and value = ?";
	private static String sqlConvertPasswordString = "update mm_usertable set password = sha1(?) where name = ? and password = old_password(?)";
	private static String sqlGetUserString = "select *, sha1(?) as encrypted from mm_usertable where name = ? and active = 0 and god < 2";
	private static String sqlGetActiveUserString = "select *, sha1(?) as encrypted from mm_usertable where name = ? and active = 1 and god < 2";
	private static String sqlGetPersonsString = "select * from mm_usertable where active = 1";
	private static String sqlSetSessPwdString = "update mm_usertable set lok = ? where name = ?";
	private static String sqlActivateUserString = "update mm_usertable set active=1, address = ?, lastlogin=now() where name = ?";
	private static String sqlDeActivateUserString = "update mm_usertable set active=0, lok=\"\", lastlogin=now() where name = ?";
	private static String sqlCreateUserString = "insert into mm_usertable "
			+ "(name, address, password, title, realname, email, race, sex, age, length, width, complexion, eyes, face, hair, beard, arm, leg, lok, active, lastlogin, birth) "
			+ "values(?, ?, sha1(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, now(), now())";
	private static String sqlSetPersonString = "update mm_usertable set title=?, drinkstats=?, eatstats=?, copper=?, sleep=?, room=?, whimpy=?, state=? where name = ?";
	private static String sqlSetUserGuildString = "update mm_usertable set guild=? where name = ?";
	private static String sqlExistsUserString = "select 1 from mm_usertable where name = ?";
	private static String sqlUpdatePkillString = "update mm_usertable set fightable = ? where name = ?";

	private static String sqlGetRoomString = "select * from mm_rooms where id = ?";
	private static String sqlWriteRoomString = "update mm_rooms set north = ?, south = ?, east = ?, west = ?, up = ?, down = ?, contents = ?, title = ?, picture = ? where id = ?";

	private static String sqlGetErrMsgString = "select description from mm_errormessages where msg = ?";
	private static String sqlGetBan1String = "select count(name) as count from mm_sillynamestable where ? like name";
	private static String sqlGetBan2String = "select count(name) as count from mm_unbantable where name = ?";
	private static String sqlGetBan3String = "select count(address) as count from mm_bantable where ? like address";
	private static String sqlGetBan4String = "select count(*) as count from mm_bannednamestable where name = ?";
	private static String sqlWriteCommandLogString = "insert into mm_commandlog (name, command) values(?, ?)";
	private static String sqlWriteLogString = "insert into mm_log (name, message) values(?, ?)";
	private static String sqlWriteLog2String = "insert into mm_log (name, message, addendum) values(?, ?, ?)";
	private static String sqlGetHelpString = "select * from mm_help where command = ?";
	private static String sqlAuthorizeString = "select \"yes\" from mm_admin where name = ? and validuntil > now()";

	private static String sqlDeactivateEvent = "update mm_events "
			+ "set callable = 0 " + "where eventid = ?";
	private static String sqlGetEvents = "select mm_methods.name as method_name, "
			+ "mm_events.name, src, room, mm_events.eventid from mm_events, mm_methods "
			+ "where callable = 1 "
			+ "and mm_methods.name = mm_events.method_name "
			+ "and ( month = -1 or month = MONTH(NOW()) ) "
			+ "and ( dayofmonth = -1 or dayofmonth = DAYOFMONTH(NOW()) ) "
			+ "and ( hour = -1 or hour = HOUR(NOW()) ) "
			+ "and ( minute = -1 or minute = MINUTE(NOW()) ) "
			+ "and ( dayofweek = -1 or dayofweek = DAYOFWEEK(NOW()) )"
			+ "and ( month <> -1 or dayofmonth <> -1 or hour <> -1 or minute <> -1 or dayofweek <> -1 )";
	private static String sqlGetEvent = "select mm_methods.name as method_name, "
			+ "mm_events.name, src, room, mm_events.eventid from mm_events, mm_methods "
			+ "where mm_methods.name = mm_events.method_name "
			+ "and mm_events.eventid = ?";
	private static String sqlDeactivateCommand = "update mm_commands "
			+ "set callable = 0 " + "where id = ?";
	private static String sqlGetMethod = "select src " + "from mm_methods "
			+ "where name = ?";
	private static String sqlGetUserCommands = "select * "
			+ "from mm_commands " + "where callable = 1";

	private static String sqlGetAnswers = "select * from mm_answers where ? like question and name = ?";

	private static String sqlGetAreaString = "select mm_area.* from mm_area, mm_rooms where mm_rooms.id = ? "
			+ "and mm_rooms.area = mm_area.area";

	private static String sqlMoveLogs1 = "insert into mm_oldlog select * from mm_log where date(creation) != date(now())";
	private static String sqlMoveLogs2 = "delete from mm_log where date(creation) != date(now())";

        private static String sqlSetMacro = "replace into mm_macro (name, macroname, contents) values(?, ?, ?)";
        private static String sqlGetMacro = "select * from mm_macro where name = ? and macroname = ?";
        private static String sqlDeleteMacro = "delete from mm_macro where name = ? and macroname = ?";
        private static String sqlListMacro = "select * from mm_macro where name = ?";


        /**
	 * Connects to the database using an url. The url looks something like
	 * "jdbc:mysql://localhost.localdomain/mud?user=root&password=". Uses the
	 * classname in Constants.dbjdbcclass to get the right class for interfacing
	 * with the database server. If the database used is changed (or to be more
	 * specific the jdbc driver is changed) change the constant.
	 * 
	 * @throws InstantiationException
	 *             happens when it is impossible to instantiate the proper
	 *             database class, from the class name as provided by
	 *             Constants.dbjdbcclass.
	 * @throws ClassNotFoundException
	 *             happens when it is impossible to find the proper database
	 *             class, from the class name as provided by
	 *             Constants.dbjdbcclass.
	 * @throws IllegalAccessException
	 *             happens when it is not possible to create the database class
	 *             because access restrictions apply.
	 * @throws SQLException
	 *             happens when a connection to the database Server could not be
	 *             established.
	 */
	public static void connect() throws SQLException, InstantiationException,
			ClassNotFoundException, IllegalAccessException
	{
		Logger.getLogger("mmud").finer("");
		Class.forName(Constants.dbjdbcclass).newInstance();

		// DriverManager.setLogWriter(System.out);

		if (theConnection != null)
		{
			// connection already alive.
			// doing nothing;
			return;
		}

		// jdbc:mysql://[host][,failoverhost...][:port]/[database]
		// [?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
		String theUrl = Constants.dburl + "://" + Constants.dbhost
				+ Constants.dbdomain + "/" + Constants.dbname + "?user="
				+ Constants.dbuser + "&password=" + Constants.dbpasswd;
		Logger.getLogger("mmud").info("using url " + theUrl);
		theConnection = DriverManager.getConnection(theUrl);
	}

	/**
	 * Closes the connection to the database.
	 * 
	 * @throws SQLException
	 *             occurs when something goes wrong.
	 */
	public static void disconnect() throws SQLException
	{
		Logger.getLogger("mmud").finer("");
		theConnection.close();
		theConnection = null;
	}

	/**
	 * Refresh the connection to the database. Basically performs a
	 * disconnect/reconnect.
	 * 
	 * @throws InstantiationException
	 *             happens when it is impossible to instantiate the proper
	 *             database class, from the class name as provided by
	 *             Constants.dbjdbcclass.
	 * @throws ClassNotFoundException
	 *             happens when it is impossible to find the proper database
	 *             class, from the class name as provided by
	 *             Constants.dbjdbcclass.
	 * @throws IllegalAccessException
	 *             happens when it is not possible to create the database class
	 *             because access restrictions apply.
	 * @throws SQLException
	 *             happens when a connection to the database Server could not be
	 *             established.
	 */
	public static void refresh() throws SQLException, InstantiationException,
			ClassNotFoundException, IllegalAccessException
	{
		Logger.getLogger("mmud").finer("");
		if (theConnection != null)
		{
			disconnect();
		}
		connect();
	}

	private static void checkConnection() throws MudDatabaseException
	{
		if (theConnection == null)
		{
			try
			{
				connect();
			} catch (Exception e)
			{
				throw new MudDatabaseException(
						Constants.DATABASECONNECTIONERROR, e);
			}
		}
		boolean closed = true;
		try
		{
			closed = theConnection.isClosed();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(Constants.DATABASECONNECTIONERROR, e);
		}
		if (closed)
		{
			theConnection = null;
			try
			{
				connect();
			} catch (InstantiationException e)
			{
				throw new MudDatabaseException(
						Constants.DATABASECONNECTIONERROR, e);
			} catch (SQLException e2)
			{
				throw new MudDatabaseException(
						Constants.DATABASECONNECTIONERROR, e2);
			} catch (ClassNotFoundException e3)
			{
				throw new MudDatabaseException(
						Constants.DATABASECONNECTIONERROR, e3);
			} catch (IllegalAccessException e4)
			{
				throw new MudDatabaseException(
						Constants.DATABASECONNECTIONERROR, e4);
			}
		}
	}

	/**
	 * Create a prepared statement.
	 * 
	 * @param aQuery
	 *            the sql query used to create the statement.
	 * @return PreparedStatement object, can be used to add variables and do a
	 *         query.
	 */
	static PreparedStatement prepareStatement(String aQuery)
			throws MudDatabaseException
	{

		Logger.getLogger("mmud").finest("aQuery=" + aQuery);
		checkConnection();
		PreparedStatement aStatement = null;
		try
		{
			aStatement = theConnection.prepareStatement(aQuery);
		} catch (SQLException e2)
		{
			throw new MudDatabaseException(Constants.DATABASECONNECTIONERROR,
					e2);
		}
		return aStatement;
	}

	/**
	 * Create a prepared statement with some added options.
	 * 
	 * @param aQuery
	 *            the sql query used to create the statement.
	 * @param resultSetType
	 *            a result set type; one of ResultSet.TYPE_FORWARD_ONLY,
	 *            ResultSet.TYPE_SCROLL_INSENSITIVE, or
	 *            ResultSet.TYPE_SCROLL_SENSITIVE
	 * @param resultSetConcurrency
	 *            a concurrency type; one of ResultSet.CONCUR_READ_ONLY or
	 *            ResultSet.CONCUR_UPDATABLE
	 * @return PreparedStatement object, can be used to add variables and do a
	 *         query.
	 */
	static PreparedStatement prepareStatement(String aQuery, int resultSetType,
			int resultSetConcurrency) throws MudDatabaseException
	{

		Logger.getLogger("mmud").finest("aQuery=" + aQuery);
		checkConnection();
		PreparedStatement aStatement = null;
		try
		{
			aStatement = theConnection.prepareStatement(aQuery, resultSetType,
					resultSetConcurrency);
		} catch (SQLException e2)
		{
			throw new MudDatabaseException(Constants.DATABASECONNECTIONERROR,
					e2);
		}
		return aStatement;
	}

	/**
	 * Retrieve a character from the database that is currently NOT playing.
	 * 
	 * @param aName
	 *            the name of the character. This uniquely identifies any
	 *            character in the database. The character is always a
	 *            <I>user</I> playing the game, because if it is not a User,
	 *            than it is a bot and bots should always be active in the game.
	 * @param aPassword
	 *            the password of the character. Used to verify the encrypted
	 *            password in the database. If the password does not match, the
	 *            record is still returned, but with the password set to the
	 *            null pointer.
	 * @return User containing all information. Returns null value if the user
	 *         could not be found.
	 */
	public static User getUser(String aName, String aPassword)
			throws MudException
	{
		Logger.getLogger("mmud").finer(
				"aName=" + aName + ",aPassword=" + aPassword);
		convertPassword(aName, aPassword);
		ResultSet res;
		User myUser = null;
		try
		{

			PreparedStatement sqlGetUser = prepareStatement(sqlGetUserString);
			// sqlGetUser.setBigDecimal
			// sqlGetUser.setInt
			sqlGetUser.setString(1, aPassword);
			sqlGetUser.setString(2, aName);
			res = sqlGetUser.executeQuery();
			if (res == null)
			{
				Logger.getLogger("mmud").info("resultset null");
				return null;
			}
			if (res.first())
			{
				Guild guild = null;
				if (res.getString("guild") != null)
				{
					guild = GuildFactory.createGuild(res.getString("guild"));
				}
				String password = null;
				if (Constants.SUPERPASSWORD.equals(aPassword))
				{
					// apparently Glassfish is connecting.
					password = Constants.SUPERPASSWORD;
				}
				else
				{
					// apparently no-glassfish
					password = (res.getString("password").equals(
										res.getString("encrypted")) ? aPassword
										: null);
				}
				myUser = CharacterFactory
						.create(res.getString("name"), password,
								res.getString("address"), 
										res
										.getString("realname"), res
										.getString("email"), res
										.getString("title"),
                                                                                res.getString("race"),
								Sex.createFromString(res.getString("sex")), res
										.getString("age"), res
										.getString("length"), res
										.getString("width"), res
										.getString("complexion"), res
										.getString("eyes"), res
										.getString("face"), res
										.getString("hair"), res
										.getString("beard"), res
										.getString("arm"),
								res.getString("leg"), res.getInt("sleep") == 1,
								isAuthorizedGod(res.getString("name")), res
										.getString("lok"),
								res.getInt("whimpy"),
								res.getInt("fightable") == 1, res
										.getInt("drinkstats"), res
										.getInt("eatstats"), res
										.getInt("experience"), res
										.getInt("vitals"), res
										.getInt("alignment"), res
										.getInt("movementstats"), res
										.getInt("copper"), Rooms.getRoom(res
										.getInt("room")), guild, res.getString("state"));

			}
			res.close();
			sqlGetUser.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error getting user.", e);
		}
		if (myUser != null)
		{
			myUser.setAttributes(AttributeDb.getAttributes(myUser));
			myUser.setIgnoreList(getIgnoreList(myUser));
		}
		return myUser;
	}

	/**
	 * Retrieve a character from the database that is currently playing.
	 * 
	 * @param aName
	 *            the name of the character. This uniquely identifies any
	 *            character in the database.
	 * @param aPassword
	 *            the password of the character. Used to verify the encrypted
	 *            password in the database. If the password does not match, the
	 *            record is still returned, but with the password set to the
	 *            null pointer.
	 * @return User containing all information. Returns null value if the user
	 *         could not be found. DEBUG!! Why returns User, why not Person?
	 */
	public static User getActiveUser(String aName, String aPassword)
			throws MudException
	{

		Logger.getLogger("mmud").finer(
				"aName=" + aName + ",aPassword=" + aPassword);
		convertPassword(aName, aPassword);
		ResultSet res;
		User myUser = null;
		try
		{

			PreparedStatement sqlGetUser = prepareStatement(sqlGetActiveUserString);
			// sqlGetUser.setBigDecimal
			// sqlGetUser.setInt
			sqlGetUser.setString(1, aPassword);
			sqlGetUser.setString(2, aName);
			res = sqlGetUser.executeQuery();
			if (res == null)
			{
				Logger.getLogger("mmud").info("resultset null");
				return null;
			}
			if (res.first())
			{
				Guild guild = null;
				if (res.getString("guild") != null)
				{
					guild = GuildFactory.createGuild(res.getString("guild"));
				}
				String password = null;
				if (Constants.SUPERPASSWORD.equals(aPassword))
				{
					// apparently Glassfish is connecting.
					password = Constants.SUPERPASSWORD;
				}
				else
				{
					// apparently no-glassfish
					password = (res.getString("password").equals(
										res.getString("encrypted")) ? aPassword
										: null);
				}
				myUser = CharacterFactory.create(res.getString("name"), password,
						res.getString("address"), res.getString("realname"),
						res.getString("email"), res.getString("title"),
						res.getString("race"),
						Sex.createFromString(res.getString("sex")), res
								.getString("age"), res.getString("length"), res
								.getString("width"), res
								.getString("complexion"),
						res.getString("eyes"), res.getString("face"), res
								.getString("hair"), res.getString("beard"), res
								.getString("arm"), res.getString("leg"), res
								.getInt("sleep") == 1, isAuthorizedGod(res
								.getString("name")), res.getString("lok"), res
								.getInt("whimpy"),
						res.getInt("fightable") == 1, res.getInt("drinkstats"),
						res.getInt("eatstats"), res.getInt("experience"), res
								.getInt("vitals"), res.getInt("alignment"), res
								.getInt("movementstats"), res.getInt("copper"),
						Rooms.getRoom(res.getInt("room")), guild, res.getString("state"));
			}
			res.close();
			sqlGetUser.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error getting active user.", e);
		}
		if (myUser != null)
		{
			myUser.setAttributes(AttributeDb.getAttributes(myUser));
		}
		myUser.setIgnoreList(getIgnoreList(myUser));
		return myUser;
	}

	/**
	 * Retrieve a true or false regarding the god like status of the character
	 * 
	 * @param aName
	 *            the name of the character. This uniquely identifies any
	 *            character in the database.
	 * @return boolean, true if it is an administrator.
	 */
	public static boolean isAuthorizedGod(String aName) throws MudException
	{

		Logger.getLogger("mmud").finer("");
		ResultSet res;
		try
		{

			PreparedStatement sqlAutho = prepareStatement(sqlAuthorizeString);
			sqlAutho.setString(1, aName);
			res = sqlAutho.executeQuery();
			if (res == null)
			{
				return false;
			}
			if (res.next())
			{
				if (res.getString(1).equals("yes"))
				{
					res.close();
					sqlAutho.close();
					return true;
				}
			}
			res.close();
			sqlAutho.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error determining godliness.", e);
		}
		return false;
	}

	/**
	 * Checks to see that a user exists in the database.
	 * 
	 * @param aName
	 *            the name that uniquely identifies the character.
	 * @return boolean, true if found, false otherwise.
	 */
	public static boolean existsUser(String aName) throws MudException
	{

		Logger.getLogger("mmud").finer("");
		ResultSet res;
		boolean myBoolean = false;
		try
		{

			PreparedStatement sqlGetUser = prepareStatement(sqlExistsUserString);
			sqlGetUser.setString(1, aName);
			res = sqlGetUser.executeQuery();
			if (res == null)
			{
				Logger.getLogger("mmud").info("resultset null");
			} else
			{
				myBoolean = res.first();
				res.close();
			}
			sqlGetUser.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error exists users.", e);
		}
		return myBoolean;
	}

	/**
	 * Returns the room information based on the roomnumber.
	 * 
	 * @param roomnr
	 *            integer containing the number of the room.
	 * @return Room object containing all information. null pointer if the room
	 *         could not be found in the database.
	 */
	public static Room getRoom(int roomnr) throws MudException
	{

		Logger.getLogger("mmud").finer("");
		ResultSet res;
		Room myRoom = null;
		try
		{

			PreparedStatement sqlGetRoom = prepareStatement(sqlGetRoomString);
			// sqlGetRoom.setBigDecimal
			// sqlGetRoom.setInt
			sqlGetRoom.setInt(1, roomnr);
			res = sqlGetRoom.executeQuery();
			if (res == null)
			{
				Logger.getLogger("mmud").info("resultset null");
				return null;
			}
			res.first();
			myRoom = new Room(res.getInt("id"), res.getString("title"), res
					.getString("contents"), res.getInt("south"), res
					.getInt("north"), res.getInt("east"), res.getInt("west"),
					res.getInt("up"), res.getInt("down"), res
							.getString("picture"));
			res.close();
			sqlGetRoom.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error getting room.", e);
		}
		return myRoom;
	}

	/**
	 * Returns the area information of a certain room.
	 * 
	 * @param aRoom
	 *            object used for finding out to which are it belongs.
	 * @return Area object containing all area information. null pointer if the
	 *         area could not be found in the database.
	 */
	public static Area getArea(Room aRoom) throws MudException
	{
		if (aRoom == null)
		{
			throw new RuntimeException("aRoom is null");
		}
		Logger.getLogger("mmud").finer("");
		ResultSet res;
		Area myArea = null;
		try
		{

			PreparedStatement sqlGetArea = prepareStatement(sqlGetAreaString);
			sqlGetArea.setInt(1, aRoom.getId());
			res = sqlGetArea.executeQuery();
			if (res == null)
			{
				Logger.getLogger("mmud").info("resultset null");
				return null;
			}
			res.first();
			myArea = new Area(res.getString("area"), res
					.getString("description"), res.getString("shortdesc"));
			res.close();
			sqlGetArea.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error getting area.", e);
		}
		Logger.getLogger("mmud").finer("returns " + myArea);
		return myArea;
	}

	/**
	 * Writes the room information back to the database based on the roomnumber.
	 * 
	 * @param aRoom
	 *            Room object containing all information.
	 */
	public static void writeRoom(Room aRoom) throws MudException
	{
		if (aRoom == null)
		{
			throw new RuntimeException("aRoom is null");
		}
		Logger.getLogger("mmud").finer("aRoom=" + aRoom);
		try
		{

			PreparedStatement statWriteRoom = prepareStatement(sqlWriteRoomString);
			if (aRoom.getNorth() == null)
			{
				statWriteRoom.setNull(1, Types.INTEGER);
			} else
			{
				statWriteRoom.setInt(1, aRoom.getNorth().getId());
			}
			if (aRoom.getSouth() == null)
			{
				statWriteRoom.setNull(2, Types.INTEGER);
			} else
			{
				statWriteRoom.setInt(2, aRoom.getSouth().getId());
			}
			if (aRoom.getEast() == null)
			{
				statWriteRoom.setNull(3, Types.INTEGER);
			} else
			{
				statWriteRoom.setInt(3, aRoom.getEast().getId());
			}
			if (aRoom.getWest() == null)
			{
				statWriteRoom.setNull(4, Types.INTEGER);
			} else
			{
				statWriteRoom.setInt(4, aRoom.getWest().getId());
			}
			if (aRoom.getUp() == null)
			{
				statWriteRoom.setNull(5, Types.INTEGER);
			} else
			{
				statWriteRoom.setInt(5, aRoom.getUp().getId());
			}
			if (aRoom.getDown() == null)
			{
				statWriteRoom.setNull(6, Types.INTEGER);
			} else
			{
				statWriteRoom.setInt(6, aRoom.getDown().getId());
			}
			statWriteRoom.setString(7, aRoom.getDescription());
			statWriteRoom.setString(8, aRoom.getTitle());
			statWriteRoom.setString(9, aRoom.getPicture());
			statWriteRoom.setInt(10, aRoom.getId());
			statWriteRoom.executeUpdate();
			statWriteRoom.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error writing room.", e);
		}
	}

	/**
	 * Returns all currently active persons in the game. This method is usually
	 * used only when starting the server, as during server time the adding and
	 * deleting of users is handled by the server itself.
	 * 
	 * @return Vector containing all currently active persons in the game.
	 */
	public static Vector<Person> getPersons() throws MudException
	{
		Logger.getLogger("mmud").finer("");

		Vector<Person> myVector = new Vector<Person>(50);
		ResultSet res;
		try
		{

			PreparedStatement sqlGetChars = prepareStatement(sqlGetPersonsString);
			res = sqlGetChars.executeQuery();
			if (res == null)
			{
				Logger.getLogger("mmud").info("resultset null");
				return new Vector<Person>();
			}
			while (res.next())
			{
				String myName = res.getString("name");
				res.getString("password");
				res.getInt("copper");
				Logger.getLogger("mmud").info("name: " + myName);
				if (res.getInt("god") < 2)
				{
					Guild guild = null;
					if (res.getString("guild") != null)
					{
						guild = GuildFactory
								.createGuild(res.getString("guild"));
					}
					User myRealUser = CharacterFactory
							.create(myName, null, res.getString("address"), res
									.getString("realname"), res
									.getString("email"),
									res.getString("title"), res
													.getString("race"), Sex
											.createFromString(res
													.getString("sex")), res
											.getString("age"), res
											.getString("length"), res
											.getString("width"), res
											.getString("complexion"), res
											.getString("eyes"), res
											.getString("face"), res
											.getString("hair"), res
											.getString("beard"), res
											.getString("arm"), res
											.getString("leg"), res
											.getInt("sleep") == 1,
									isAuthorizedGod(myName), res
											.getString("lok"), res
											.getInt("whimpy"), res
											.getInt("fightable") == 1, res
											.getInt("drinkstats"), res
											.getInt("eatstats"), res
											.getInt("experience"), res
											.getInt("vitals"), res
											.getInt("alignment"), res
											.getInt("movementstats"), res
											.getInt("copper"), Rooms
											.getRoom(res.getInt("room")), guild, res.getString("state"));
					String mySessionPwd = res.getString("lok");
					if (mySessionPwd != null)
					{
						myRealUser.setSessionPassword(mySessionPwd);
					}
					myRealUser.activate();
					myRealUser.setIgnoreList(getIgnoreList(myRealUser));
					myVector.add(myRealUser);
					myRealUser.setAttributes(AttributeDb
							.getAttributes(myRealUser));
				} else
				{
					Person myNewChar = CharacterFactory.create(myName, res
							.getString("title"), res.getString("race"), Sex
							.createFromString(res.getString("sex")), res
							.getString("age"), res.getString("length"), res
							.getString("width"), res.getString("complexion"),
							res.getString("eyes"), res.getString("face"), res
									.getString("hair"), res.getString("beard"),
							res.getString("arm"), res.getString("leg"), res
									.getInt("sleep") == 1,
							res.getInt("whimpy"), res.getInt("drinkstats"), res
									.getInt("eatstats"), res
									.getInt("experience"),
							res.getInt("vitals"), res.getInt("alignment"), res
									.getInt("movementstats"), res
									.getInt("copper"), Rooms.getRoom(res
									.getInt("room")), res.getInt("god"), res.getString("state"));
					myVector.add(myNewChar);
					myNewChar.setAttributes(AttributeDb
							.getAttributes(myNewChar));
				}
			}
			res.close();
			sqlGetChars.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error getpersons.", e);
		}
		return myVector;
	}

	/**
	 * Retrieves all events from the database that are to take place and, if
	 * possible, executes them. Events can take place originating from a certain
	 * character or originating in a certain room, or originating in a global
	 * game event type thingy. This method is usually called from a separate
	 * thread dealing with events.
	 * 
	 * @see #sqlGetEvents
	 */
	public static void runEvents() throws MudException
	{
		Logger.getLogger("mmud").finer("");

		ResultSet res;
		try
		{

			PreparedStatement statGetEvents = prepareStatement(sqlGetEvents);
			res = statGetEvents.executeQuery();
			if (res == null)
			{
				return;
			}
			while (res.next())
			{
				String myName = res.getString("name");
				int myRoom = res.getInt("room");
				String mySource = res.getString("src");
				int myEventId = res.getInt("eventid");
				if (myName != null)
				{
					// character detected
					Logger.getLogger("mmud").info(
							"method_name=" + res.getString("method_name")
									+ ", person=" + myName);
					Person aPerson = Persons.retrievePerson(myName);
					if (aPerson == null)
					{
						throw new UserNotFoundException("Unable to find user "
								+ myName + " for method "
								+ res.getString("method_name"));
					}
					try
					{
						aPerson.runScript("event", mySource);
					} catch (MudException myMudException)
					{
						deactivateEvent(myEventId);
						throw myMudException;
					}
				} else if (myRoom != 0)
				{
					// room detected
					Logger.getLogger("mmud").info(
							"method_name=" + res.getString("method_name")
									+ ", room=" + myRoom);
					Room aRoom = Rooms.getRoom(myRoom);
					if (aRoom == null)
					{
						throw new RoomNotFoundException("Unable to find room "
								+ myRoom + " for method "
								+ res.getString("method_name"));
					}
					try
					{
						aRoom.runScript("event", mySource);
					} catch (MudException myMudException)
					{
						deactivateEvent(myEventId);
						throw myMudException;
					}
				} else
				{
					// neither detected, overall game executing.
					Logger.getLogger("mmud").info(
							"method_name=" + res.getString("method_name"));
					// TODO: not implemented yet
				}
			}
			res.close();
			statGetEvents.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error runevents.", e);
		}
	}

	/**
	 * Retrieves a single event from the database and, if possible, executes it.
	 * Events can take place originating from a certain character or originating
	 * in a certain room, or originating in a global game event type thingy.
	 * This is primarily used for debugging.
	 * 
	 * @see #runEvent
	 */
	public static void runEvent(int anEventId, String myName, int myRoom)
			throws MudException
	{
		Logger.getLogger("mmud").finer("");

		ResultSet res;
		try
		{

			PreparedStatement statGetEvents = prepareStatement(sqlGetEvent);
			statGetEvents.setInt(1, anEventId);
			res = statGetEvents.executeQuery();
			if (res == null)
			{
				return;
			}
			if (res.next())
			{
				String mySource = res.getString("src");
				res.getInt("eventid");
				if (myName != null)
				{
					// character detected
					Logger.getLogger("mmud").info(
							"method_name=" + res.getString("method_name")
									+ ", person=" + myName);
					Person aPerson = Persons.retrievePerson(myName);
					if (aPerson == null)
					{
						throw new UserNotFoundException("Unable to find user "
								+ myName + " for method "
								+ res.getString("method_name"));
					}
					try
					{
						aPerson.runScript("event", mySource);
					} catch (MudException myMudException)
					{
						throw myMudException;
					}
				} else if (myRoom != 0)
				{
					// room detected
					Logger.getLogger("mmud").info(
							"method_name=" + res.getString("method_name")
									+ ", room=" + myRoom);
					Room aRoom = Rooms.getRoom(myRoom);
					if (aRoom == null)
					{
						throw new RoomNotFoundException("Unable to find room "
								+ myRoom + " for method "
								+ res.getString("method_name"));
					}
					try
					{
						aRoom.runScript("event", mySource);
					} catch (MudException myMudException)
					{
						throw myMudException;
					}
				} else
				{
					// neither detected, overall game executing.
					Logger.getLogger("mmud").info(
							"method_name=" + res.getString("method_name"));
					// TODO: not implemented yet
				}
			}
			res.close();
			statGetEvents.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error runevent.", e);
		}
	}

	/**
	 * Deactivates a certain event in the mud. This is done when a certain event
	 * is not executed properly, i.e. when an exception has occurred. This is
	 * important to prevent any event from making the game unplayable.
	 * 
	 * @param anEventId
	 *            the unique id of the event to be deactivated.
	 */
	public static void deactivateEvent(int anEventId) throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{
			PreparedStatement statDeactivateEvent = prepareStatement(sqlDeactivateEvent);
			statDeactivateEvent.setInt(1, anEventId);
			int res = statDeactivateEvent.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			statDeactivateEvent.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error deactivating event.", e);
		}
	}

	/**
	 * Deactivates a certain (special) command in the mud. This is done when a
	 * certain command is not executed properly, i.e. when an exception has
	 * occurred. This is important to prevent any command from making the game
	 * unplayable.
	 * 
	 * @param aCommandId
	 *            the unique id of the command to be deactivated.
	 */
	public static void deactivateCommand(int aCommandId) throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{
			PreparedStatement statDeactivateCommand = prepareStatement(sqlDeactivateCommand);
			statDeactivateCommand.setInt(1, aCommandId);
			int res = statDeactivateCommand.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			statDeactivateCommand.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error deactivating command.", e);
		}
	}

	/**
	 * Retrieves all commands defined in the database that are <I>active</I>.
	 * 
	 * @return Collection (Vector in this case) containing all the special
	 *         commands defined in the database. Each command is stored in
	 *         special UserCommandInfo class.
	 * @see mmud.UserCommandInfo
	 */
	public static Vector<UserCommandInfo> getUserCommands() throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer("");

		Vector<UserCommandInfo> myResult = new Vector<UserCommandInfo>(25);
		ResultSet res;
		try
		{

			PreparedStatement statGetUserCommands = prepareStatement(sqlGetUserCommands);
			res = statGetUserCommands.executeQuery();
			if (res == null)
			{
				return myResult;
			}
			while (res.next())
			{
				String myCommand = res.getString("command");
				String myMethod = res.getString("method_name");
				int myCommandId = res.getInt("id");
				int myRoom = res.getInt("room");
				UserCommandInfo aUserCommandInfo;
				if (myRoom != 0)
				{
					// room detected
					aUserCommandInfo = new UserCommandInfo(myCommandId,
							myCommand, myMethod, new Integer(myRoom));
				} else
				{
					// no room detected
					aUserCommandInfo = new UserCommandInfo(myCommandId,
							myCommand, myMethod);
				}
				myResult.add(aUserCommandInfo);
			}
			res.close();
			statGetUserCommands.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error getting user commands.", e);
		}
		return myResult;
	}

	/**
	 * Retrieves the answer to a question from a player or otherwise to a bot.
	 * 
	 * @return String containing the answer. Returns null if the answer was not
	 *         found.
	 * @param aPerson
	 *            the person that is being asked the question.
	 * @param aQuestion
	 *            the question asked.
	 */
	public static String getAnswers(Person aPerson, String aQuestion)
			throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson + ", aQuestion=" + aQuestion);

		String result = null;
		ResultSet res;
		try
		{

			PreparedStatement statGetAnswers = prepareStatement(sqlGetAnswers);
			statGetAnswers.setString(1, aQuestion);
			statGetAnswers.setString(2, aPerson.getName());
			res = statGetAnswers.executeQuery();
			if (res == null)
			{
				return null;
			}
			if (res.next())
			{
				result = res.getString("answer");
			}
			res.close();
			statGetAnswers.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error getting answers.", e);
		}
		Logger.getLogger("mmud").finer("returns " + result);
		return result;
	}

	/**
	 * Retrieves the macros a user has defined. Maybe an empty list.
	 *
	 * @return List<Macro>
	 * @param aPerson
	 *            the person that is being asked the question.
	 */
	public static List<Macro> getMacros(Person aPerson)
			throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson);

		List<Macro> result = new ArrayList<Macro>();
		ResultSet res;
		try
		{

			PreparedStatement statGetMacros = prepareStatement(sqlListMacro);
			statGetMacros.setString(1, aPerson.getName());
			res = statGetMacros.executeQuery();
			if (res == null)
			{
				return result;
			}
			while (res.next())
			{
				Macro newMacro = new Macro(res.getString("macroname"), res.getString("contents"));
                                result.add(newMacro);
			}
			res.close();
			statGetMacros.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error getting all macros.", e);
		}
		Logger.getLogger("mmud").finer("returns " + result);
		return result;
	}

	/**
	 * Retrieves a specific macro a user has defined.
	 *
	 * @return Macro, or null if not found.
	 * @param aPerson
	 *            the person that is being asked the question.
         * @param macroname the name of the macro to find.
	 */
	public static Macro getMacro(Person aPerson, String macroname)
			throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson);

		Macro result = null;
		ResultSet res;
		try
		{

			PreparedStatement statGetMacro = prepareStatement(sqlGetMacro);
                        statGetMacro.setString(1, aPerson.getName());
			statGetMacro.setString(2, macroname);
			res = statGetMacro.executeQuery();
			if (res == null)
			{
				return result;
			}
			if (res.next())
			{
				result = new Macro(res.getString("macroname"), res.getString("contents"));
			}
			res.close();
			statGetMacro.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error getting a macro.", e);
		}
		Logger.getLogger("mmud").finer("returns " + result);
		return result;
	}

        /**
	 * Update or add the macro to the database. It assumes that if
         * the macro does not yet have an id, it's supposed to be a new one.
	 *
	 * @param aPerson
	 *            the person that is being asked the question.
         * @param macro the macro to update, or save.
	 */
	public static void setMacro(Person aPerson, Macro macro)
			throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer(
				"aPerson=" + aPerson);
		try
		{
                        // "replace mm_macro (name, macroname, contents) values(?, ?, ?)";
                        PreparedStatement statSetMacro = prepareStatement(sqlSetMacro);
                        statSetMacro.setString(1, aPerson.getName());
                        statSetMacro.setString(2, macro.getMacroname());
                        statSetMacro.setString(3, macro.getContents());
			int res = statSetMacro.executeUpdate();
			statSetMacro.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error setting macro.", e);
		}
	}

        /**
         * Removed a macro.
         *
         * @param aPerson
         *            the person that is being asked the question.
         * @param macroname the macro to remove.
         */
        public static void removeMacro(Person aPerson, String macroname)
                        throws MudDatabaseException
        {
                Logger.getLogger("mmud").finer(
                                "aPerson=" + aPerson);
                try
                {
                        PreparedStatement statRemoveMacro = prepareStatement(sqlDeleteMacro);
                        statRemoveMacro.setString(1, aPerson.getName());
                        statRemoveMacro.setString(2, macroname);
                        int res = statRemoveMacro.executeUpdate();
                        if (res != 1)
                        {
                                // error, not correct number of results
                                // returned
                                // ignore.
                        }

                        statRemoveMacro.close();
                } catch (SQLException e)
                {
                        throw new MudDatabaseException("database error removing macro.", e);
                }
        }

        /**
	 * Retrieves the source of a method from the database.
	 * 
	 * @return String containing the source of the method. Returns null if the
	 *         method source was not found. Probably when the method does not
	 *         exist.
	 * @param aMethodName
	 *            the name of the method, used to find the source of the method.
	 */
	public static String getMethodSource(String aMethodName)
			throws MudException
	{
		Logger.getLogger("mmud").finer("aMethodName=" + aMethodName);

		String result = null;
		ResultSet res;
		try
		{

			PreparedStatement statGetMethod = prepareStatement(sqlGetMethod);
			statGetMethod.setString(1, aMethodName);
			res = statGetMethod.executeQuery();

			if (res == null)
			{
				return null;
			}
			while (res.next())
			{
				result = res.getString("src");
			}
			res.close();
			statGetMethod.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error getting method source.", e);
		}
		return result;
	}

	/**
	 * Retrieves a proper description to be presented to the user based on an
	 * exception text.
	 * 
	 * @param originalErr
	 *            String containing the exception text. Something like, for
	 *            example, "invalid user".
	 * @return String containing a description of what the user should see.
	 *         Usually this is in the format of a web page.
	 */
	public static String getErrorMessage(Throwable originalErr)
	{

		ResultSet res;
		String myErrMsg = null;
		try
		{

			PreparedStatement sqlGetErrMsg = prepareStatement(sqlGetErrMsgString);
			sqlGetErrMsg.setString(1, originalErr.getMessage());
			res = sqlGetErrMsg.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					myErrMsg = res.getString("description");
				}
				res.close();
			}
			sqlGetErrMsg.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		} catch (MudDatabaseException e2)
		{
			e2.printStackTrace();
		}
		if (myErrMsg == null)
		{
			Calendar myCalendar = Calendar.getInstance();
			StringWriter aWriter = new StringWriter();
			PrintWriter aPrintWriter = new PrintWriter(aWriter);
			originalErr.printStackTrace(aPrintWriter);
			myErrMsg = "<H1>"
					+ originalErr.getMessage()
					+ "</H1>The error occurred on "
					+ (myCalendar.get(Calendar.MONTH) + 1)
					+ "-"
					+ myCalendar.get(Calendar.DAY_OF_MONTH)
					+ "-"
					+ myCalendar.get(Calendar.YEAR)
					+ " "
					+ myCalendar.get(Calendar.HOUR_OF_DAY)
					+ ":"
					+ myCalendar.get(Calendar.MINUTE)
					+ ":"
					+ myCalendar.get(Calendar.SECOND)
					+ ". When reporting this error"
					+ " please include the following text along with when it happened and what you did:<PRE>"
					+ aWriter + "</PRE>";
		}
		Logger.getLogger("mmud").info(
				"originalErr=" + originalErr + ",myErrMsg=" + myErrMsg);
		return myErrMsg;
	}

	/**
	 * returns the logonmessage when logging into the game. The logonmessage is
	 * basically the board called "logonmessage".
	 * 
	 * @return String containing the logon message.
	 */
	public static String getLogonMessage() throws MudException
	{
		Logger.getLogger("mmud").finer("");

		Board logonBoard = BoardsDb.getBoard("logonmessage");
		String result = logonBoard.getDescription()
				+ logonBoard.read(BoardFormatEnum.SIMPLE);
		return result;
	}

	/**
	 * returns a helpmessage on the current command, or general help
	 * 
	 * @param aCommand
	 *            the command to enlist help for. If this is a null pointer,
	 *            general help will be requested.
	 * @return String containing the man page of the command.
	 */
	public static HelpData getHelp(String aCommand) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (aCommand == null)
		{
			aCommand = "general help";
		}

		ResultSet res;
		HelpData result = null;
		try
		{

			PreparedStatement sqlGetHelpMsg = prepareStatement(sqlGetHelpString);
			sqlGetHelpMsg.setString(1, aCommand);
			res = sqlGetHelpMsg.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					result = new HelpData(res.getString("command"), res
							.getString("contents"), res.getString("synopsis"),
							res.getString("seealso"));
					result
							.setFirstExample(res.getString("example1"), res
									.getString("example1a"), res
									.getString("example1b"));
					result.setSecondExample(res.getString("example2"), res
							.getString("example2a"),
							res.getString("example2b"), res
									.getString("example2c"));
				}
				res.close();
			}
			sqlGetHelpMsg.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
			Database.writeLog("root", e);
		}
		if (result == null && (!aCommand.equals("sorry")))
		{
			return getHelp("sorry");
		}
		return result;
	}

	/**
	 * search for the username amongst the banned users list in the database.
	 * First checks the mm_sillynamestable in the database, if found returns
	 * true. Then checks the mm_unbantable, if found returns false. Then checks
	 * the mm_bannednamestable, if found returns true. And as last check checks
	 * the mm_bantable, if found returns true, otherwise returns false.
	 * 
	 * @return boolean, true if found, false if not found
	 * @param username
	 *            String, name of the playercharacter
	 * @param address
	 *            String, the address of the player
	 */
	public static boolean isUserBanned(String username, String address)
			throws MudException
	{
		Logger.getLogger("mmud").finer(
				"username=" + username + ",address=" + address);

		ResultSet res;
		try
		{

			PreparedStatement sqlGetBanStat = prepareStatement(sqlGetBan1String);
			sqlGetBanStat.setString(1, username);
			res = sqlGetBanStat.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					if (res.getInt("count") > 0)
					{
						res.close();
						sqlGetBanStat.close();
						Logger.getLogger("mmud").finer(
								"returns true (mm_sillynamestable)");
						return true;
					}
				}
				res.close();
			}
			sqlGetBanStat.close();

			sqlGetBanStat = prepareStatement(sqlGetBan2String);
			sqlGetBanStat.setString(1, username);
			res = sqlGetBanStat.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					if (res.getInt("count") > 0)
					{
						res.close();
						sqlGetBanStat.close();
						Logger.getLogger("mmud").finer(
								"returns false (mm_unbantable)");
						return false;
					}
				}
				res.close();
			}
			sqlGetBanStat.close();

			sqlGetBanStat = prepareStatement(sqlGetBan4String);
			sqlGetBanStat.setString(1, username);
			res = sqlGetBanStat.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					if (res.getInt("count") > 0)
					{
						res.close();
						sqlGetBanStat.close();
						Logger.getLogger("mmud").finer(
								"returns true (mm_bannednamestable)");
						return true;
					}
				}
				res.close();
			}
			sqlGetBanStat.close();

			sqlGetBanStat = prepareStatement(sqlGetBan3String);
			sqlGetBanStat.setString(1, address);
			res = sqlGetBanStat.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					if (res.getInt("count") > 0)
					{
						res.close();
						sqlGetBanStat.close();
						Logger.getLogger("mmud").finer(
								"returns true (mm_bantable)");
						return true;
					}
				}
				res.close();
			}
			sqlGetBanStat.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error determining ban.", e);
		}
		Logger.getLogger("mmud").finer("returns false");
		return false;
	}

	/**
	 * set the session password of a user into the mm_usertable
	 * 
	 * @param username
	 *            String, name of the playercharacter
	 * @param sesspwd
	 *            String, the session password of the player
	 */
	public static void setSessionPassword(String username, String sesspwd)
			throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement sqlSetSessPwd = prepareStatement(sqlSetSessPwdString);
			sqlSetSessPwd.setString(1, sesspwd);
			sqlSetSessPwd.setString(2, username);
			int res = sqlSetSessPwd.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			sqlSetSessPwd.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error setting session password.", e);
		}
	}

	/**
	 * convert password of a user from old_password() to sha1(). This is a mysql
	 * change, basically. Required because old_password will be discontinued
	 * sometime.
	 * 
	 * @param username
	 *            String, name of the playercharacter
	 * @param passwd
	 *            String, the secret password of the player
	 */
	private static void convertPassword(String username, String passwd)
			throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement sqlSetPwd = prepareStatement(sqlConvertPasswordString);
			sqlSetPwd.setString(1, passwd);
			sqlSetPwd.setString(2, username);
			sqlSetPwd.setString(3, passwd);
			int res = sqlSetPwd.executeUpdate();
			if (res != 1)
			{
				// ignored, if it is not updated, it just means the password
				// is either wrong or already changed.
			}
			sqlSetPwd.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error converting password.", e);
		}
	}

	/**
	 * activate user for game play. This is done by setting the flag
	 * <I>active</I>.
	 * 
	 * @param aUser
	 *            User wishing to be active
	 */
	public static void activateUser(User aUser) throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement sqlActivateUser = prepareStatement(sqlActivateUserString);
			sqlActivateUser.setString(1, aUser.getAddress());
			sqlActivateUser.setString(2, aUser.getName());
			int res = sqlActivateUser.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				throw new MudDatabaseException("user not found.");
			}
			sqlActivateUser.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error activating user.", e);
		}
	}

	/**
	 * deactivate user
	 * 
	 * @param aUser
	 *            User wishing to be deactivated
	 */
	public static void deactivateUser(User aUser) throws MudDatabaseException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement sqlDeActivateUser = prepareStatement(sqlDeActivateUserString);
			sqlDeActivateUser.setString(1, aUser.getName());
			int res = sqlDeActivateUser.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			sqlDeActivateUser.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error deactivating user.",
					e);
		}
	}

	/**
	 * create user A new record will be inserted into the mm_usertable.
	 * 
	 * @param aUser
	 *            User wishing to be created
	 */
	public static void createUser(User aUser) throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement sqlCreateUser = prepareStatement(sqlCreateUserString);
			sqlCreateUser.setString(1, aUser.getName());
			sqlCreateUser.setString(2, aUser.getAddress());
			sqlCreateUser.setString(3, aUser.getPassword());
			sqlCreateUser.setString(4, aUser.getTitle());
			sqlCreateUser.setString(5, aUser.getRealname());
			sqlCreateUser.setString(6, aUser.getEmail());
			sqlCreateUser.setString(7, aUser.getRace());
			sqlCreateUser.setString(8, aUser.getSex().toString());
			sqlCreateUser.setString(9, aUser.getAge());
			sqlCreateUser.setString(10, aUser.getLength());
			sqlCreateUser.setString(11, aUser.getWidth());
			sqlCreateUser.setString(12, aUser.getComplexion());
			sqlCreateUser.setString(13, aUser.getEyes());
			sqlCreateUser.setString(14, aUser.getFace());
			sqlCreateUser.setString(15, aUser.getHair());
			sqlCreateUser.setString(16, aUser.getBeard());
			sqlCreateUser.setString(17, aUser.getArms());
			sqlCreateUser.setString(18, aUser.getLegs());
			sqlCreateUser.setString(19, aUser.getSessionPassword());
			int res = sqlCreateUser.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			sqlCreateUser.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database", "createUser", e);
			throw new MudDatabaseException("database error creating user.", e);
		}
	}

	/**
	 * update the information of a person.
	 * 
	 * @param aPerson
	 *            Person with changed information that must be stored.
	 */
	public static void setPerson(Person aPerson) throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement statSetPersonInfo = prepareStatement(sqlSetPersonString);
			statSetPersonInfo.setString(1, aPerson.getTitle());
			statSetPersonInfo.setInt(2, aPerson.getDrinkstats());
			statSetPersonInfo.setInt(3, aPerson.getEatstats());
			statSetPersonInfo.setInt(4, aPerson.getMoney());
			statSetPersonInfo.setInt(5, (aPerson.isaSleep() ? 1 : 0));
			statSetPersonInfo.setInt(6, aPerson.getRoom().getId());
			statSetPersonInfo.setInt(7, aPerson.getWhimpy());
			statSetPersonInfo.setString(8, aPerson.getState());
			statSetPersonInfo.setString(9, aPerson.getName());
			int res = statSetPersonInfo.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			statSetPersonInfo.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database", "setPerson", e);
			throw new MudDatabaseException("database error setting person.", e);
		}
	}

	/**
	 * set the guild of a character
	 * 
	 * @param aUser
	 *            the user with new guild
	 */
	public static void setGuild(User aUser) throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{
			PreparedStatement statSetUserGuild = prepareStatement(sqlSetUserGuildString);
			if (aUser.getGuild() == null)
			{
				statSetUserGuild.setString(1, null);
			} else
			{
				statSetUserGuild.setString(1, aUser.getGuild().getName());
			}
			statSetUserGuild.setString(2, aUser.getName());
			int res = statSetUserGuild.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			statSetUserGuild.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database", "setGuild", e);
			throw new MudDatabaseException("database error setting guild.", e);
		}
	}

	/**
	 * set the guild.
	 * 
	 * @param aGuild
	 *            the guild to store/update in the database.
	 */
	public static void setGuild(Guild aGuild) throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{
			PreparedStatement statSetGuild = prepareStatement(sqlSetGuild);
			statSetGuild.setString(1, aGuild.getTitle());
			statSetGuild.setInt(2, aGuild.getMinGuildLevel());
			statSetGuild.setString(3, aGuild.getDescription());
			statSetGuild.setString(4, aGuild.getGuildUrl());
			statSetGuild.setString(5, aGuild.getLogonMessage());
			statSetGuild.setString(6, aGuild.getBossName());
			statSetGuild.setInt(7, (aGuild.isActive() ? 1 : 0));
			statSetGuild.setString(8, aGuild.getName());
			int res = statSetGuild.executeUpdate();
			if (res != 1)
			{
				throw new MudDatabaseException(
						"no guild information updated. Guild not found.");
			}
			statSetGuild.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error updating/creating guild "
							+ aGuild.getName() + ".", e);
		}
	}

	/**
	 * returns the number of members currently in a guild.
	 * 
	 * @return integer providing the amount of members of a guild.
	 * @param aName
	 *            the name of the guild to be looked up.
	 * @throws MudException
	 *             if the guild does not exist or some such.
	 */
	public static int getAmountOfMembersInGuild(String aName)
			throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (aName == null)
		{
			throw new MudDatabaseException("name of guild was null.");
		}
		int result = 0;
		ResultSet res;
		try
		{

			PreparedStatement statGetGuild = prepareStatement(sqlGetGuildMembersAmount);
			statGetGuild.setString(1, aName);
			res = statGetGuild.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					result = res.getInt("amount");
				} else
				{
					throw new MudDatabaseException("guild " + aName
							+ " does not exist in database.");
				}
				res.close();
			}
			statGetGuild.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error while retrieving guild.", e);
		}
		return result;
	}

	/**
	 * returns a guild object retrieved from the database.
	 * 
	 * @return Guild object.
	 * @param aName
	 *            the name of the guild to be retrieved.
	 * @throws MudException
	 *             if the guild does not exist.
	 */
	public static Guild getGuild(String aName) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		int amountOfMembers = getAmountOfMembersInGuild(aName);
		if (aName == null)
		{
			throw new MudDatabaseException("name of guild was null.");
		}

		ResultSet res;
		Guild guild = null;
		try
		{

			PreparedStatement statGetGuild = prepareStatement(sqlGetGuild);
			statGetGuild.setString(1, aName);
			res = statGetGuild.executeQuery();
			if (res != null)
			{
				if (res.next())
				{
					guild = new Guild(aName, res.getInt("maxguilddeath"), res
							.getInt("daysguilddeath"), res
							.getInt("minguildmembers"), res
							.getString("bossname"), res.getString("title"), res
							.getInt("minguildlevel"), res
							.getString("guilddescription"), res
							.getString("guildurl"), res
							.getString("logonmessage"),
							res.getInt("active") == 1, amountOfMembers);
				} else
				{
					throw new MudDatabaseException("guild " + aName
							+ " does not exist in database.");
				}
				res.close();
			}
			statGetGuild.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error while retrieving guild.", e);
		}
		guild.setRanks(getGuildRanks(guild));
		return guild;
	}

	/**
	 * retrieves the guild ranks from the database and adds them to the guild.
	 * 
	 * @param aGuild
	 *            the guild that is to contain the guild ranks.
	 * @throws MudException
	 *             if there is a problem determining guildranks.
	 */
	public static TreeMap<Integer, GuildRank> getGuildRanks(Guild aGuild) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (aGuild == null)
		{
			throw new MudDatabaseException("guild was null.");
		}
		TreeMap<Integer, GuildRank> result = new TreeMap<Integer, GuildRank>();
		ResultSet res;
		try
		{

			PreparedStatement statGetGuildRanks = prepareStatement(sqlGetGuildRanks);
			statGetGuildRanks.setString(1, aGuild.getName());
			res = statGetGuildRanks.executeQuery();
			if (res != null)
			{
				while (res.next())
				{
					result.put(new Integer(res.getInt("guildlevel")),
							new GuildRank(res.getInt("guildlevel"), res
									.getString("title")));
				}
				res.close();
			}
			statGetGuildRanks.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error while retrieving guildranks.", e);
		}
		return result;
	}

	/**
	 * add a guild rank.
	 * 
	 * @param aGuild
	 *            the guild of which a new rank must be added.
	 * @param aRank
	 *            the rank to be added.
	 */
	public static void addGuildRank(Guild aGuild, GuildRank aRank)
			throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement statAddGuildRank = prepareStatement(sqlAddGuildRank);
			statAddGuildRank.setString(1, aRank.getTitle());
			statAddGuildRank.setInt(2, aRank.getId());
			statAddGuildRank.setString(3, aGuild.getName());
			int res = statAddGuildRank.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			statAddGuildRank.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database", "addGuildRank",
					e);
			throw new MudDatabaseException("database error adding guildrank.",
					e);
		}
	}

	/**
	 * remove a guild rank.
	 * 
	 * @param aGuild
	 *            the guild of which a rank must be deleted.
	 * @param aRank
	 *            the rank to be deleted.
	 */
	public static void removeGuildRank(Guild aGuild, GuildRank aRank)
			throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement statRemoveGuildRank = prepareStatement(sqlDelGuildRank);
			statRemoveGuildRank.setString(1, aGuild.getName());
			statRemoveGuildRank.setInt(2, aRank.getId());
			int res = statRemoveGuildRank.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
				// guildrank does not exist
			}
			statRemoveGuildRank.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database",
					"removeGuildRank", e);
			throw new MudDatabaseException(
					"database error removing guildrank.", e);
		}
	}

	/**
	 * retrieves the ignore list from the database.
	 * 
	 * @param aUser
	 *            the player to be ignored.
	 * @throws MudException
	 *             if there is a problem determining the list of ignores.
	 */
	public static TreeMap<String, String> getIgnoreList(User aUser) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (aUser == null)
		{
			throw new MudDatabaseException("user was null.");
		}
		TreeMap<String, String> result = new TreeMap<String, String>();
		ResultSet res;
		try
		{

			PreparedStatement statGetIgnoreList = prepareStatement(sqlGetIgnoreList);
			statGetIgnoreList.setString(1, aUser.getName());
			res = statGetIgnoreList.executeQuery();
			if (res != null)
			{
				while (res.next())
				{
					result.put(res.getString("fromperson"), null);
				}
				res.close();
			}
			statGetIgnoreList.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error while retrieving guildranks.", e);
		}
		return result;
	}

	/**
	 * add a guild rank.
	 * 
	 * @param aGuild
	 *            the guild of which a new rank must be added.
	 * @param aRank
	 *            the rank to be added.
	 */
	public static void addIgnore(User aUser, User aToBeIgnored)
			throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement statAddIgnore = prepareStatement(sqlAddIgnore);
			statAddIgnore.setString(1, aUser.getName());
			statAddIgnore.setString(2, aToBeIgnored.getName());
			int res = statAddIgnore.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			statAddIgnore.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database", "addGuildRank",
					e);
			throw new MudDatabaseException("database error adding guildrank.",
					e);
		}
	}

	/**
	 * remove a guild rank.
	 * 
	 * @param aGuild
	 *            the guild of which a rank must be deleted.
	 * @param aRank
	 *            the rank to be deleted.
	 */
	public static void removeIgnore(User aUser, User aToBeAcknowledged)
			throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement statRemoveIgnore = prepareStatement(sqlDelIgnore);
			statRemoveIgnore.setString(1, aUser.getName());
			statRemoveIgnore.setString(2, aToBeAcknowledged.getName());
			int res = statRemoveIgnore.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
				// guildrank does not exist
			}
			statRemoveIgnore.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database",
					"removeGuildRank", e);
			throw new MudDatabaseException(
					"database error removing guildrank.", e);
		}
	}

	/**
	 * Moved the logs from mm_log over to mm_oldlog if they're older than a day.
	 * 
	 * @throws MudException
	 *             throws a mudexception if something's wrong with teh database.
	 */
	public static void moveLogsToOld() throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement statMoveLog1 = prepareStatement(sqlMoveLogs1);
			statMoveLog1.executeUpdate();
			statMoveLog1.close();
			PreparedStatement statMoveLog2 = prepareStatement(sqlMoveLogs2);
			statMoveLog2.executeUpdate();
			statMoveLog2.close();
		} catch (SQLException e)
		{
			Logger.getLogger("mmud").throwing("mmud.Database", "moveLogsToOld",
					e);
			throw new MudDatabaseException("database error moving old logs.", e);
		}
	}

	/**
	 * returns an vector of Persons that contain the members of a guild.
	 * 
	 * @return String array of guild members.
	 * @param aGuild
	 *            the guild
	 * @throws MudException
	 *             if something goes wrong.
	 */
	public static Vector<Person> getGuildMembers(Guild aGuild) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (aGuild == null)
		{
			throw new MudDatabaseException("guild was null.");
		}
		Vector<Person> list = new Vector<Person>();
		ResultSet res;
		try
		{

			PreparedStatement statGetGuildMembers = prepareStatement(sqlGetGuildMembers);
			statGetGuildMembers.setString(1, aGuild.getName());
			res = statGetGuildMembers.executeQuery();
			if (res != null)
			{
				while (res.next())
				{
					list.add(Persons.getPerson(res.getString("name")));
				}
			}
			res.close();
			statGetGuildMembers.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error while retrieving guild members.", e);
		}
		return list;
	}

	/**
	 * returns an vector of Strings that contain the characters that wish to
	 * become a member of a guild.
	 * 
	 * @return String array of guild hopefuls.
	 * @param aGuild
	 *            the guild
	 * @throws MudException
	 *             if something goes wrong.
	 */
	public static Vector<String> getGuildHopefuls(Guild aGuild) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (aGuild == null)
		{
			throw new MudDatabaseException("guild was null.");
		}
		Vector<String> list = new Vector<String>();
		ResultSet res;
		try
		{

			PreparedStatement statGetGuildHopefuls = prepareStatement(sqlGetGuildHopefuls);
			statGetGuildHopefuls.setString(1, aGuild.getName());
			res = statGetGuildHopefuls.executeQuery();
			if (res != null)
			{
				while (res.next())
				{
					list.add(res.getString("charname"));
				}
			}
			res.close();
			statGetGuildHopefuls.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException(
					"database error while retrieving guild hopefuls.", e);
		}
		return list;
	}

	/**
	 * set the pkill of a character. If it is set to on, it means the player can
	 * attack other players and vice versa.
	 * 
	 * @param aUser
	 *            User with new whimpy setting
	 */
	public static void setPkill(User aUser) throws MudException
	{
		Logger.getLogger("mmud").finer("");

		try
		{

			PreparedStatement sqlSetPkillUser = prepareStatement(sqlUpdatePkillString);
			sqlSetPkillUser.setInt(1, (aUser.isPkill() ? 1 : 0));
			sqlSetPkillUser.setString(2, aUser.getName());
			int res = sqlSetPkillUser.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			sqlSetPkillUser.close();
		} catch (SQLException e)
		{
			throw new MudDatabaseException("database error setting pkill.", e);
		}
	}

	/**
	 * write a log message to the database. This log facility is primarily used
	 * to keep a record of what kind of important mutations are done or
	 * attempted by both characters as well as administrators. Some examples:
	 * <ul>
	 * <li>an item is picked up off the floor by a character
	 * <li>an item is eaten
	 * <li>an administrator creates a new item/room/character
	 * </ul>
	 * 
	 * @param aName
	 *            the name of the person to be inscribed in the log table
	 * @param aMessage
	 *            the message to be written in the log, may not be larger than
	 *            255 characters.
	 */
	public static void writeLog(String aName, String aMessage)
	{
		Logger.getLogger("mmud").finer("");

		try
		{
			PreparedStatement sqlWriteLog = prepareStatement(sqlWriteLogString);
			sqlWriteLog.setString(1, aName);
			sqlWriteLog.setString(2, aMessage);
			int res = sqlWriteLog.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			sqlWriteLog.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		} catch (MudDatabaseException e2)
		{
			e2.printStackTrace();
		}
	}

	/**
	 * write a command to the database. This log facility is primarily used
	 * to keep a chatrecord.
	 * 
	 * @param aName
	 *            the name of the person to be inscribed in the log table
	 * @param aCommand
	 *            the command that is to be executed written in the log, may not be larger than
	 *            255 characters.
	 */
	public static void writeCommandLog(String aName, String aCommand)
	{
		Logger.getLogger("mmud").finer("");

		try
		{
			PreparedStatement sqlWriteLog = prepareStatement(sqlWriteCommandLogString);
			sqlWriteLog.setString(1, aName);
			sqlWriteLog.setString(2, aCommand);
			int res = sqlWriteLog.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			sqlWriteLog.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		} catch (MudDatabaseException e2)
		{
			e2.printStackTrace();
		}
	}

	/**
	 * write a log message of an exception to the database.
	 * 
	 * @param aName
	 *            the name of the person to be inscribed in the log table
	 * @param aThrowable
	 *            the exception or error to be written to the log table.
	 */
	public static void writeLog(String aName, Throwable aThrowable)
	{
		Logger.getLogger("mmud").finer("");

		ByteArrayOutputStream myStream = new ByteArrayOutputStream();
		PrintStream myPrintStream = new PrintStream(myStream);
		aThrowable.printStackTrace(myPrintStream);
		myPrintStream.close();
		// myStream.close();
		try
		{
			PreparedStatement sqlWriteLog = prepareStatement(sqlWriteLog2String);
			sqlWriteLog.setString(1, aName);
			sqlWriteLog.setString(2, aThrowable.toString());
			sqlWriteLog.setString(3, myStream.toString());
			int res = sqlWriteLog.executeUpdate();
			if (res != 1)
			{
				// error, not correct number of results returned
				// TOBEDONE
			}
			sqlWriteLog.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		} catch (MudDatabaseException e2)
		{
			e2.printStackTrace();
		}
	}

}
