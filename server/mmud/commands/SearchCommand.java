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
 * Retrieve item from another item in the room: "search shrubbery".
 * Basically, you search <i>inside</i> an item for items.
 * The first item found will be returned into your inventory, if possible.
 */
public class SearchCommand extends NormalCommand
{
	private String name;
	private String adject1;
	private String adject2;
	private String adject3;
	private int amount;

	public SearchCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	/**
	 * This method will make a <I>best effort</I> regarding searching for
	 * items inside items. The first item in the container that 
	 * we are able to get is returned.
	 * @throws ItemException in case the item requested could not be located
	 * or is not allowed to be retrieved from the container.
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
			Vector stuff = Constants.parseItemDescription(myParsed, 1, myParsed.length - 1);
			amount = 1;
			adject1 = (String) stuff.elementAt(1);
			adject2 = (String) stuff.elementAt(2);
			adject3 = (String) stuff.elementAt(3);
			name = (String) stuff.elementAt(4);

			Vector myItems = 
				aUser.getRoom().getItems(adject1, adject2, adject3, name);
			if (myItems.size() < amount)
			{
				aUser.writeMessage("You search but find nothing.<BR>\r\n");
				return true;
			}
			Item aContainer = (Item) myItems.elementAt(0);
			if (!aContainer.isAttribute("container"))
			{
				aUser.writeMessage("You search " + aContainer.getDescription() + " but find nothing.<BR>\r\n");
				aUser.sendMessage(aUser.getName() + " searches " + aContainer.getDescription() + " but finds nothing.<BR>\r\n");
				return true;
			}
			myItems = ItemsDb.getItemsFromContainer(aContainer);
			if (myItems.size() == 0)
			{
				aUser.writeMessage("You search " + aContainer.getDescription() + " but find nothing.<BR>\r\n");
				aUser.sendMessage(aUser.getName() + " searches " + aContainer.getDescription() + " but finds nothing.<BR>\r\n");
				return true;
			}
			Item firstItem = (Item) myItems.elementAt(0);
			// here needs to be a check for validity of the item
			Database.writeLog(aUser.getName(), "searched " + aContainer + " in room " + aUser.getRoom().getId() + " and found " + firstItem);
			ItemsDb.deleteItemFromContainer(firstItem);
			ItemsDb.addItemToChar(firstItem, aUser);
			aUser.sendMessage(aUser.getName() + " searches " + aContainer.getDescription() + " and finds " + firstItem.getDescription() + ".<BR>\r\n");
			aUser.writeMessage("You search " + aContainer.getDescription() + " and find " + firstItem.getDescription() + ".<BR>\r\n");
			return true;
		}
		return false;
	}

}
