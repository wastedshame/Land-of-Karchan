/*-------------------------------------------------------------------------
cvsinfo: $Header$
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

package mmud;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.File;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.MalformedPatternException;

import mmud.characters.*;
import mmud.database.*;
import mmud.commands.*;

public class MudSocket extends Thread
{
	private Socket theSocket = null;
	private boolean theSuccess = true;

	public String isOffline()
		throws IOException, MudException
	{
		if (Constants.offline)
		{
			File myFile = new File(Constants.mudofflinefile);
			if (!myFile.exists())
			{
				throw new MudException("mudofflinefile '" + Constants.mudofflinefile +"' not found.");
			}
			if (!myFile.canRead())
			{
				throw new MudException("mudofflinefile '" + Constants.mudofflinefile + "' unreadable.");
			}
			return Constants.readFile(Constants.mudofflinefile);
		}
		return null;
	}

	public boolean isSuccessfull()
	{
		if (Constants.logging)
		{
			System.err.println("MudSocket.isSuccessFull");
		}
		return theSuccess;
	}

	public MudSocket(Socket aSocket)
	{
		if (Constants.logging)
		{
			System.err.println("MudSocket.MudSocket");
		}
		theSocket = aSocket;
	}

	public void run()
	{
		if (Constants.logging)
		{
			System.err.println("MudSocket.run");
		}
		PrintWriter myOutputStream = null;  
		BufferedReader myInputStream = null;
		try
		{
			myOutputStream = new PrintWriter(theSocket.getOutputStream(), true);
			myInputStream = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));
			
		} catch (UnknownHostException e) 
		{
			System.err.println("Don't know about host.");
			theSuccess = false;
			return;
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for "
			+ "the connection to: taranis.");
			theSuccess = false;
			return;
		}

		try
		{
			StringBuffer userInput = new StringBuffer();

			myOutputStream.println(Constants.IDENTITY);
			try
			{
				String myAction = myInputStream.readLine();
				if (myAction.equals("logon"))
				{
		 			myOutputStream.println(
						enterMud(myInputStream.readLine(),
							myInputStream.readLine(),
							theSocket.getInetAddress().getCanonicalHostName(),
							myInputStream.readLine(),
							Integer.parseInt(myInputStream.readLine())));
				}
				if (myAction.equals("mud"))
				{
					while (true)
					{
					myOutputStream.println(
						executeMud("qwerty", 
						theSocket.getInetAddress().getCanonicalHostName(),
						"qwerty",
						0,
						myInputStream.readLine()));
					}
//					myOutputStream.println(
//						executeMud(myInputStream.readLine(),
//						theSocket.getInetAddress().getCanonicalHostName(),
//						myInputStream.readLine(),
//						Integer.parseInt(myInputStream.readLine()),
//						myInputStream.readLine()));
				}
				if (myAction.equals("newchar"))
				{
					myOutputStream.println(
						newUserMud(myInputStream.readLine(),
						myInputStream.readLine(),
						theSocket.getInetAddress().getCanonicalHostName(),

						"Maarten van Leunen",
						"maarten_l@yahoo.com",
						"Ruler of Stuff",
						"human",
						Sex.createFromString("male"),
						"old",
						"tall",
						"thin",
						"dark-skinned",
						"yellow-eyed",
						"handsome",
						"black-haired",
						"none",
						"none",
						"none",
						myInputStream.readLine(),
						Integer.parseInt(myInputStream.readLine())));
				}
			}
			catch (Exception e)
			{
				myOutputStream.println(e.toString());
			}
//			while (userInput.indexOf("<root>") == -1)
//			{
//				userInput.append(myInputStream.readLine());
//				userInput.append("\n");
//			}
//			Echo myEcho = new Echo();
//			myOutputStream.println(userInput.toString());
//			myEcho.parse(userInput.toString());
//			myOutputStream.println(myEcho.getOutput());
			String expectingOk;
			expectingOk = myInputStream.readLine();
			while (expectingOk != null && !expectingOk.equals("Ok"))
			{
				expectingOk = myInputStream.readLine();
			}
			myOutputStream.close();
			myInputStream.close();
			theSocket.close();
			if (Constants.logging)
			{
				System.err.println("MudSocket.run: closing connection");
			}
		} catch (IOException e)
		{
			theSuccess = false;
			return;
		}
		theSuccess = true;
		return;
	}

	/**
	 * main function for logging into the game<P>
	 * The following checks and tasks are performed:
	 * <OL><LI>check if mud is enabled/disabled
	 * <LI>validate input using perl reg exp.
	 * <LI>check if user is banned
	 * <LI>create new sessionpassword
	 * </OL>
	 * @param socketfd int, is the socketdescriptor of the communication channel,
	 * can using find_in_list for example. 
	 * @see find_in_list
	 * @see remove_from_list
	 * @returns integer with the success code, 0 is bad, 1 is good
	 */
	private String enterMud(String aName, String aPassword, String aAddress, String aCookie, int aFrames)
		throws MudException
	{
		if (Constants.logging)
		{
			System.err.println("MudSocket.enterMud: " + aName + "," + aPassword + "," + aAddress + "," + aCookie + "," + aFrames);
		}
		try
		{
		try
		{
		String myOfflineString = isOffline();
		if (myOfflineString != null) 
		{
			return myOfflineString;
		}
		}
		catch (IOException e)
		{
			throw new MudException(e.getMessage());
		}
		Perl5Compiler myCompiler = new Perl5Compiler();
		Perl5Matcher myMatcher = new Perl5Matcher();
		try
		{
			if (!myMatcher.matches(aName, myCompiler.compile("[A-Z|_|a-z]{3,}")))
			{
				if (Constants.logging)
				{
					System.err.println("MudSocket.enterMud: invalid name " + aName);
				}
				return Constants.logoninputerrormessage;
			}
			if (aPassword.length() < 5)
			{
				if (Constants.logging)
				{
					System.err.println("MudSocket.enterMud: password too short" + aPassword);
				}
				return Constants.logoninputerrormessage;
			}
		}
		catch (MalformedPatternException e)
		{
			// this should not happen
			e.printStackTrace();
		}
		if (Database.isUserBanned(aName, aAddress))
		{
			if (Constants.logging)
			{
				System.err.println("thrown: " + Constants.USERBANNEDERROR);
			}
			throw new MudException(Constants.USERBANNEDERROR);
		}
		User myUser = Characters.activateUser(aName, aPassword, aCookie);
		if (myUser == null)
		{
			// new user
			String myString = "";
			try
			{
			myString = Constants.readFile(Constants.mudnewcharfile);
			}
			catch (IOException e)
			{
				throw new MudException(e.getMessage());
			}
			myString += "<INPUT TYPE=\"hidden\" NAME=\"name\" VALUE=\"" + aName + "\">\n";
			myString += "<INPUT TYPE=\"hidden\" NAME=\"password\" VALUE=\"" + aPassword + "\">\n";
			myString += "<INPUT TYPE=\"hidden\" NAME=\"frames\" VALUE=\"" + (aFrames + 1) + "\">\n";
			myString += "<INPUT TYPE=\"submit\" VALUE=\"Submit\">\n";
			myString += "<INPUT TYPE=\"reset\" VALUE=\"Clear\">\n";
			myString += "</FORM></BODY></HTML>\n";
			return myString;
		}
		myUser.generateSessionPassword();
		myUser.setFrames(aFrames);
		myUser.writeMessage(Database.getLogonMessage());
		if (Database.hasUserNewMail(myUser)) 
		{
			myUser.writeMessage("You have no new Mudmail...<P>\r\n");
		}
		else
		{
			myUser.writeMessage("You have new Mudmail!<P>\r\n");
		}
		String returnStuff;
		switch (myUser.getFrames())
		{
			case 0 :
			{
				returnStuff = "Content-type: text/html\r\n";
				returnStuff += "Set-cookie: Karchan=" + myUser.getSessionPassword() + ";\r\n\r\n";
				returnStuff += gameMain(myUser, "me has entered the game...<BR>\r\n");
				break;
			}
			case 1 :
			{
				returnStuff = "Content-type: text/html\r\n";
				returnStuff += "Set-cookie: Karchan=" + myUser.getSessionPassword() + ";\r\n\r\n";
				returnStuff += "<HTML><HEAD><TITLE>Land of Karchan - " + myUser.getName() + "</TITLE></HEAD>\r\n";
				returnStuff += "<FRAMESET ROWS=\"*,50\">\r\n";
				returnStuff += " <FRAMESET COLS=\"*,180\">\r\n";
				returnStuff += "	 <FRAME SRC=" + Constants.mudcgi + "?command=me+has+entered+the+game...&name=" + myUser.getName() + "&password=" + myUser.getPassword() + "&frames=2 NAME=\"main\" border=0>\r\n";
				returnStuff += "	 <FRAME SRC=" + Constants.leftframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"leftframe\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " </FRAMESET>\r\n";
				returnStuff += " <FRAME SRC=" + Constants.logonframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"logon\" scrolling=\"no\" border=0>\r\n";
				returnStuff += "</FRAMESET>\r\n";
				returnStuff += "</HTML>\r\n";
				break;
			}
			case 2 :
			{
				returnStuff = "Content-type: text/html\r\n";
				returnStuff += "Set-cookie: Karchan=" + myUser.getSessionPassword() + ";\r\n\r\n";
				returnStuff += "<HTML><HEAD><TITLE>Land of Karchan - " + myUser.getName() + "</TITLE></HEAD>\r\n";
				returnStuff += "<FRAMESET ROWS=\"*,50,0,0\">\r\n";
				returnStuff += " <FRAMESET COLS=\"*,180\">\r\n";
				returnStuff += "	 <FRAMESET ROWS=\"60%,40%\">\r\n";
				returnStuff += "	 <FRAME SRC=" + Constants.mudcgi + "?command=me+has+entered+the+game...&name=" + myUser.getName() + "&password=" + myUser.getPassword() + "&frames=3 NAME=\"statusFrame\" border=0>\r\n";
				returnStuff += "	 <FRAME SRC=/karchan/empty.html NAME=\"logFrame\">\r\n";
				returnStuff += "	 </FRAMESET>\r\n";
				returnStuff += " <FRAME SRC=" + Constants.nph_leftframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"leftFrame\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " </FRAMESET>\r\n\r\n";
				returnStuff += " <FRAME SRC=" + Constants.nph_logonframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"commandFrame\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " <FRAME SRC=" + Constants.nph_javascriptframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"javascriptFrame\">\r\n";
				returnStuff += " <FRAME SRC=/karchan/empty.html NAME=\"duhFrame\">\r\n";
				returnStuff += "</FRAMESET>\r\n";
				returnStuff += "</HTML>\r\n";
				break;
			}
			default :
			{
				if (Constants.logging)
				{
					System.err.println("thrown: " + Constants.INVALIDFRAMEERROR);
				}
				throw new CharacterException(Constants.INVALIDFRAMEERROR);
			}
		}
		return returnStuff;
		}
		catch (CharacterException e)
		{
			if (e.getMessage().equals(Constants.USERALREADYACTIVEERROR))
			{
				// already active user wishes to relogin
				return reloginMud(aName, aPassword, aFrames);
			}
			return Database.getErrorMessage(e.getMessage());
		}
		catch (MudException e)
		{
			return Database.getErrorMessage(e.getMessage());
		}
	}


	/**
	 * main function for executing a command in the game<P>
	 * The following checks and tasks are performed:
	 * <OL><LI>check if mud is enabled/disabled
	 * <LI>validate input using perl reg exp.
	 * <LI>check if user is banned
	 * </OL>
	 */
	private String executeMud(String aName, String aAddress, String aCookie, int aFrames, String aCommand)
	{
		if (Constants.logging)
		{
			System.err.println("MudSocket.executeMud: " + aName + "," + aAddress + "," + aCookie + "," + aFrames + "," + aCommand);
		}
		try
		{
		try
		{
		String myOfflineString = isOffline();
		if (myOfflineString != null) 
		{
			return myOfflineString;
		}
		}
		catch (IOException e)
		{
			throw new MudException(e.getMessage());
		}
		Perl5Compiler myCompiler = new Perl5Compiler();
		Perl5Matcher myMatcher = new Perl5Matcher();
		try
		{
			if (!myMatcher.matches(aName, myCompiler.compile("[A-Z|_|a-z]{3,}")))
			{
				if (Constants.logging)
				{
					System.err.println("MudSocket.enterMud: invalid name " + aName);
				}
				return Constants.logoninputerrormessage;
			}
/*			if (myMatcher.matches(aCommand, myCompiler.compile("(.)*([<applet|<script|java-script|CommandForm])+(.)*")))
			{
				if (Constants.logging)
				{
					System.err.println("thrown: " + Constants.INVALIDCOMMANDERROR);
				}
				throw new MudException(Constants.INVALIDCOMMANDERROR);
			}
*/
		}
		catch (MalformedPatternException e)
		{
			// this should not happen
			e.printStackTrace();
		}
		if (Database.isUserBanned(aName, aAddress))
		{
			if (Constants.logging)
			{
				System.err.println("thrown: " + Constants.USERBANNEDERROR);
			}
			throw new MudException(Constants.USERBANNEDERROR);
		}
		Character myChar = Characters.retrieveCharacter(aName);
		if (myChar == null)
		{
			if (Constants.logging)
			{
				System.err.println("thrown: " + Constants.USERNOTFOUNDERROR);
			}
			throw new MudException(Constants.USERNOTFOUNDERROR);
		}
		if (!(myChar instanceof User))
		{
			if (Constants.logging)
			{
				System.err.println("thrown: " + Constants.NOTAUSERERROR);
			}
			throw new MudException(Constants.NOTAUSERERROR);
		}
		User myUser = (User) myChar;
		if (!myUser.verifySessionPassword(aCookie))
		{
			if (Constants.logging)
			{
				System.err.println("thrown: " + Constants.PWDINCORRECTERROR);
			}
			throw new MudException(Constants.PWDINCORRECTERROR);
		}
		myUser.setFrames(aFrames);
		String returnStuff;
		returnStuff = "Content-type: text/html\r\n\r\n";
		returnStuff += gameMain(myUser, aCommand);
		returnStuff += "<HR><FONT Size=1><DIV ALIGN=right>" + Constants.mudcopyright + "<DIV ALIGN=left><P>";
		returnStuff += "</BODY></HTML>";
		return returnStuff;
		}
		catch (CharacterException e)
		{
			return Database.getErrorMessage(e.getMessage());
		}
		catch (MudException e)
		{
			return Database.getErrorMessage(e.getMessage());
		}
	}

	/**
	 * dump a page to the user displaying a <I>relogging in</I> page.
	 */
	private String reloginMud(String aName, String aPassword, int aFrames)
		throws MudException
	{
		String returnStuff;
		returnStuff = "Content-type: text/html\r\n\r\n";
		switch (aFrames)
		{
			case 0 :
			{
				returnStuff += "<HTML><HEAD><TITLE>Error</TITLE></HEAD>\n\n";
				returnStuff += "<BODY>\n";
				returnStuff += "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\"><H1>Already Active</H1><HR>\n";
				returnStuff += "You tried to start a session which is already in progress. You can't play \n";
				returnStuff += "two sessions at the same time! Please check below to try again. In case you \n";
				returnStuff += "accidently turned of your computerterminal or Netscape or Lynx without first \n";
				returnStuff += "having typed <B>QUIT</B> while you were in the MUD, you can reenter the game \n";
				returnStuff += "by using the second link. You have to sit at the same computer as you did \n";
				returnStuff += "when you logged in.<P>\n";
				returnStuff += "<A HREF=\"/karchan/enter.html\">Click here to\n";
				returnStuff += "retry</A><P>\n";
		
				returnStuff += "Do you wish to enter into the active character?<BR><UL><LI>";
				returnStuff += "<A HREF=\"" + Constants.mudcgi + "?command=me+entered+the+game+again...&name=" + aName + "&password=" + aPassword + "&frames=" + aFrames+1 + "\">Yes</A>";
				returnStuff += "<LI><A HREF=\"/karchan/index.html\">No</A></UL>";
				returnStuff += "<HR><FONT Size=1><DIV ALIGN=right>" + Constants.mudcopyright;
				returnStuff += "<DIV ALIGN=left><P>";
				returnStuff += "</body>\n";
				returnStuff += "</HTML>\n";
				break;
			}
			case 1 :
			{
				returnStuff += "<HTML><HEAD><TITLE>Land of Karchan - " + aName + "</TITLE></HEAD>\r\n";
				returnStuff += "<FRAMESET ROWS=\"*,50\">\r\n";
				returnStuff += " <FRAMESET COLS=\"*,180\">\r\n";
				returnStuff += "	 <FRAME SRC=\"/karchan/already_active.html\" NAME=\"main\" border=0>\r\n";
				returnStuff += "	 <FRAME SRC=\"" + Constants.leftframecgi + "?name=" + aName + "&password=" + aPassword + "\" NAME=\"leftframe\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " </FRAMESET>\r\n";
				returnStuff += " <FRAME SRC=\"" + Constants.logonframecgi + "?name=" + aName + "&password=" + aPassword + "\" NAME=\"logon\" scrolling=\"no\" border=0>\r\n";
				returnStuff += "</FRAMESET>\r\n";
				returnStuff += "</HTML>\r\n";
				break;
			}
			case 2 :
			{
				returnStuff += "<HTML><HEAD><TITLE>Land of Karchan - " + aName + "</TITLE></HEAD>\r\n";
				returnStuff += "<FRAMESET ROWS=\"*,50,0,0\">\r\n";
				returnStuff += " <FRAMESET COLS=\"*,180\">\r\n";
				returnStuff += "	 <FRAMESET ROWS=\"60%,40%\">\r\n";
				returnStuff += "	 <FRAME SRC=\"/karchan/already_active.html\" NAME=\"statusFrame\" border=0>\r\n";
				returnStuff += "	 <FRAME SRC=\"/empty.html\" NAME=\"logFrame\">\r\n";
				returnStuff += "	 </FRAMESET>\r\n";
				returnStuff += " <FRAME SRC=\"" + Constants.nph_leftframecgi + "?name=" + aName + "&password=" + aPassword + "\" NAME=\"leftFrame\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " </FRAMESET>\r\n\r\n";
				returnStuff += " <FRAME SRC=\"" + Constants.nph_logonframecgi + "?name=" + aName + "&password=" + aPassword + "\" NAME=\"commandFrame\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " <FRAME SRC=\"" + Constants.nph_javascriptframecgi + "?name=" + aName + "&password=" + aPassword + "\" NAME=\"javascriptFrame\">\r\n";
				returnStuff += " <FRAME SRC=\"/karchan/empty.html\" NAME=\"duhFrame\">\r\n";
				returnStuff += "</FRAMESET>\r\n";
				returnStuff += "</HTML>\r\n";
				break;
			}
			default :
			{
				throw new MudException(Constants.INVALIDFRAMEERROR);
			}
		} 

		return returnStuff;
	}

	/**
	 * create a new user and log him in 
	 */
	private String newUserMud(String aName, String aPassword, String anAddress,
		String aRealName,
		String aEmail,
		String aTitle,
		String aRace,
		Sex aSex,
		String aAge,
		String aLength,
		String aWidth,
		String aComplexion,
		String aEyes,
		String aFace,
		String aHair,
		String aBeard,
		String aArms,
		String aLegs,
		String aCookie, int aFrames)
	{
		if (Constants.logging)
		{
			System.err.println("MudSocket.newUserMud: " + aName + "," + aPassword + "," + anAddress + "," + aCookie + "," + aFrames);
		}
		try
		{
		try
		{
		String myOfflineString = isOffline();
		if (myOfflineString != null) 
		{
			return myOfflineString;
		}
		}
		catch (IOException e)
		{
			throw new MudException(e.getMessage());
		}
		Perl5Compiler myCompiler = new Perl5Compiler();
		Perl5Matcher myMatcher = new Perl5Matcher();
		try
		{
			if (!myMatcher.matches(aName, myCompiler.compile("[A-Z|_|a-z]{3,}")))
			{
				if (Constants.logging)
				{
					System.err.println("MudSocket.enterMud: invalid name " + aName);
				}
				return Constants.logoninputerrormessage;
			}
			if (aPassword.length() < 5)
			{
				if (Constants.logging)
				{
					System.err.println("MudSocket.enterMud: password too short" + aPassword);
				}
				return Constants.logoninputerrormessage;
			}
		}
		catch (MalformedPatternException e)
		{
			// this should not happen
			e.printStackTrace();
		}
		if (Database.isUserBanned(aName, anAddress))
		{
			if (Constants.logging)
			{
				System.err.println("thrown: " + Constants.USERBANNEDERROR);
			}
			throw new MudException(Constants.USERBANNEDERROR);
		}
		User myUser = Characters.createUser(aName,
			aPassword, anAddress, aRealName,
			aEmail,
			aTitle,
			aRace,
			aSex,
			aAge,
			aLength,
			aWidth,
			aComplexion,
			aEyes,
			aFace,
			aHair,
			aBeard,
			aArms,
			aLegs,
			aCookie);
		myUser.generateSessionPassword();
		myUser.setFrames(aFrames);
		myUser.writeMessage(Database.getLogonMessage());
		if (Database.hasUserNewMail(myUser)) 
		{
			myUser.writeMessage("You have no new Mudmail...<P>\r\n");
		}
		else
		{
			myUser.writeMessage("You have new Mudmail!<P>\r\n");
		}
		String returnStuff;
		switch (myUser.getFrames())
		{
			case 0 :
			{
				returnStuff = "Content-type: text/html\r\n";
				returnStuff += "Set-cookie: Karchan=" + myUser.getSessionPassword() + ";\r\n\r\n";
				returnStuff += gameMain(myUser, "me has entered the game...<BR>\r\n");
				break;
			}
			case 1 :
			{
				returnStuff = "Content-type: text/html\r\n";
				returnStuff += "Set-cookie: Karchan=" + myUser.getSessionPassword() + ";\r\n\r\n";
				returnStuff += "<HTML><HEAD><TITLE>Land of Karchan - " + myUser.getName() + "</TITLE></HEAD>\r\n";
				returnStuff += "<FRAMESET ROWS=\"*,50\">\r\n";
				returnStuff += " <FRAMESET COLS=\"*,180\">\r\n";
				returnStuff += "	 <FRAME SRC=" + Constants.mudcgi + "?command=me+has+entered+the+game...&name=" + myUser.getName() + "&password=" + myUser.getPassword() + "&frames=2 NAME=\"main\" border=0>\r\n";
				returnStuff += "	 <FRAME SRC=" + Constants.leftframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"leftframe\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " </FRAMESET>\r\n";
				returnStuff += " <FRAME SRC=" + Constants.logonframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"logon\" scrolling=\"no\" border=0>\r\n";
				returnStuff += "</FRAMESET>\r\n";
				returnStuff += "</HTML>\r\n";
				break;
			}
			case 2 :
			{
				returnStuff = "Content-type: text/html\r\n";
				returnStuff += "Set-cookie: Karchan=" + myUser.getSessionPassword() + ";\r\n\r\n";
				returnStuff += "<HTML><HEAD><TITLE>Land of Karchan - " + myUser.getName() + "</TITLE></HEAD>\r\n";
				returnStuff += "<FRAMESET ROWS=\"*,50,0,0\">\r\n";
				returnStuff += " <FRAMESET COLS=\"*,180\">\r\n";
				returnStuff += "	 <FRAMESET ROWS=\"60%,40%\">\r\n";
				returnStuff += "	 <FRAME SRC=" + Constants.mudcgi + "?command=me+has+entered+the+game...&name=" + myUser.getName() + "&password=" + myUser.getPassword() + "&frames=3 NAME=\"statusFrame\" border=0>\r\n";
				returnStuff += "	 <FRAME SRC=/karchan/empty.html NAME=\"logFrame\">\r\n";
				returnStuff += "	 </FRAMESET>\r\n";
				returnStuff += " <FRAME SRC=" + Constants.nph_leftframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"leftFrame\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " </FRAMESET>\r\n\r\n";
				returnStuff += " <FRAME SRC=" + Constants.nph_logonframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"commandFrame\" scrolling=\"no\" border=0>\r\n";
				returnStuff += " <FRAME SRC=" + Constants.nph_javascriptframecgi + "?name=" + myUser.getName() + "&password=" + myUser.getPassword() + " NAME=\"javascriptFrame\">\r\n";
				returnStuff += " <FRAME SRC=/karchan/empty.html NAME=\"duhFrame\">\r\n";
				returnStuff += "</FRAMESET>\r\n";
				returnStuff += "</HTML>\r\n";
				break;
			}
			default :
			{
				if (Constants.logging)
				{
					System.err.println("thrown: " + Constants.INVALIDFRAMEERROR);
				}
				throw new CharacterException(Constants.INVALIDFRAMEERROR);
			}
		}
		return returnStuff;
		}
		catch (CharacterException e)
		{
			return Database.getErrorMessage(e.getMessage());
		}
		catch (MudException e)
		{
			return Database.getErrorMessage(e.getMessage());
		}
	}

	/**
	 * the main batch of the server. It parses and executes
	 * the command of the user.
	 * @param aUser User who wishes to execute a command.
	 * @param aCommand String containing the command entered
	 * @return returns a string containing the answer.
	 */
	public String gameMain(User aUser, String aCommand)
		throws MudException
	{
		if (Constants.logging)
		{
			System.err.println("MudSocket.gameMain: " + aUser.getName() + "," + aCommand);
		}
		String returnStuff = "<HTML>\n";
		returnStuff += "<HEAD>\n";
		returnStuff += "<TITLE>\n";
		returnStuff += Constants.mudtitle;
		returnStuff += "\n</TITLE>\n";
		returnStuff += "</HEAD>\n";
		switch (aUser.getFrames())
		{
			case 0 :
			{
				returnStuff += "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"" + Constants.mudbackground + "\" onLoad=\"setfocus()\">\n";
				break;
			}
			case 1 :
			{
				returnStuff += "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"" + Constants.mudbackground + "\" OnLoad=\"top.frames[2].document.myForm.command.value='';top.frames[2].document.myForm.command.focus()\">\n";
				break;
			}
			case 2 :
			{
				returnStuff += "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"" + Constants.mudbackground + "\"onLoad=\"top.frames[3].document.myForm.command.value='';top.frames[3].document.myForm.command.focus()\">\n";
				break;
			}
			default :
			{
				if (Constants.logging)
				{
					System.err.println("thrown: " + Constants.INVALIDFRAMEERROR);
				}
				throw new CharacterException(Constants.INVALIDFRAMEERROR);
			}
		} // end switch 
		int i = aCommand.indexOf(' ');
		Command myCommand = 
			Constants.getCommand( (i != -1 ? aCommand.substring(0, i) : aCommand) );
		myCommand.run(aUser, aCommand);
		if (myCommand.getResult() == null)
		{
			returnStuff += aUser.getRoom().getDescription(aUser);
			returnStuff += aUser.printForm();
			returnStuff += aUser.readLog();
		}
		else
		{
			returnStuff += myCommand.getResult();
		}
		return returnStuff;
	}

}
