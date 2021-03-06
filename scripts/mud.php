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
<?php
include $_SERVER['DOCUMENT_ROOT']."/scripts/connect.php";

/**
 * verify form information
 */      
if (!isset($_REQUEST{"command"}) &&
	!isset($_COOKIE["karchanname"]) &&
	!isset($_COOKIE["karchanpassword"]) )
{   
	error_message("Form information missing.");
}
// Hack prevention.
//	$headers = apache_request_headers();
//	header()
//	setcookie();

//	foreach ($headers as $header => $value) 
//	{
//		echo "$header: $value <br />\n";
//	}
	$fp = fsockopen ($server_host, $server_port, $errno, $errstr, 30);
	if (!$fp) 
	{?>
		<HTML>
		<TITLE>An Error Occured Attempting to Play To The Mud
		</TITLE>
		<BODY BGCOLOR=#FFFFFF BACKGROUND="/images/gif/webpic/back4.gif">
		<H1><IMG SRC="/images/gif/dragon.gif">An Error Occured Attempting To Logon To The Mud</H1><HR>
		An error occurred whilst attempting to connect to host <?php echo $server_host; ?>
		 on port number <?php echo $server_port ?>.<P>
		The following error occured:<P><TT>
		<?php
		echo "$errstr ($errno)<p>\n";
		if ($errno == "111")
		{
		?>Error code <B>111</B> usually indicates that the mud server has crashed or has
		been deactivated. It needs to be restarted.<P><?php
		}
		if ($errno == "13")
		{
		?>Error code <B>13</B> usually indicates that httpd server is not allowing network connections being made from within scripts. For example when using SELinux.<P><?php
		}
		?>
		Please contact Karn (at <A HREF-"mailto:karn@karchan.org">karn@karchan.org</A>) 
		as soon as possible to mention this problem and 
		please mention the error message and error code.<P>
		Thank you.
		</BODY>
		</HTML>	
	<?php
	} 
	else 
	{
//		if (trim($_REQUEST{"command"}) != "")
//		{
//			mysql_query("insert into mm_commandlog (name, command) values(\"".
//				quote_smart($_COOKIE["karchanname"])."\", \"".
//				quote_smart($_REQUEST{"command"})."\")"
//				, $dbhandle)
//				or error_message("Query failed : " . mysql_error());
//		}
		fgets ($fp,128); // mud id
		fgets ($fp,128); // action
		fputs ($fp, "mud\n");
		fgets ($fp,128); // name
		fputs ($fp, $_COOKIE["karchanname"]."\n");
		fgets ($fp,128); // cookie
		fputs ($fp, $_COOKIE["karchanpassword"]."\n");
		fgets ($fp,128); //  frames
		fputs ($fp, $_REQUEST{"frames"}."\n");
		fgets ($fp,128); // command
		if ($_REQUEST{"command"} == "sendmail")
		{
			$foo = strlen($_REQUEST{"mailheader"});
			fputs ($fp, "sendmail "
				.$_REQUEST{"mailto"}." "
				.$foo." "
				.$_REQUEST{"mailheader"}." "
				.$_REQUEST{"mailbody"}
				."\n");
		}
		else
		{
			fputs ($fp, $_REQUEST{"command"}."\n");
		}
		fputs ($fp, ".\n");
		$cookie = fgets ($fp,128);
		if (strstr($cookie, "sessionpassword=") != FALSE)
		{
			$cookie = substr($cookie, 16);
			$cookie = substr_replace($cookie, "", -1, 1);
			setcookie("karchanpassword", $cookie);
			if ($cookie == "")
			{
				setcookie("karchanname", $cookie);
			}
		}
		else
		{
			if (get_cfg_var("magic_quotes_gpc") == "1")
			{
				$cookie = stripslashes($cookie);
			}
			echo $cookie;
		}
		$readline = fgets ($fp,128);
		while ((!feof($fp)) && ($readline != ".\n"))
		{
			if (get_cfg_var("magic_quotes_gpc") == "1")
			{
				$readline = stripslashes($readline);
			}
			echo $readline;
			$readline = fgets ($fp, 128);
		}

		fputs ($fp, "\nOk\n");
		fputs ($fp, "\nOk\n");
		fclose ($fp);
	}
	exit;
?>
