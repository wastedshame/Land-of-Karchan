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
Land of Karchan - Poll
</TITLE>
</HEAD>
<BODY>
<BODY BGCOLOR=#FFFFFF BACKGROUND="/images/gif/webpic/back4.gif">
<H1><IMG SRC="/images/gif/dragon.gif">

</H1>
<?php
include $_SERVER['DOCUMENT_ROOT']."/scripts/connect.php"; 
// show results
$result = mysql_query("select * from polls where id=".
	quote_smart($_REQUEST{"number"})
	, $dbhandle)
	or error_message("Query failed : " . mysql_error());
while ($myrow = mysql_fetch_array($result)) 
{
	printf($myrow["title"]."<P>".$myrow["description"]."<P>");
	$closed = $myrow["closed"];
}
if ($_REQUEST{"value"} != "")
{
	/**
	 * verify form information
	 */      
    if (!isset($_COOKIE["karchanname"]) &&   
       !isset($_COOKIE["karchanpassword"]) )
    {   
        error_message("Form information missing. You must be logged into the game ".
        "to enter into the polls.");
    }
	$query = "replace into poll_values
		(id, name, value, comments)
		select ".
		quote_smart($_REQUEST{"number"}).", \"".
		quote_smart($_COOKIE["karchanname"])."\", ".
		quote_smart($_REQUEST{"value"}).", \"".
		quote_smart($_REQUEST{"comments"})."\" 
		from mm_usertable
		where name = \"".
		quote_smart($_COOKIE["karchanname"]).
		"\" and lok = \"".
		quote_smart($_COOKIE["karchanpassword"]).
		"\"";
	mysql_query($query
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
}

if ($closed == "1")
{
	$result = mysql_query("select count(*) as count 
		from poll_values 
		where id=".quote_smart($_REQUEST{"number"})
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	$count = 1;
	while ($myrow = mysql_fetch_array($result)) 
	{
		$count = $myrow["count"];
	}
	printf($count." people voted.<BR>");
	$result = mysql_query("select count(poll_values.id) as count, poll_choices.choice 
		from poll_choices, poll_values 
		where pollid=".quote_smart($_REQUEST{"number"})." and 
		pollid = poll_values.id and
		poll_choices.id = poll_values.value 
		group by poll_choices.id 
		order by poll_choices.id"
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	printf("<UL>");
	while ($myrow = mysql_fetch_array($result)) 
	{
		$vote = (int) $myrow["count"];
		printf("<LI>".$myrow["choice"]." (".$myrow["count"]." votes, ".
			(int) (($vote/$count)*100)." %%)<BR>");
		printf("<IMG SRC=\"/images/jpeg/mainbar2.jpg\"
			HEIGHT=20 width=\"".(int)(800*$vote/$count)."\" ALT=\"".(int)
			(($vote/$count)*100)."%%\"><BR>");

	}
	printf("</UL>");
} 
else
{
	/**
	 * verify form information
	 */      
	 if (!isset($_COOKIE["karchanname"]) &&   
	    !isset($_COOKIE["karchanpassword"]) )
     {   
	    error_message("Form information missing. You must be logged into the game ".
        "to enter into the polls.");
     }
	// show form
	$result = mysql_query("select * 
		from poll_choices 
		where pollid=".quote_smart($_REQUEST{"number"})."
		order by id"
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	printf("<FORM METHOD=\"GET\" ACTION=\"/scripts/poll.php\">");
	while ($myrow = mysql_fetch_array($result)) 
	{
		printf("<input type=\"radio\" name=\"value\" value=\"".
			$myrow["id"]."\">".$myrow["choice"]."<BR>");
	}
	printf("Comments: <INPUT TYPE=\"text\" NAME=\"comments\" VALUE=\"\" SIZE=\"20\"><P>");
	printf("<INPUT TYPE=\"hidden\" NAME=\"number\" VALUE=\"".$_REQUEST{"number"}."\" SIZE=\"20\" MAXLENGTH=\"40\"><P>");
	printf("<INPUT TYPE=\"submit\" VALUE=\"Submit\">");
	printf("<INPUT TYPE=\"reset\" VALUE=\"Clear\"><P>");
	printf("</FORM>");
}

mysql_close($dbhandle);
?>


</BODY>
</HTML>

