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
package mmud.commands;  

import java.util.logging.Logger;

import mmud.*;
import mmud.characters.*;
import mmud.items.*;
import mmud.rooms.*;
import mmud.database.*;

/**
 * Go down below: "down".
 * This is the same command as the command "go down", except the other
 * way to execute it.
 */
public class DownCommand extends NormalCommand
{

	public DownCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	public boolean run(User aUser)
	throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (!super.run(aUser))
		{
			return false;
		}
		Room myRoom = aUser.getRoom();
		if (myRoom.getDown() != null)
		{
			aUser.writeMessage("You leave down.<BR>\r\n");
			aUser.sendMessage(aUser.getName() + " leaves down.<BR>\r\n");
			aUser.setRoom(myRoom.getDown());
			aUser.sendMessage(aUser.getName() + " appears.<BR>\r\n");
		}
		else
		{
			aUser.writeMessage("You cannot go down.<BR>\r\n");
		}
		return true;
	}

}