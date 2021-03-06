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
package mmud.commands.guild;

import mmud.commands.GuildMasterCommand;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import mmud.database.entities.characters.Person;
import mmud.database.entities.characters.User;
import mmud.database.entities.game.DisplayInterface;
import mmud.exceptions.MudException;
import mmud.rest.services.LogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes you, as guildmaster, remove a member of the guild forcibly. Command
 * syntax something like : <TT>guildremove &lt;person&gt;</TT>
 * @author maartenl
 */
public class RemoveCommand extends GuildMasterCommand
{
    private static final Logger itsLog = LoggerFactory.getLogger(RemoveCommand.class);

    public RemoveCommand(String aRegExpr)
    {
        super(aRegExpr);
    }

    @Override
    public DisplayInterface run(String command, User aUser) throws MudException
    {

        LogBean logBean;
        try
        {
            logBean = (LogBean) new InitialContext().lookup("java:module/LogBean");

        } catch (NamingException e)
        {
            throw new MudException("Unable to retrieve person.", e);
        }
        String[] myParsed = parseCommand(command);
        Person guildmember = aUser.getGuild().getMember(myParsed[1]);
        if (guildmember == null)
        {
            aUser.writeMessage("Cannot find that person.<BR>\r\n");
            return aUser.getRoom();
        }
        if (guildmember.getName().equals(aUser.getName()))
        {
            aUser.writeMessage("A guildmaster cannot remove him/herself from the guild.<BR>\r\n");
            return aUser.getRoom();
        }
        if (!aUser.getGuild().getName().equals(guildmember.getGuild().getName()))
        {
            aUser.writeMessage("That person is not a member of your guild.<BR>\r\n");
            return aUser.getRoom();
        }
        guildmember.setGuild(null);
        // TODO?
        // aUser.getGuild().decreaseAmountOfMembers();
        logBean.writeLog(aUser, "removed " + guildmember.getName()
                + " from guild " + aUser.getGuild().getName());
        aUser.writeMessage("You have removed " + guildmember.getName()
                + " from your guild.<BR>\r\n");
        aUser.getGuild().sendMessage("<B>"
                + guildmember.getName()
                + "</B> has been removed from the guild.<BR>\r\n");
        if (guildmember.isActive())
        {
            guildmember.writeMessage("You have been removed from the guild <I>"
                    + aUser.getGuild().getTitle() + "</I>.<BR>\r\n");
        }
        return aUser.getRoom();
    }

}
