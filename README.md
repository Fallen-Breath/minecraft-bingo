# Minecraft Bingo
Item bingo in Minecraft

![Item Bingo](https://i.imgur.com/7qXBAQK.png)

## Changes in fallen's fork

### Mechanism Tweaks

- Add config `auto-save-disabled` for re-enabling auto save, to fix infinity memory consuming since loaded chunks cannot be saved
- Add config `spawn-locations-chunk-loading-radius` to preload more chunks at team's spawn chunk
- Add config `allow-mid-game-join` to allow player join in mid-game and join the spectator team
- Add config `allow-spawn-biome-without-tree` to prevent spawning in biome without trees

### Quidditch Mode

Quidditch mode is a special 'lines' mode, where any team completing given amount of lines and triggers the bingo ends the game, 
but the winner is the team with the most score.

By default, a team's score equals to the amount of items it collects, 
but the team that triggers the bingo can get a Golden Snitch score bonus (can be modified in config `quidditch-golden-snitch-bonus`)

- Add a "line" mode variant: `quidditch` mode 
- Add config `default-win-condition-is-quidditch`, to set the default mode from `lines` to `quidditch`
- Add config `quidditch-golden-snitch-bonus` to adjust the extra bonus for the team who gets bingo (default `1`)

### QOL++

- Show num & row collected of all teams in tab list, so you don't have to count the dots in the bingo card
- Improved the `<color> team has obtained <material>` message: make the material name translatable
- Give bingo cards of all teams to the spectator player
- Allow spectator team to see the bingo card inventory by clicking blocks with bingo card
- Better bingo item display in bingo card
  - Show collector team names in item hover texts
  - Show collector team amount with item count (`count = 10 + n`, iff. `n > 0`)
- Reset player time since reset stat on game start
- Add in-game notification. When a player right-click or left-click the items displayed in the bingo card inventory, a message when be sent to notify the team.

### Fixes

- Fix double timer running by entering double `/start` and double game starting
- Fix existing spectator player still being in survival mode when join in mid-game

## What is minecraft bingo?
Minecraft Bingo is a gamemode in Minecraft in which teams battle in order to collect items on their bingo card.
The first team to reach a completed row, column or diagonal wins the game.
Collecting items is simply done through the vanilla game experience of minecraft.

## Usage
Players that have OP on the server have access to all commands. 
Before being able to start the game, a player needs to create teams with the /team command. 
Other settings can be configured as explained in the commands section below. 
As soon as the game is started, teams are scattered across the map (players within teams are grouped together). 
The teams work together to gather the items on their bingo card. 
If a team successfully completed the card, the game ends. 
When, however, the time limit is reached, a winner is decided based on the number of items collected by each of the teams. 
The team with the highest number of items wins, or the game ends in a tie.
Note that the bingo card can be right-clicked in order to view which items need to be gathered.

## Install
Download the [latest release](https://github.com/Extremelyd1/minecraft-bingo/releases/latest) or compile it yourself using Gradle.
The plugin requires either a [Spigot](https://www.spigotmc.org/) or [Paper](https://papermc.io/) server to run.
Place the `MinecraftBingo-[version].jar` file in the plugins directory of your server.
Unzip the `item_data.zip` into the `<server>/plugins/MinecraftBingo/` directory.
If done successfully, you should have the following two directories:
- `<server>/plugins/MinecraftBingo/item_data/`
- `<server>/plugins/MinecraftBingo/item_data/images/`

The first time you run the plugin a config file will be generated in `<server>/plugins/MinecraftBingo`, in which you can edit some configuration settings.

## Commands
#### Team manage command
- `/team [random|add|remove]`
  - `/team random <number of teams> [-e] [players...]` Create a set number of teams dividing the players randomly over them. If given a list of player names, it will only create teams with those players (or exclude those player if the flag `-e` is given).
  - `/team add <player name> <team name>` Add a player to a given team  
  Possible team names are: Red, Blue, Green, Yellow, Pink, Aqua, Orange, Gray
  - `/team remove <player name>` Remove a player from a given team

#### Game start/end commands
- `/start` Start the game
- `/end` End the game

#### Configuration commands  
- `/pvp` Enable/disable PvP
- `/maintenance` Enable maintenance mode (this will disallow all non-OP players from joining)
- `/wincondition <full|lines|quidditch|lockout> [number]` Change the wincondition to either a full card, a number of lines (rows, columns or diagonals) to complete in order to win or lockout. 
  In case of 'lines' or 'lockout' you can specify a number to indicate how many lines needed to be completed, or after how many collections an item locks.  
  For 'quidditch' mode, see the [Quidditch Mode](#quidditch-mode) section above.
  (alias: `/wincon`)
- `/itemdistribution <S> <A> <B> <C> <D>` Change the item distribution scales, the number of S, A, B, C, and D tier items that appear on the bingo card. 
  These numbers must add up to 25. (aliases: `/itemdist`, `/distribution`, `/dist`)
- `/timer <enable|disable|length>` Enable/disable the timer or set the length of the timer (the length can be specified in hours/minutes/seconds, such as `/timer 10m` or `/timer 1h20m30s`)

#### Miscellaneous commands
- `/bingo` Check the items on the card
- `/card` Receive a new bingo card (if somehow lost)
- `/reroll` Re-roll the items on the bingo card
- `/coordinates [message]` Sends your current coordinates to your team, optionally with a message (aliases: `/coord`, `/coords`)
- `/all [message]` Allows players to talk to all players in the game (aliases: `/a`, `/g`, `/global`)
- `/teamchat [message]` Allows players to talk to their team members (alias: `/tc`)
- `/channel <team|global>` Allows players to switch the default chat channel that chat messages will be sent to (alias `/c`)
- `/join [team name]` Allows players to join a team by the given name

## World generation
The plugin offers the ability to pre-generate worlds to reduce chunk generation lag during gameplay.
This feature can be used both on [Spigot](https://www.spigotmc.org/) and on [Paper](https://papermc.io/), but will be significantly faster on Paper.
The following command can be used to manage this:
- `/generate <start world number> <number of worlds>` Start pre-generating the given number of worlds from the given index and storing them in zip format
- `/generate stop` Stop pre-generating worlds  

This command only works if the config value `pregeneration-mode.enabled` is set to `True`.
Pre-generation also requires you to set a world border for the worlds you want to have generated.
The sizes for these borders can be set in the config file: `border.overworld-size` and `border.nether-size`.
All chunks within the world border, including a buffer of 2 chunks outside the world border will be generated in this process.
When finished, the directories for the overworld and nether will be zipped as `world[number].zip` and placed in `<server>/plugins/MinecrftBingo/worlds/` directory.
