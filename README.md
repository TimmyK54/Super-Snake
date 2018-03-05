# Super Snake
This project is my first "real" programming application with a robust and functional GUI. It took about 4 and a half weeks to complete the first version, with approximately 15-20 hours of work per week. Essentially, it's a snake game that features [powerups](https://github.com/TimmyK54/Super-Snake#powerups), [multiplayer](https://github.com/TimmyK54/Super-Snake#multiplayer), and a built-in [AI](https://github.com/TimmyK54/Super-Snake#ai). If you decide to go through the code, be aware that the entire program is written in one class filled with nested subclasses, so it's not very well structured. However, I think I made it readable enough so navigation should really be the only issue.

## Installation
Requirements: Java 1.8+

#### School Computer
The downloaded .jar file does not run on a school computer because it is from an "unidentified developer" - that's me! Follow these steps to run this, or any other .jar file on your school computer:
1. Make sure you have the .jar file downloaded
1. If you don't have Eclipse, install Eclipse Java from the Self Service app
1. Load up Eclipse and click out of the welcome tab if it's your first time on Eclipse
1. In the top bar, navigate to File >> New >> Java Project
1. Name the file whatever you want - this will be the directory for all the future .jar files you download
1. Navigate to the src folder in the file you just created and add the downloaded .jar to it
1. Done! Anytime you want to run the .jar file, run it from the src folder in Eclipse

## Gameplay

#### Controls
- Use WASD or the arrow keys to move.
- Press space to pause.
- In a menu screen, press enter to select a menu item.
- In a menu screen, press backspace to return to the main menu.
- After entering a leaderboard name, press tab to exit text box before pressing enter to play again.

#### Powerups
There are various powerups to be taken advantage of in Super Snake - 5 to be exact! Powerups can be obtained by eating special colored pellets. Each powerup lasts for 6 seconds, and can be stacked. Eating a special pellet when you currently have an active powerup will not replace your current powerup. However you will not have a visual indicator for when the previous powerup runs out!

Here is a list of the 5 powerups and what they do:

Powerup | Color | Image | Description
:------:|:-----:|:-----:|:-----------
Freeze|Blue|(not available)|This powerup halves your snake's speed. It is useful for performing precise maneuvers or slowing yourself down if you stacked up one too many turbo powerups.
Turbo|Red|(not available)|This powerup doubles your snake's speed. It is useful in multiplayer to trap opponents or get to a pellet first.
Warp|Yellow|(not available)|This powerup allows you to teleport through walls to the other side of the screen. It is useful for getting across quickly and beating your opponents to a pellet.
Ghost|Gray|(not available)|One of the more powerful powerups in the game, ghost allows you to pass through other snakes - even yourself. It is very useful for escaping from some sticky situations and giving you a break from any obstacles in the game.
Growth|Green|(not available)|While a snake usually only grows by one each time it eats a pellet, a snake with the growth powerup will grow by one more for each active growth powerup. This can be very powerful if used effectively.
Mystery|Magenta|(not available)|Wait, I thought you said there were 5 powerups? Well there are! The mystery powerup will grant you a random powerup from above.

#### Multiplayer
In my biased opinion, the multiplayer in this game is pretty well integrated for a local snake multiplayer. In the multiplayer menu screen, you can select the number of players and CPUs with WASD or the arrow keys up to a max of 2 players, 20 AIs, and 20 total snakes. The game actually supports more players; in fact there is no real theoretical limit to the amount of snakes. However, it doesn't make much sense to have an option for 3 or more players because of a lack of sensible keyboard input from one computer. But, if this local multiplayer is ever extended to online multiplayer (very unlikely), the support for more players is already built-in.

## AI
The goal of the AI is to play a perfect game of snake by reacting to the current state of the game. Currently there is only one AI implemented in this game, and it is used both in the singleplayer AI and the CPU in the multiplayer. Since I couldn't find a suitable and visually pleasing AI algorithm for the specific goal of beating a game of snake, I made my own unique algorithm that I call the [short-sighted algorithm](https://github.com/TimmyK54/Super-Snake#short-sighted-algorithm) for the simple reason that it doesn't use maze searching or pathfinding to compute the future state resulting from present moves. It performs surprisingly well at first, but begins to show its flaws when it gets to a length of about 70.

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

## Version History

1.0.0 (March 4, 2018) - initial version