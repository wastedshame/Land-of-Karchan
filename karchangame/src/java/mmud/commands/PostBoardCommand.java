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
package mmud.commands;

import mmud.database.entities.characters.User;
import mmud.database.entities.game.Board;
import mmud.database.entities.game.DisplayInterface;
import mmud.exceptions.MudException;

/**
 * Posts a message to a general board. Command is like "post public First post!" in the proper room.
 *
 * @author maartenl
 */
public class PostBoardCommand extends NormalCommand
{

    public PostBoardCommand(String aRegExpr)
    {
        super(aRegExpr);
    }

    @Override
    public DisplayInterface run(String command, User aUser) throws MudException
    {
        String[] myParsed = parseCommand(command, 3);
        Board board = aUser.getRoom().getBoard(myParsed[1]);
        if (board == null)
        {
            aUser.writeMessage("Unable to find board [" + myParsed[1] + "] in this room. Board unknown.<br/>\r\n");
            return aUser.getRoom();
        }
        board.addMessage(aUser, myParsed[2]);
        aUser.writeMessage("Message posted.<br/>\r\n");
        return aUser.getRoom();
    }
}
