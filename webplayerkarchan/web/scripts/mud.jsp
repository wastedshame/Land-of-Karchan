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
<%@ page language="java" import="java.net.*"%>
<%@ page language="java" import="java.io.*"%>
<%@ page language="java" import="java.util.Enumeration"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Land of Karchan - Links</title>
    </head>
<BODY BGCOLOR=#FFFFFF>
            <%
    // authentication && authorization

    /* name of the current user logged in */
    String itsPlayerName;

    /* password of the current user logged in, unsure if used */
    String itsPlayerPassword = "";

    /* sessionid/cookiepassword of current user */
    String itsPlayerSessionId;
  itsPlayerName = request.getRemoteUser();
  itsPlayerSessionId = request.getSession(true).getId();
Socket mySocket = new Socket(getServletContext().getInitParameter("mudhost"),
        new Integer(getServletContext().getInitParameter("mudport")));
InputStream myInputStream = mySocket.getInputStream();
OutputStream myOutputStream = mySocket.getOutputStream();
boolean continueThis = true;
while (continueThis)
{

}
mySocket.close();
                       %>

</body>
</html>
