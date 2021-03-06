/*
 *  Copyright (C) 2012 maartenl
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
package mmud.commands.movement;

import java.util.logging.Logger;
import mmud.commands.NormalCommand;
import mmud.database.entities.characters.User;
import mmud.database.entities.game.DisplayInterface;
import mmud.database.entities.game.Room;
import mmud.exceptions.MudException;

/**
 * Proceed to the room in the north, if possible : "north".
 *
 * @see GoCommand
 * @author maartenl
 */
public class NorthCommand extends NormalCommand
{

    public NorthCommand(String aRegExpr)
    {
        super(aRegExpr);
    }

    @Override
    public DisplayInterface run(String command, User aUser) throws MudException
    {
        Logger.getLogger("mmud").finer("");
        Room myRoom = aUser.getRoom();
        if (myRoom.getNorth() != null)
        {
            myRoom.sendMessageExcl(aUser, "%SNAME leave%VERB2 north.<BR>\r\n");
            aUser.setRoom(myRoom.getNorth());
            aUser.getRoom().sendMessageExcl(aUser, "%SNAME appear%VERB2.<BR>\r\n");
        } else
        {
            aUser.writeMessage("You cannot go north.<BR>\r\n");
        }
        return aUser.getRoom();
    }
}
