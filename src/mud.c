/*-------------------------------------------------------------------------
cvsinfo: $Header$
Maarten's Mud, WWW-based MUD using MYSQL
Copyright (C) 1998  Maarten van Leunen

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This nifty program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Maarten van Leunen
Driek van Erpstraat 9
5341 AK Oss
Nederland
Europe
maartenl@il.fontys.nl
-------------------------------------------------------------------------*/
#include <time.h>
#include "mud-lib3.h"

int commandlineinterface = 0;

/* three strings destined for parsing the commands */
extern char    *command;
extern char    *junk;
extern char    *printstr;
extern char    *tokens[100];
extern int      aantal;
extern struct tm datumtijd;
extern time_t   datetime;
extern char secretpassword[40];

char *name;		/* contains the name derived from the forms */
char *password;	/* contains the password derived from the forms */
int room;		/* contains the room where the character is located */
int godstatus;		/* contains the godstatus of the character */
char sexstatus[8];
int sleepstatus;	/* contains the sleepstatus of the character */
char guildstatus[10];		/* contains the guild a person belongsto */
int punishment;	/* contains wether or not you are normal, are a frog, or are a rat. */

MYSQL_RES *res;
MYSQL_ROW row;
char sqlstring[1024];

void 
InitVar(char *fcommand)
{
	printstr = (char *) malloc(strlen(fcommand)+500);
	junk = (char *) malloc(strlen(fcommand));
	time(&datetime);
	datumtijd = *(gmtime(&datetime));
	srandom(datetime);
}				/* endproc */

void
GoDown_Command(char *name, char *password, int room)
{
	roomstruct*temproom;
	int i=0;
	char logname[100];
	MYSQL_RES *res;
	MYSQL_ROW row;
	char temp[1024];
	int strength, movementstats, maxmove;

//	RoomTextProc(room);

	sprintf(logname, "%s%s.log",USERHeader,name);

	sprintf(temp, "select strength, movementstats, maxmove from tmp_usertable "
		"where name='%s'"
		, name);
	res=SendSQL2(temp, NULL);
	row = mysql_fetch_row(res);
	strength = atoi(row[0]);
	movementstats = atoi(row[1]);
	maxmove = atoi(row[2]);
	mysql_free_result(res);

	temproom=GetRoomInfo(room);

	if (!temproom->down)  {
		WriteSentenceIntoOwnLogFile(logname, "You can't go that way.<BR>");
	} else {
		if (movementstats >= maxmove)
		{
			/* if exhausted */
			WriteMessage(name, room, "%s attempts to leave north, but is exhausted.<BR>\r\n", name, name);
			WriteSentenceIntoOwnLogFile(logname, "You are exhausted.<BR>\r\n");
		}
		else   
		{
			/* if NOT exhausted */
			int burden;
			burden = CheckWeight(name);
			/* if burden too heavy to move */
			if (computeEncumberance(burden, strength) == -1)
			{
				WriteMessage(name, room, "%s attempts to leave north, but is too heavily burdened.<BR>\r\n", name, name);
				WriteSentenceIntoOwnLogFile(logname, "You are carrying <I>way</I> too many items to move.<BR>\r\n");
			}
			else /* if burden NOT too heavy to move */
			{
				movementstats = movementstats + computeEncumberance(burden, strength);
				if (movementstats > maxmove) {movementstats = maxmove;}
				WriteMessage(name, room, "%s leaves down.<BR>\r\n", name);
				room = temproom->down;
				sprintf(temp, "update tmp_usertable set room=%i where name='%s'"
								, room, name);
				res=SendSQL2(temp, NULL);
				mysql_free_result(res);
				WriteMessage(name, room, "%s appears.<BR>\r\n", name);
			} /* if burden NOT too heavy to move */
		} /* if NOT exhausted */
	}
	free(temproom);
	WriteRoom(name, password, room, 0);
	KillGame();
} 				/* endproc */

void
GoUp_Command(char *name, char *password, int room)
{
	roomstruct*temproom;
	int i=0;
	char logname[100];
	MYSQL_RES *res;
	MYSQL_ROW row;
	char temp[1024];
	int strength, movementstats, maxmove;

//	RoomTextProc(room);

	sprintf(logname, "%s%s.log",USERHeader,name);

	sprintf(temp, "select strength, movementstats, maxmove from tmp_usertable "
		"where name='%s'"
		, name);
	res=SendSQL2(temp, NULL);
	row = mysql_fetch_row(res);
	strength = atoi(row[0]);
	movementstats = atoi(row[1]);
	maxmove = atoi(row[2]);
	mysql_free_result(res);

	temproom=GetRoomInfo(room);

	if (!temproom->up)  {
		WriteSentenceIntoOwnLogFile(logname, "You can't go that way.<BR>");
	} else {
		if (movementstats >= maxmove)
		{
			/* if exhausted */
			WriteMessage(name, room, "%s attempts to leave north, but is exhausted.<BR>\r\n", name, name);
			WriteSentenceIntoOwnLogFile(logname, "You are exhausted.<BR>\r\n");
		}
		else   
		{
			/* if NOT exhausted */
			int burden;
			burden = CheckWeight(name);
			/* if burden too heavy to move */
			if (computeEncumberance(burden, strength) == -1)
			{
				WriteMessage(name, room, "%s attempts to leave north, but is too heavily burdened.<BR>\r\n", name, name);
				WriteSentenceIntoOwnLogFile(logname, "You are carrying <I>way</I> too many items to move.<BR>\r\n");
			}
			else /* if burden NOT too heavy to move */
			{
				movementstats = movementstats + computeEncumberance(burden, strength);
				if (movementstats > maxmove) {movementstats = maxmove;}
				WriteMessage(name, room, "%s leaves up.<BR>\r\n", name);
				room = temproom->up;
				sprintf(temp, "update tmp_usertable set room=%i where name='%s'"
								, room, name);
				res=SendSQL2(temp, NULL);
				mysql_free_result(res);
				WriteMessage(name, room, "%s appears.<BR>\r\n", name);
			} /* if burden NOT too heavy to move */
		} /* if NOT exhausted */
	}
	free(temproom);
	WriteRoom(name, password, room, 0);
	KillGame();
} 				/* endproc */


void BannedFromGame(char *name, char *address)
{
	char printstr[512];
	time_t tijd;
	struct tm datum;
	fprintf(cgiOut, "<HTML><HEAD><TITLE>You have been banned</TITLE></HEAD>\n\n");
	fprintf(cgiOut, "<BODY>\n");
	fprintf(cgiOut, "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\"><H1>Banned</H1><HR>\n");
	fprintf(cgiOut, "You, or someone in your domain,  has angered the gods by behaving badly on this mud. ");
	fprintf(cgiOut, "Your ip domain is therefore banned from the game.<P>\n");
	fprintf(cgiOut, "If you have not misbehaved or even have never before played the game before, and wish"
	" to play with your current IP address, email to "
	"<A HREF=\"mailto:deputy@"ServerName"\">deputy@"ServerName"</A> and ask them to make "
	"an exception in your case. Do <I>not</I> forget to provide your "
	"Character name.<P>You'll be okay as long as you follow the rules.<P>\n");
	fprintf(cgiOut, "</body>\n");
	fprintf(cgiOut, "</HTML>\n");
	time(&tijd);
	datum=*(gmtime(&tijd));
	WriteSentenceIntoOwnLogFile(AuditTrailFile,"%i:%i:%i %i-%i-%i Banned from mud by %s (%s) <BR>\n",datum.tm_hour,
	datum.tm_min,datum.tm_sec,datum.tm_mday,datum.tm_mon+1,datum.tm_year+1900,name, address);
	exit(0);
}

void CookieNotFound(char *name, char *address)
{
	char printstr[512];
	time_t tijd;
	struct tm datum;
	fprintf(cgiOut, "<HTML><HEAD><TITLE>Unable to logon</TITLE></HEAD>\n\n");
	fprintf(cgiOut, "<BODY>\n");
	fprintf(cgiOut, "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\"><H1>Unable to logon</H1><HR>\n");
	fprintf(cgiOut, "When you logon, a cookie is automatically generated. ");
	fprintf(cgiOut, "However, I have been unable to find my cookie.<P>\n");
	fprintf(cgiOut, "Please attempt to relogon.<P>\n");
	fprintf(cgiOut, "</body>\n");
	fprintf(cgiOut, "</HTML>\n");
	time(&tijd);
	datum=*(gmtime(&tijd));
	WriteSentenceIntoOwnLogFile(AuditTrailFile,"%i:%i:%i %i-%i-%i Cookie not found for mud by %s (%s) <BR>\n",datum.tm_hour,
	datum.tm_min,datum.tm_sec,datum.tm_mday,datum.tm_mon+1,datum.tm_year+1900,name, address);
	closedbconnection();
	exit(0);
}

int 
gameMain(char *fcommand, char *fname, char *fpassword)
{
	int             oldroom;
	int             i;
	char						frames[10];
	char           *temp;
	char            logname[100];

	command = fcommand;
	name = fname;
	password = fpassword;

	umask(0000);

	InitVar(command);

	opendbconnection();

	if (SearchBanList(cgiRemoteAddr, name)) {BannedFromGame(name, cgiRemoteAddr);}

	if (!ExistUser(name)) {
		NotActive(name,password,1);
	}

//	openDatabase();
	sprintf(sqlstring, "select name, lok, sleep, room, lastlogin, god, sex, vitals, maxvital, guild, punishment "
		"from tmp_usertable "
		"where name='%s' and lok<>''", name);
	res=SendSQL2(sqlstring, NULL);
	if (res==NULL)
	{
		NotActive(name, password,2);
	}
	row = mysql_fetch_row(res);
	if (row==NULL)
	{
		NotActive(name, password,3);
	}
	
	strcpy(name, row[0]); /* copy name to name from database */
	room=atoi(row[3]); /* copy roomnumber to room from database */
	sleepstatus=atoi(row[2]); /* copy sleep to sleepstatus from database */
	godstatus=atoi(row[5]); /* copy godstatus of player from database */
	strcpy(sexstatus,row[6]); /* sex status (male, female) */
	if (atoi(row[7])>atoi(row[8])) { /* check vitals along with maxvital */
		Dead(name, password, room);
	}
	strcpy(guildstatus	, row[9]);
	punishment = atoi(row[10]);
	if (strcmp(row[1], password)) {
		if (!commandlineinterface) 
		{
			NotActive(name, password,4);
		}
	}
	mysql_free_result(res);
	if (DebugOptionsOn) {
		if (!strcmp(name, "Karn")) {
			fprintf(cgiOut, "Command: %s<BR>ActivePersonPos: %i<BR>Password: %s<BR>", command, password);
		}
	}
	if (*name == '\0') 
	{ 
		NotActive(name, password,5);
	}
	if (godstatus==2) 
	{
		NotActive(name, password,6);
	}
	sprintf(logname, "%s%s.log", USERHeader, name);

//	'0000-01-01 00:00:00' - '9999-12-31 23:59:59'
	sprintf(sqlstring, "update tmp_usertable set lastlogin=date_sub(NOW(), INTERVAL 2 HOUR), "
			"address='%s' where name='%s'",	cgiRemoteAddr, name);
	res=SendSQL2(sqlstring, NULL);
	
	mysql_free_result(res);

	strcpy(junk, command);
	tokens[0] = junk;
	tokens[0] = strtok(junk, " ");
	if (tokens[0] != NULL) {
		i = 1;
		while (i != -1) {
			tokens[i] = strtok(NULL, " ");
			i++;
			if (tokens[i - 1] == NULL) {
				aantal = i - 1;
				i = -1;
			}	/* endif */
			if (i>95) {
				aantal = i - 1;
				i = -1;
			}	/* endif */
		}		/* endwhile */
	}			/* endif */

	if ((strstr(command,"<applet")!=NULL) || (strstr(command,"<script")!=NULL)
		|| (strstr(command,"java-script")!=NULL) || (strstr(command,"CommandForm")!=NULL)) { 
		WriteSentenceIntoOwnLogFile(logname, "I am afraid, I do not understand that.<BR>\r\n");
		WriteRoom(name, password, room, 0);
		KillGame();
		}

	if (sleepstatus==1) 
	{
		if (!strcasecmp(command, "awaken"))
		{
			Awaken_Command(name, password, room);
		}
		WriteSentenceIntoOwnLogFile(logname, "You can't do that. You are asleep, silly.<BR>\r\n");
		WriteRoom(name, password, room, 1);
		KillGame();
	}
	
	if (punishment > 0)
	{
		if (!strcasecmp(command, "say rrribbit"))
		{
		WriteMessage(name, room, "The toad called %s says : <I>rrrrrribbit.</I><BR>\r\n", name);
		WriteSentenceIntoOwnLogFile(logname, "You say : <I>rrrrrribbit.</I><BR>\r\n"
			"You still have %i ribbits to go...<BR>\r\n", punishment-1);
		if (punishment == 1)
		{
		WriteMessage(name, room, "The toad called %s changes back into it's old self.<BR>\r\n", name);
		WriteSentenceIntoOwnLogFile(logname, "You change back into your old self.<BR>\r\n"
			 "Do not do again what you did to become this.<BR>\r\n");
		
		}
		sprintf(sqlstring, "update tmp_usertable set punishment=punishment-1 where name='%s'",
			name);
		res=SendSQL2(sqlstring, NULL);
		mysql_free_result(res);
		WriteRoom(name, password, room, 0);
		KillGame();
		}
		if ((*command == '\0') ||
		(!strcasecmp(command, "look around")) ||
		(!strcasecmp(command, "look")) ||
		(!strcasecmp(command, "l"))) {
			WriteRoom(name, password, room, 0);
			KillGame();
		}
		if ((!strcasecmp(command, "go west")) ||
		(!strcasecmp(command, "west")) ||
		(!strcasecmp(command, "w"))) {
				GoWest_Command(name, password, room);
		}
		if ((!strcasecmp(command, "go east")) ||
		(!strcasecmp(command, "east")) ||
		(!strcasecmp(command, "e"))) {
			GoEast_Command(name, password, room);
		}
		if ((!strcasecmp(command, "go north")) ||
		(!strcasecmp(command, "north")) ||
		(!strcasecmp(command, "n"))) {
			GoNorth_Command(name, password, room);
		}
		if ((!strcasecmp(command, "go south")) ||
		(!strcasecmp(command, "south")) ||
		(!strcasecmp(command, "s"))) {
			GoSouth_Command(name, password, room);
		}
	
		if ((!strcasecmp(command, "go down")) ||
		(!strcasecmp(command, "down"))) {
			GoDown_Command(name, password, room);
		}
		if ((!strcasecmp(command, "go up")) ||
		(!strcasecmp(command, "up"))) {
			GoUp_Command(name, password, room);
		}
		if (!strcasecmp(command, "quit")) 
		{
			Quit_Command(name);
		}

	WriteSentenceIntoOwnLogFile(logname, "You cannot do that, you are a frog, remember?<BR>\r\n");
	WriteRoom(name, password, room, 0);
	KillGame();
	}

	SearchForSpecialCommand(name, password, room);
	
	if (!strcasecmp(command, "sleep"))
	{
		Sleep_Command(name, password, room);
	}
	
	if (!strcasecmp(command, "awaken"))
	{
		WriteSentenceIntoOwnLogFile(logname, "You aren't asleep, silly.<BR>\r\n");
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	
	if (!strcasecmp(command, "quit")) 
	{
		Quit_Command(name);
	}

	if (godstatus==1) {Root_Command(name, password, room);}
	if (godstatus==2) {Evil_Command(name, password, room);}

	if (!strcasecmp(command, "help hint")) 
	{
		HelpHint_Command(name, password, room);
	}
	if (!strcasecmp(command, "help")) 
	{
		MYSQL_RES *res;
		MYSQL_ROW row;
		int i;
		char temp[1024];
		
		fprintf(cgiOut, "<HTML>\r\n");
		fprintf(cgiOut, "<HEAD>\r\n");
		fprintf(cgiOut, "<TITLE>\r\n");
		fprintf(cgiOut, "Land of Karchan - General Help\r\n");
		fprintf(cgiOut, "</TITLE>\r\n");
		fprintf(cgiOut, "</HEAD>\r\n");
		
		fprintf(cgiOut, "<BODY>\r\n");
		fprintf(cgiOut, "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\">\r\n");
	
		sprintf(temp, "select contents from help where command='general help'");
		res=SendSQL2(temp, NULL);
		row = mysql_fetch_row(res);
		if (row==NULL) 
		{
			mysql_free_result(res);
			res=SendSQL2("select contents from help where command='sorry'", NULL);
			row = mysql_fetch_row(res);
			fprintf(cgiOut, "%s",row[0]);
		}
		else
		{
			fprintf(cgiOut, "%s",row[0]);
		}
		mysql_free_result(res);
		PrintForm(name, password);
		if (getFrames()!=2) {ReadFile(logname);}
		KillGame();
	}
	if ((aantal >= 2) && (!strcasecmp(tokens[0],"help"))) 
	{
		MYSQL_RES *res;
		MYSQL_ROW row;
		int i;
		char temp[1024];
		
		fprintf(cgiOut, "<HTML>\r\n");
		fprintf(cgiOut, "<HEAD>\r\n");
		fprintf(cgiOut, "<TITLE>\r\n");
		fprintf(cgiOut, "Land of Karchan - Command %s\r\n", tokens[1]);
		fprintf(cgiOut, "</TITLE>\r\n");
		fprintf(cgiOut, "</HEAD>\r\n");
		
		fprintf(cgiOut, "<BODY>\r\n");
		fprintf(cgiOut, "<BODY BGCOLOR=#FFFFFF BACKGROUND=\"/images/gif/webpic/back4.gif\">\r\n");
	
		sprintf(temp, "select contents from help where command='%s'", tokens[1]);
		res=SendSQL2(temp, NULL);
		row = mysql_fetch_row(res);
		if (row==NULL) 
		{
			mysql_free_result(res);
			res=SendSQL2("select contents from help where command='sorry'", NULL);
			row = mysql_fetch_row(res);
			fprintf(cgiOut, "%s",row[0]);
		}
		else
		{
			fprintf(cgiOut, "%s",row[0]);
		}
		mysql_free_result(res);
		PrintForm(name, password);
		if (getFrames()!=2) {ReadFile(logname);}
		KillGame();
	}

   if ((!strcasecmp(command, "inventory")) || (!strcasecmp(command, "i"))) 
   {
		WriteInventoryList(name, password);
		KillGame();
	}           /* Inventory_Command */
	if (!strcasecmp(command, "clear")) {
	        char logname[100];
	        sprintf(logname, "%s%s.log", USERHeader, name);
		WriteRoom(name, password, room, 0);
		ClearLogFile(logname);
		WriteSentenceIntoOwnLogFile(logname, "You cleared your mind.<BR>\r\n");
		KillGame();
	}			/* Clear_Command */
	if (!strcasecmp(command, "who")) {
		ListActivePlayers(name, password);
		WriteRoom(name, password, room, 0);
		KillGame();
	}			/* Who_Command */
	if (!strcasecmp("big talk", command)) 
	{
		BigTalk_Command(name, password);
	}
	if (!strcasecmp(command, "time")) 
	{
		Time_Command(name, password, room);
	}
	if (!strcasecmp(command, "date")) 
	{
		Date_Command(name, password, room);
	}
	if ((!strcasecmp("look at sky", command)) ||
	   (!strcasecmp("look at clouds", command))) 
	{
		LookSky_Command(name, password);
	}
/*	if (!strcasecmp(command, "introduce me")) {
		IntroduceMe_Command(logname);
	}*/
	if (!strcasecmp(command, "bow")) {
		WriteSentenceIntoOwnLogFile(logname, "You bow gracefully.<BR>\r\n");
		WriteMessage(name, room, "%s bows gracefully.<BR>\r\n", name);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if (aantal == 3 && (!strcasecmp(tokens[0], "bow")) && (!strcasecmp(tokens[1], "to"))) {
		if (WriteMessageTo(tokens[2], name, room, "%s bows gracefully to %s.<BR>\r\n", name, tokens[2]) == 0) {
			WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
		} else {
			WriteSayTo(tokens[2], name, room, "%s bows gracefully to you.<BR>\r\n", name);
			WriteSentenceIntoOwnLogFile(logname, "You bow gracefully to %s.<BR>\r\n", tokens[2]);
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if (!strcasecmp(command, "eyebrow")) {
		WriteSentenceIntoOwnLogFile(logname, "You raise an eyebrow.<BR>\r\n");
		WriteMessage(name, room, "%s raises an eyebrow.<BR>\r\n", name);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	/* smile */
	if ((temp = get_pluralis(command)) != NULL) {
		WriteSentenceIntoOwnLogFile(logname, "You %s.<BR>\r\n", command);
		WriteMessage(name, room, "%s %s.<BR>\r\n", name, temp);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	/* smile engagingly */
	if ( (aantal==2) && ((temp = get_pluralis(tokens[0])) != NULL) &&
		 (exist_adverb(tokens[1])) ) {
		WriteSentenceIntoOwnLogFile(logname, "You %s %s.<BR>\r\n", tokens[0], tokens[1]);
		WriteMessage(name, room, "%s %s %s.<BR>\r\n", name, temp, tokens[1]);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	/* smile to bill */
	if ((aantal == 3) && ((temp = get_pluralis(tokens[0])) != NULL) && 
		(!strcasecmp(tokens[1], "to")) ) {
		if (WriteMessageTo(tokens[2], name, room, "%s %s to %s.<BR>\r\n", name, temp, tokens[2]) == 0) {
			WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
		} else {
			WriteSayTo(tokens[2], name, room, "%s %s to you.<BR>\r\n", name, temp);
			WriteSentenceIntoOwnLogFile(logname, "You %s to %s.</BR>\r\n", tokens[0], tokens[2]);
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	/* smile(0) engagingly(1) to(2) Bill(3) */
	if ((aantal == 4) && ((temp = get_pluralis(tokens[0])) != NULL) && 
		(!strcasecmp(tokens[2], "to")) && (exist_adverb(tokens[1])) ) {
		if (WriteMessageTo(tokens[3], name, room, "%s %s %s to %s.<BR>\r\n", name, temp, tokens[1], tokens[3]) == 0) {
			WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
		} else {
			WriteSayTo(tokens[3], name, room, "%s %s %s to you.<BR>\r\n", name, temp, tokens[1]);
			WriteSentenceIntoOwnLogFile(logname, "You %s %s to %s.</BR>\r\n", tokens[0], tokens[1], tokens[3]);
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if (!strcasecmp(command, "curtsey")) {
		WriteSentenceIntoOwnLogFile(logname, "You drop a curtsey.<BR>\r\n");
		WriteMessage(name, room, "%s drops a curtsey.<BR>\r\n", name);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if (aantal == 3 && (!strcasecmp(tokens[0], "curtsey")) && (!strcasecmp(tokens[1], "to"))) {
		if (WriteMessageTo(tokens[2], name, room, "%s drops a curtsey to %s.<BR>\r\n", name, tokens[2]) == 0) {
			WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
		} else {
			WriteSayTo(tokens[2], name, room, "%s drops a curtsey to you.<BR>\r\n", name);
			WriteSentenceIntoOwnLogFile(logname, "You drop a curtsey to %s.</BR>\r\n", tokens[2]);
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if (!strcasecmp(command, "flinch")) {
		WriteSentenceIntoOwnLogFile(logname, "You flinch.<BR>\r\n");
		WriteMessage(name, room, "%s flinches.<BR>\r\n", name);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if (aantal == 3 && (!strcasecmp(tokens[0], "flinch")) && (!strcasecmp(tokens[1], "to"))) {
		if (WriteMessageTo(tokens[2], name, room, "%s flinches to %s.<BR>\r\n", name, tokens[2]) == 0) {
			WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
		} else {
			WriteSayTo(tokens[2], name, room, "%s flinches to you.<BR>\r\n", name);
			WriteSentenceIntoOwnLogFile(logname, "You flinch to %s.</BR>\r\n", tokens[2]);
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if ((*command == '\0') ||
	    (!strcasecmp(command, "look around")) ||
	    (!strcasecmp(command, "look")) ||
	    (!strcasecmp(command, "l"))) {
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if ((!strcasecmp(command, "go west")) ||
	    (!strcasecmp(command, "west")) ||
	    (!strcasecmp(command, "w"))) {
		GoWest_Command(name, password, room);
	}
	if ((!strcasecmp(command, "go east")) ||
	    (!strcasecmp(command, "east")) ||
	    (!strcasecmp(command, "e"))) {
		GoEast_Command(name, password, room);
	}
	if ((!strcasecmp(command, "go north")) ||
	    (!strcasecmp(command, "north")) ||
	    (!strcasecmp(command, "n"))) {
		GoNorth_Command(name, password, room);
	}
	if ((!strcasecmp(command, "go south")) ||
	    (!strcasecmp(command, "south")) ||
	    (!strcasecmp(command, "s"))) {
		GoSouth_Command(name, password, room);
	}

	if ((!strcasecmp(command, "go down")) ||
	    (!strcasecmp(command, "down"))) {
		GoDown_Command(name, password, room);
	}
	if ((!strcasecmp(command, "go up")) ||
	    (!strcasecmp(command, "up"))) {
		GoUp_Command(name, password, room);
	}
	if ((aantal >1) && (!strcasecmp(tokens[0], "title"))) {
		ChangeTitle_Command(name, password, room);
	}
	if ((aantal > 1) && (!strcasecmp(tokens[0], "look"))) {
		Look_Command(name, password, room);
	}
	if ((aantal > 1) && (!strcasecmp(tokens[0], "me"))) {
		WriteMessage(name, room, "%s %s<BR>\r\n", name, command + 3);
		WriteSentenceIntoOwnLogFile(logname, "%s %s<BR>\r\n", name, command + 3);
		WriteRoom(name, password, room, 0);
		KillGame();
	} /* endme */
	if ((aantal > 3) && (!strcasecmp("tell", tokens[0])) && (!strcasecmp("to", tokens[1]))) {
		if (!WriteLinkTo(tokens[2], name, "<B>%s tells you </B>: %s<BR>\r\n",
					    name, command + (tokens[3] - tokens[0]))) {
			WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
		} else {
			WriteSentenceIntoOwnLogFile(logname, "<B>You tell %s</B> : %s<BR>\r\n",
			      tokens[2], command + (tokens[3] - tokens[0]));
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}
//	if ((aantal > 1) && (!strcasecmp("me", tokens[0]))) {
//		WriteSentenceIntoOwnLogFile(logname, "%s %s<BR>\r\n", name, command + 3);
//		WriteRoom(name, password, room, 0);
//		KillGame();
//	}			/* endme */
	if ((!strcasecmp("say", tokens[0])) && (aantal > 1)) {
		if ((!strcasecmp("to", tokens[1])) && (aantal > 3)) {
			if (!WriteMessageTo(tokens[2], name, room, "%s says [to %s] : %s<BR>\r\n",
					    name, tokens[2], command + (tokens[3] - tokens[0]))) {
				WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
			}
			 /* person not found */ 
			else {
				WriteSayTo(tokens[2], name, room, 
					   "<B>%s says [to you]</B> : %s<BR>\r\n", name, command + (tokens[3] - tokens[0]));
				WriteSentenceIntoOwnLogFile(logname, "<B>You say [to %s]</B> : %s<BR>\r\n", tokens[2], command + (tokens[3] - tokens[0]));
				ReadBill(tokens[2], command + (tokens[3] - tokens[0]), name, room);
			}
			WriteRoom(name, password, room, 0);
			KillGame();
		}
		WriteSentenceIntoOwnLogFile(logname, "<B>You say </B>: %s<BR>\r\n", command + 4);
		WriteMessage(name, room, "%s says : %s<BR>\r\n", name, command + 4);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if ((aantal > 1) && (!strcasecmp("shout", tokens[0]))) {
		if ((!strcasecmp("to", tokens[1])) && (aantal > 3)) {
			char           *temp1, *temp2;
			temp1 = (char *) malloc(cgiContentLength + 80);
			temp2 = (char *) malloc(cgiContentLength + 80);
			sprintf(temp1, "<B>%s shouts [to you] </B>: %s<BR>\r\n",
				name, command + (tokens[3] - tokens[0]));
			sprintf(temp2, "%s shouts [to %s] : %s<BR>\r\n",
				name, tokens[2], command + (tokens[3] - tokens[0]));
			if (!WriteMessageTo(tokens[2], name, room, temp2)) {
				WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
			} else {
				WriteSayTo(tokens[2], name, room, temp1);
				WriteSentenceIntoOwnLogFile(logname, "<B>You shout [to %s] </B>: %s<BR>\r\n",
							     tokens[2], command + (tokens[3] - tokens[0]));
			}
			free(temp2);
			free(temp1);
			WriteRoom(name, password, room, 0);
			KillGame();
		}
		WriteSentenceIntoOwnLogFile(logname, "<B>You shout</B> : %s<BR>\r\n", command + 6);
		WriteMessage(name, room, "%s shouts : %s<BR>\r\n", name, command + 6);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if ((aantal > 1) && (!strcasecmp("ask", tokens[0]))) {
		if ((!strcasecmp("to", tokens[1])) && (aantal > 3)) {
			char           *temp1, *temp2;
			temp1 = (char *) malloc(cgiContentLength + 80);
			temp2 = (char *) malloc(cgiContentLength + 80);
			sprintf(temp1, "<B>%s asks you </B>: %s<BR>\r\n",
				name, command + (tokens[3] - tokens[0]));
			sprintf(temp2, "%s asks %s : %s<BR>\r\n",
				name, tokens[2], command + (tokens[3] - tokens[0]));
			if (!WriteMessageTo(tokens[2], name, room, temp2)) {
				WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
			} else {
				WriteSayTo(tokens[2], name, room, temp1);
				WriteSentenceIntoOwnLogFile(logname, "<B>You ask %s</B> : %s<BR>\r\n",
							     tokens[2], command + (tokens[3] - tokens[0]));
			}
			free(temp2);
			free(temp1);
			WriteRoom(name, password, room, 0);
			KillGame();
		}
		WriteSentenceIntoOwnLogFile(logname, "<B>You ask</B> : %s<BR>\r\n", command + 4);
		WriteMessage(name, room, "%s asks : %s<BR>\r\n", name, command + 4);
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if ((aantal > 1) && (!strcasecmp("whisper", tokens[0]))) {
		if ((!strcasecmp("to", tokens[1])) && (aantal > 3)) {
			char           *temp1, *temp2;
			temp1 = (char *) malloc(cgiContentLength + 80);
			temp2 = (char *) malloc(cgiContentLength + 80);
			sprintf(temp1, "<B>%s whispers [to you]</B> : %s<BR>\r\n",
				name, command + (tokens[3] - tokens[0]));
			sprintf(temp2, "%s is whispering something to %s, but you cannot hear what.<BR>\r\n",
				name, tokens[2]);
			if (!WriteMessageTo(tokens[2], name, room, temp2)) {
				WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
			} else {
				WriteSayTo(tokens[2], name, room, temp1);
				WriteSentenceIntoOwnLogFile(logname, "<B>You whisper [to %s]</B> : %s<BR>\r\n",
							     tokens[2], command + (tokens[3] - tokens[0]));
			}
			free(temp2);
			free(temp1);
			WriteRoom(name, password, room, 0);
			KillGame();
		}
		WriteSentenceIntoOwnLogFile(logname, "<B>You whisper </B>: %s<BR>\r\n", command + 8);
		WriteMessage(name, room, "%s whispers : %s<BR>\r\n", name, command + 8);
		WriteRoom(name, password, room, 0);
		KillGame();
	}

	if ( (aantal >= 2) && (!strcasecmp("fight", tokens[0])) )
	{
		int myFightable;
		char *myFightingName;
		char *myDescription;
		sprintf(sqlstring, "select fightable from tmp_usertable where "
			"name='%s'", name);
		res=SendSQL2(sqlstring, NULL);
		row = mysql_fetch_row(res);
		myFightable = atoi(row[0]);
		mysql_free_result(res);

		/* this section takes care of the looking up of the description */
		myFightingName = 
		ExistUserByDescription(tokens, 1, aantal - 1, room, &myDescription);
		if (myFightingName == NULL)
		{
			myFightingName = tokens[1];
			myDescription = tokens[1];
		}
		else
		{
			printf("<%s><%s>", myFightingName, myDescription);
		}

		sprintf(sqlstring, "select name,god from tmp_usertable where "
		"name<>'%s' and "
		"name='%s' and "
		"fightable=1 and "
		"god<>2 and "
		"room=%i"
		, name, myFightingName, room);
		res=SendSQL2(sqlstring, NULL);
		row = mysql_fetch_row(res);
		if ((myFightable!=1) && (atoi(row[1])!=3))
		{
			mysql_free_result(res);
			if (myFightingName != tokens[1]) {free(myFightingName);}
			if (myDescription != tokens[1]) {free(myDescription);}
			WriteSentenceIntoOwnLogFile(logname, "Pkill is off, so you cannot fight.<BR>\r\n");
			WriteRoom(name, password, room, 0);
			KillGame();
		}
		if (row!=NULL)
		{
			WriteSentenceIntoOwnLogFile(logname, "You start to fight against %s.<BR>\r\n", myDescription);
			WriteMessageTo(myFightingName, name, room, "%s starts fighting against %s.<BR>\r\n",
				    name, myDescription);
			WriteSayTo(myDescription, name, room, 
				   "%s starts fighting against you.<BR>\r\n", name);
			mysql_free_result(res);
			sprintf(sqlstring, "update tmp_usertable set fightingwho='%s' where name='%s'",
			myFightingName, name);
			res=SendSQL2(sqlstring, NULL);
			mysql_free_result(res);
			sprintf(sqlstring, "update tmp_usertable set fightingwho='%s' where name='%s'",
			name, myFightingName);
			res=SendSQL2(sqlstring, NULL);
		}
		else
		{
			WriteSentenceIntoOwnLogFile(logname, "Person not found.<BR>\r\n");
		}
		mysql_free_result(res);
		if (myFightingName != tokens[1]) {free(myFightingName);}
		if (myDescription != tokens[1]) {free(myDescription);}
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if (!strcasecmp("stop fighting", command))
	{
		sprintf(sqlstring, "select fightingwho from tmp_usertable where "
			"name='%s'", name);
		res=SendSQL2(sqlstring, NULL);
		row = mysql_fetch_row(res);
		if (row[0][0]==0)
		{
			mysql_free_result(res);
			WriteSentenceIntoOwnLogFile(logname, "You are not fighting anyone.<BR>\r\n");
			WriteRoom(name, password, room, 0);
			KillGame();
		}
		mysql_free_result(res);
		sprintf(sqlstring, "update tmp_usertable set fightingwho='' where "
			"name='%s'"
			, name);
		res=SendSQL2(sqlstring, NULL);
		mysql_free_result(res);
		WriteMessage(name, room, "%s stops fighting.<BR>\r\n", name);
		WriteSentenceIntoOwnLogFile(logname, "You stop fighting.<BR>\r\n");
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if ((aantal == 2) && (!strcasecmp("pkill", tokens[0]))) 
	{
		sprintf(sqlstring, "select fightingwho from tmp_usertable where "
			"name='%s'", name);
		res=SendSQL2(sqlstring, NULL);
		row = mysql_fetch_row(res);
		if (row[0][0]!=0)
		{
			mysql_free_result(res);
			WriteSentenceIntoOwnLogFile(logname, "You cannot change your pkill status during combat.<BR>\r\n");
		}
		else
		{
			mysql_free_result(res);
			if (!strcasecmp("on", tokens[1])) 
			{
				sprintf(sqlstring, "update tmp_usertable set fightable=1 where "
					"name='%s'", name);
				res=SendSQL2(sqlstring, NULL);
				mysql_free_result(res);
				WriteSentenceIntoOwnLogFile(logname, "Pkill is now on.<BR>\r\n");
			} /* pkill is turned on */
			else
			{
				sprintf(sqlstring, "update tmp_usertable set fightable=0 where "
					"name='%s'", name);
				res=SendSQL2(sqlstring, NULL);
				mysql_free_result(res);
				WriteSentenceIntoOwnLogFile(logname, "Pkill is now off.<BR>\r\n");
			} /* pkill is turned of */
		} /* if indeed the person is not already fighting */
		WriteRoom(name, password, room, 0);
		KillGame();
	}
	if ((aantal >= 1) && (!strcasecmp("whimpy", tokens[0]))) {
		char number[10];
		if (aantal == 1) {
			strcpy(number,"0");
		} else {
			if (!strcasecmp("help", command + (tokens[1] - tokens[0]))) {
				WriteSentenceIntoOwnLogFile(logname, 
				"Syntax: <B>whimpy &lt;string&gt;</B><UL><LI>feeling well"
				"<LI>feeling fine<LI>feeling quite nice<LI>slightly hurt"
				"<LI>hurt<LI>quite hurt<LI>extremely hurt<LI>terribly hurt"
				"<LI>feeling bad<LI>feeling very bad<LI>at death's door</UL>\r\n");
				WriteRoom(name, password, room, 0);
				KillGame();
			}
			if (!strcasecmp("feeling well", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"10");
				//x.whimpy = 10;
			}
			if (!strcasecmp("feeling fine", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"20");
				//x.whimpy = 20;
			}
			if (!strcasecmp("feeling quite nice", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"30");
				//x.whimpy = 30;
			}
			if (!strcasecmp("slightly hurt", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"40");
				//x.whimpy = 40;
			}
			if (!strcasecmp("hurt", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"50");
				//x.whimpy = 50;
			}
			if (!strcasecmp("quite hurt", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"60");
				//x.whimpy = 60;
			}
			if (!strcasecmp("extremely hurt", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"70");
				//x.whimpy = 70;
			}
			if (!strcasecmp("terribly hurt", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"80");
				//x.whimpy = 80;
			}
			if (!strcasecmp("feeling bad", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"90");
				//x.whimpy = 90;
			}
			if (!strcasecmp("feeling very bad", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"100");
				//x.whimpy = 100;
			}
			if (!strcasecmp("at death's door", command + (tokens[1] - tokens[0]))) {
				strcpy(number,"110");
				//x.whimpy = 110;
			}
		}
		sprintf(sqlstring, "update tmp_usertable set whimpy=%s "
			" where name='%s'", number, name);
		res=SendSQL2(sqlstring, NULL);
		if (res!=NULL)
		{
			mysql_free_result(res);
		}
		
		WriteSentenceIntoOwnLogFile(logname, "<I>Whimpy set.</I><BR>\r\n");
		WriteRoom(name, password, room, 0);
		KillGame();
	}			/* endwhimpy */
	if ((aantal == 1) && (!strcasecmp("stats", tokens[0]))) {
		Stats_Command(name, password);
	}
	if ( ((aantal==3) || (aantal==4)) && (!strcasecmp("get", tokens[0])) )
	{
	/* get copper coin(s)
	   get 10 copper coin(s)
	*/
		if ((!strcasecmp("coin",tokens[aantal-1])) ||
			(!strcasecmp("coins", tokens[aantal-1])) )
		{
			if ((!strcasecmp("copper",tokens[aantal-2])) ||
				(!strcasecmp("silver",tokens[aantal-2])) ||
				(!strcasecmp("gold", tokens[aantal-2])) )
			{
				GetMoney_Command(name, password, room);
			}
		}
	}
	if ((aantal >= 2) && (!strcasecmp("get", tokens[0]))) 
	{
		Get_Command(name, password, room);
	}
	if ( ((aantal==3) || (aantal==4)) && (!strcasecmp("drop", tokens[0])) )
	{
	/* drop copper coin(s)
	   drop 10 copper coin(s)
	*/
		if ((!strcasecmp("coin",tokens[aantal-1])) ||
			(!strcasecmp("coins", tokens[aantal-1])) )
		{
			if ((!strcasecmp("copper",tokens[aantal-2])) ||
				(!strcasecmp("silver",tokens[aantal-2])) ||
				(!strcasecmp("gold", tokens[aantal-2])) )
			{
				DropMoney_Command(name, password, room);
			}
		}
	}
	if ((aantal >= 2) && (!strcasecmp("drop", tokens[0]))) 
	{
		Drop_Command(name, password, room);
	}
	if ((aantal >= 4) && (!strcasecmp("put", tokens[0]))) 
	{
		/* put [amount] <item> in <item> ;
		    <item> = [bijv vmw] [bijv vnm] [bijv vnm] name
	   */
		Put_Command(name, password, room);
	}
	if ((aantal >= 4) && (!strcasecmp("retrieve", tokens[0]))) 
	{
		/* retrieve [amount] <item> from <item> ;
	       <item> = [bijv vmw] [bijv vnm] [bijv vnm] name
		*/
		Retrieve_Command(name, password, room);
	}
	if ((aantal >= 2) && (!strcasecmp("eat", tokens[0]))) 
	{
		Eat_Command(name, password, room);
	}
	if ((aantal >= 2) && (!strcasecmp("drink", tokens[0]))) 
	{
		Drink_Command(name, password, room);
	}
	if ((aantal >= 4) && (!strcasecmp("wear", tokens[0]))) 
	{
		Wear_Command(name, password, room);
	}
	if ((aantal >= 2) && (!strcasecmp("remove", tokens[0]))) 
	{
		Unwear_Command(name, password, room);
	}
	if ((aantal >= 2) && (!strcasecmp("wield", tokens[0]))) 
	{
		Wield_Command(name, password, room);
	}
	if ((aantal >= 2) && (!strcasecmp("unwield", tokens[0]))) 
	{
		Unwield_Command(name, password, room);
	}
	if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==9)) 
	{
		Buy_Command(name, password, room, "Bill");
	}
	if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==16)) 
	{
		Buy_Command(name, password, room, "Karcas");
	}
	if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==64)) 
	{
		Buy_Command(name, password, room, "Karina");
	}
        if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==73))
        {
                Buy_Command(name, password, room, "Hagen");
        }
	if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==85)) 
	{
		Buy_Command(name, password, room, "Karsten");
	}
	if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==87))
        {
                Buy_Command(name, password, room, "Kurst");
        }
        if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==161))
        {
                Buy_Command(name, password, room, "Karstare");
        }
        if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==228))
        {
                Buy_Command(name, password, room, "Vimrilad");
        }
        if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==2550))
        {
                Buy_Command(name, password, room, "Telios");
        }
        if ((aantal >= 2) && (!strcasecmp("buy", tokens[0])) && (room==2551))
        {
                Buy_Command(name, password, room, "Nolli");
        }
        if ((aantal >= 2) && (!strcasecmp("sell", tokens[0])) && (room==16)) 
	{
		Sell_Command(name, password, room, "Karcas");
	}
	if ((aantal >= 2) && (!strcasecmp("search", tokens[0]))) 
	{
		Search_Command(name, password, room);
	}
	if ( ((aantal==5) || (aantal==6)) && (!strcasecmp("give", tokens[0])) )
	{
	/* give copper coin(s)
	   give 10 copper coin(s)
	*/
		if ((!strcasecmp("coin",tokens[aantal-3])) ||
			(!strcasecmp("coins", tokens[aantal-3])) )
		{
			if ((!strcasecmp("copper",tokens[aantal-4])) ||
				(!strcasecmp("silver",tokens[aantal-4])) ||
				(!strcasecmp("gold", tokens[aantal-4])) )
			{
				GiveMoney_Command(name, password, room);
			}
		}
	}
	if ((aantal >= 4) && (!strcasecmp("give", tokens[0]))) 
	{
		Give_Command(name, password, room);
	}
	if ((aantal >= 2) && (!strcasecmp("read", tokens[0]))) 
	{
		Read_Command(name, password, room);
	}
	/* multiple person emotions */
	/* caress bill */
	if ((aantal == 2) && ((temp = get_pluralis2(tokens[0])) != NULL)) {
		char            temp1[50], temp2[50];
		sprintf(temp1, "%s %s you.<BR>\r\n", name, temp);
		sprintf(temp2, "%s %s %s.<BR>\r\n", name, temp, tokens[1]);
		if (!WriteMessageTo(tokens[1], name, room, "%s %s %s.<BR>\r\n",
					    name, temp, tokens[1])) {
			WriteSentenceIntoOwnLogFile(logname, "That user doesn't exist.<BR>\r\n");
		} else {
			WriteSayTo(tokens[1], name, room,
				"%s %s you.<BR>\r\n", name, temp);
			WriteSentenceIntoOwnLogFile(logname, "You %s %s.<BR>\r\n", tokens[0], tokens[1]);
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}			/* end of multiple persons emotions */
	/* caress bill absentmindedly */
	if ((aantal == 3) && ((temp = get_pluralis2(tokens[0])) != NULL) &&
		(exist_adverb(tokens[2])) ) {
		char            temp1[50], temp2[50];
		sprintf(temp1, "%s %s you, %s.<BR>\r\n", name, temp, tokens[2]);
		sprintf(temp2, "%s %s %s, %s.<BR>\r\n", name, temp, tokens[1], tokens[2]);
		if (!WriteMessageTo(tokens[1], name, room, "%s %s %s, %s.<BR>\r\n",
					    name, temp, tokens[1], tokens[2])) {
			WriteSentenceIntoOwnLogFile(logname, "That user doesn't exist.<BR>\r\n");
		} else {
			WriteSayTo(tokens[1], name, room,
				"%s %s you, %s.<BR>\r\n", name, temp, tokens[2]);
			WriteSentenceIntoOwnLogFile(logname, "You %s %s, %s.<BR>\r\n", tokens[0], tokens[1], tokens[2]);
		}
		WriteRoom(name, password, room, 0);
		KillGame();
	}			/* end of multiple persons emotions */
	/* Guilds */
	if (!strcasecmp("rangers", guildstatus))
	{
		if (!strcasecmp("nature list", command))
		{
			RangerList(name, password, room);
			KillGame();
		}
		if ( (!strcasecmp("nature call", command)) && (room==43) )
		{
			RangerEntryIn(name, password, room);
			KillGame();
		}
		if ( (!strcasecmp("nature call", command)) && (room==216) )
		{
			RangerEntryOut(name, password, room);
			KillGame();
		}
		if ( (!strcasecmp("nature", tokens[0])) && (!strcasecmp("talk", tokens[1])) )
		{
			RangerTalk(name, password, room);
			KillGame();
		}
	}
	if (!strcasecmp("mif", guildstatus))
	{
		if (!strcasecmp("magic list", command))
		{
			MIFList(name, password, room);
			KillGame();
		}
		if ( (!strcasecmp("magic wave", command)) && (room==142) )
		{
			MIFEntryIn(name, password, room);
			KillGame();
		}
		if ( (!strcasecmp("magic wave", command)) && (room==143) )
		{
			MIFEntryOut(name, password, room);
			KillGame();
		}
		if ( (!strcasecmp("magic", tokens[0])) && (!strcasecmp("talk", tokens[1])) )
		{
			MIFTalk(name, password, room);
			KillGame();
		}

	}

/* add SWTalk */		
	if (!strcasecmp("SW", guildstatus))
	{
		if ( (!strcasecmp("pow", tokens[0])) && (!strcasecmp("wow", tokens[1])) )
		{
			SWTalk(name, password, room);
			KillGame();
		}

	}		

/* add BKTalk */		
	if (!strcasecmp("BKIC", guildstatus))
	{
		if ( (!strcasecmp("chaos", tokens[0])) && (!strcasecmp("murmur", tokens[1])) )
		{
			BKTalk(name, password, room);
			KillGame();
		}

	}

/* add VampTalk */		
	if (!strcasecmp("Kindred", guildstatus))
	{
		if ( (!strcasecmp("misty", tokens[0])) && (!strcasecmp("whisper", tokens[1])) )
		{
			VampTalk(name, password, room);
			KillGame();
		}

	}

/* add KnightTalk */		
	if (!strcasecmp("Knights", guildstatus))
	{
		if ( (!strcasecmp("knight", tokens[0])) && (!strcasecmp("talk", tokens[1])) )
		{
			KnightTalk(name, password, room);
			KillGame();
		}

	}

/* add CoDTalk */		
	if (!strcasecmp("CoD", guildstatus))
	{
		if ( (!strcasecmp("mogob", tokens[0])) && (!strcasecmp("burz", tokens[1])) )
		{
			CoDTalk(name, password, room);
			KillGame();
		}

	}

	/* End Guilds */

	if (!strcasecmp("mail", command))
	{
		MailFormDumpOnScreen(name, password);
	}
	if (!strcasecmp("sendmail", tokens[0])) {
		char mailto[100], *mailbody, mailheader[100];
		
		mailbody = (char *) malloc(cgiContentLength);
		cgiFormString("mailto", mailto, 99);
		cgiFormString("mailheader", mailheader, 99);
		cgiFormString("mailbody", mailbody, cgiContentLength - 2);
		
		sprintf(sqlstring, "select name from usertable where "
			"name='%s' and god<2", mailto);
		res=SendSQL2(sqlstring, NULL);
		
		if (res!=NULL)
		{
			row = mysql_fetch_row(res);
		
			if (row!=NULL) 
			{
				WriteMail(name, row[0], mailheader, mailbody);
				WriteSentenceIntoOwnLogFile(logname, "Mail sent.<BR>\r\n");
			}  /* endif real user */ 
			else 
			{
				WriteSentenceIntoOwnLogFile(logname, "Mail not sent! User not found.<BR>\r\n");
			}
		}
		else
		{
			WriteSentenceIntoOwnLogFile(logname, "Mail not sent! User not found.<BR>\r\n");
		}
		mysql_free_result(res);
		
		WriteRoom(name, password, room, 0);
		KillGame();
	}			/* endofmailmessage */
	if ((!strcasecmp("readmail", tokens[0])) && (aantal == 2)) {
		ReadMail(name, password, room, atoi(tokens[1]), 0);
		KillGame();
	}			/* endofreadmailmessage */
	if ((!strcasecmp("deletemail", tokens[0])) && (aantal == 2)) {
		ReadMail(name, password, room, atoi(tokens[1]), 2);
		KillGame();
	}			/* endofreadmailmessage */
	if (!strcasecmp("listmail", tokens[0])) {
		ListMail(name, password, logname);
		KillGame();
	}			/* endoflistmail */
	WriteSentenceIntoOwnLogFile(logname, "I am afraid, I do not understand that.<BR>\r\n");
	WriteRoom(name, password, room, 0);
}

int 
cgiMain()
{
	char *command;
	char name[22];
	char password[40];
	char frames[10];

	if (commandlineinterface) {
		command = (char *) malloc(1000);
	} else {
		command = (char *) malloc(cgiContentLength);
	}

	if (commandlineinterface) {
		printf("Command:");
		gets(command);
		printf("Name:");
		gets(name);
		printf("Password:");
		gets(password);
		setFrames(0);
	}
	else 
	{
		char cookiepassword[40];
		cgiFormString("command", command, cgiContentLength - 2);
		if (command[0]==0) {strcpy(command,"l");}
		cgiFormString("name", name, 20);
		cgiFormString("password", password, 40);
		getCookie("Karchan", cookiepassword);
		if (strcmp(cookiepassword, password))
		{
			cgiHeaderContentType("text/html");
			NotActive(name, password,3);
		}
		if (cgiFormString("frames", frames, 10)!=cgiFormSuccess)
		{
			strcpy(frames, "none");
			setFrames(0);
		}
		if (!strcmp(frames,"1")) {setFrames(0);}
		if (!strcmp(frames,"2")) {setFrames(1);}
		if (!strcmp(frames,"3")) {setFrames(2);}
		WriteSentenceIntoOwnLogFile(BigFile,
				     "%s (%s): |%s|\n", name, password, command);

	}
	if (strcasecmp("quit", command))
	{
		cgiHeaderContentType("text/html");
	}
	else
	{
		fprintf(cgiOut, "Content-type: text/html\r\n");
		fprintf(cgiOut, "Set-cookie: Karchan=; expires= Monday, 01-January-01 00:05:00 GMT\r\n\r\n");
	}
	gameMain(command, name, password);
	free(command);
}
