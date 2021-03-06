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
package mmud.database.entities.characters;

import javax.persistence.*;

/**
 * A bot in the game. Might be a shopkeeper.
 * @author maartenl
 */
@Entity
@DiscriminatorValue("2")
public class Bot extends Person
{

    @Override
    public Boolean getFightable()
    {
        return false;
    }

    /**
     * Bots cannot fight. Use mobs for that. Any entry is ignored.
     * @param fightable
     */
    @Override
    public void setFightable(Boolean fightable)
    {
        super.setFightable(false);
    }
}
