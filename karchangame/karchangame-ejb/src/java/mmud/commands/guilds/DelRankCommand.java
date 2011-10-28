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

import mmud.exceptions.MmudException;
import mmud.characters.GuildRank;
import mmud.database.entities.Player;
import mmud.commands.Command;
import mmud.database.Database;

/**
 * Makes you, as guildmaster, remove a rank from the guild.
 * Command syntax something like : <TT>guilddelrank &lt;id&gt;</TT>
 */
public class DelRankCommand extends GuildMasterCommand
{

	public DelRankCommand(String aRegExpr)
	{
		super(aRegExpr);
	}

	public boolean run(Player aPlayer)
	throws MmudException
	{
		Logger.getLogger("mmud").finer("");
		if (!super.run(aPlayer))
		{
			return false;
		}
		String[] myParsed = getParsedCommand();
		int id = 0;
		try
		{
			id = Integer.parseInt(myParsed[1]);
		}
		catch (NumberFormatException e)
		{
			aPlayer.writeMessage("You did not enter an appropriate rank id, which should be a number.<BR>\r\n");
			return true;
		}
		GuildRank rank = aPlayer.getGuild().getRank(id);
		aPlayer.getGuild().removeRank(rank);
		Database.writeLog(aPlayer.getName(), " removed guildrank of " + 
			" guild " + aPlayer.getGuild().getName() + " called " + rank);
		aPlayer.writeMessage("You have removed the " + rank.getTitle() +
			" rank from your guild.<BR>\r\n");
		return true;
	}

	public Command createCommand()
	{
		return new DelRankCommand(getRegExpr());
	}
	
}
