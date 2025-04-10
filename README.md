# Blackjack64
A spigot plugin that adds a user-friendly blackjack GUI game.

Created by tbm00 for play.mc64.wtf.


## Dependencies
- **Java 17+**: REQUIRED
- **Spigot 1.18.1+**: UNTESTED ON OLDER VERSIONS
- **Vault**: REQUIRED


## Commands
#### Player Commands 
- `/blackjack` Open blackjack GUI
- `/blackjack <#>` Open blackjack GUI with specific bet
- `/blackjack <player>` Get player's blackjack stats
- `/z64blackjack` Quickly open blackjack GUI

#### Admin Commands
- `/blackjackadmin` Get server's blackjack stats


## Permissions
#### Player Permissions
- `blackjack64.play` Ability to use player commands *(default: everyone)*

#### Admin Permissions
- `blackjack64.admin` Ability to use admin commands *(default: op)*


## Config
```
# Blackjack64 v2.0.1-beta by @tbm00
# https://github.com/tbm00/Blackjack64

enabled: true

bet-min: 0.0
bet-max: 0.0

double-down-enabled: true
surrender-enabled: true
surrender-percentage-to-take: 50.0
blackjack-multiplier-enabled: true
blackjack-multiplier: 1.5

# Set to true if you wish start a new session for each /blackjack command
new-game-override-enabled: false

# Set to 0 to disable
bet-modifiers:
   increaseValue1: 10.0
   increaseValue2: 100.0
   increaseValue3: 1000.0
   increaseValue4: 10000.0
   increaseValue5: 100000.0
   decreaseValue1: 10.0
   decreaseValue2: 100.0
   decreaseValue3: 1000.0
   decreaseValue4: 10000.0
   decreaseValue5: 100000.0

# Gui Labels
hand-menu-title: "&lBlackjack: &r&8$$bet$ Bet"
hit-item: "&eHit"
stand-item: "&eStand"
surrender-item: "&eSurrender"
double-down-item: "&eDouble Down"
dealers-hand-game-item: "&eDealer's Hand"
your-hand-game-item: "&eYour Hand"
prefix-card-item: "&e"
hidden-card-item: "???"
session-menu-title: "&lBlackjack: $hand$ &8Hand"
summary-item: "&eSession Summary"
keep-playing-item: "&aNext Hand"
stop-playing-item: "&cEnd Session"
enter-bet-item: "&eBetting &f$$number$"
dealers-hand-session-item: "&eDealer's Prior Hand"
your-hand-session-item: "&eYour Prior Hand"
prefix-increase-bet-item: "&a+ $"
prefix-decrease-bet-item: "&c- $"

# Messages
no-permission: "&cYou do not have permissions to do this!"
number-or-player: "&eStart a blackjack session with &f/blackjack <bet amount>"
previous-game: "&7Opening previous blackjack session..."
quitter-mid-game: "&eYou left in the middle of a blackjack hand so you forfeitted &c$$amount$&e!"
session-ending: "&eAfter $amount$ hands, you resulted in $outcome$&e!"
blackjack-reward: "&aYou won &2$$amount$ &afrom that blackjack!"
cannot-bet-that-much: "&cYou cannot bet more money than you have!"
can-only-bet: "&cYou can only bet $amount$ over your total balance!"
bet-max-message: "&cThe maximum you can bet is $amount$!"
bet-min-message: "&cThe minimum you can bet is $amount$!"
name-stat: "&cName: &7$name$"
wins-stat: "&cWins: &7$amount$"
losses-stat: "&cLosses: &7$amount$"
ratio-stat: "&cW/L Ratio: &7$amount$"
ties-stat: "&cTies: &7$amount$"
winnings-stat: "&cWinnings: &7$amount$"

# Names of Cards
spades: "Spades"
hearts: "Hearts"
clubs: "Clubs"
diamonds: "Diamonds"
jack: "Jack"
queen: "Queen"
king:  "King"
ace: "Ace"
```