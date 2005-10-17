/*-------------------------------------------------------------------------
cvsinfo: $Header: /karchan/mud/cvsroot/server/mmud/items/StdItemContainer.java,v 1.2 2004/11/20 08:22:44 karn Exp $
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
package mmud.items;     

import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.Vector;
import java.util.Hashtable;

import mmud.*;
import mmud.characters.*;
import mmud.items.*;
import mmud.rooms.*;
import mmud.database.*;

import simkin.*;

/**
 * An item in the mud that can contain other items.
 * Basically consists of an ItemDefinition and a number of Attributes
 * specific to this item but has some additional properties.
 */
public class StdItemContainer extends Item implements Container
{

	private ItemDef theKeyId;
	private Types theType;

	/**
	 * Create this item object with a default Item Definition and id.
	 * This method is usually only used by the database.
	 * @param anItemDef definition of the container
	 * @param anId integer identification of the item
	 */
	public StdItemContainer(ContainerDef anItemDef, int anId, PersonPositionEnum aPosBody)
	{
		super(anItemDef, anId, aPosBody);
	} 

	/**
	 * Create this item object with a default Item Definition and id.
	 * This method is usually only used by the database.
	 * @param anItemDef definition of the container
	 * @param anId integer identification of the item
	 */
	public StdItemContainer(ContainerDef anItemDef, int anId)
	{
		super(anItemDef, anId);
	} 

	/**
	 * Retrieve items from this container.
	 * @param adject1 the first adjective
	 * @param adject2 the second adjective
	 * @param adject3 the third adjective
	 * @param name the name of the item
	 * @return Vector containing item objects found.
	 * @see mmud.database.ItemsDb#getItemsFromContainer
	 */
	public Vector getItems(String adject1, String adject2, String adject3, String name) 
	{
		return ItemsDb.getItemsFromContainer(adject1, adject2, adject3, name, this);
	}


	public int getCapacity()
	{
		if (isAttribute("capacity"))
		{
			return new
				Integer(getAttribute("capacity").getValue()).intValue();
		}
		return ((ContainerDef) getItemDef()).getCapacity();
	}

	public boolean isOpenable()
	{
		if (isAttribute("isOpenable"))
		{
			return getAttribute("isOpenable").getValue().equals("true");
		}
		return ((ContainerDef) getItemDef()).isOpenable();
	}

	public boolean hasLock()
	{
		return getKeyId() != null || ((ContainerDef) getItemDef()).hasLock();
	}

	public boolean isLocked()
	{
		if (isAttribute("islocked"))
		{
			return getAttribute("islocked").getValue().equals("true");
		}
		return false;
	}

	public boolean isOpen()
	{
		if (isAttribute("isopen"))
		{
			return getAttribute("isopen").getValue().equals("true");
		}
		return false;
	}

	public ItemDef getKeyId()
	{
		if (theKeyId != null)
		{
			return theKeyId;
		}
		return ((ContainerDef) getItemDef()).getKeyId();
	}

	public Types getContainTypes()
	{
		return theType;
	}

	public void setLidsNLocks(boolean isOpenable, 
		boolean newIsOpen,
		ItemDef newLock, 
		boolean newIsLocked)
	{
		setAttribute(new Attribute("isopenable", "" + isOpenable, "boolean"));
		setAttribute(new Attribute("isopen", "" + newIsOpen, "boolean"));
		setAttribute(new Attribute("islocked", "" + newIsLocked, "boolean"));
		theKeyId = newLock;
	}

	/**
	 * Basically does the same as the operation in the parent class, but
	 * adds that this item can contain items.
	 * @return String containing the description.
	 */
	public String getLongDescription()
	{
		String isopen = "";
		if (isOpenable())
		{
			isopen = (isOpen() ?
				"It is open.<BR>\r\n" :
				"It is closed.<BR>\r\n");
		}
		return super.getLongDescription() + 
			"It can contain other items.<BR>\r\n" + 
			isopen;
	}
}
