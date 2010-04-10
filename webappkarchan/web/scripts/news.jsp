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
<%@ page language="java" import="java.sql.*"%>
<%@ page language="java" import="javax.naming.InitialContext"%>
<%@ page language="java" import="javax.naming.Context"%>
<%@ page language="java" import="javax.sql.DataSource"%>
<%@ page language="java" import="java.util.Enumeration"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>News of Land of Karchan</title>
    </head>
    <body>
    <body BGCOLOR=#FFFFFF BACKGROUND="/images/gif/webpic/back4.gif">
        <H2><IMG SRC="/images/gif/dragon.gif">
News of Land of Karchan</H2>
<BR>
<IMG SRC="/images/gif/letters/w.gif"
ALT="W" ALIGN=left>
elcome to the Land of Karchan MUD, a land filled with mystery
and enchantment, where weapons, magic, intelligence, and common
sense play key roles in the realm. Where love and war can be one
and the same. Where elves coexist peacefully with the humans, and
make war with the dwarves. Where the sun rises, and the moon falls.
Where one can change into a hero with a single swipe of his
sword.<P>


<%

Connection con=null;
ResultSet rst=null;
PreparedStatement stmt=null;

try
{Context ctx = new InitialContext();
DataSource ds = (DataSource) ctx.lookup("jdbc/mmud");
con = ds.getConnection();

stmt=con.prepareStatement("select mm_boardmessages.name, date_format(posttime, \"%W, %M %e %Y, %H:%i\") as posttime, message from mm_boardmessages, mm_boards where boardid=id and	mm_boards.name = \"logonmessage\" order by mm_boardmessages.posttime desc limit 10");
rst=stmt.executeQuery();
while(rst.next())
{
	out.println("<hr>" + rst.getString("posttime") + "<p>" +
		rst.getString("message") + "<p><i>" +
                rst.getString("name") + "</i>");
}
rst.close();
stmt.close();
con.close();
}
catch(Exception e)
{
System.out.println(e.getMessage());
%><%=e.getMessage()%>
<%
}
%>

    </body>
</html>