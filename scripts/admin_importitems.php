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
Items from <?php echo $_REQUEST{"char"} ?></H1>

<?php
include $_SERVER['DOCUMENT_ROOT']."/scripts/admin_authorize.php";
if ($_COOKIE["karchanadminname"] != "Karn")
{
    error_message("This administration option is only available to Karn.");
}

/**
 * verify form information
 */
if (!isset($_REQUEST{"char"}))
{
    error_message("Form information missing.");
}

$result = mysql_query("select * from mud.itemtable 
	where belongsto = \"".quote_smart($_REQUEST{"char"})."\" order by id"
	, $dbhandle)
	or error_message("Query failed : " . mysql_error());
$numfields = mysql_num_fields($result);
while ($myrow = mysql_fetch_array($result)) 
{
	printf("# ");
	for ($i = 0; $i < $numfields; $i++) 
	{
		printf(mysql_field_name($result, $i) . "=" . $myrow[$i] . " ");
	}
	printf("<BR>");
	for ($i=0;$i < $myrow["amount"];$i++)
	{
		$select = "insert into mm_itemtable (itemid, creation, owner) 
			values(".$myrow["id"].", now(), null);";
		printf("<TT>".$select."</TT><BR>");
		mysql_query($select, $dbhandle)
			or error_message("Query failed : " . mysql_error());
		$select = "insert into mm_charitemtable (id, belongsto) 
			select id, \"".$myrow["belongsto"]."\"
			from mm_itemtable
			where id IS NULL;";
		printf("<TT>".$select."</TT><BR>");
		mysql_query($select, $dbhandle)
			or error_message("Query failed : " . mysql_error());
	}
}


mysql_close($dbhandle);
?>

</BODY>
</HTML>
