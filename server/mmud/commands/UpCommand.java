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

import mmud.MudException;
import mmud.characters.Persons;
import mmud.characters.User;
import mmud.rooms.Room;

/**
 * Moves the character to the room above: "up". Is the same as "go up".
 * 
 * @see DownCommand
 */
public class UpCommand extends NormalCommand
{

	public UpCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	@Override
	public boolean run(User aUser) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		Room myRoom = aUser.getRoom();
		if (myRoom.getUp() != null)
		{
			Persons.sendMessageExcl(aUser, "%SNAME leave%VERB2 up.<BR>\r\n");
			aUser.setRoom(myRoom.getUp());
			Persons.sendMessageExcl(aUser, "%SNAME appear%VERB2.<BR>\r\n");
		} else
		{
			aUser.writeMessage("You cannot go up.<BR>\r\n");
		}
		return true;
	}

	public Command createCommand()
	{
		return new UpCommand(getRegExpr());
	}

}
