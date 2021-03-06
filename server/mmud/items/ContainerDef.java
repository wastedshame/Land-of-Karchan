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
package mmud.items;     


/**
 * The definition of a container. Has some extra attributes only available
 * to containers. The analogy with Java would be the difference
 * between a class and an object.
 */
public class ContainerDef extends ItemDef
{
	private int theCapacity;
	private boolean isOpenable;
	private ItemDef theKeyId;

	/**
	 * Create an item definition.
	 * @param anId identification of the item definition
	 * @param anAdjective1 the first adjective of the item
	 * @param anAdjective2 the second adjective of the item
	 * @param anAdjective3 the third adjective of the item
	 * @param aVerb the verb/name of the item
	 * @param aDescription a long description of the item
	 * @param aCopper the number of copper coins the item costs 
	 * @param aWearable the possible positions that the item
	 * can be worn on.
	 * @param aCapacity maximum weight units possible to store in the item.
	 *  param aIsOpenable if the item can be opened and closed or not.
	 * @param aKeyId if the item has a lock, and to unlock it with which
	 * item. If this itemdef is NULL, then the container has no lock.
	 */
	public ContainerDef(int anId, String anAdjective1, String anAdjective2, 
		String anAdjective3, String aVerb, String aDescription, 
		int aCopper,
		int aWearable, int aCapacity, boolean aIsOpenable, ItemDef aKeyId)
	{
		super(anId, anAdjective1, anAdjective2, 
		anAdjective3, aVerb, aDescription, 
		aCopper, aWearable);
		theCapacity = aCapacity;
		isOpenable = aIsOpenable;
		theKeyId = aKeyId;
	} 

	/**
	 * Standard copy constructor.
	 * @param anItemDef the original Item Definition.
	 */
	public ContainerDef(ContainerDef anItemDef)
	{
		super(anItemDef);
		theCapacity = anItemDef.getCapacity();
		isOpenable = anItemDef.isOpenable();
		theKeyId = anItemDef.getKeyId();
	} 

	/**
	 * Return the item definition of the key to open the container.
	 * @return ItemDef of the key item.
	 */
	public ItemDef getKeyId()
	{
		return theKeyId;
	}

	/**
	 * Check if this definition of a container has a lock.
	 * @return boolean, true if a lock is present.
	 */
	public boolean hasLock()
	{
		return (getKeyId() != null);
	}

	/**
	 * Check if the definition of a container has a lid for opening and
	 * closing.
	 * @return boolean, true if a lid is present.
	 */
	public boolean isOpenable()
	{
		return isOpenable;
	}

	/**
	 * Return the capacity of the definition of the container.
	 * @return int, containing the maximum weight of the container.
	 */
	public int getCapacity()
	{
		return theCapacity;
	}

}
