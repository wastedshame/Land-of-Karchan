<?
/*-------------------------------------------------------------------------
cvsinfo: $Header$
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
<?php
if ($_REQUEST{"username"} != "")
{
	setcookie("karchanname", $_REQUEST{"username"}, time() + 7200);
	setcookie("karchanpassword", $_REQUEST{"userpassword"}, time() + 7200);
}
?>

<HTML>
<HEAD>
<TITLE>
Land of Karchan - Admin
</TITLE>
</HEAD>
                                                                                                                      
<BODY>
<BODY BGCOLOR=#FFFFFF BACKGROUND="/images/gif/webpic/back4.gif">
<H1>
<IMG SRC="/images/gif/dragon.gif">Karchan Admin Page</H1>

<?php
if ( ($_COOKIE["karchanname"] == "") &&
	($_REQUEST{"username"} == ""))
{
?>
<FORM METHOD="GET" ACTION="/scripts/admin.php">
Administrator name:<BR>
<INPUT TYPE="text" NAME="username" VALUE="" SIZE="39" MAXLENGTH="39"><P>
Administrator password:<BR>
<INPUT TYPE="text" NAME="userpassword" VALUE="" SIZE="38" MAXLENGTH="38"><P>
<INPUT TYPE="submit" VALUE="Submit">
<INPUT TYPE="reset" VALUE="Clear"><P>
</FORM>
<?php
}
else
{
?>

<UL>
<LI><A HREF="/scripts/admin_problems.php">Report Problems</A>
<LI><A HREF="/scripts/admin_itemlist.php">Item List</A>
<LI><A HREF="/scripts/admin_log.php?status=1">Log - all</A>
<LI><A HREF="/scripts/admin_log.php?status=2">Log - last week</A>
<LI><A HREF="/scripts/admin_log.php?status=3">Log - yesterday</A>
<LI><A HREF="/scripts/admin_banned.php">Banned People</A>
</UL>
Room:
<FORM METHOD="GET" ACTION="/scripts/admin_rooms.php">
Room id:
<INPUT TYPE="text" NAME="room" VALUE="" SIZE="19" MAXLENGTH="19"><P>
<INPUT TYPE="submit" VALUE="Submit">
<INPUT TYPE="reset" VALUE="Clear"><P>
</FORM>

Get character info:<FORM METHOD="GET" ACTION="/scripts/admin_chars.php">
Character name:
<INPUT TYPE="text" NAME="char" VALUE="" SIZE="20" MAXLENGTH="20"><P>
<INPUT TYPE="submit" VALUE="Submit">
<INPUT TYPE="reset" VALUE="Clear"><P>
</FORM>

Import items:<FORM METHOD="GET" ACTION="/scripts/admin_importitems.php">
Character name:
<INPUT TYPE="text" NAME="char" VALUE="" SIZE="20" MAXLENGTH="20"><P>
<INPUT TYPE="submit" VALUE="Submit">
<INPUT TYPE="reset" VALUE="Clear"><P>
</FORM>

Logonmessage:
<FORM METHOD="GET" ACTION="/scripts/admin_logonmessage.php">
<TEXTAREA NAME="message" VALUE="" 
ROWS="20" COLS="85"></TEXTAREA><P>
<INPUT TYPE="submit" VALUE="Submit">
<INPUT TYPE="reset" VALUE="Clear"><P>
</FORM>
<FORM METHOD="GET" ACTION="/scripts/admin_select.php">

Select statement:<BR>
(<I>Examples</I>: <TT>select * from mm_usertable where name="Karn"</TT> or
<TT>show fields from mm_usertable</TT>)<BR>
<TEXTAREA NAME="select" VALUE="select ..." 
ROWS="20" COLS="85"></TEXTAREA><P>
<INPUT TYPE="submit" VALUE="Submit">
<INPUT TYPE="reset" VALUE="Clear"><P>
</FORM>
<?php
}
?>

</BODY>
</HTML>


