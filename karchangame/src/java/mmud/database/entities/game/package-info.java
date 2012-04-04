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

/**
 * <p>Provides the JPA Database Entities.</p>
 * <p>Mapping of database tables to database entities.<table>
 * <tr><th>database table</th><th>entity</th></tr>
 * <tr><td>mm_usertable</td><td>Person</td></tr>
 * <tr><td>mm_items</td><td>ItemDefinition</td></tr>
 * <tr><td>mm_itemtable</td><td>Item</td></tr>
 * <tr><td>mm_charitemtable</td><td>CharitemTable</td></tr>
 * </table>
 * <p><img src="../../../../images/package-info_gameentities.png"/></p>
 *
 * @startuml package-info_gameentities.png
 * mm_rooms "1" *-- "many" mm_usertable : contains
 *
 * class mm_usertable {
 *    -BigInteger id
 *    -String name
 *    -String description
 *    -Date creation_date
 *    -BigInteger parent_id
 *    -BigInteger highlight
 *    -int sortorder
 * }
 * class mm_rooms {
 *    -BigInteger id
 *    -String name
 *    -String description
 *    -BigInteger gallery_id
 *    -BigInteger photograph_id
 *    -int sortorder
 * }
 * @enduml
 */
package mmud.database.entities.game;
