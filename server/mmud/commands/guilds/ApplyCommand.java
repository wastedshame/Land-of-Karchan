/*-------------------------------------------------------------------------
svninfo: $Id: PkillCommand.java 994 2005-10-23 10:19:20Z maartenl $
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
package mmud.commands.guilds;

import java.util.logging.Logger;

import mmud.Attribute;
import mmud.MudException;
import mmud.characters.Guild;
import mmud.characters.GuildFactory;
import mmud.characters.User;
import mmud.commands.Command;
import mmud.commands.NormalCommand;

/**
 * Makes you apply to a guild. There are some requirements to follow:
 * <UL>
 * <LI>the guild must exist
 * <LI>you must not already belong to a guild
 * </UL>
 * Command syntax something like : <TT>guildapply &lt;guildname&gt;</TT>
 */
public class ApplyCommand extends NormalCommand
{

	public ApplyCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	@Override
	public boolean run(User aUser) throws MudException
	{
		Logger.getLogger("mmud").finer("");
		String[] myParsed = getParsedCommand();
		if (myParsed.length == 1)
		{
			aUser.removeAttribute("guildwish");
			aUser
					.writeMessage("You have no longer applied to any guild.<BR>\r\n");
			return true;
		}
		Guild guild = null;
		try
		{
			guild = GuildFactory.createGuild(myParsed[1]);
		} catch (MudException e)
		{
			aUser.writeMessage("Unable to find guild <I>" + myParsed[1]
					+ "</I>.<BR>\r\n");
			return false;
		}
		if (aUser.getGuild() != null)
		{
			aUser.writeMessage("You already belong to guild <I>"
					+ aUser.getGuild().getTitle() + "</I>.<BR>\r\n");
			return false;
		}
		aUser.setAttribute(new Attribute(Attribute.GUILDWISH, guild.getName(),
				"string"));
		aUser.writeMessage("You have applied to guild <I>" + guild.getTitle()
				+ "</I>.<BR>\r\n");
		return true;
	}

	public Command createCommand()
	{
		return new ApplyCommand(getRegExpr());
	}

}
