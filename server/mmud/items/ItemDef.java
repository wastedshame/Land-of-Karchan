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

import java.util.TreeMap;
import java.util.Vector;

import mmud.Attribute;
import mmud.AttributeContainer;
import mmud.Constants;
import mmud.MudException;

/**
 * The definition of an item. The analogy with Java would be the difference
 * between a class and an object.
 */
public class ItemDef implements AttributeContainer {
	private final String theVerb;
	private final String theAdjective1;
	private final String theAdjective2;
	private final String theAdjective3;
	private final String theDescription;
	private final int theId;
	private final int theCopper;
	private final int theWearable;
	private final TreeMap<String, Attribute> theAttributes = new TreeMap<String, Attribute>();

	/**
	 * Create an item definition.
	 * 
	 * @param anId
	 *            identification of the item definition
	 * @param anAdjective1
	 *            the first adjective of the item
	 * @param anAdjective2
	 *            the second adjective of the item
	 * @param anAdjective3
	 *            the third adjective of the item
	 * @param aVerb
	 *            the verb/name of the item
	 * @param aDescription
	 *            a long description of the item
	 * @param aCopper
	 *            the number of copper coins the item costs (base value)
	 * @param aWearable
	 *            the possible positions that the item can be worn on.
	 */
	public ItemDef(int anId, String anAdjective1, String anAdjective2,
			String anAdjective3, String aVerb, String aDescription,
			int aCopper, int aWearable) {
		theId = anId;
		theVerb = aVerb;
		theAdjective1 = anAdjective1;
		theAdjective2 = anAdjective2;
		theAdjective3 = anAdjective3;
		theDescription = aDescription;
		theCopper = aCopper;
		theWearable = aWearable;
	}

	/**
	 * Standard copy constructor.
	 * 
	 * @param anItemDef
	 *            the original Item Definition.
	 */
	public ItemDef(ItemDef anItemDef) {
		this(anItemDef.getId(), anItemDef.getAdjective1(), anItemDef
				.getAdjective2(), anItemDef.getAdjective3(), anItemDef
				.getVerb(), anItemDef.getLongDescription(), anItemDef
				.getMoney(), anItemDef.getWearable());
	}

	/**
	 * Return the id.
	 * 
	 * @return integer containing the identification number of the item
	 *         definition.
	 */
	public int getId() {
		return theId;
	}

	/**
	 * Return the verb.
	 * 
	 * @return String containing the verb of the item definition.
	 */
	public String getVerb() {
		return theVerb;
	}

	/**
	 * Return the first adjective.
	 * 
	 * @return String containing the first adjective of the item definition.
	 */
	String getAdjective1() {
		return theAdjective1;
	}

	/**
	 * Return the second adjective.
	 * 
	 * @return String containing the second adjective of the item definition.
	 */
	String getAdjective2() {
		return theAdjective2;
	}

	/**
	 * Return the third adjective.
	 * 
	 * @return String containing the third adjective of the item definition.
	 */
	String getAdjective3() {
		return theAdjective3;
	}

	/**
	 * Return the amount of copper it costs.
	 * 
	 * @return integer containing number of copper coins.
	 */
	public int getMoney() {
		return theCopper;
	}

	/**
	 * Return the wearable on setting.
	 * 
	 * @return integer containing different positions that this thing is
	 *         wearable on.
	 */
	private int getWearable() {
		return theWearable;
	}

	/**
	 * Return if the position entered is a member of the possible positions that
	 * this item can be worn on.
	 * 
	 * @return boolean, true if this item is wearable there.
	 */
	public boolean isWearable(PersonPositionEnum aPos) {
		return PersonPositionEnum.isIn(theWearable, aPos);
	}

	/**
	 * standard to string implementation.
	 * 
	 * @return String in format id+adject1+adject2+adject3+verb.
	 */
	@Override
	public String toString() {
		return theId + " " + theAdjective1 + " " + theAdjective2 + " "
				+ theAdjective3 + " " + theVerb;
	}

	/**
	 * Return the long description.
	 * 
	 * @return String containing the long description.
	 */
	public String getLongDescription() {
		return theDescription;
	}

	/**
	 * Set or add an attribute for this itemdefinition.
	 * 
	 * @param anAttribute
	 *            the attribute to be set or added.
	 */
	public void setAttribute(Attribute anAttribute) {
		theAttributes.put(anAttribute.getName(), anAttribute);
	}

	/**
	 * Set or add an attribute of this item.
	 * 
	 * @param anAttributeVector
	 *            vector containing the attributes to be added/set. This does
	 *            not use the database, i.e. should be used <I>by</I> the
	 *            database, upon creation of items.
	 */
	public void setAttributes(Vector anAttributeVector) throws MudException {
		if (anAttributeVector == null) {
			return;
		}
		for (int i = 0; i < anAttributeVector.size(); i++) {
			Attribute attrib = (Attribute) anAttributeVector.elementAt(i);
			theAttributes.put(attrib.getName(), attrib);
		}
	}

	/**
	 * Retrieve an attribute for this itemdefinition.
	 * 
	 * @param aName
	 *            the name of the attribute.
	 * @return Attribute object if found, otherwise null.
	 */
	public Attribute getAttribute(String aName) {
		return (Attribute) theAttributes.get(aName);
	}

	/**
	 * Remove an attribute for this itemdefinition.
	 * 
	 * @param aName
	 *            the name of the attribute.
	 */
	public void removeAttribute(String aName) {
		theAttributes.remove(aName);
	}

	/**
	 * Check if an attribute exists.
	 * 
	 * @param aName
	 *            the name of the attribute to search for.
	 * @return boolean, true if the attribute exists.
	 */
	public boolean isAttribute(String aName) {
		return theAttributes.containsKey(aName);
	}

	/**
	 * default equals implementation.
	 */
	@Override
	public boolean equals(Object r) {
		if (r == null) {
			return false;
		}
		if (!(r instanceof ItemDef)) {
			return false;
		}
		ItemDef u = (ItemDef) r;
		return u.getId() == getId();
	}

	/**
	 * get a description of the item.
	 * 
	 * @return String containing the description in the format: "an/a [adject1],
	 *         [adject2], [adject3] [verb]".
	 */
	public String getDescription() {
		return ItemDef.getDescription(getAdjective1(), getAdjective2(),
				getAdjective3(), getVerb());
	}

	/**
	 * get a description based on the adjectives and the verb.
	 * 
	 * @param adject1
	 *            String containing the first adjective.
	 * @param adject2
	 *            String containing the second adjective.
	 * @param adject3
	 *            String containing the third adjective.
	 * @param verb
	 *            String containing the verb.
	 * @return String containing the description in the format: "an/a [adject1],
	 *         [adject2], [adject3] [verb]".
	 */
	public static String getDescription(String adject1, String adject2,
			String adject3, String verb) {
		int i = 0;
		StringBuffer buf = new StringBuffer();
		if ((adject1 != null) && !adject1.trim().equals("")) {
			i++;
			buf.append(adject1);
		}
		if ((adject2 != null) && !adject2.trim().equals("")) {
			if (i == 1) {
				buf.append(", ");
			}
			buf.append(adject2);
			i++;
		}
		if ((adject3 != null) && !adject3.trim().equals("")) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(adject3);
			i++;
		}
		buf.append(" " + verb);
		String total = buf.toString();
		return (Constants.isQwerty(total.charAt(0)) ? "an " : "a ") + total;
	}

	/**
	 * determines whether an adjective is actually part of this items
	 * description.
	 * 
	 * @param anAdjective
	 *            String containing the adjective to look for.
	 * @return boolean true if the adjective entered is part of the description
	 *         of this item.
	 */
	public boolean isAdjective(String anAdjective) {
		return ((getAdjective1().equalsIgnoreCase(anAdjective))
				|| (getAdjective2().equalsIgnoreCase(anAdjective)) || (getAdjective3()
				.equalsIgnoreCase(anAdjective)));
	}
}
