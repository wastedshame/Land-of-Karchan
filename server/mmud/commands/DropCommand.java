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

import java.util.Vector;
import java.util.logging.Logger;

import mmud.Constants;
import mmud.MudException;
import mmud.ParseException;
import mmud.characters.Persons;
import mmud.characters.User;
import mmud.database.Database;
import mmud.database.ItemsDb;
import mmud.items.Item;
import mmud.items.ItemException;

/**
 * Drop an item onto the floor: "drop bucket".
 * 
 * @see GetCommand
 */
public class DropCommand extends NormalCommand
{
	private String name;
	private String adject1;
	private String adject2;
	private String adject3;
	private int amount;

	public DropCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	/**
	 * This method will make a <I>best effort</I> regarding dropping of the
	 * requested items. This means that, if you request 5 items, and there are 5
	 * or more items in your inventory, this method will attempt to aquire 5
	 * items. It is possible that not all items are available, in which case you
	 * could conceivably only receive 3 items or instance.
	 * 
	 * @throws ItemException
	 *             in case the item requested could not be located or is not
	 *             allowed to be dropped.
	 * @throws ParseException
	 *             in case the user entered an illegal amount of items. Illegal
	 *             being defined as smaller than 1.
	 */
	@Override
	public boolean run(User aUser) throws ItemException, ParseException,
			MudException
	{
		Logger.getLogger("mmud").finer("");
		// initialise string, important otherwise previous instances will return
		// this
		String[] myParsed = getParsedCommand();
		if (myParsed.length > 1)
		{
			Vector stuff = Constants.parseItemDescription(myParsed, 1,
					myParsed.length - 1);
			amount = ((Integer) stuff.elementAt(0)).intValue();
			adject1 = (String) stuff.elementAt(1);
			adject2 = (String) stuff.elementAt(2);
			adject3 = (String) stuff.elementAt(3);
			name = (String) stuff.elementAt(4);

			Vector myItems = aUser.getItems(adject1, adject2, adject3, name);
			if (myItems.size() < amount)
			{
				if (amount == 1)
				{
					aUser
							.writeMessage("You cannot find that item in your inventory.<BR>\r\n");
					return true;
				} else
				{
					aUser
							.writeMessage("You cannot find that many items in your inventory.<BR>\r\n");
					return true;
				}
			}
			int j = 0;
			for (int i = 0; ((i < myItems.size()) && (j != amount)); i++)
			{
				// here needs to be a check for validity of the item
				boolean valid = true;
				Item myItem = (Item) myItems.elementAt(i);
				if (myItem.isWearing())
				{
					aUser
							.writeMessage("You are wearing or wielding that item.<BR>\r\n");
					valid = false;
				}
				if (myItem.isAttribute("notdropable"))
				{
					aUser.writeMessage("You cannot drop that item.<BR>\r\n");
					valid = false;
				}
				if (valid)
				{
					Database.writeLog(aUser.getName(), "dropped " + myItem
							+ " into room " + aUser.getRoom().getId());
					ItemsDb.deleteItemFromChar(myItem);
					ItemsDb.addItemToRoom(myItem, aUser.getRoom());
					Persons.sendMessage(aUser, "%SNAME drop%VERB2 "
							+ myItem.getDescription() + ".<BR>\r\n");
					j++;
				}
			}
			return true;
		}
		return false;
	}

	public Command createCommand()
	{
		return new DropCommand(getRegExpr());
	}

}
