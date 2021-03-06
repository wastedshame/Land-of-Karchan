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

import mmud.MudException;
import mmud.characters.User;

/**
 * The interface used for all commands to be executed. Commands are executed by
 * characters.
 */
public interface Command
{

	/**
	 * Starts the command, this method is used for default behaviour that needs
	 * to take place before the run command is issued. This method starts the
	 * run command.
	 * 
	 * @param aUser
	 *            the user that executed the command
	 * @return true or false, depending on success.
	 * @throws MudException
	 *             when anything goes wrong.
	 */
	public boolean start(User aUser) throws MudException;

	/**
	 * Runs the command. The usual sequence of events is:
	 * <UL>
	 * <LI>parsing the command string
	 * <LI>retrieving the appropriate information items/rooms/characters/etc)
	 * <LI>doing the action
	 * <LI>composing a return message for the user.
	 * </UL>
	 * 
	 * @param aUser
	 *            the user that is executing the command
	 * @return boolean, wether or not the command was successfull.
	 * @throws MudException
	 *             if something goes wrong.
	 */
	public boolean run(User aUser) throws MudException;

	/**
	 * Sets the command originally used to execute this command. Useful for
	 * parsing.
	 * 
	 * @param aCommand
	 *            String containing the original command.
	 */
	public void setCommand(String aCommand);

	/**
	 * returns the regular expression the command structure must follow.
	 * 
	 * @return String containing the regular expression.
	 */
	public String getRegExpr();

	/**
	 * Gets the command originally used to execute this command. Useful for
	 * parsing.
	 * 
	 * @return String containing the original command.
	 */
	public String getCommand();

	/**
	 * Gets the command parsed into words.
	 * 
	 * @return String[] containing the individual words in the command.
	 */
	public String[] getParsedCommand();

	/**
	 * creates a new instance of the current object.
	 */
	public Command createCommand();

	/**
	 * Returns the appropriate view for a player.
	 * 
	 * @return String containing the view of a player. Header, description,
	 *         form, logfile, footer.
	 */
	public String getResult();

}
