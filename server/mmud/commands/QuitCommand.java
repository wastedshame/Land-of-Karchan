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

public class QuitCommand implements Command
{

	public boolean run(User aUser, String command)
	{
		if (Constants.logging)
		{
			System.err.println("QuitCommand.run " + aUser + "," + command);
		}
		aUser.sendMessage(aUser.getName() + " left the game.<BR>\r\n");
		try
		{
			Characters.deactivateUser(aUser);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	public String getResult()
	{
		if (Constants.logging)
		{
			System.err.println("QuitCommand.getResult");
		}
		try
		{
			String aString = Constants.readFile(Constants.goodbyefile);
			return aString;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return Constants.goodbyemessage;
	}

}
