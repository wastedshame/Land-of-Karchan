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
#include <time.h>
#include "typedefs.h"
#include "guild.h"

/*! \file guild.c
	\brief  part of the server that does the whole guild thing as well as
some "talk lines". */

extern char* command;

//! list of guildmembers of the MIF
void 
MIFList(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	send_printf(getMMudOut(), "<HTML>\n");
	send_printf(getMMudOut(), "<HEAD>\n");
	send_printf(getMMudOut(), "<TITLE>\n");
	send_printf(getMMudOut(), "Land of Karchan - MIF List\n");
	send_printf(getMMudOut(), "</TITLE>\n");
	send_printf(getMMudOut(), "</HEAD>\n");

	send_printf(getMMudOut(), "<BODY>\n");
	if (!getFrames())
	{
		send_printf(getMMudOut(), "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\" onLoad=\"setfocus()\">\n");
	}
	else
	{
		if (getFrames()==1)
		{
			send_printf(getMMudOut(), "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\" onLoad=\"top.frames[2].document.myForm.command.value='';top.frames[2].document.myForm.command.focus()\">\n");
		} else
		{
			send_printf(getMMudOut(), "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\" onLoad=\"top.frames[3].document.myForm.command.value='';top.frames[3].document.myForm.command.focus()\">\n");
		}
	}

	send_printf(getMMudOut(), "<H1><IMG SRC=\"http://%s/images/gif/dragon.gif\">MIF List of Members</H1><HR><UL>\r\n", getParam(MM_SERVERNAME));
	temp = composeSqlStatement("select name, title from usertable where guild='mif'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL; // temp was allocated by composeSqlStatement 
	while (row = mysql_fetch_row(res))
	{
		send_printf(getMMudOut(), "<LI>%s, %s\r\n", row[0], row[1]);
	}
	mysql_free_result(res);
	send_printf(getMMudOut(), "</UL>\r\n");
	PrintForm(name, password);
	if (getFrames()!=2) {ReadFile(logname);}
	send_printf(getMMudOut(), "<HR><FONT Size=1><DIV ALIGN=right>%s", getParam(MM_COPYRIGHTHEADER));
	send_printf(getMMudOut(), "<DIV ALIGN=left><P>");
}

//! make a member of the mif enter the mif domicile
void 
MIFEntryIn(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, mysex[10];
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp = composeSqlStatement("select sex from tmp_usertable where name='%x'", name);
	res = SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	row = mysql_fetch_row(res);
	strcpy(mysex, row[0]);
	mysql_free_result(res);

	WriteMessage(name, room, "You notice %s waving %s hand in front of the south-wall. "
	"At first you wonder what %s is waving at, then suddenly the entire wall disappears! "
	" %s leaves to the south and behind %s the wall immediately appears again as before.<BR>\r\n",
	name, HeShe3(mysex), name, name, HeShe2(mysex));
	room = 143;
	WriteMessage(name, room, "You notice that the wall in the north disappears, %s appears, and the wall replaces itself.<BR>\r\n",
	name);

	temp = composeSqlStatement("update tmp_usertable set room=143 where name='%x'", name);
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	mysql_free_result(res);

	res=SendSQL2("select contents from action where id=9", NULL);
	row = mysql_fetch_row(res);
	LookString(row[0], name, password);
	mysql_free_result(res);

}

//! make a member leave the mif domicile
void 
MIFEntryOut(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, mysex[10];
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp = composeSqlStatement("select sex from tmp_usertable where name='%x'", name);
	res = SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	row = mysql_fetch_row(res);
	strcpy(mysex, row[0]);
	mysql_free_result(res);

	WriteMessage(name, room, "You notice %s waving %s hand in front of the north-wall. "
	"At first you wonder what %s is waving at, then suddenly the entire wall disappears! "
	" %s leaves to the north and behind %s the wall immediately appears again as before.<BR>\r\n", 
	name, HeShe3(mysex), name, name, HeShe2(mysex));
	room = 142;
	WriteMessage(name, room, "You notice that the wall in the south of this chamber suddenly disappears. "
	" %s appears from behind the wall, where you can see another room, and the wall "
	"represents itself. You are pretty amazed.<BR>\r\n", name);

	temp = composeSqlStatement("update tmp_usertable set room=142 where name='%x'", name);
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	mysql_free_result(res);

	res=SendSQL2("select contents from action where id=10", NULL);
	row = mysql_fetch_row(res);
	LookString(row[0], name, password);
	mysql_free_result(res);

}

//! talk to other mif members on the game
void 
MIFTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=red>Magitalk</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where guild='mif'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}

//! list ranger members
void 
RangerList(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	send_printf(getMMudOut(), "<HTML>\n");
	send_printf(getMMudOut(), "<HEAD>\n");
	send_printf(getMMudOut(), "<TITLE>\n");
	send_printf(getMMudOut(), "Land of Karchan - Ranger List\n");
	send_printf(getMMudOut(), "</TITLE>\n");
	send_printf(getMMudOut(), "</HEAD>\n");

	send_printf(getMMudOut(), "<BODY>\n");
	if (!getFrames())
	{
		send_printf(getMMudOut(), "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\" onLoad=\"setfocus()\">\n");
	}
	else
	{
		if (getFrames()==1)
		{
			send_printf(getMMudOut(), "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\" onLoad=\"top.frames[2].document.myForm.command.value='';top.frames[2].document.myForm.command.focus()\">\n");
		} else
		{
			send_printf(getMMudOut(), "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\" onLoad=\"top.frames[3].document.myForm.command.value='';top.frames[3].document.myForm.command.focus()\">\n");
		}
	}

	send_printf(getMMudOut(), "<H1><IMG SRC=\"http://%s/images/gif/dragon.gif\">Ranger List of Members</H1><HR><UL>\r\n", getParam(MM_SERVERNAME));
	temp = composeSqlStatement("select name, title from usertable where guild='rangers'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		send_printf(getMMudOut(), "<LI>%s, %s\r\n", row[0], row[1]);
	}
	mysql_free_result(res);
	send_printf(getMMudOut(), "</UL>\r\n");
	PrintForm(name, password);
	if (getFrames()!=2) {ReadFile(logname);}
	send_printf(getMMudOut(), "<HR><FONT Size=1><DIV ALIGN=right>%s", getParam(MM_COPYRIGHTHEADER));
	send_printf(getMMudOut(), "<DIV ALIGN=left><P>");
}

//! make a member enter the ranger guild room
void 
RangerEntryIn(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, mysex[10];
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp = composeSqlStatement("select sex from tmp_usertable where name='%x'", name);
	res = SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	row = mysql_fetch_row(res);
	strcpy(mysex, row[0]);
	mysql_free_result(res);

	WriteMessage(name, room, "%s walks into the calm, crystal-clear, water, and "
	 "steps up to the waterfall. %s then makes the sound of a bird and the"
	 " water slowy parts and %s passes through the solid stone wall"
	 " without a trace, leaving you baffled.<BR>\r\n",
	name, HeSheSmall(mysex), name);
	room = 216;
	WriteMessage(name, room, "You notice that the waterfall parts in two streams, "
	 "%s appears, and the waterfall flows back together.<BR>\r\n",
	name);

	temp = composeSqlStatement("update tmp_usertable set room=216 where name='%x'", name);
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	mysql_free_result(res);

	res=SendSQL2("select contents from action where id=13", NULL);
	row = mysql_fetch_row(res);
	LookString(row[0], name, password);
	mysql_free_result(res);

}

//! make a ranger leave the guild room
void 
RangerEntryOut(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, mysex[10];
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp = composeSqlStatement("select sex from tmp_usertable where name='%x'", name);
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	row = mysql_fetch_row(res);
	strcpy(mysex, row[0]);
	mysql_free_result(res);

	WriteMessage(name, room, "%s steps up to the waterfall. %s then makes the sound of a bird and the"
	" water slowy parts and %s passes through the solid stone wall"
	" without a trace.<BR>\r\n",
	name, HeSheSmall(mysex));
	room = 43;
	WriteMessage(name, room, "You hear the rustling water change in sound, Before your eye's"
	" %s slowly appears through the seemingly solid waterfall without a trace.<BR>\r\n", name);

	temp = composeSqlStatement("update tmp_usertable set room=43 where name='%x'", name);
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	mysql_free_result(res);

	res=SendSQL2("select contents from action where id=14", NULL);
	row = mysql_fetch_row(res);
	LookString(row[0], name, password);
	mysql_free_result(res);

}

//! talk to other rangers on the game
void 
RangerTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=green>Naturetalk</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where guild='rangers'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}

/*! add SWTalk */
void 
SWTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=brown>Pow Wow</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where guild='SW'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}

/*! add DepTalk */
void 
DepTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=purple>Deputy Line</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where god=1");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}	
/*! add BKTalk */
void 
BKTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=#CC0000>Chaos Murmur</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where guild='BKIC'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}
/*! add VampTalk */
void 
VampTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=#666666>Misty Whisper</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where guild='Kindred'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}
/*! add KnightTalk */
void 
KnightTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=#0000CC>Knight Talk</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where guild='Knights'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}
/*! add CoDTalk */
void 
CoDTalk(char *name, char *password, int room)
{
	char 		logname[100];

	MYSQL_RES *res;
	MYSQL_ROW row;
	char *temp, *temp2;
	
	sprintf(logname, "%s%s.log", getParam(MM_USERHEADER), name);

	temp2 = (char *) malloc(strlen(command) + 80);
	sprintf(temp2, "<B><Font color=#660000>Mogob Burz</font></B> [%s] : %s<BR>\r\n",
	name, command + (getToken(2) - getToken(0)));
	
	temp = composeSqlStatement("select name from tmp_usertable where guild='CoD'");
	res=SendSQL2(temp, NULL);
	free(temp);temp=NULL;
	while (row = mysql_fetch_row(res))
	{
		WriteLinkTo(row[0], name, temp2);
	}
	mysql_free_result(res);
	
	free(temp2);
	WriteRoom(name, password, room, 0);
}

