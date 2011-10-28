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

import mmud.exceptions.MmudException;
import mmud.database.entities.Persons;
import mmud.database.entities.Player;
import mmud.rooms.Room;

/**
 * Go south: "south".
 * 
 * @see GoCommand
 */
public class SouthCommand extends NormalCommand
{

	public SouthCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	@Override
	public boolean run(Player aPlayer) throws MmudException
	{
		Logger.getLogger("mmud").finer("");
		Room myRoom = aPlayer.getRoom();
		if (myRoom.getSouth() != null)
		{
			Persons.sendMessageExcl(aPlayer, "%SNAME leave%VERB2 south.<BR>\r\n");
			aPlayer.setRoom(myRoom.getSouth());
			Persons.sendMessageExcl(aPlayer, "%SNAME appear%VERB2.<BR>\r\n");
		} else
		{
			aPlayer.writeMessage("You cannot go south.<BR>\r\n");
		}
		return true;
	}

	public Command createCommand()
	{
		return new SouthCommand(getRegExpr());
	}

}
