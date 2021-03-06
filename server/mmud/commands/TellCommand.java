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
package mmud.commands;

import java.util.logging.Logger;

import mmud.Constants;
import mmud.MudException;
import mmud.characters.CommunicationListener;
import mmud.characters.Person;
import mmud.characters.Persons;
import mmud.characters.User;
import mmud.characters.CommunicationListener.CommType;

/**
 * Tell to someone something. The difference with normal communication commands,
 * is that this one is mandatory to a person and the person does not need to be
 * in the same room: "tell to Karn Help!".
 */
public class TellCommand extends CommunicationCommand
{

	public TellCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	@Override
	/*
	 * Once again, this one is special, because tell works across all rooms, and
	 * is only ever targeted to only one person directly.
	 */
	public boolean run(User aUser) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		String command = getCommand();
		String[] myParsed = Constants.parseCommand(command);
		if (myParsed.length <= 3 || (!myParsed[1].equalsIgnoreCase("to")))
		{
			return false;
		}
		Person toChar = Persons.retrievePerson(myParsed[2]);
		if (toChar == null)
		{
			aUser.writeMessage("Cannot find that person.<BR>\r\n");
			return true;
		}
		if (aUser.isIgnored(toChar))
		{
			aUser.writeMessage(toChar.getName()
					+ " is ignoring you fully.<BR>\r\n");
			return true;
		}
		setMessage(command.substring(
				command.indexOf(myParsed[3], getCommType().toString().length()
						+ 1 + 2 + 1 + myParsed[2].length())).trim());
		aUser.writeMessage("<B>You tell " + toChar.getName() + "</B> : "
				+ getMessage() + "<BR>\r\n");
		toChar.writeMessage("<B>" + aUser.getName() + " tells you</B> : "
				+ getMessage() + "<BR>\r\n");
		if (toChar instanceof CommunicationListener)
		{
			((CommunicationListener) toChar).commEvent(aUser,
					CommunicationListener.TELL, getMessage());
		}
		return true;
	}

	@Override
	public Command createCommand()
	{
		return new TellCommand(getRegExpr());
	}

	@Override
	public CommType getCommType()
	{
		return CommType.TELL;
	}

}
