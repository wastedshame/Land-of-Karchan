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
import java.util.Vector;

import mmud.*;
import mmud.characters.*;
import mmud.items.*;
import mmud.rooms.*;
import mmud.database.*;

/**
 * Put an item into a container: "put ring in sack".
 * Requirements for it to be successfull:
 * <ul><li>the item to put must be in your inventory
 * <li>the container must be in your inventory or in the room
 * <li>the container must be a container
 * </ul>
 * The possible syntax can range from: "put ring in sack" to
 * "put 8 old gold shiny ring in new leather beaten sack".
 */
public class PutCommand extends NormalCommand
{
	private String name;
	private String adject1;
	private String adject2;
	private String adject3;
	private int amount;

	public PutCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	/**
	 * This method will make a <I>best effort</I> regarding transferring of
	 * items into items. 
	 * @throws ItemException in case the item requested could not be located
	 * or is not allowed to be put into the container.
	 * @throws ParseException in case the user entered an illegal amount of
	 * items. Illegal being defined as smaller than 1.
	 */
	public boolean run(User aUser)
	throws ItemException, ParseException, MudException
	{
		Logger.getLogger("mmud").finer("");
		if (!super.run(aUser))
		{
			return false;
		}
		// initialise string, important otherwise previous instances will return this
		String[] myParsed = getParsedCommand();
		if (myParsed.length > 1)
		{
			// check for in.
			int intopos = 0;
			while (!myParsed[intopos].equalsIgnoreCase("in")) 
			{
				intopos++;
			}
			Vector stuff = Constants.parseItemDescription(myParsed, 1, intopos - 1);
			amount = ((Integer) stuff.elementAt(0)).intValue();
			adject1 = (String) stuff.elementAt(1);
			adject2 = (String) stuff.elementAt(2);
			adject3 = (String) stuff.elementAt(3);
			name = (String) stuff.elementAt(4);

			Vector myItems = 
				aUser.getItems(adject1, adject2, adject3, name);
			if (myItems.size() < amount)
			{
				if (amount == 1)
				{
					aUser.writeMessage("You do not have that item.<BR>\r\n");
				}
				else
				{
					aUser.writeMessage("You do not have that many items.<BR>\r\n");
				}
				return true;
			}

			stuff = Constants.parseItemDescription(myParsed, intopos+1, myParsed.length - intopos - 1);
			adject1 = (String) stuff.elementAt(1);
			adject2 = (String) stuff.elementAt(2);
			adject3 = (String) stuff.elementAt(3);
			name = (String) stuff.elementAt(4);
			Room someRoom = null;

			Vector myContainers = 
				aUser.getItems(adject1, adject2, adject3, name);
			if (myContainers.size() < 1)
			{
				myContainers = 
					aUser.getRoom().getItems(adject1, adject2, adject3, name);
				someRoom = aUser.getRoom(); // usefull for the writeLog.
				if (myContainers.size() < 1)
				{
					aUser.writeMessage("You do not have that container.<BR>\r\n");
					return true;
				}
			}
			Item aContainer = (Item) myContainers.elementAt(0);
			if (!(aContainer instanceof Container))
			{
				aUser.writeMessage(aContainer.getDescription() + " is not a container.<BR>\r\n");
				return true;
			}

			Container myCon = (Container) aContainer;
			if (!myCon.isOpen())
			{
				aUser.writeMessage(aContainer.getDescription() + " is closed.<BR>\r\n");
				return true;
			}

			int j = 0;
			for (int i = 0; ((i < myItems.size()) && (j != amount)); i++)
			{
				// here needs to be a check for validity of the item
				boolean success = true;
				Item myItem = (Item) myItems.elementAt(i);
				Database.writeLog(aUser.getName(), "put " + myItem + " in container " + aContainer + (someRoom != null? " in room " + someRoom.getId() : ""));
				ItemsDb.deleteItemFromChar(myItem);
				ItemsDb.addItemToContainer(myItem, aContainer);
				Persons.sendMessage(aUser, "%SNAME put%VERB2 " + myItem.getDescription() + " in " + aContainer.getDescription() + ".<BR>\r\n");
				j++;
			}
			return true;
		}
		return false;
	}

	public Command createCommand(String aRegExpr)
	{
		return new PutCommand(aRegExpr);
	}
	
}
