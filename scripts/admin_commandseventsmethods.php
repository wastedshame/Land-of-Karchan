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
Commands, Events and Methods</H1>

<?php
include $_SERVER['DOCUMENT_ROOT']."/scripts/admin_authorize.php";

/* the following constraints need to be checked before any kind of update is
to take place:

changing event:
- check that event is owned by owner or is null
- check callable is either 1 or 0
- check that methodname exists
- check that name of character (if filled in) exists
- check that room number (if filled in) exists
- check that numbers are filled in for times and room
adding event:
- check that method exists
deleting event:
- check that correct owner or owner==null
- check that eventid is numeric in format

changing command:
adding command:
- check that method exists

changing method:
- first check that the change is approved (i.e. owner or null)
adding method:
- no check
deleting method:
- check that no event is using this method
- check that no command is using this method
- check that correct owner or owner==null
/* 
------------------------------------------------------------------
EVENTS
------------------------------------------------------------------
*/
if ( (isset($_REQUEST{"eventid"}))
	&& (isset($_REQUEST{"eventcharname"}))
    && isset($_REQUEST{"eventroom"})
    && isset($_REQUEST{"eventmonth"})
    && isset($_REQUEST{"eventdayofmonth"})
    && isset($_REQUEST{"eventhour"})
    && isset($_REQUEST{"eventminute"})
    && isset($_REQUEST{"eventdayofweek"})
    && isset($_REQUEST{"eventmethodname"})
    && isset($_REQUEST{"eventcallable"}) )
{
	// check proper information formats
	if ( !is_numeric($_REQUEST{"eventid"}) ||
         !is_numeric($_REQUEST{"eventmonth"}) ||
         !is_numeric($_REQUEST{"eventdayofmonth"}) ||
         !is_numeric($_REQUEST{"eventhour"}) ||
         !is_numeric($_REQUEST{"eventminute"}) ||
         !is_numeric($_REQUEST{"eventdayofweek"}) )
	{
		error_message("Expected one of the fields to be an integer, and it wasn't.");
	}
	// check that the event is the proper owner.
    // check it.
    $result = mysql_query("select eventid from mm_events where eventid = \"".
        quote_smart($_REQUEST{"eventid"}).  
        "\" and (owner is null or owner = \"".   
        quote_smart($_COOKIE["karchanadminname"]).
        "\")"
        , $dbhandle)
        or error_message("Query(1) failed : " . mysql_error());
    if (mysql_num_rows($result) != 1)
    {
        error_message("You are not the owner of this event.");
    }
	// check to see that method exists
	$result = mysql_query("select 1 from mm_methods where name = \"".
		quote_smart($_REQUEST{"eventmethodname"}).
		"\""
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	if (mysql_num_rows($result) != 1)
	{
		error_message("Method does not exist.");
	}
    if ( ($_REQUEST{"eventcallable"} != "1") &&
        ($_REQUEST{"eventcallable"} != "0") )
    {
        error_message("Callable should be either 0 or 1.");
    }
	// check to see that character exists
	if (trim($_REQUEST{"eventcharname"}) != "")
	{
		$result = mysql_query("select 1 from mm_usertable where name = \"".
			quote_smart($_REQUEST{"eventcharname"}).
			"\""
			, $dbhandle)
			or error_message("Query failed : " . mysql_error());
		if (mysql_num_rows($result) != 1)
		{
			error_message("Character ".$_REQUEST{"eventcharname"}." does not exist.");
		}
	}
	// check to see that room exists
	if (trim($_REQUEST{"eventroom"}) != "")
	{
		$result = mysql_query("select 1 from mm_rooms where id = \"".
			quote_smart($_REQUEST{"eventroom"}).
			"\""
			, $dbhandle)
			or error_message("Query failed : " . mysql_error());
		if (mysql_num_rows($result) != 1)
		{
			error_message("Room ".$_REQUEST{"eventroom"}." does not exist.");
		}
	}
	// make that change
	$query = "update mm_events set name=".
		(trim($_REQUEST{"eventcharname"}) != "" ?
			"\"".quote_smart($_REQUEST{"eventcharname"})."\"":
			"null").
		", month=\"".
		quote_smart($_REQUEST{"eventmonth"}).
		"\", dayofmonth=\"".
		quote_smart($_REQUEST{"eventdayofmonth"}).
		"\", hour=\"".
		quote_smart($_REQUEST{"eventhour"}).
		"\", minute=\"".
		quote_smart($_REQUEST{"eventminute"}).
		"\", dayofweek=\"".
		quote_smart($_REQUEST{"eventdayofweek"}).
		"\", callable=\"".
		quote_smart($_REQUEST{"eventcallable"}).
		"\", method_name=\"".
		quote_smart($_REQUEST{"eventmethodname"}).
		"\", room=".
		(trim($_REQUEST{"eventroom"}) != "" ?
			"\"".quote_smart($_REQUEST{"eventroom"})."\"":
			"null").
		", owner=\"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\" where eventid = \"".
		quote_smart($_REQUEST{"eventid"}).
		"\"";
    mysql_query($query
        , $dbhandle)
        or error_message("Query(8) failed : " . mysql_error());
    writeLogLong($dbhandle, "Changed event ".$_REQUEST{"eventid"}.
		".", $query);
}
if (isset($_REQUEST{"addeventmethodname"}))
{
	// check to see that method exists
	$result = mysql_query("select 1 from mm_methods where name = \"".
		quote_smart($_REQUEST{"addeventmethodname"}).
		"\""
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	if (mysql_num_rows($result) != 1)
	{
		error_message("Method does not exist.");
	}
	// create new id
	$result = mysql_query("select max(eventid) as id from mm_events"
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	$maxid = 0;
	while ($myrow = mysql_fetch_array($result)) 
	{
		$maxid = $myrow["id"];
	}
	if ($maxid == 0)
	{
		error_message("Could not compute id for event.");
	}
	
	// make that change
	$query = "insert into mm_events (eventid, method_name, owner) values(".
		($maxid + 1).
		", \"".
		quote_smart($_REQUEST{"addeventmethodname"}).
		"\", \"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\")";
    mysql_query($query
        , $dbhandle)
        or error_message("Query(8) failed : " . mysql_error());
    writeLogLong($dbhandle, "Added event ".($maxid + 1).
		" which uses method ".$_REQUEST{"addeventmethodname"}.".", $query);
}
if (isset($_REQUEST{"deleteeventid"}))
{
	if ( !is_numeric($_REQUEST{"deleteeventid"}))
	{
		error_message("Expected eventid to be an integer, and it wasn't.");
	}
        $query = "delete from mm_events where eventid = ".
		quote_smart($_REQUEST{"deleteeventid"}).
		" and (owner is null or owner = \"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\")";
	mysql_query($query, $dbhandle)
		or error_message("Query (".$query.") failed : " . mysql_error());
	if (mysql_affected_rows() != 1)
	{
		error_message("Event does not exist or not proper owner.");
	}
    writeLogLong($dbhandle, "Removed event ".$_REQUEST{"deleteeventid"}.".", $query);
}

$result = mysql_query("select date_format(now(), \"%Y-%m-%d %T\") as now"
	, $dbhandle)
	or error_message("Query failed : " . mysql_error());
$myrow = mysql_fetch_array($result);
printf("<H2>Current date/time</H2>".$myrow["now"]."<P>");
?>
<H2><A HREF="/karchan/admin/help/scripting1.html" target="_blank">
<IMG SRC="/images/icons/9pt4a.gif" BORDER="0"></A>
Events</H2>
<?php
$result = mysql_query("select *, date_format(creation, \"%Y-%m-%d %T\") as creation2 
	from mm_events"
	, $dbhandle)
	or error_message("Query failed : " . mysql_error());
while ($myrow = mysql_fetch_array($result)) 
{
	if ( ($myrow["owner"] == null || $myrow["owner"] == "" ||
		$myrow["owner"] == $_COOKIE["karchanadminname"]) )
	{
		printf("<b>eventid:</b> <A
    	   HREF=\"/scripts/admin_commandseventsmethods.php?eventid=%s\">%s</A> ",
	       $myrow["eventid"], $myrow["eventid"]);
	}
	else
	{
		printf("<b>eventid:</b> %s ",
	       $myrow["eventid"]);
	}
	if ($myrow["room"] != null)
	{
		printf("<b>room:</b> <A
        HREF=\"/scripts/admin_rooms.php?room=%s\">%s</A> ",
        $myrow["room"], $myrow["room"]);
	}
	if ($myrow["name"] != null)
	{
		printf("<b>name:</b> <A
    HREF=\"/scripts/admin_chars.php?char=%s\">%s</A> ", $myrow["name"],
    $myrow["name"]);
	}
	if ($myrow["month"] != "-1")
	{
		printf("<b>month:</b> %s ", $myrow["month"]);
	}
	if ($myrow["dayofmonth"] != "-1")
	{
		printf("<b>dayofmonth:</b> %s ", $myrow["dayofmonth"]);
	}
	if ($myrow["hour"] != "-1")
	{
		printf("<b>hour:</b> %s ", $myrow["hour"]);
	}
	if ($myrow["minute"] != "-1")
	{
		printf("<b>minute:</b> %s ", $myrow["minute"]);
	}
	if ($myrow["dayofweek"] != "-1")
	{
		printf("<b>dayofweek:</b> %s ", $myrow["dayofweek"]);
	}
	printf("<b>callable:</b> %s ", ($myrow["callable"]=="1"?"yes":"no"));
	printf("<b>method_name:</b> <A
HREF=\"/scripts/admin_commandseventsmethods.php?methodname=%s\">%s</A> ", $myrow["method_name"], $myrow["method_name"]);
	printf("<b>owner:</b> %s ", $myrow["owner"]);
	printf("<b>creation:</b> %s<BR>", $myrow["creation2"]);
	if ( ($_REQUEST{"eventid"} == $myrow["eventid"]) &&
		($myrow["owner"] == null || $myrow["owner"] == "" ||
		$myrow["owner"] == $_COOKIE["karchanadminname"]) )
	{
?>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
<INPUT TYPE="hidden" NAME="deleteeventid" VALUE="<?php echo $myrow["eventid"] ?>">
<INPUT TYPE="submit" VALUE="Delete Event">
</b>
</FORM>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
<INPUT TYPE="hidden" NAME="eventid" VALUE="<?php echo $myrow["eventid"] ?>">
<TABLE>
<TR><TD>character name:</TD><TD><INPUT TYPE="text" NAME="eventcharname" VALUE="<?php echo $myrow["name"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>room:</TD><TD><INPUT TYPE="text" NAME="eventroom" VALUE="<?php echo $myrow["room"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>month:</TD><TD><INPUT TYPE="text" NAME="eventmonth" VALUE="<?php echo $myrow["month"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>dayofmonth:</TD><TD><INPUT TYPE="text" NAME="eventdayofmonth" VALUE="<?php echo $myrow["dayofmonth"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>hour:</TD><TD><INPUT TYPE="text" NAME="eventhour" VALUE="<?php echo $myrow["hour"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>minute:</TD><TD><INPUT TYPE="text" NAME="eventminute" VALUE="<?php echo $myrow["minute"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>dayofweek:</TD><TD><INPUT TYPE="text" NAME="eventdayofweek" VALUE="<?php echo $myrow["dayofweek"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>method_name:</TD><TD><INPUT TYPE="text" NAME="eventmethodname" VALUE="<?php echo $myrow["method_name"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>callable</TD><TD><SELECT NAME="eventcallable" SIZE="2">
<option value="1" <?php if ($myrow["callable"] == "1") {printf("selected");} ?>>yes
<option value="0" <?php if ($myrow["callable"] == "0") {printf("selected");} ?>>no
</SELECT></TD></TR>
</TABLE>
<INPUT TYPE="submit" VALUE="Change Event">
</b>
</FORM>

<FORM METHOD="GET" ACTION="/scripts/admin_ownership.php">
<b>
<INPUT TYPE="hidden" NAME="id" VALUE="<?php echo $myrow["eventid"] ?>">
<INPUT TYPE="hidden" NAME="removeownership" VALUE="3">
<INPUT TYPE="submit" VALUE="Remove Ownership">
</b>
</FORM>

<?php
	}
}
?>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
Method name: <INPUT TYPE="text" NAME="addeventmethodname" VALUE="">
<INPUT TYPE="submit" VALUE="Add Event">
</b>
</FORM>
<P>

<?php
/* 
------------------------------------------------------------------
COMMANDS
------------------------------------------------------------------
*/
?>

<H2><A HREF="/karchan/admin/help/scripting2.html" target="_blank">
<IMG SRC="/images/icons/9pt4a.gif" BORDER="0"></A>
Commands</H2>

<?php
if ( isset($_REQUEST{"commandid"})
	&& isset($_REQUEST{"commandroom"})
    && isset($_REQUEST{"commandmethodname"})
    && isset($_REQUEST{"commandcallable"}) )
{
	// check proper information formats
	if ( !is_numeric($_REQUEST{"commandid"}) )
	{
		error_message("Expected commandid (".$_REQUEST{"commandid"}.") to be an integer, and it wasn't.");
	}
	// check that the command is the proper owner.
    $result = mysql_query("select id from mm_commands where id = \"".
        quote_smart($_REQUEST{"commandid"}).  
        "\" and (owner is null or owner = \"".   
        quote_smart($_COOKIE["karchanadminname"]).
        "\")"
        , $dbhandle)
        or error_message("Query(1) failed : " . mysql_error());
    if (mysql_num_rows($result) != 1)
    {
        error_message("You are not the owner of this command.");
    }
	// check to see that method exists
	$result = mysql_query("select 1 from mm_methods where name = \"".
		quote_smart($_REQUEST{"commandmethodname"}).
		"\""
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	if (mysql_num_rows($result) != 1)
	{
		error_message("Method does not exist.");
	}
    if ( ($_REQUEST{"commandcallable"} != "1") &&
        ($_REQUEST{"commandcallable"} != "0") )
    {
        error_message("Callable should be either 0 or 1.");
    }
	// check to see that room exists
	if (trim($_REQUEST{"commandroom"}) != "")
	{
		$result = mysql_query("select 1 from mm_rooms where id = \"".
			quote_smart($_REQUEST{"commandroom"}).
			"\""
			, $dbhandle)
			or error_message("Query failed : " . mysql_error());
		if (mysql_num_rows($result) != 1)
		{
			error_message("Room ".$_REQUEST{"commandroom"}." does not exist.");
		}
	}
	// make that change
	$query = "update mm_commands set command=\"".
		quote_smart($_REQUEST{"commandcommand"}).
		"\", callable=\"".
		quote_smart($_REQUEST{"commandcallable"}).
		"\", method_name=\"".
		quote_smart($_REQUEST{"commandmethodname"}).
		"\", room=".
		(trim($_REQUEST{"commandroom"}) != "" ?
			"\"".quote_smart($_REQUEST{"commandroom"})."\"":
			"null").
		", owner=\"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\" where id = \"".
		quote_smart($_REQUEST{"commandid"}).
		"\"";
    mysql_query($query
        , $dbhandle)
        or error_message("Query(8) failed : " . mysql_error());
    writeLogLong($dbhandle, "Changed command ".$_REQUEST{"commandid"}.
		".", $query);
}
if (isset($_REQUEST{"addcommandname"}) &&
   isset($_REQUEST{"addcommandmethodname"}))
{
	// check to see that method exists
	$result = mysql_query("select 1 from mm_methods where name = \"".
		quote_smart($_REQUEST{"addcommandmethodname"}).
		"\""
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	if (mysql_num_rows($result) != 1)
	{
		error_message("Method does not exist.");
	}
	// create new id
	$result = mysql_query("select max(id) as id from mm_commands"
		, $dbhandle)
		or error_message("Query failed : " . mysql_error());
	$maxid = 0;
	while ($myrow = mysql_fetch_array($result)) 
	{
		$maxid = $myrow["id"];
	}
	if ($maxid == 0)
	{
		error_message("Could not compute id for command.");
	}
	
	// make that change
	$query = "insert into mm_commands (id, command, method_name, owner) values(".
		($maxid + 1).
		", \"".
		quote_smart($_REQUEST{"addcommandname"}).
		"\", \"".
		quote_smart($_REQUEST{"addcommandmethodname"}).
		"\", \"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\")";
    mysql_query($query
        , $dbhandle)
        or error_message("Query(8) failed : " . mysql_error());
    writeLogLong($dbhandle, "Added command ".$_REQUEST{"addcommandname"}.".", $query);
}
if (isset($_REQUEST{"deletecommandid"}))
{
	if ( !is_numeric($_REQUEST{"deletecommandid"}))
	{
		error_message("Expected commandid to be an integer, and it wasn't.");
	}
        $query = "delete from mm_commands where id = ".
		quote_smart($_REQUEST{"deletecommandid"}).
		" and (owner is null or owner = \"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\")";
	mysql_query($query, $dbhandle)
		or error_message("Query (".$query.") failed : " . mysql_error());
	if (mysql_affected_rows() != 1)
	{
		error_message("Command does not exist or not proper owner.");
	}
    writeLogLong($dbhandle, "Removed command ".$_REQUEST{"deletecommandid"}.".", $query);
}
$result = mysql_query("select *, date_format(creation, \"%Y-%m-%d %T\") as creation2 
	from mm_commands"
	, $dbhandle)
	or error_message("Query failed : " . mysql_error());
while ($myrow = mysql_fetch_array($result)) 
{
	if ( ($myrow["owner"] == null || $myrow["owner"] == "" ||
		$myrow["owner"] == $_COOKIE["karchanadminname"]) )
	{
		printf("<b>id:</b> <A
    	   HREF=\"/scripts/admin_commandseventsmethods.php?commandid=%s\">%s</A> ",
	       $myrow["id"], $myrow["id"]);
	}
	else
	{
		printf("<b>id:</b> %s ",
	       $myrow["id"]);
	}
	printf("<b>callable:</b> %s ", ($myrow["callable"]=="1"?"yes":"no"));
	printf("<b>command:</b> %s ", $myrow["command"]);
	printf("<b>method_name:</b> <A
HREF=\"/scripts/admin_commandseventsmethods.php?methodname=%s\">%s</A> ", $myrow["method_name"], $myrow["method_name"]);
	if ($myrow["room"] != null)
	{
		printf("<b>room:</b> <A
        HREF=\"/scripts/admin_rooms.php?room=%s\">%s</A> ",
        $myrow["room"], $myrow["room"]);
	}
	printf("<b>owner:</b> %s ", $myrow["owner"]);
	printf("<b>creation:</b> %s<BR>", $myrow["creation2"]);
	if ( ($_REQUEST{"commandid"} == $myrow["id"]) &&
		($myrow["owner"] == null || $myrow["owner"] == "" ||
		$myrow["owner"] == $_COOKIE["karchanadminname"]) )
	{
?>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
<INPUT TYPE="hidden" NAME="deletecommandid" VALUE="<?php echo $myrow["id"] ?>">
<INPUT TYPE="submit" VALUE="Delete Command">
</b>
</FORM>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
<INPUT TYPE="hidden" NAME="commandid" VALUE="<?php echo $myrow["id"] ?>">
<TABLE>
<TR><TD>command:</TD><TD><INPUT TYPE="text" NAME="commandcommand" VALUE="<?php echo $myrow["command"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>room:</TD><TD><INPUT TYPE="text" NAME="commandroom" VALUE="<?php echo $myrow["room"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>method_name:</TD><TD><INPUT TYPE="text" NAME="commandmethodname" VALUE="<?php echo $myrow["method_name"] ?>" SIZE="40" MAXLENGTH="40"></TD></TR>
<TR><TD>callable</TD><TD><SELECT NAME="commandcallable" SIZE="2">
<option value="1" <?php if ($myrow["callable"] == "1") {printf("selected");} ?>>yes
<option value="0" <?php if ($myrow["callable"] == "0") {printf("selected");} ?>>no
</SELECT></TD></TR>
</TABLE>
<INPUT TYPE="submit" VALUE="Change Command">
</b>
</FORM>

<FORM METHOD="GET" ACTION="/scripts/admin_ownership.php">
<b>
<INPUT TYPE="hidden" NAME="id" VALUE="<?php echo $myrow["id"] ?>">
<INPUT TYPE="hidden" NAME="removeownership" VALUE="2">
<INPUT TYPE="submit" VALUE="Remove Ownership">
</b>
</FORM>
<?php
	}
}
?>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
Command name: <INPUT TYPE="text" NAME="addcommandname" VALUE="">
<BR>
Method name: <INPUT TYPE="text" NAME="addcommandmethodname" VALUE="">
<INPUT TYPE="submit" VALUE="Add Command">
</b>
</FORM>
<P>

<?php
/* 
------------------------------------------------------------------
METHODS
------------------------------------------------------------------
*/
?>

<H2><A HREF="/karchan/admin/help/scripting3.html" target="_blank">
<IMG SRC="/images/icons/9pt4a.gif" BORDER="0"></A>
Methods</H2>

<?php

if (isset($_REQUEST{"methodname"}) && isset($_REQUEST{"src"}))
{
	// make that change
	$query = "update mm_methods set src=\"".
		quote_smart($_REQUEST{"src"}).
		"\" where name = \"".
		quote_smart($_REQUEST{"methodname"}).
		"\"";
    mysql_query($query
        , $dbhandle)
        or error_message("Query(8) failed : " . mysql_error());
    writeLogLong($dbhandle, "Changed method ".$_REQUEST{"methodname"}.".", $query);
}
if (isset($_REQUEST{"addmethodname"}))
{
	// make that change
	$query = "insert into mm_methods (name, owner) values(\"".
		quote_smart($_REQUEST{"addmethodname"}).
		"\", \"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\")";
    mysql_query($query
        , $dbhandle)
        or error_message("Query(8) failed : " . mysql_error());
    writeLogLong($dbhandle, "Added method ".$_REQUEST{"addmethodname"}.".", $query);
}
if (isset($_REQUEST{"deletemethodname"}))
{
  // check if no event is using this method
  $result = mysql_query("select 1 from mm_events where method_name = \"".
    quote_smart($_REQUEST{"deletemethodname"}).
	"\"", $dbhandle);
        if (mysql_num_rows($result) > 0)
        {
            error_message("There are still events using this method.");
        }
        // check if no command is using this method
  $result = mysql_query("select 1 from mm_commands where method_name = \"".
    quote_smart($_REQUEST{"deletemethodname"}).
	"\"", $dbhandle);
        if (mysql_num_rows($result) > 0)
        {
            error_message("There are still commands using this method.");
        }

        // make it so
        $query = "delete from mm_methods where name = \"".
		quote_smart($_REQUEST{"deletemethodname"}).
		"\" and (owner is null or owner = \"".
		quote_smart($_COOKIE["karchanadminname"]).
		"\")";
	mysql_query($query, $dbhandle)
		or error_message("Query (".$query.") failed : " . mysql_error());
	if (mysql_affected_rows() != 1)
	{
		error_message("Method does not exist or not proper owner.");
	}
    writeLogLong($dbhandle, "Removed method ".$_REQUEST{"deletemethodname"}.".", $query);
}

$result = mysql_query("select *, 
replace(replace(replace(src, \"&\", \"&amp;\"), \">\",\"&gt;\"), \"<\", \"&lt;\") 
as src2, date_format(creation, \"%Y-%m-%d %T\") as creation2 
	from mm_methods"
	, $dbhandle)
	or error_message("Query failed : " . mysql_error());
while ($myrow = mysql_fetch_array($result)) 
{
	printf("<b>name:</b> <A
HREF=\"/scripts/admin_commandseventsmethods.php?methodname=%s\">%s</A> ",
	$myrow["name"], $myrow["name"]);
	printf("<b>owner:</b> %s ", $myrow["owner"]);
	printf("<b>creation:</b> %s<BR>", $myrow["creation2"]);
	if ($_REQUEST{"methodname"} == $myrow["name"])
	{
		printf("<b>src:</b> <PRE>%s</PRE>", $myrow["src2"]);
	}
	if ( ($_REQUEST{"methodname"} == $myrow["name"]) &&
		($myrow["owner"] == null || $myrow["owner"] == "" ||
		$myrow["owner"] == $_COOKIE["karchanadminname"]) )
	{
?>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
<INPUT TYPE="hidden" NAME="deletemethodname" VALUE="<?php echo $myrow["name"] ?>">
<INPUT TYPE="submit" VALUE="Delete Method">
</b>
</FORM>
<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<I>(Refresh the command list in the game, after changing a method.)</I><BR>
<b>
<INPUT TYPE="hidden" NAME="methodname" VALUE="<?php echo $myrow["name"] ?>">
<TABLE>
<TR><TD>src</TD><TD><TEXTAREA NAME="src" ROWS="14" COLS="85"><?php echo
$myrow["src2"] ?></TEXTAREA></TD></TR>
</TABLE>
<INPUT TYPE="submit" VALUE="Change Method">
</b>
</FORM>

<FORM METHOD="GET" ACTION="/scripts/admin_ownership.php">
<b>
<INPUT TYPE="hidden" NAME="id" VALUE="<?php echo $myrow["name"] ?>">
<INPUT TYPE="hidden" NAME="removeownership" VALUE="1">
<INPUT TYPE="submit" VALUE="Remove Ownership">
</b>
</FORM>
<?php
	}
}

mysql_close($dbhandle);
?>

<FORM METHOD="POST" ACTION="/scripts/admin_commandseventsmethods.php">
<b>
Method name: <INPUT TYPE="text" NAME="addmethodname" VALUE="">
<INPUT TYPE="submit" VALUE="Add Method">
</b>
</FORM>
<P>

<a HREF="/karchan/admin/rooms.html">
<img SRC="/images/gif/webpic/buttono.gif"  
BORDER="0"></a><p>

</BODY>
</HTML>
