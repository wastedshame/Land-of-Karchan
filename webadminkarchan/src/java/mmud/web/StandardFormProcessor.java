/*-----------------------------------------------------------------------
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
Appelhof 27
5345 KA Oss
Nederland
Europe
maarten_l@yahoo.com
-----------------------------------------------------------------------*/

package mmud.web;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.text.DateFormat;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

/**
 * Standard class for processing the different administrative possibiities
 * of the mmud web pages.
 *
 * @author Maarten van Leunen
 */
public class StandardFormProcessor implements FormProcessor
{
    public static final String CREATION = "creation";
    public static final String OWNER = "owner";
    protected Connection itsConnection;

    protected String itsTableName;

    protected String[] itsColumns;

    protected String[] itsDisplay;

    protected String itsPlayerName;

    public void checkAuthorization()
    throws SQLException
    {
        ResultSet rst = null;
        PreparedStatement stmt = null;

        try
        {
            // ===============================================================================
            // begin authorization check
            stmt = itsConnection.prepareStatement("select * from mm_admin where name =	'" +
                    itsPlayerName + "' and validuntil >= now()"); //  and mm_usertable.lok = '" + itsPlayerSessionId + "'
            rst = stmt.executeQuery();
            if (!rst.next()) {
                // error getting the info, user not found?
                throw new RuntimeException("Cannot find " + itsPlayerName + " in the database!");
            }
            // end authorization check
            // ===============================================================================
        }
        finally
        {
            if (rst != null) {rst.close();}
            stmt.close();
        }
    }

    public void closeConnection()
    throws SQLException
    {
        // itsConnection.commit();
        itsConnection.close();
    }

    /**
     * Initialises the object with a connection to the database.
     */
    public StandardFormProcessor(String aTableName, String aPlayerName)
    throws SQLException
    {
        if (aTableName == null || aTableName.trim().equals(""))
        {
            throw new RuntimeException("aTableName is empty.");
        }
        itsTableName = aTableName;
        itsPlayerName = aPlayerName;

        Context ctx = null;
        try
        {
            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/mmud");
            itsConnection = ds.getConnection();
        }
        catch (NamingException e)
        {
            throw new RuntimeException("Getting the datasource failed!", e);
        }
        checkAuthorization();
    }

    /**
     * @param itsColums the itsColums to set
     */
    public void setColumns(String[] itsColumns) {
        if (itsDisplay != null && itsColumns.length != itsDisplay.length)
        {
            throw new RuntimeException("Design failure. Not equivalent number of names and columns.");
        }
        this.itsColumns = itsColumns;
    }

    /**
     * @param itsDisplay the itsDisplay to set
     */
    public void setDisplayNames(String[] itsDisplay) {
        if (itsColumns != null && itsColumns.length != itsDisplay.length)
        {
            throw new RuntimeException("Design failure. Not equivalent number of names and columns.");
        }
        this.itsDisplay = itsDisplay;
    }

    public String getList(HttpServletRequest request)
            throws SQLException
    {
        return getList(request, false);
    }

    public String getList(HttpServletRequest request, boolean newLines)
            throws SQLException
    {
        StringBuffer result = new StringBuffer();
        String td = (!newLines ? "<td>" : "");
        String nottd = (!newLines ? "</td>" : "<br/>");
        result.append("<table>");
        ResultSet rst=null;
        PreparedStatement stmt=null;

        // show list of commands
        if (request.getParameter("id") != null)
        {
            stmt=itsConnection.prepareStatement("select * from " + itsTableName + " where " + itsColumns[0] + " like ?");
            stmt.setString(1, request.getParameter("id"));
        }
        else
        if (request.getParameter("idstartswith") != null)
        {
            stmt=itsConnection.prepareStatement("select * from " + itsTableName + " where " + itsColumns[1] + " like ?");
            stmt.setString(1, request.getParameter("idstartswith") + "%" );
        }
        else
        {
            stmt=itsConnection.prepareStatement("select * from " + itsTableName);
        }

        rst=stmt.executeQuery();
        while (rst.next())
        {
            result.append("<tr>");
            boolean accessGranted = itsPlayerName.equals(rst.getString(OWNER)) ||
                    rst.getString(OWNER) == null ||
                    rst.getString(OWNER).trim().equals("");
            
           if (!rst.getString(itsColumns[0]).equals(request.getParameter("id")))
           {

               if (newLines) { result.append("<td>");}
               // put the list here
               if (accessGranted)
                {
                    result.append(td + "<a HREF=\"" + itsTableName.replace("mm_", "").toLowerCase() +
                            ".jsp?id=" + rst.getString(itsColumns[0]) + "\">E</a> ");
                    result.append("<a HREF=\"remove_" + itsTableName.replace("mm_", "").toLowerCase() +
                            ".jsp?id=" + rst.getString(itsColumns[0]) + "\">X</a> ");
                    result.append("<a HREF=\"remove_ownership.jsp?id=" +
                            rst.getString(itsColumns[0]) + "&table=" + itsTableName.replace("mm_", "") + "\">O</a>" + nottd);
                }
                else
                {
                    result.append(td + nottd);
                }

                for (int i=0; i < itsColumns.length; i++)
                {
                    if (rst.getString(itsColumns[i]) == null)
                    {
                        result.append(td + nottd);
                    }
                    else
                    if ("0".equals(rst.getString(itsColumns[i])))
                    {
                        result.append(td + "<b>" + itsDisplay[i] + ":</b> No" + nottd);
                    }
                    else
                    if ("1".equals(rst.getString(itsColumns[i])))
                    {
                        result.append(td + "<b>" + itsDisplay[i] + ":</b> Yes" + nottd);
                    }
                    else
                    if (itsColumns[i].equals(CREATION))
                    {
                        Date creation = rst.getDate(itsColumns[i]);
                        DateFormat formatter = DateFormat.getInstance();
                        result.append(td + "<b>" + itsDisplay[i] + ":</b> " + formatter.format(creation) + nottd);
                    }
                    else
                    {
                        result.append(td + "<b>" + itsDisplay[i] + ":</b> " + rst.getString(itsColumns[i]) + nottd);
                    }
                }
                if (newLines) { result.append("</td>");}
                result.append("</tr>");
           }
           else
            {
                // put some editing form here.
                result.append("<tr><td><table><tr><FORM METHOD=\"POST\" ACTION=\"" +
                        itsTableName.replace("mm_", "").toLowerCase() +
                        ".jsp\">");
                result.append("<td><b>" + itsDisplay[0] + ": </b></td><td>" + rst.getString(itsColumns[0]) + "</td></tr>");
                for (int i=1; i < itsColumns.length; i++)
                {
                    result.append("<td><b>" + itsDisplay[i] + ": </b></td><td>");
                    if (itsColumns[i].equals("callable"))
                    {
                        result.append("<SELECT NAME=\"callable\" SIZE=\"2\">");
                        result.append("<option value=\"1\" " +
                                ("1".equals(rst.getString("callable")) ? "selected " : " ") + ">yes");
                        result.append("<option value=\"0\" " +
                                ("0".equals(rst.getString("callable")) ? "selected " : " ") + ">no");
                        result.append("</SELECT><br/>");
                    }
                    else
                    {
                        String disp = rst.getString(itsColumns[i]);
                        disp = (disp == null ? "" : disp);
                        result.append("<INPUT TYPE=\"text\" NAME=\"" +
                            itsColumns[i] + "\" VALUE=\"" +
                            disp + "\" SIZE=\"40\" MAXLENGTH=\"40\"><br/>");
                    }
                    result.append("</td></tr>");
                }
                result.append("</table><INPUT TYPE=\"submit\" VALUE=\"Change " + itsTableName.replace("mm_", "") + "\">");
                result.append("</FORM></td></tr>");
            }
        }
        rst.close();
        stmt.close();
        result.append("</table>");

        return result.toString();
    }

    /**
     * <pre>insert into mm_areas values(area, desc, shordesc)
     * (?, ?, ?)</pre>
     * @param request
     * @throws SQLException
     */
    public void addEntry(HttpServletRequest request)
            throws SQLException
    {
        StringBuffer query = new StringBuffer();
        PreparedStatement stmt=null;

        // add one
        query.append("insert into " + itsTableName + " values(");
        String appendstuff = "";
        for (int i=0; i < itsColumns.length; i++)
        {
            query.append(itsColumns[i]);
            appendstuff+="?";
            if (i != itsColumns.length - 1)
            {
                query.append(",");
                appendstuff+=",";
            }
        }
        query.append(") (" + appendstuff + ")");

        stmt=itsConnection.prepareStatement(query.toString());
        for (int i=0; i < itsColumns.length; i++)
        {
            if (itsColumns[i].equals(OWNER))
            {
                stmt.setString(i + 1, itsPlayerName);
            }
            else
            {
                stmt.setString(i + 1, request.getParameter(itsColumns[i]));
            }
        }

        stmt.executeUpdate(query.toString());
        stmt.close();
    }

    /**
     * <pre>delete from mm_areas where 
     * where (owner = "" or owner = null or owner = ?) and area = ?</pre>
     * @param request
     * @throws SQLException
     */
    public void removeEntry(HttpServletRequest request)
    throws SQLException
    {
        StringBuffer query = new StringBuffer();
        PreparedStatement stmt=null;

        // add one
        query.append("delete from " + itsTableName + 
                " where (owner = \"\" or owner = null or owner = ?) and " + itsColumns[0] + " = ?");
        stmt=itsConnection.prepareStatement(query.toString());
        stmt.setString(1, itsPlayerName);
        stmt.setString(2, request.getParameter("id"));
        stmt.executeUpdate(query.toString());
        stmt.close();
    }

    /**
     * Sends an update statement, in the following form:
     * <p/>
     * <pre>update mm_areas set desc = ?, short = ?
     * where (owner = "" or owner = null or owner = ?) and area = ?</pre>
     * And fills it up with:
     * <ul><li>desc
     * <li>short
     * <li>itsPlayerName
     * <li>area
     * </ul>
     * @param request
     * @throws SQLException
     */
    public void changeEntry(HttpServletRequest request)
    throws SQLException
    {
        StringBuffer query = new StringBuffer();
        PreparedStatement stmt=null;

        // add one
        query.append("update " + itsTableName + " set ");
        for (int i=1; i < itsColumns.length; i++)
        {
            if (CREATION.equals(itsColumns[i]))
            {
                // creation timestamp! skip it!
                continue;
            }
            query.append(itsColumns[i] + " = ?");
            if (i != itsColumns.length - 1)
            {
                query.append(", ");
            }
        }
        query.append(" where (owner = \"\" or owner = null or owner = ?) and " + itsColumns[0] + " = ?");
        stmt=itsConnection.prepareStatement(query.toString());
        for (int i=1; i < itsColumns.length; i++)
        {
            if (CREATION.equals(itsColumns[i]))
            {
                // creation timestamp! skip it!
                continue;
            }
            if (itsColumns[i].equals(OWNER))
            {
                stmt.setString(i, itsPlayerName);
            }
            else
            {
                stmt.setString(i, request.getParameter(itsColumns[i]));
            }
        }
        stmt.setString(itsColumns.length + 1, itsPlayerName);
        stmt.setString(itsColumns.length + 2, request.getParameter(itsColumns[0]));

        stmt.executeUpdate(query.toString());
        stmt.close();
    }

    /**
     * Sends an update statement, removing the ownership from a table row.
€     * <p/>
     * <pre>update mm_areas set ownership = null
     * where (owner = "" or owner = null or owner = ?) and area = ?</pre>
     * @param request
     * @throws SQLException
     */
    public void removeOwnershipFromEntry(HttpServletRequest request)
    throws SQLException
    {
        StringBuffer query = new StringBuffer();
        PreparedStatement stmt=null;

        // add one
        query.append("update " + itsTableName + " set owner = null " +
               " where (owner = \"\" or owner = null or owner = ?) and " +
                itsColumns[0] + " = ?");
        stmt=itsConnection.prepareStatement(query.toString());
        stmt.setString(1, itsPlayerName);
        stmt.setString(1, request.getParameter("id"));
        stmt.executeUpdate(query.toString());
        stmt.close();
    }

}
