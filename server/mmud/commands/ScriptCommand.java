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
 * The Script Command. Runs a script.
 */
public class ScriptCommand extends NormalCommand
{
	private String theMethodName;

	private String theResult;

	private Integer theRoom;

	public ScriptCommand(String aRegExpr, String aMethodName, Integer aRoom)
	{
		super(aRegExpr);
		theMethodName = aMethodName;
		theRoom = aRoom;
	}

	public ScriptCommand(String aRegExpr, String aMethodName)
	{
		super(aRegExpr);
		theMethodName = aMethodName;
	}

	public boolean run(User aUser)
	throws MudException
	{
		Logger.getLogger("mmud").finer("");
		if (!super.run(aUser))
		{
			return false;
		}
		if ( (theRoom != null) && 
			(aUser.getRoom().getId() != theRoom.intValue()) )
		{
			return false;
		}
		String mySource = Database.getMethodSource(theMethodName);
		if (mySource == null)
		{
			throw new MethodDoesNotExistException(" (" + theMethodName + ")");
		}
		Object stuff = aUser.runScript("command", mySource, getParsedCommand());
		if (!(stuff instanceof String))
		{
			theResult = null;
			return true;
		}
		theResult = (String) stuff;
		if ("false".equalsIgnoreCase(theResult))
		{
			theResult = null;
			return false;
		}
		theResult +=  aUser.printForm();
		return true;
	}

	public String getResult()
	{
		Logger.getLogger("mmud").finer("");
		return theResult;
	}

	/**
	 * There are too many parameters required for this one
	 * to be automatically created using the standard.
	 * Don't use this method.
	 * @return null value, always
	 */
	public Command createCommand(String aRegExpr)
	{
		return null;
	}
	
}
