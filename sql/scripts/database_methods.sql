#!/bin/sh

cd /karchan/mud/sql   

. ./mysql_constants

${MYSQL_BIN} -h ${MYSQL_HOST} -u ${MYSQL_USR} --password=${MYSQL_PWD} -s ${MYSQL_DB} <<END_OF_DATA

update methods
set src = "say(\"You yawn.<BR>\")
sayeveryone(\"%me yawns.<BR>\")
showstandard
return
" where id = 2;

update methods
set src = "if sql(\"select 1 from tmp_usertable where name='%me' and room in (1,3,164)\")
	say(\"Fighting is not allowed in this area.<BR>\")
	showstandard
return
" where id = 4;

update methods
set src = "if sql(""select 1 from rooms where id=20 and south=0"")
	say(""You succeed in opening the door of the cupboard. \\\\
		 [<A HREF=""http://www.karchan.org/images/mpeg/her.mpg"">\\\\
		MPEG</A>]<BR>"")
	sayeveryone(""%me  opens the door of the cupboard.<BR>"")
	sql(""update items set description='<H1><IMG SRC=""http://www.karchan.org/images/gif/herberg5.gif"">The Cupboard</H1><HR>\\\\
		You look at the cupboard. It is very old and wormeaten. With one\\\\
		knock you could probably knock it down, but I doubt if the barman would appreciate \\\\
		this much. It is open. Both doors of the cupboard are ajar. In it you can \\\\
		see, amazingly, a staircase leading to the north and up into a hidden\\\\
		room.<P>', adject1='comparatively',\\\\
		adject2='big', name='cupboard' where id=-32"")
	sql(""update rooms set contents='<IMG SRC=""http://www.karchan.org/images/gif/herberg4.gif"" ALIGN=CENTER> \\\\
		<H1>The Taverne &quot;The Twisted Dwarf&quot;</H1>\\\\
		<IMG SRC=""http://www.karchan.org/images/gif/letters/y.gif"" ALIGN=left>\\\\
		ou are now in the Inn &quot;The Twisted Dwarf&quot; . It is \\\\
		dark, as always in these places. The windows are of a dark blue color, which \\\\
		doesn\\\\'t allow any light to enter the room. A lot of woodwork, wooden tables, \\\\
		chairs and a rather large bar on the right of you is situated almost against the \\\\
		the back of the room a comparatively big cupboard is visible. The cupboard \\\\
		appears to be open. Behind the bar a norse small ugly dwarf is cleaning some \\\\
		glasses at the bar. On the same bar you see a sign on a piece of wood, \\\\
		apparently this is the menu for the day.<BR> Scattered among the tables are \\\\
		groups of people, playing what seems to be a dwarfish version of Poker.You\\\\
		see a sign on the wall behind the counter.<P>', north=20 where id=9"")
	sql(""update rooms set south=9 where id=20"")
	showstandard
#	show(""select contents from action where id=7"")
else
	say(""The cupboard is already open.<BR>"")
	showstandard
end
return
" where id=1;

update methods
set src = "if sql(""select 1 from rooms where id=20 and south=9"")
	say(""You succeed in closing the door of the cupboard. \\\\
		 [<A HREF=""http://www.karchan.org/images/mpeg/her2.mpg"">\\\\
		MPEG</A>]<BR>"")
	sayeveryone(""%me  closes the door of the cupboard.<BR>"")
	sql(""update items set description='<H1><IMG SRC=""http://www.karchan.org/images/gif/herberg3.gif"">The Cupboard</H1><HR>\\\\
		You look at the cupboard. It is very old and wormeaten. With one\\\\
		knock you could probably knock it down, but I doubt if the barman would appreciate \\\\
		this much. It hasn\\\\'t got a lock so you could open it, I doubt if the barman \\\\
		would mind.<P>', adject1='comparatively',\\\\
		adject2='big', name='cupboard' where id=-32"")
	sql(""update rooms set contents='<IMG SRC=""http://www.karchan.org/images/gif/herberg1.gif"" ALIGN=CENTER> \\\\
		<H1>The Taverne &quot;The Twisted Dwarf&quot;</H1>\\\\
		<IMG SRC=""http://www.karchan.org/images/gif/letters/y.gif"" ALIGN=left>\\\\
		ou are now in the Inn &quot;The Twisted Dwarf&quot; . It is \\\\
		dark, as always in these places. The windows are of a dark blue color, which \\\\
		doesn\\\\'t allow any light to enter the room. A lot of woodwork, wooden tables, \\\\
		chairs and a rather large bar on the right of you is situated almost against the \\\\
		the back of the room a comparatively big cupboard is visible. \\\\
		Behind the bar a norse small ugly dwarf is cleaning some \\\\
		glasses at the bar. On the same bar you see a sign on a piece of wood, \\\\
		apparently this is the menu for the day.<BR> Scattered among the tables are \\\\
		groups of people, playing what seems to be a dwarfish version of Poker.You\\\\
		see a sign on the wall behind the counter.<P>', north=0 where id=9"")
	sql(""update rooms set south=0 where id=20"")
	showstandard
#	show(""select contents from action where id=7"")
else
	say(""The cupboard is already closed.<BR>"")
	showstandard
end
return
" where id=3;

update methods
set src = "sayeveryone(""%me says [to Kainian] :  Please heal me, good priest.<BR>"")
if sql(""select 1 from tmp_usertable where name='%me' and \\\\
	copper+silver*10+gold*100>=10000"")
	sayeveryone(""Kainian prays to the Almighty Karn for his divine influence \\\\
	in the restoration of the health of %me. Suddenly %me is struck with an \\\\
	unearthly light!<BR>"")
	say(""Kainian prays to the Almighty Karn for his divine influence in the \\\\
	restoration of your health. Suddenly you feel yourself full of new \\\\
	energy!<BR>"")
	sql(""update tmp_usertable set gold=gold-100 where name='%me'"")
	sql(""update tmp_usertable set silver=silver+gold*10, gold=0 where \\\\
	name='%me' and gold<0"")
	sql(""update tmp_usertable set copper=copper+silver*10, silver=0 where \\\\
	 name='%me' and silver<0"")
	sql(""update tmp_usertable set vitals=vitals-125 where name='%me'"")
	sql(""update tmp_usertable set vitals=0 where name='%me' and vitals<0"")
else
	say(""Kainian says [to you] : You do not have enough money.<BR>"")
	sayeveryone(""Kainian says [to %me] : You do not have enough money.<BR>"")
end
showstandard
" where id = 5;

update methods
set src = "sayeveryone(""%me says [to Kainian] :  Please heal me, good priest.<BR>"")
if sql(""select 1 from tmp_usertable where name='%me' and \\\\
	copper+silver*10+gold*100>=2500"")
	sayeveryone(""Kainian prays to the Almighty Karn for his divine influence \\\\
	in the restoration of the health of %me. Suddenly %me is struck with an \\\\
	unearthly light!<BR>"")
	say(""Kainian prays to the Almighty Karn for his divine influence in the \\\\
	restoration of your health. Suddenly you feel yourself full of new \\\\
	energy!<BR>"")
	sql(""update tmp_usertable set gold=gold-25 where name='%me'"")
	sql(""update tmp_usertable set silver=silver+gold*10, gold=0 where \\\\
	name='%me' and gold<0"")
	sql(""update tmp_usertable set copper=copper+silver*10, silver=0 where \\\\
	 name='%me' and silver<0"")
	sql(""update tmp_usertable set vitals=vitals-40 where name='%me'"")
	sql(""update tmp_usertable set vitals=0 where name='%me' and vitals<0"")
else
	say(""Kainian says [to you] : You do not have enough money.<BR>"")
	sayeveryone(""Kainian says [to %me] : You do not have enough money.<BR>"")
end
showstandard
" where id = 6;

update methods
set src = "if sql(""select 1 from items where id=-3 and adject3!='loose'"")
	sql(""update items set adject3='loose' where id=-3"")
	say(""You let go of the chain, wondering what that was all about.<BR>"")
	sayeveryone(""%me pulls at the chain in the wall. You hear the ominous \\\\
	sounds of wheels churning and rope snapping, yet the sound suddenly stops \\\\
	again.<BR>"")
	show(""select contents from action where id=1"")
else
	say(""You pull vehemently, but nothing exciting happens.<BR>"")
	sayeveryone(""%me pulls vehemently on a chain in the wall.<BR>"")
	showstandard
end
return
" where id = 7;

update methods
set src = "if sql(""select 1 from items where id=-3 and adject3!='loose'"")
	say(""You appear from nowhere.<BR>"")
	sayeveryone(""%me appears from nowhere.<BR>"")
	show(""select contents from action where id=2"")
else
	say(""You crawl out of the lake, dripping wet.<BR>"")
	sayeveryone(""%me jumps into the lake.<BR>"")
	# This part needs to be fixed, attention
	sql(""update tmp_usertable set room=17 where name='%me'"")
	sayeveryone(""%me crawls out of the lake.<BR>"")
	show(""select contents from action where id=3"")
end
return
" where id = 8;

update methods
set src = "# Karcas travels from shop to square
if sql(""select 1 from tmp_usertable where room = 16 and name='Karcas' and hour(now()) % 2 = 0"")
	sayeveryone(""Karcas leaves west.<BR>"")
	sql(""update tmp_usertable set room=5 where name='Karcas'"")
	set room=5
	sayeveryone(""Karcas appears from nowhere.<BR>"")
end
return
" where id = 9;

update methods
set src = "# Karcas travels from square to road
if sql(""select 1 from tmp_usertable where room = 5 and name='Karcas' and hour(now()) % 2 = 0"")
	sayeveryone(""Karcas leaves south.<BR>"")
	sql(""update tmp_usertable set room=3 where name='Karcas'"")
	set room=3
	sayeveryone(""Karcas appears from nowhere.<BR>"")
end
return
" where id = 10;

update methods
set src = "# Karcas travels from road to mountains
if sql(""select 1 from tmp_usertable where room = 3 and name='Karcas' and hour(now()) % 2 = 0"")
	sayeveryone(""Karcas leaves south.<BR>"")
	sql(""update tmp_usertable set room=4 where name='Karcas'"")
	set room=4
	sayeveryone(""Karcas appears from nowhere.<BR>"")
end
return
" where id = 11;

update methods
set src = "# Karcas travels from mountains back to road
if sql(""select 1 from tmp_usertable where room = 4 and name='Karcas' and hour(now()) % 2 = 0"")
	sayeveryone(""Karcas leaves north.<BR>"")
	sql(""update tmp_usertable set room=3 where name='Karcas'"")
	set room=3
	sayeveryone(""Karcas appears from nowhere.<BR>"")
end
return
" where id = 12;

update methods
set src = "# Karcas travels from road back to square
if sql(""select 1 from tmp_usertable where room = 3 and name='Karcas' and hour(now()) % 2 = 0"")
	sayeveryone(""Karcas leaves north.<BR>"")
	sql(""update tmp_usertable set room=5 where name='Karcas'"")
	set room=5
	sayeveryone(""Karcas appears from nowhere.<BR>"")
end
return
" where id = 13;

update methods
set src = "# Karcas travels from square back to shop
if sql(""select 1 from tmp_usertable where room = 5 and name='Karcas' and hour(now()) % 2 = 0"")
	sayeveryone(""Karcas leaves west.<BR>"")
	sql(""update tmp_usertable set room=16 where name='Karcas'"")
	set room=16
	sayeveryone(""Karcas appears from nowhere.<BR>"")
end
return
" where id = 14;

update methods set src="sql(\"update tmp_usertable set vitals = vitals - jumpvital - round(3*eatstats/30) where vitals>0\")
sql(\"update tmp_usertable set manastats = manastats - jumpmana - round(eatstats/30) where manastats>0\")
sql(\"update tmp_usertable set movementstats = movementstats - jumpmove - round(3*eatstats/30) where movementstats>0\")

sql(\"update tmp_usertable set vitals = vitals - round(3*drinkstats/30) where vitals>0 and drinkstats>0\")
sql(\"update tmp_usertable set manastats = manastats - round(drinkstats/30) where manastats>0 and drinkstats>0\")
sql(\"update tmp_usertable set movementstats = movementstats -round(3*drinkstats/30) where movementstats>0 and drinkstats>0\")

sql(\"update tmp_usertable set vitals = 0 where vitals<0\")
sql(\"update tmp_usertable set manastats = 0 where manastats<0\")
sql(\"update tmp_usertable set movementstats = 0 where movementstats<0\")

sql(\"update tmp_usertable set drinkstats = drinkstats - 1 where drinkstats>0\")
sql(\"update tmp_usertable set drinkstats = drinkstats + 1 where drinkstats<0\")
sql(\"update tmp_usertable set eatstats = eatstats - 1 where eatstats>0\")

# Corpses, decaying

sql(\"delete from tmp_itemtable where id=55\")
sql(\"delete from bogus_itemtable\")
sql(\"insert into bogus_itemtable \\
select 55, \'\', \'\', count(amount), room, \'\', \'\' \\
from tmp_itemtable where id>=40 and id<=54 group by room\")
sql(\"insert into tmp_itemtable select * from bogus_itemtable\")
sql(\"delete from tmp_itemtable where id>=40 and id<=54\")
return
" where id=50;

END_OF_DATA