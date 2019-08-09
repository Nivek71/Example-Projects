# Example-Projects
This repository is aimed at allowing dependents to create extensive MiniGames quickly and easily. It includes three seperate plugins, which are all listed here in the same repository because it is easier to manage them together using Intellij's modules.

The three plugins, and their main features are listed as follows:

# UtilityAPI
The UtilityAPI plugin contains miscellaneous utility classes, which are not unique to MiniGames. These classes were seperated to another project so they may be reused for non-minigame projects. The main features of the UtilityAPI include:
 - the function package, which includes several FunctionalInterfaces; (most of) these interfaces extend a Java JDK FunctionalInterface, but provide a new method -- %name%Ex, which has the same signature as their super interface, except they allow implementors to throw an Exception. The functions also provide a default implementation fro the super interface function, which calls the newly defined method, catches any exception and wraps it in a WrappedException.
 - the input package, which contains methods for player input -- currently Inventory GUIs.
 - rules, which dicate what groups can and cannot do (such as placing blocks, or taking damage). Objects can be added to groups, and rule enforcers are defined for these groups. Rule groups implement the RuleBound class; rules extend the Rule class.
 - timer, which features some scheduling, using bukkit tasks. The timers feature some common functionality of bukkit tasks, such as Countdowns, but provide easier ways of managing timers (notably the ability to register timers with specific objects, then later cancel all timers associated with that object).
 - Helper class, which is a Utility class for methods that don't belong anywhere else
 - Logger class, which provides simple Exception logging, which can be used cleanly without clutter
 And a few other various utility classes/packages
 
 # MiniGameAPI
 The MiniGameAPI is used to easily define new MiniGames. The MiniGameAPI has some events which may be listened to, but the majority of the interaction between the MiniGame and the MiniGameAPI is done with callbacks; callbacks are performed during various state changes.
 The structure of the MiniGameAPI is as follows:
 - The Lobby class, which holds all players interested in the MiniGame, such as active players, spectators, or players waiting for the the minigame to start. The Lobby does very little work on its own, instead delegating tasks to the active LobbyState.
 - The LobbyState class, which is responsible for all that occurs in the Lobby, from switching the active MiniGame, to starting or stopping the active MiniGame, to any other task the Lobby needs to execute.
 - The MiniGameMap, which handles the area of land the Map resides over, and handles tasks such as rebuilding this area.
 - The MapConfiguration, which handles MiniGame-specific attributes of a Map, such as CTF flag locations, or spawn locations for players (which may vary depending on the MiniGame type).
 - The MiniGamePlayer, which stores information about a player in a Lobby, such as respawning timers and damage tracking.
 - The MiniGame class, which keeps track of the MiniGame itself, and any state that a MiniGame may need to track
 - The MiniGameInfo class, which stores information about a type of MiniGame, such as the minimum amount of players needed to start or the kits available in a certain minigame.
 
 Rule bounds:
 The MiniGameAPI defines the default rule policy for players: players are bound to their MiniGamePlayer instance, or given EMPTY_BOUNDS if not in a Lobby.
 The MiniGamePlayer has parent bounds (keep in mind that children bounds override parent bounds) and the order is as follows (by default):
 For active players:
 MiniGamePlayer > MiniGameTeam (if not team game, skip this) > MapConfiguration > MiniGameMap > MiniGame > LobbyState > Lobby
 For spectators:
 MiniGamePlayer > MiniGame's spectator bounds > LobbyState > Lobby
 For non MiniGame LobbyStates (such as Waiting):
 MiniGamePlayer > LobbyState > Lobby
 
 So, if you want players to be able to change their kit, modify the LobbyState -- this will allow both players and spectators to change their kit, which will apply on next respawn, unless they have the PLAYER_APPLY_KIT rule.
 
 # MiniGame-CTF
 The MiniGame-CTF project is a sample MiniGame created using the UtilityAPI and the MiniGameAPI.
 - The CTF_Plugin defines the KitManager (which holds all of the kits for this MiniGame), the MiniGameInfo (which stores MetaData about the MiniGame type), a MiniGameMap instance (which is not specific to this MiniGame, but an instance of the MiniGame map must be defined to play any MiniGame) and a CTF_MapConfiguration instance (which is a type of MapConfiguration, specific to CTF), which has been added to the defined MiniGameMap.
- CaptureTheFlag class: simply defines some rules for the MiniGame
- MapConfiguration class: keeps track of the amount of points needed to win
- CTF_Team class: tracks flag captures, amounts of points per team, and other behavior.
