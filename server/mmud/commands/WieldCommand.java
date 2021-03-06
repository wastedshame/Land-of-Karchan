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
import mmud.items.Item;
import mmud.items.ItemException;
import mmud.items.PersonPositionEnum;

/**
 * Starts you wielding an item. Syntax: wield &lt;item&gt; with
 * &lt;lefthand|righthand|both|hands|bothhandsd&gt;
 */
public class WieldCommand extends NormalCommand
{

	public WieldCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	@Override
	public boolean run(User aUser) throws ItemException, ParseException,
			MudException
	{
	        if (Constants.debugOn(aUser.getName()))
		{   
		        Logger.getLogger("mmud_debug").finest("run");
		}
		Logger.getLogger("mmud").finer("");
		// initialise string, important otherwise previous instances will return
		// this
		String[] myParsed = getParsedCommand();
		// determine the appropriate body position entered by the
		// user
		String pos = myParsed[myParsed.length - 1];
		PersonPositionEnum position = null;
		if (pos.equalsIgnoreCase("lefthand"))
		{
			position = PersonPositionEnum.WIELD_LEFT;
		} else if (pos.equalsIgnoreCase("righthand"))
		{
			position = PersonPositionEnum.WIELD_RIGHT;
		} else if (pos.equalsIgnoreCase("hands"))
		{
			position = PersonPositionEnum.WIELD_BOTH;
		} else if (pos.equalsIgnoreCase("both"))
		{
			position = PersonPositionEnum.WIELD_BOTH;
		} else if (pos.equalsIgnoreCase("bothhands"))
		{
			position = PersonPositionEnum.WIELD_BOTH;
		} else
		{
			aUser.writeMessage("Cannot wield something that way.<BR>\r\n");
			return true;
		}

		Logger.getLogger("mmud").finer("position=" + position);
		// we need to seriously do something about this.
		// oh well, works for now I guess.
		/* check to see if something is already being wielded there. */
		Item alreadyWieldedItem = null;
		Item alreadyWieldedItem1 = aUser.isWorn(PersonPositionEnum.WIELD_LEFT);
		Item alreadyWieldedItem2 = aUser.isWorn(PersonPositionEnum.WIELD_RIGHT);
		Item alreadyWieldedItem3 = aUser.isWorn(PersonPositionEnum.WIELD_BOTH);
		PersonPositionEnum alreadyPosition = null;
		if (position == PersonPositionEnum.WIELD_BOTH)
		{
			// if we wish to wield something in both hands
			// we need to check that neither hand is occupied
			// nor both are occupied.
			if (alreadyWieldedItem1 != null)
			{
				Logger.getLogger("mmud").finer(
						"already wielding something in lefthand");
				alreadyWieldedItem = alreadyWieldedItem1;
				alreadyPosition = PersonPositionEnum.WIELD_LEFT;
			}
			if (alreadyWieldedItem2 != null)
			{
				Logger.getLogger("mmud").finer(
						"already wielding something in righthand");
				alreadyWieldedItem = alreadyWieldedItem2;
				alreadyPosition = PersonPositionEnum.WIELD_RIGHT;
			}
			if (alreadyWieldedItem3 != null)
			{
				Logger.getLogger("mmud").finer(
						"already wielding something in both hands");
				alreadyWieldedItem = alreadyWieldedItem3;
				alreadyPosition = PersonPositionEnum.WIELD_BOTH;
			}
		}
		if (position == PersonPositionEnum.WIELD_LEFT)
		{
			// if we wish to wield something in left hand
			// we need to check that left hand nor both hands
			// are occupied
			if (alreadyWieldedItem1 != null)
			{
				Logger.getLogger("mmud").finer(
						"already wielding something in lefthand");
				alreadyWieldedItem = alreadyWieldedItem1;
				alreadyPosition = PersonPositionEnum.WIELD_LEFT;
			}
			if (alreadyWieldedItem3 != null)
			{
				Logger.getLogger("mmud").finer(
						"already wielding something in both hands");
				alreadyWieldedItem = alreadyWieldedItem3;
				alreadyPosition = PersonPositionEnum.WIELD_BOTH;
			}
		}
		if (position == PersonPositionEnum.WIELD_RIGHT)
		{
			// if we wish to wield something in right hand
			// we need to check that right hand nor both hands
			// are occupied
			if (alreadyWieldedItem2 != null)
			{
				Logger.getLogger("mmud").finer(
						"already wielding something in righthand");
				alreadyWieldedItem = alreadyWieldedItem2;
				alreadyPosition = PersonPositionEnum.WIELD_RIGHT;
			}
			if (alreadyWieldedItem3 != null)
			{
				Logger.getLogger("mmud").finer(
						"already wielding something in both hands");
				alreadyWieldedItem = alreadyWieldedItem3;
				alreadyPosition = PersonPositionEnum.WIELD_BOTH;
			}
		}

		if (alreadyWieldedItem != null)
		{
			String stuff2 = "You are already wielding "
					+ alreadyWieldedItem.getDescription() + " "
					+ alreadyPosition + ".<BR>\r\n";
			stuff2 = stuff2.replaceAll("%SHISHER", "your");
			aUser.writeMessage(stuff2);
			return true;
		}
		// check for item in posession
		Vector stuff = Constants.parseItemDescription(myParsed, 1,
				myParsed.length - 3);
		int amount = ((Integer) stuff.elementAt(0)).intValue();
		String adject1 = (String) stuff.elementAt(1);
		String adject2 = (String) stuff.elementAt(2);
		String adject3 = (String) stuff.elementAt(3);
		String name = (String) stuff.elementAt(4);

		Vector myItems = aUser.getItems(adject1, adject2, adject3, name);
		if (myItems.size() == 0)
		{
			aUser.writeMessage("You do not have that item.<BR>\r\n");
			return true;
		}
		int j = 0;
		for (int i = 0; ((i < myItems.size()) && (j != amount)); i++)
		{
			// here needs to be a check for validity of the item
			boolean success = true;
			Item myItem = (Item) myItems.elementAt(i);
			if (myItem.getWearing() != null)
			{
				success = false;
			}
			if (!myItem.isWearable(position))
			{
				success = false;
			}
			if (success)
			{
				// transfer item to other person
				myItem.setWearing(position);
				Database.writeLog(aUser.getName(), "wields " + myItem + " "
						+ position);
				Persons.sendMessage(aUser, "%SNAME wield%VERB2 "
						+ myItem.getDescription() + " " + position
						+ ".<BR>\r\n");
				return true;
			}
		}
		return false;
	}

	public Command createCommand()
	{
		return new WieldCommand(getRegExpr());
	}

}
