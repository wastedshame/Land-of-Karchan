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
package mmud.testing;

import com.sun.xml.internal.ws.api.ha.HaInfo;
import java.util.Date;
import mmud.database.entities.game.Admin;
import mmud.database.entities.game.Area;
import mmud.database.entities.game.Person;
import mmud.database.entities.game.Room;
import mmud.database.enums.Sex;
import mmud.exceptions.MudException;

/**
 * Generates constants for use in testcases.
 * @author maartenl
 */
public class TestingConstants
{

    public static Area getArea()
    {
        Area aArea = new Area();
        aArea.setShortdescription("On board the Starship Heart of Gold");
        return aArea;
    }

    public static Room getRoom(Area aArea)
    {
        Room aRoom = new Room();
        aRoom.setTitle("The bridge");
        aRoom.setArea(aArea);
        return aRoom;
    }

    /**

     | Hotblack | 82-170-94-123.ip.telfort.nl | 93ef5f419670b2d0efe0c9461b765725a74c86eb | Desiato |          |                     | human | male | young | tall   | slender   | swarthy    | black-eyed | long-faced | black-haired | none  | none | none |      2 |    1 |      |      0 |          0 |             |     0 |          0 |         0 |      0 |         0 |        0 |          0 |        0 |      0 | 2011-06-12 08:54:10 | 2010-07-06 12:05:58 |    0 | NULL  |        2 |            2 |         2 |            2 |      2 |         0 |        0 |       0 |         8 |         0 |          1000 |     118 |     500 |      118 | NULL              | NULL          | NULL                | NULL              | NULL          | NULL             | NULL        | NULL              | NULL          | NULL          | NULL          | NULL        | NULL          | NULL           | NULL           | NULL      | NULL         |        1 |        1 |         1 | NULL  | 2011-06-12 08:54:10 | NULL  | NULL  |
     | Marvin   | 195.241.128.249             | a4cac82164ef67d9d07d379b5d5d8c4abe1e02ff | Person  | Marvin   | maarten_l@yahoo.com | human | male | young | tall   | very thin | swarthy    | black-eyed | long-faced | black-haired |       | none | none |      2 |    1 | NULL |      0 |          0 |             |     0 |          0 |         0 |      0 |         0 |        0 |          0 |        0 |      0 | 2011-02-07 23:19:19 | 2011-02-07 23:19:19 |    0 | NULL  |        2 |            2 |         2 |            2 |      2 |         0 |        0 |       0 |         8 |         0 |          1000 |     118 |     500 |      118 | NULL              | NULL          | NULL                | NULL              | NULL          | NULL             | NULL        | NULL              | NULL          | NULL          | NULL          | NULL        | NULL          | NULL           | NULL           | NULL      | NULL         |        1 |        1 |         1 | NULL  | 2011-02-07 23:19:19 | NULL  | NULL  |
     +----------+-----------------------------+------------------------------------------+---------+----------+---------------------+-------+------+-------+--------+-----------+------------+------------+------------+--------------+-------+------+------+--------+------+---
     * @param aRoom
     * @return
     * @throws MudException
     */
    public static Person getHotblack(Room aRoom) throws MudException
    {
        Person person = new Person();
        person.setName("Hotblack");
        // JDK7: number formats, for clarification.
        // 1_000_000 ms = 1_000 sec = 16 min, 40 sec
        person.setLastlogin(new Date((new Date()).getTime() - 1_000_000));
        person.setSleep(Boolean.FALSE);
        person.setTitle("Guitar keyboard player of the rock group Disaster Area");
        person.setRoom(aRoom);
        person.setSex(Sex.MALE);
        person.setRace("undead");
        person.setLok("lok");
        person.setAddress("82-170-94-123.ip.telfort.nl");
        person.setPassword("93ef5f419670b2d0efe0c9461b765725a74c86eb"); // sha1 of "hotblack"

        person.setRealname(null);
        person.setEmail(null);

        person.setRace("undead");
        person.setSex(Sex.MALE);
        person.setAge("young");
        person.setHeight("tall");
        person.setWidth("slender");
        person.setComplexion("swarthy");
        person.setEyes("black-eyed");
        person.setFace("long-faced");
        person.setHair("black-haired");
        person.setBeard("none");
        person.setArm("none");
        person.setLeg("none");
        person.setState("Rocking out!");
        return person;
    }

    public static Person getMarvin(Room aRoom) throws MudException
    {
        Person person = new Person();
        person.setName("Marvin");
        // JDK7: number formats, for clarification.
        // 2_000_000 ms = 2_000 sec = 33 min, 20 sec
        person.setLastlogin(new Date((new Date()).getTime() - 2_000_000));
        person.setRoom(aRoom);
        person.setSex(Sex.MALE);
        person.setSleep(Boolean.TRUE);
        person.setRace("android");
        person.setTitle("The Paranoid Android");
        person.setLok("lok");
        person.setAddress("82-170-94-123.ip.telfort.nl");
        person.setPassword("a4cac82164ef67d9d07d379b5d5d8c4abe1e02ff"); // sha1 of "marvin"
        person.setRace("human");
        return person;
    }

    public static Admin getAdmin()
    {
        Admin admin = new Admin();
        admin.setIp("10.0.0.12");
        admin.setEmail("maarten_l@yahoo.com");
        admin.setName("Karn");
        admin.setPasswd("somesecretpasswordthatnobodycanguessinanmillionyears");
        admin.setValiduntil(new Date((new Date()).getTime() + 100_000_000));
        return admin;
    }
}