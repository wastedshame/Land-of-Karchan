<?
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
?>
<HTML>
<HEAD>
<TITLE>
Mmud - Admin
</TITLE>
</HEAD>
                                                                                                                      
<BODY>
<BODY BGCOLOR=#FFFFFF BACKGROUND="/images/gif/webpic/back4.gif">
<H1>
<IMG SRC="/images/gif/dragon.gif">
Item Definitions</H1>

<?php
include $_SERVER['DOCUMENT_ROOT']."/scripts/admin_authorize.php";
$result = mysql_query("select id, adject1, adject2, adject3, name from mm_items order by id"
	, $dbhandle)
	or error_message("Query failed : " . mysql_error());
while ($myrow = mysql_fetch_array($result)) 
{
	printf("<b>id:</b> <A HREF=\"/scripts/admin_itemdefs.php?item=%s\">%s<A> ", 
		$myrow["id"], $myrow["id"]);
	printf("<b>name:</b> %s %s %s %s<BR>", 
		$myrow["adject1"], $myrow["adject2"], $myrow["adject3"], $myrow["name"]);
}

mysql_close($dbhandle);
?>

</BODY>
</HTML>
