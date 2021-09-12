# wPaintball
A Paintball plugin, including Kill Coins and powerups

### **Introduction**
Paintball is a minigame where you have two teams, team red and team blue. 
Every team has pre-set lives (configurable). The goal is to kill as many enemies as possible
until all of their lives are gone. This can be achieved by throwing paintballs 
at players from the opposite team. wPaintball also provides powerups, examples of
powerups are triple shot, quintuple shot, extra lives, nuke, and more! 
These can be purchased with kill coins. Kill Coins can be obtained by killing enemies.
Per arena configuration is provided, this way you can customize everything.

### **How to set it up**
Setting it up is very straightforward. 
The first thing you have to do is create a world spawn. 
Players who leave a game will be teleported to that location or if the game ends. 
You can do that by using the command [/pb setworldspawn].

**Spawn Points**
You will need to set 3 different spawn points for the game itself,
one for the waiting room, one for team red, 
and one for team blue. You can set the waiting room location by typing the command [/pb setlobby arenaName].
For the team spawns, there are two separate commands. To set team red’s spawn, 
you type [/pb setredspawn arenaName], and for team blue’s spawn, you type [/pb setbluespawn arenaName].

### **Powerups and Kill Coins**
Kill Coins are the type of currency that can be used to purchase powerups.
You can obtain kill coins by killing players. 
Some examples of powerups are, nuke, strongarm, super strongarm, triple shot, quintuple shot, 
extra lives, and extra ammo. The prices and durations of these powerups are configurable.

### **Commands**

`/pb createarena <arenaName>` Creates a new arena\
`/pb deletearena <arenaName>` Deletes an existing arena\
`/pb join <arenaName>` Join an arena\
`/pb leave` Leaves an arena\
`/pb setlobby <arenaName>` Sets the lobby spawn point for an arena\
`/pb setredspawn <arenaName>` Sets a game spawn point for the red team\
`/pb setbluespawn <arenaName>` Sets a game spawn point for the blue team\
`/pb setworldspawn` Sets the world spawn, the spawn where players go to after the game ends, or they leave\
`/pb help` Displays the help message\
`/pb admin` Displays the admin message\
`/pb tp <arenaName>` Teleports you to an arena
