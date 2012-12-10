/*
 * Copyright (C) 2012 maartenl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmud.database.enums;

/**
 *
 * @author maartenl
 */
public enum Wielding
{

    WIELD_LEFT(1, "with %SHISHER left hand"),
    WIELD_RIGHT(2, "with %SHISHER right hand"),
    WIELD_BOTH(4, "with both %SHISHER hands");
    private int bitmask;
    private String description;

    /**
     * Constructor for the enum.
     * @param aVal the integer value (database)
     * @param str the description of use in communication
     * @param parse for parsing commands, contains one word indicating the
     * lefthand,righthand or bothhands.
     */
    private Wielding(int bitmask, String description)
    {
        this.bitmask = bitmask;
        this.description = description;
    }

    /**
     * Recreates the constants from an integer. An integer value of 0 causes a
     * null pointer to be returned.
     *
     * @param aVal the integer corresponding to the constant.
     * @return the constant object
     * @throws RuntimeException in case the integer provided does not correspond
     * to any of the available objects.
     */
    public static Wielding get(int aVal)
    {
        if (aVal == 0)
        {
            return null;
        }
        for (Wielding position : Wielding.values())
        {
            if (position.toInt() == aVal)
            {
                return position;
            }
        }
        throw new RuntimeException("value " + aVal + " does not "
                + "correspond to a Wielding");
    }

    /**
     * Receives an integer and checks to see that the Wielding is a
     * part of it.
     *
     * @param aVal the integer corresponding to a number of Wieldings
     * @param aPos the Wielding for which to check.
     * @return boolean, true if the integer contains the Wielding.
     */
    public static boolean isIn(int aVal, Wielding aPos)
    {
        if (aPos == null)
        {
            // the empty position is always a good position.
            return true;
        }
        return (aVal & aPos.toInt()) == aPos.toInt();
    }

    /**
     * Returns the name of the format.
     *
     * @return format name.
     */
    @Override
    public String toString()
    {
        return description;
    }

    /**
     * Returns the numerical representation of the format.
     *
     * @return identification integer.
     */
    public int toInt()
    {
        return bitmask;
    }

    /**
     * Provides some parsing, a string is translated to the enum in question.
     * For example "both" will return Wielding.WIELD_BOTH.
     *
     * @param aVal the string to be parsed
     * @return the constant object. Will return null, if a faulty aval is provided
     * that does not map to any of the enums.
     */
    public static Wielding parse(String aVal)
    {
        if (aVal == null)
        {
            return null;
        }
        switch (aVal.toLowerCase())
        {
            case "both":
            case "hands":
            case "bothhands":
                return Wielding.WIELD_BOTH;
            case "left":
            case "lefthand":
                return Wielding.WIELD_LEFT;
            case "right":
            case "righthand":
                return Wielding.WIELD_RIGHT;
            default:
                return null;
        }
    }
}
