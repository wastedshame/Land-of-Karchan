<%--
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
Appelhof 27
5345 KA Oss
Nederland
Europe
maarten_l@yahoo.com
-----------------------------------------------------------------------

--%>
<%@ page language="java"%>
<%@ page language="java" import="javax.naming.InitialContext"%>
<%@ page language="java" import="java.io.PrintWriter"%>
<%@ page language="java" import="java.io.IOException"%>
<%@ page language="java" import="javax.naming.Context"%>
<%@ page language="java" import="javax.sql.DataSource"%>
<%@ page language="java" import="java.sql.*"%>
<%@ page language="java" import="java.util.Enumeration"%>
<%@ page language="java" import="mmud.web.FormProcessorFactory"%>
<%@ page language="java" import="mmud.web.FormProcessor"%>
<%@ page language="java" import="mmud.web.Formatter"%>
<%@ page language="java" import="mmud.web.TableFormatter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
<%!
// authentication && authorization

/* name of the current user logged in */
private String itsPlayerName;

/* password of the current user logged in, unsure if used */
private String itsPlayerPassword = "";

/* sessionid/cookiepassword of current user */
private String itsPlayerSessionId;
%>
<%
  itsPlayerName = request.getRemoteUser();
  itsPlayerSessionId = request.getSession(true).getId();
%>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Mmud - Admin</title>
        <%@include file="/includes/head.jsp" %>
    </head>
<BODY BGCOLOR=#FFFFFF>
<H1>
<IMG SRC="/images/gif/dragon.gif"  alt="dragon">Methods</H1>
<H2><A HREF="/karchan/admin/help/scripting3.html" target="_blank">
<IMG SRC="/images/icons/9pt4a.gif" BORDER="0"></A>
Methods</H2>

<A HREF="methods.jsp?idstartswith=A">A</A>
<A HREF="methods.jsp?idstartswith=B">B</A>
<A HREF="methods.jsp?idstartswith=C">C</A>
<A HREF="methods.jsp?idstartswith=D">D</A>
<A HREF="methods.jsp?idstartswith=E">E</A>
<A HREF="methods.jsp?idstartswith=F">F</A>
<A HREF="methods.jsp?idstartswith=G">G</A>
<A HREF="methods.jsp?idstartswith=H">H</A>
<A HREF="methods.jsp?idstartswith=I">I</A>
<A HREF="methods.jsp?idstartswith=J">J</A>
<A HREF="methods.jsp?idstartswith=K">K</A>
<A HREF="methods.jsp?idstartswith=L">L</A>
<A HREF="methods.jsp?idstartswith=M">M</A>
<A HREF="methods.jsp?idstartswith=N">N</A>
<A HREF="methods.jsp?idstartswith=O">O</A>
<A HREF="methods.jsp?idstartswith=P">P</A>
<A HREF="methods.jsp?idstartswith=Q">Q</A>
<A HREF="methods.jsp?idstartswith=R">R</A>
<A HREF="methods.jsp?idstartswith=S">S</A>
<A HREF="methods.jsp?idstartswith=T">T</A>
<A HREF="methods.jsp?idstartswith=U">U</A>
<A HREF="methods.jsp?idstartswith=V">V</A>
<A HREF="methods.jsp?idstartswith=W">W</A>
<A HREF="methods.jsp?idstartswith=X">X</A>
<A HREF="methods.jsp?idstartswith=Y">Y</A>
<A HREF="methods.jsp?idstartswith=Z">Z</A>
<P>

<%

if (itsPlayerName == null)
{
    throw new RuntimeException("User undefined.");
}
if (!request.isUserInRole("deputies"))
{
    throw new RuntimeException("User does not have role 'deputies'.");
}
/*
showing the different areas and what rooms belong to which area.

the following constraints need to be checked before any kind of update
is to take place:

changing area:
- the area must exist
- is the administrator the owner of the area
*/


// show list of areas
FormProcessor processor = null;
try {
    if (request.getParameter("id") != null)
    {
        String[] columns = {"name", "name", "owner", "creation", "src"};
        String[] displays = {"Name", "Name", "Owner", "Created on", "Method Source"};
        processor = FormProcessorFactory.create("mm_methods", itsPlayerName, displays, columns, new TableFormatter());
    }
    else
    {
        String[] columns = {"name", "name", "owner", "creation"};
        String[] displays = {"Name", "Name", "Owner", "Created on"};
        processor = FormProcessorFactory.create("mm_methods", itsPlayerName, 
                displays, columns, new TableFormatter());
    }
    out.println(processor.getList(request));
} catch (SQLException e) {
    out.println(e.getMessage());
    e.printStackTrace(new PrintWriter(out));
%><%=e.getMessage()%>
<%
} finally {
    if (processor != null) {processor.closeConnection();}
}

%>

</body>
</html>
