# Super Snake
This project is my first "real" programming application with a functional GUI and an extensive amount of work. It is essentially a snake game that features powerups, multiplayer, and a built-in AI. If you decide to go through the code, 

### Installation
Requirements: Java 1.8+

##### School Computer
The downloaded .jar file does not run on a school computer because it is from an "unidentified developer" - me! Follow these steps to run this, or any other .jar file on your school computer:
1. Make sure you have the .jar file downloaded
1. If you don't have Eclipse, install Eclipse Java from the Self Service app
1. Load up Eclipse and click out of the welcome tab if it's your first time on Eclipse
1. In the top bar, navigate to File >> New >> Java Project
1. Name the file whatever you want - this will be the directory for all the future .jar files you download
1. Add the downloaded .jar file to the src folder
1. Done! Anytime you want to run the file, run it from the src folder in Eclipse

### Gameplay

##### Controls
- Use WASD or the arrow keys to move.
- Press space to pause.
- In a menu screen, press enter to select a menu item.
- In a menu screen, press backspace to return to the main menu.
- After entering a leaderboard name, press tab to exit text box before pressing enter to play aain.

##### Powerups
There are various powerups to be taken advantage of in Super Snake - 5 to be exact! Powerups can be obtained by eating special colored pellets. Each powerup lasts for 6 seconds, and can be stacked. Eating a special pellet when you currently have an active powerup will not replace your current powerup. But be careful using the previous powerup's effects because you won't have a visual indicator of when it runs out!

Here is a list of the 5 powerups and what they do:

Powerup | Color | Image | Description
:------:|:-----:|:-----:|:-----------
Freeze|Blue|(not available)|This powerup halves your snake's speed. It is useful for performing precise maneuvers or slowing yourself down if you stacked up one too many turbo powerups. But be careful using this in multiplayer as it will give your opponents a temporary speed advantage!
Turbo|Red|(not available)|This powerup doubles your snake's speed. It is useful in multiplayer to trap opponents or get to a pellet first. It could be dangerous in singleplayer since it is more difficult to control.
Warp|Yellow|(not available)|Getting trapped into a corner? With the warp powerup, you can teleport through walls to get to the other side! It is useful for getting to the other side of the screen and beating your opponents to a pellet.
Ghost|Gray|(not available)|One of the more powerful powerups in the game, ghost allows you to pass through other snakes - even yourself! It is very useful for escaping some sticky situations and giving you a break from any obstacles in the game. But be wary of using this ability when the timer is almost out - or you may find yourself trapped in another snake's body!
Growth|Green|(not available)|This powerup is relatively underrated, but it can be very powerful if used effectively. While a snake usually only grows by one each time it eats a pellet, a snake with the growth powerup will grow by one more for each active growth powerup. Make sure to try to eat as many pellets as you can while this powerup is active!
Mystery|Magenta|(not available)|Wait, I thought you said there were 5 powerups? Well there are! The mystery powerup will grant you a random powerup from above. But you won't know what you get until you eat the pellet, so be ready for anything!

##### Multiplayer

### AI

### Version History

1.0.0 (March 4, 2018) - initial version