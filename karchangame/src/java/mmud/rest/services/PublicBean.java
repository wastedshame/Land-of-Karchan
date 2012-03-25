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
package mmud.rest.services;

import com.sun.org.apache.xml.internal.serializer.utils.Utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import mmud.database.entities.game.Board;
import mmud.database.entities.game.BoardMessage;
import mmud.database.entities.game.Guild;
import mmud.database.entities.game.Person;
import mmud.rest.webentities.Fortune;
import mmud.rest.webentities.News;
import mmud.rest.webentities.PublicGuild;
import mmud.rest.webentities.PublicPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all rest calls that are available to the world, without
 * authentication or authorization. You can find them at
 * /karchangame/resources/public.
 *
 * @author maartenl
 */
@Stateless
@LocalBean
@Path("/public")
public class PublicBean
{

    @PersistenceContext(unitName = "karchangamePU")
    private EntityManager em;

    /**
     * Returns the entity manager of Hibernate/JPA. This is defined in
     * build/web/WEB-INF/classes/META-INF/persistence.xml.
     *
     * @return EntityManager
     */
    protected EntityManager getEntityManager()
    {
        return em;
    }
    private static final Logger itsLog = LoggerFactory.getLogger(PublicBean.class);

    /**
     * Returns a Fortune 100 of players on karchan. The URL:
     * /karchangame/resources/public/fortunes. Can produce both application/xml
     * and application/json.
     */
    @GET
    @Path("fortunes")
    @Produces(
    {
        "application/xml", "application/json"
    })
    public List<Fortune> fortunes()
    {
        itsLog.debug("entering fortunes");

        List<Fortune> res = new ArrayList<>();
        try
        {
            Query query = getEntityManager().createNamedQuery("Person.getFortunes");
            query.setMaxResults(100);
            List<Person> list = query.getResultList();

            for (Person person : list)
            {
                res.add(new Fortune(person));
            }
        } catch (Exception e)
        {
            itsLog.debug("throws fortunes", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        itsLog.debug("exiting fortunes");
        return res;
    }

    /**
     * Returns a List of people currently online. The URL:
     * /karchangame/resources/public/who. Can produce both application/xml and
     * application/json.
     */
    @GET
    @Path("who")
    @Produces(
    {
        "application/xml", "application/json"
    })
    public List<PublicPerson> who()
    {
        itsLog.debug("entering who");
        List<PublicPerson> res = new ArrayList<>();
        try
        {
            Query query = getEntityManager().createNamedQuery("Person.who");
            List<Person> list = query.getResultList();

            for (Person person : list)
            {
                PublicPerson publicPerson = new PublicPerson();
                publicPerson.name = person.getName();
                publicPerson.title = person.getTitle();
                publicPerson.sleep = person.getSleep() == 1 ? "sleeping" : "";
                publicPerson.area = person.getRoom().getArea().getShortdesc();
                Long now = (new Date()).getTime();
                Long backThen = person.getLastlogin().getTime();
                publicPerson.min = (now - backThen) / 60000;
                publicPerson.sec = ((now - backThen) / 1000) % 60;
                res.add(publicPerson);
            }
        } catch (Exception e)
        {
            itsLog.debug("throws who", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        itsLog.debug("exiting who");
        return res;
    }

    /**
     * Returns a List of news, recent first. The URL:
     * /karchangame/resources/public/news. Can produce both application/xml and
     * application/json.
     */
    @GET
    @Path("news")
    @Produces(
    {
        "application/xml", "application/json"
    })
    public List<News> news()
    {
        itsLog.debug("entering news");

        List<News> res = new ArrayList<>();
        try
        {
            itsLog.debug("news: getting board");

            Query query = getEntityManager().createNamedQuery("Board.findByName");
            query.setParameter("name", "logonmessage");
            Board board = (Board) query.getSingleResult();

            itsLog.debug("news: getting news");
            query = getEntityManager().createNamedQuery("BoardMessage.news");
            //query.setParameter("board", board);
            query.setMaxResults(10);
            List<BoardMessage> list = query.getResultList();
            itsLog.debug("news: found " + list.size() + " entries.");
            for (BoardMessage message : list)
            {
                News news = new News();
                news.name = message.getPerson().getName();
                news.posttime = message.getPosttime();
                news.message = message.getMessage();
                res.add(news);
            }
        } catch (Exception e)
        {
            itsLog.debug("news: throws ", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        itsLog.debug("exiting news");
        return res;
    }

    /**
     * Returns a List of current active and paid up deputies. The URL:
     * /karchangame/resources/public/status. Can produce both application/xml
     * and application/json.
     */
    @GET
    @Path("status")
    @Produces(
    {
        "application/xml", "application/json"
    })
    public List<PublicPerson> status()
    {
        itsLog.debug("entering status");

        List<PublicPerson> res = new ArrayList<>();
        try
        {
            Query query = getEntityManager().createNamedQuery("Person.status");
            List<Person> list = query.getResultList();

            for (Person person : list)
            {
                PublicPerson publicPerson = new PublicPerson();
                publicPerson.name = person.getName();
                publicPerson.title = person.getTitle();
                res.add(publicPerson);
            }
        } catch (Exception e)
        {
            itsLog.debug("status: throws ", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        itsLog.debug("exiting status");
        return res;
    }

    /**
     * Returns a list of Guilds. The URL: /karchangame/resources/public/guilds.
     * Can produce both application/xml and application/json.
     */
    @GET
    @Path("guilds")
    @Produces(
    {
        "application/xml", "application/json"
    })
    public List<PublicGuild> guilds()
    {
        itsLog.debug("entering guilds");
        List<PublicGuild> res = new ArrayList<>();
        try
        {
            Query query = getEntityManager().createNamedQuery("Guild.findAll");
            List<Guild> list = query.getResultList();

            for (Guild guild : list)
            {
                PublicGuild newGuild = new PublicGuild();
                newGuild.guildurl = guild.getGuildurl();
                newGuild.title = guild.getTitle();
                if (guild.getBossname() == null)
                {
                    itsLog.warn("guilds: no boss found for guild " + guild.getName());
                } else
                {
                    newGuild.bossname = guild.getBossname().getName();
                }
                newGuild.guilddescription = guild.getGuilddescription();
                newGuild.creation = guild.getCreation();
                res.add(newGuild);
            }
        } catch (Exception e)
        {
            itsLog.debug("status: throws ", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        itsLog.debug("exiting guilds");
        return res;
    }
}