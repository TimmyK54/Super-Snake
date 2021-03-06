# Super Snake
This project is my first "real" programming application with a robust and functional GUI. It took about 4 and a half weeks to complete the first version, with approximately 15-20 hours of work per week. Essentially, it's a snake game that features [powerups](https://github.com/TusharK54/Super-Snake#powerups), [multiplayer](https://github.com/TusharK54/Super-Snake#multiplayer), and a built-in [AI](https://github.com/TusharK54/Super-Snake#ai). If you decide to go through the [source code](https://github.com/TusharK54/Super-Snake/tree/master/Super%20Snake/src), be aware that the entire program is written in one class filled with nested subclasses, so it's not very well structured. However, I think I made it readable enough so navigation should really be the only issue.

## Installation
Requirements: Java 1.8+

## Gameplay

#### Controls
- Use WASD or the arrow keys to move
- Press space to pause
- In a menu screen, press enter to select a menu item
- In a menu screen, press backspace to return to the main menu
- After entering a leaderboard name, press tab to exit text box before pressing enter to play again

#### Powerups
There are various powerups to be taken advantage of in Super Snake - 5 to be exact. Powerups can be obtained by eating special colored pellets. Each powerup lasts for 6 seconds and can be stacked with other powerups. Eating a special pellet when you currently have an active powerup will not replace your current powerup. However there is no visual indicator for when the previous powerup runs out!

Here is a list of each of the powerups and what they do:

Powerup | Color | Image | Description
:------:|:-----:|:-----:|:-----------
Freeze|Blue|![Freeze Pod](https://github.com/TimmyK54/Super-Snake/blob/master/Images/FreezePod.gif)|This powerup halves your snake's speed. It is useful for performing precise maneuvers or slowing yourself down if you stacked up one too many turbo powerups.
Turbo|Red|![Turbo Pod](https://github.com/TimmyK54/Super-Snake/blob/master/Images/TurboPod.gif)|This powerup doubles your snake's speed. It is useful in multiplayer to trap opponents or get to a pellet first.
Warp|Yellow|![Warp Pod](https://github.com/TimmyK54/Super-Snake/blob/master/Images/WarpPod.gif)|This powerup allows you to teleport through walls to the other side of the screen. It is useful for getting across quickly and beating your opponents to a pellet.
Ghost|Gray|![Ghost Pod](https://github.com/TimmyK54/Super-Snake/blob/master/Images/GhostPod.gif)|This powerup allows you to pass through other snakes - even yourself. It is very useful for escaping from traps and giving you a short break from obstacles in the game.
Growth|Green|![Growth Pod](https://github.com/TimmyK54/Super-Snake/blob/master/Images/GrowthPod.gif)|While a snake usually only grows by one each time it eats a pellet, a snake with the growth powerup will grow by one more for each active growth powerup.
Mystery|Magenta|![Mystery Pod](https://github.com/TimmyK54/Super-Snake/blob/master/Images/MysteryPod.gif)|The mystery powerup will grant you a random powerup from above. You won't know what powerup it contains until you eat it.

#### Multiplayer
In the multiplayer menu screen, you can select the number of players and CPUs with WASD or the arrow keys up to a max of 2 players, 20 AIs, and 20 total snakes. The game actually supports more players; in fact there is no real theoretical limit to the amount of snakes. However, it doesn't make much sense to have an option for 3 or more players because of a lack of sensible keyboard input from one computer. But, if this local multiplayer is ever extended to online multiplayer (very unlikely), the support for more players is already built-in.

## AI

The goal of the AI is to play a perfect game of snake by reacting to the current state of the game. Currently there is only one AI implemented in this game, and it is used both in the singleplayer AI and the CPU in the multiplayer. Since I couldn't find a suitable and visually pleasing AI algorithm for the specific goal of beating a game of snake, I made my own unique algorithm that I call the [short-sighted algorithm](https://github.com/TusharK54/Super-Snake#short-sighted-algorithm) for the simple reason that it doesn't use maze searching or pathfinding to compute the future state resulting from current moves. Instead, it reacts to obstacles immediately in its way. When faced with an obstacle, it considers the two cardinal directions it can move perpendicular to its current path, and chooses the path that will lead it to a wall without running into an obstacle (if it exists). This is because even if the wall is closer than an obstacle on the other side, there is an almost guaranteed chance it will not trap itself when it reaches the wall.

#### Short-Sighted Algorithm
Every time the AI has to make a move, it first computes three sets of values:
1. *Heuristic Directions* - the one or two cardinal directions that the snake has to go to get to the pellet, assuming there are no obstacles in the way.
1. *Wall Collision* - whether or not the first collision in each cardinal direction is a wall.
1. *Collision Distance* - the distance in each cardinal direction from the snake's head to the nearest snake.

To determine the next move, the algorithm follows these set of steps:
1. Using the *collision distance* set, determine if the snake is in a corner - that is, surrounded on three sides. If so go in the only direction that is not blocked. Otherwise, proceed to the next step.
1. Using the *collision distance* set, determine if the current direction would cause the snake to run into an obstacle. If so, or if the pellet spawned behind the direction the snake is moving, follow the steps below. Otherwise, proceed to next step.
     1. Using the *wall collision* set, determine if the snake can turn in a direction that will cause it to eventually run into a wall before it runs into a snake. If so, go in that direction. If both directions will cause it to eventually run into a wall, go in the direction specified by the *heuristic directions* set. Otherwise, proceed to the next step.
     1. Using the *collision distance* set, go in the direction with a higher collision distance.
1. At this point, the algorithm should have determined that it is not in a corner and it is not going to run into an obstacle. Therefore, it should simply follow the *heuristic directions* originally determined. As to which direction it should choose, the algorithm will chose the direction it was previously traveling in if it can since that minimizes disorder.

#### Statistics
To test the performance of the AI, I ran 10000 simulations of the algorithm and compiled the results. The Short-Sighted Algorithm on average attains a score of 84.9 with a standard deviation of 30.7 - a measly amount considering the minimum theoretical score to beat the game is 1656 (36 rows x 46 columns). This means on average the AI only completes about 5% of the game.

These are the percentiles of the simulation:

Percentile|Size
:---------|:---:
0%|14
1%|27
5%|39
10%|47
25%|62
50%|82
75%|105
90%|127
95%|140
99%|162
100%|197

While the algorithm is obviously flawed, it is not too bad given its rudimentary procedure. It performs slightly worse than the average player, but it doesn't have the knowledge of the entire game state like the user does. It doesn't even realize it's trapped itself until it's too late. Considering that it is a purely reactionary algorithm, it actually performs surprisingly well within its constraints - occasionally even better than me.
