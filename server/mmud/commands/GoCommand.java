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

import mmud.*;
import mmud.characters.*;
import mmud.items.*;
import mmud.rooms.*;
import mmud.database.*;

public class GoCommand extends NormalCommand
{

	public boolean run(User aUser, String command)
	{
		if (Constants.logging)
		{
			System.err.println("GoCommand.run " + aUser + "," + command);
		}
		String[] myParsed = Constants.parseCommand(command);
		if (myParsed.length == 2)
		{
			if (myParsed[1].equalsIgnoreCase("south"))
			{
				return (new SouthCommand()).run(aUser, command);
			}
			if (myParsed[1].equalsIgnoreCase("north"))
			{
				return (new NorthCommand()).run(aUser, command);
			}
			if (myParsed[1].equalsIgnoreCase("west"))
			{
				return (new WestCommand()).run(aUser, command);
			}
			if (myParsed[1].equalsIgnoreCase("east"))
			{
				return (new EastCommand()).run(aUser, command);
			}
			if (myParsed[1].equalsIgnoreCase("up"))
			{
				return (new UpCommand()).run(aUser, command);
			}
			if (myParsed[1].equalsIgnoreCase("down"))
			{
				return (new DownCommand()).run(aUser, command);
			}
		}
		return false;
	}

}
