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

public class QuitCommand extends NormalCommand
{

	private String theResult;

	public QuitCommand(String aRegExpr)
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
		Persons.sendMessage(aUser, "%SNAME left the game.<BR>\r\n");
		try
		{
			MailDb.resetNewMailFlag(aUser);
			Persons.deactivateUser(aUser);
			Database.writeLog(aUser.getName(), "left the game.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			theResult = Constants.readFile(Constants.goodbyefile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			theResult = Constants.goodbyemessage;
		}
		return true;
	}

	public String getResult()
	{
		Logger.getLogger("mmud").finer("");
		return theResult;
	}

	public Command createCommand(String aRegExpr)
	{
		return new QuitCommand(aRegExpr);
	}
	
}
