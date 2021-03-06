/*
-----------------------------------------------------------------------
svninfo: $Id: charactersheets.php 1078 2006-01-15 09:25:36Z maartenl $
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
Regent Bannenbergstraat 44
5272 BR Sint Michielsgestel
Nederland
Europe
maarten_l@yahoo.com
-----------------------------------------------------------------------
*/
package mmud.webservices;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import mmud.webservices.webentities.DisplayResult;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import mmud.functions.CharacterSheets;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * This REST service is used for all public access to specific resources
 * of the land of karchan. To think about the guild list, characterlist
 * and the like.
 * The REST service will be hosted at the URI path "/karchan/resources/public"
 * @author maartenl
 */
@Path("public")
@Consumes("application/json")
@Produces("application/json")
public class PublicResource {


    public static final String FORTUNES_SQL = "select name, floor(copper/100) as gold, floor((copper % 100)/10) as silver, copper % 10 as copper\tfrom mm_usertable\twhere god<=1\torder by gold desc, silver desc, copper desc, name asc\tlimit 100";

    public static final String GUILDS_SQL = "select *, date_format(creation, \"%Y-%m-%d %T\") as creation2 from mm_guilds order by title";

    public static final String NEWS_SQL = "select mm_boardmessages.name, date_format(posttime, \"%W, %M %e %Y, %H:%i\") as posttime, message from mm_boardmessages, mm_boards where boardid=id and\tmm_boards.name = \"logonmessage\" order by mm_boardmessages.posttime desc limit 10";

    public static final String STATUS_SQL = "select mm_admin.name, title from mm_admin, mm_usertable where mm_admin.name = mm_usertable.name and mm_admin.validuntil > now()";

    public static final String WHO_SQL = "select name, mm_usertable.title, sleep, \tfloor((unix_timestamp(NOW())-unix_timestamp(lastlogin)) / 60) as min,\t((unix_timestamp(NOW())-unix_timestamp(lastlogin)) % 60) as sec,\tif (mm_area.area <> \"Main\", concat(\" in \" , mm_area.shortdesc), \"\") as area\tfrom mm_usertable, mm_rooms, mm_area \twhere god<=1 and active=1 and mm_rooms.id = mm_usertable.room and\tmm_rooms.area = mm_area.area";

    @Context
    private UriInfo context;

    private Logger itsLog = Logger.getLogger("mmudrest");

    /** Creates a new instance of PublicResource */
    public PublicResource() {
    }

    /**
     * Retrieves representation of an instance of mmud.webservices.PublicResource
     * @return an instance of java.lang.String
     */
    @GET
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of PublicResource
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    public void putJson(String content) {
    }

    /**
     * Returns a String in the DisplayResult set to "Hello, world!".
     */
    @GET
    @Path("helloworld")
    public DisplayResult helloWorld()
    {
        itsLog.entering(this.getClass().getName(), "helloWorld");
        DisplayResult res = new DisplayResult();
        res.setErrorMessage(null);
        res.setSuccess(true);
        res.setData("Hello, world!");
        // ResponseBuilder rb = request.evaluatePreconditions(lastModified, et);
        itsLog.exiting(this.getClass().getName(), "helloWorld");
        return res;
    }

    /**
     * Returns a List of people currently online.
     */
    @GET
    @Path("who")
    public JSONArray who()
    {
        itsLog.entering(this.getClass().getName(), "who");
        Connection con=null;
        ResultSet rst=null;
        PreparedStatement stmt=null;
        JSONArray res = new JSONArray();
        try
        {

            con = Utils.getDatabaseConnection();

            stmt=con.prepareStatement(WHO_SQL);
            rst=stmt.executeQuery();
            while(rst.next())
            {
                JSONObject myJSONObject = new JSONObject();
                myJSONObject.put("name", rst.getString("name"));
                myJSONObject.put("title", rst.getString("title"));
                myJSONObject.put("sleep", (rst.getInt("sleep") != 1 ? "" : "sleeping"));
                myJSONObject.put("area", rst.getString("area"));
                myJSONObject.put("min", rst.getString("min"));
                myJSONObject.put("sec", rst.getString("sec"));
                res.put(myJSONObject);
            }
        }
        catch(Exception e)
        {
            itsLog.throwing(this.getClass().getName(), "who", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        finally
        {
            if (rst != null) {try {rst.close();} catch (Exception e){}}
            if (stmt != null) {try {stmt.close();} catch (Exception e){}}
            if (con != null) {try {con.close();} catch (Exception e){}}
            itsLog.finest(this.getClass().getName() + ": connection with database closed.");
        }

        // ResponseBuilder rb = request.evaluatePreconditions(lastModified, et);
        itsLog.exiting(this.getClass().getName(), "who");
        return res;
    }


    /**
     * Returns a List of news, recent first.
     */
    @GET
    @Path("news")
    public JSONArray news()
    {
        itsLog.entering(this.getClass().getName(), "news");
        Connection con=null;
        ResultSet rst=null;
        PreparedStatement stmt=null;
        JSONArray res = new JSONArray();
        try
        {

            con = Utils.getDatabaseConnection();

            stmt=con.prepareStatement(NEWS_SQL);
            rst=stmt.executeQuery();
            while(rst.next())
            {
                JSONObject myJSONObject = new JSONObject();
                myJSONObject.put("name", rst.getString("name"));
                myJSONObject.put("posttime", rst.getString("posttime"));
                myJSONObject.put("message", rst.getString("message"));
                res.put(myJSONObject);
            }
        }
        catch(Exception e)
        {
            itsLog.throwing(this.getClass().getName(), "news", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        finally
        {
            if (rst != null) {try {rst.close();} catch (Exception e){}}
            if (stmt != null) {try {stmt.close();} catch (Exception e){}}
            if (con != null) {try {con.close();} catch (Exception e){}}
            itsLog.finest(this.getClass().getName() + ": connection with database closed.");
        }

        // ResponseBuilder rb = request.evaluatePreconditions(lastModified, et);
        itsLog.exiting(this.getClass().getName(), "news");
        return res;
    }

    /**
     * Returns a List of current active and paid up deputies.
     */
    @GET
    @Path("status")
    public JSONArray status()
    {
        itsLog.entering(this.getClass().getName(), "status");
        Connection con=null;
        ResultSet rst=null;
        PreparedStatement stmt=null;
        JSONArray res = new JSONArray();
        try
        {

            con = Utils.getDatabaseConnection();

            stmt=con.prepareStatement(STATUS_SQL);
            rst=stmt.executeQuery();
            while(rst.next())
            {
                JSONObject myJSONObject = new JSONObject();
                myJSONObject.put("name", rst.getString("name"));
                myJSONObject.put("title", rst.getString("title"));
                res.put(myJSONObject);
            }
        }
        catch(Exception e)
        {
            itsLog.throwing(this.getClass().getName(), "status", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        finally
        {
            if (rst != null) {try {rst.close();} catch (Exception e){}}
            if (stmt != null) {try {stmt.close();} catch (Exception e){}}
            if (con != null) {try {con.close();} catch (Exception e){}}
            itsLog.finest(this.getClass().getName() + ": connection with database closed.");
        }

        // ResponseBuilder rb = request.evaluatePreconditions(lastModified, et);
        itsLog.exiting(this.getClass().getName(), "status");
        return res;
    }


    /**
     * Returns a List of characters and their profiles.
     */
    @GET
    @Path("charactersheets")
    public JSONArray charactersheets()
    {
        return (new CharacterSheets()).charactersheets();
    }


    /**
     * Returns a Fortune 500 of players on karchan.
     */
    @GET
    @Path("fortunes")
    public JSONArray fortunes()
    {
        itsLog.entering(this.getClass().getName(), "fortunes");
        Connection con=null;
        ResultSet rst=null;
        PreparedStatement stmt=null;
        JSONArray res = new JSONArray();
        try
        {

            con = Utils.getDatabaseConnection();

            stmt=con.prepareStatement(FORTUNES_SQL);
            rst=stmt.executeQuery();
            while(rst.next())
            {
                JSONObject myJSONObject = new JSONObject();
                myJSONObject.put("name", rst.getString("name"));
                myJSONObject.put("gold", rst.getString("gold"));
                myJSONObject.put("silver", rst.getString("silver"));
                myJSONObject.put("copper", rst.getString("copper"));
                res.put(myJSONObject);
            }
        }
        catch(Exception e)
        {
            itsLog.throwing(this.getClass().getName(), "fortunes", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        finally
        {
            if (rst != null) {try {rst.close();} catch (Exception e){}}
            if (stmt != null) {try {stmt.close();} catch (Exception e){}}
            if (con != null) {try {con.close();} catch (Exception e){}}
            itsLog.finest(this.getClass().getName() + ": connection with database closed.");
        }

        // ResponseBuilder rb = request.evaluatePreconditions(lastModified, et);
        itsLog.exiting(this.getClass().getName(), "fortunes");
        return res;
    }

    /**
     * Returns a list of Guilds
     */
    @GET
    @Path("guilds")
    public JSONArray guilds()
    {
        itsLog.entering(this.getClass().getName(), "guilds");
        Connection con=null;
        ResultSet rst=null;
        PreparedStatement stmt=null;
        JSONArray res = new JSONArray();
        try
        {

            con = Utils.getDatabaseConnection();

            stmt=con.prepareStatement(GUILDS_SQL);
            rst=stmt.executeQuery();
            while(rst.next())
            {
                JSONObject myJSONObject = new JSONObject();
                myJSONObject.put("guildurl", rst.getString("guildurl"));
                myJSONObject.put("title", rst.getString("title"));
                myJSONObject.put("bossname", rst.getString("bossname"));
                myJSONObject.put("guilddescription", rst.getString("guilddescription"));
                myJSONObject.put("creation", rst.getString("creation2"));
                //myJSONObject.put("", rst.getString(""));
                //myJSONObject.put("", rst.getString(""));
                //myJSONObject.put("", rst.getString(""));
                //myJSONObject.put("", rst.getString(""));
                res.put(myJSONObject);
            }
        }
        catch(Exception e)
        {
            itsLog.throwing(this.getClass().getName(), "guilds", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        finally
        {
            if (rst != null) {try {rst.close();} catch (Exception e){}}
            if (stmt != null) {try {stmt.close();} catch (Exception e){}}
            if (con != null) {try {con.close();} catch (Exception e){}}
            itsLog.finest(this.getClass().getName() + ": connection with database closed.");
        }

        // ResponseBuilder rb = request.evaluatePreconditions(lastModified, et);
        itsLog.exiting(this.getClass().getName(), "guilds");
        return res;
    }

    /**
     * Returns all the info of a character.
     */
    @GET
    @Path("charactersheets/{name}")
    public JSONObject charactersheet(@PathParam("name") String name)
    {
        return (new CharacterSheets()).charactersheet(name);
    }

}
