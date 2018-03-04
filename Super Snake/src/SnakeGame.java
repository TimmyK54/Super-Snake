import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * 	Project started on February 13, 2018
 *  v.1.0.0 completed on March 4, 2018
 * 	by Tushar Khan
 */
public class SnakeGame {

	static String version = "v.1.0.0";
	
	public static void main(String[] args) {
		final int rows = 36, columns = 46;

		JFrame GUI = new JFrame("Super Snake " + version);

		SnakeGame x = new SnakeGame();
		SnakePanel contentPanel = x.new SnakePanel(rows, columns);

		GUI.getContentPane().add(contentPanel);

		GUI.pack();
		GUI.setResizable(false);
		GUI.setLocationRelativeTo(null);
		GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GUI.setVisible(true);
	}

	@SuppressWarnings("serial")
	public class SnakePanel extends JLayeredPane {

		// INSTANCE VARIABLES & OBJECTS
		private final String infoString = "Super-Snake " + version + ", Mar 2018";
		private final File leaderboardsFile = new File("SuperSnakeHighscores.txt");

		private final int
			cellSize = 20,
			startingSize = 5,
			abilityDuration = 6000,
			defaultSpeed = 64,
			
			maxPlayers = 2,
			maxAi = 20,
			maxSnakes = maxAi;

		public static final int
			OPEN 	= 1,	
			SNAKE 	= 2,
			POD		= 3;
		
		public final Color
			MYSTERY_COLOR = Color.MAGENTA,
			NORMAL_COLOR  = Color.WHITE,
			GHOST_COLOR   = Color.GRAY,
			SPEED_COLOR   = Color.RED,
			SLOW_COLOR 	  = Color.BLUE,
			GROW_COLOR 	  = Color.GREEN,
			WARP_COLOR 	  = Color.ORANGE,
			OPEN_COLOR 	  = Color.BLACK,
			AI_COLOR 	  = Color.CYAN,
			DEAD_COLOR	  = new Color(50,50,50);
		private Color POPUP_COLOR;

		private final int
			RUNNING = 1,
			PAUSE = 2,
			OVER = 3,
			MAIN_MENU = 4,
			MULTIPLAYER_MENU = 5;
		private int gameState;

		private final int
			UP = 1,
			DOWN = 2,
			LEFT = 3,
			RIGHT = 4;
		
		private final int
			MAIN_MENU_SINGLEPLAYER = 1,
			MAIN_MENU_MULTIPLAYER = 2,
			MAIN_MENU_AI = 3,
			MAIN_MENU_QUIT = 4,
			MULTIPLAYER_MENU_PLAYERS = 5,
			MULTIPLAYER_MAIN_MENU_AIS = 6;
		private int
			menuSelection = MAIN_MENU_SINGLEPLAYER,
			multiplayerSelection = MULTIPLAYER_MENU_PLAYERS,
			iconFrame = 0;

		private boolean
			newHighScore = false,
			highscoreAdded = false;

		private int
			numPlayers = 1,
			numAi = 0,
			multiplayerCountdown;
		
		private int newLeaderboardIndex;
		private double pauseTime;
		private Cell[][] gameBoard;
		private Pod pod;
		private ArrayList<Integer> multiplayerPoints = new ArrayList<Integer>();
		private ArrayList<Cell> openCells = new ArrayList<Cell>();
		private ArrayList<Snake> snakes = new ArrayList<Snake>();
		private Random random = new Random();
		private JTextField textField = new JTextField(5);
		private Timer menuTimer;
		private Timer multiplayerStartTimer;
		private Font defaultFont;

		public class Cell {
			
			private int y, x, t;
			
			public Cell(int row, int col, int type) {
				y = row;
				x = col;
				t = type;
			}
			public Cell(int row, int col) {
				this(row, col, 0);
			}
			
			public int getY() { return y; }
			public int getX() { return x; }
			public int getType() { return t; }
			public void setType(int type) { t = type; }
			
		}
		
		public class Snake {
			public boolean alive = true;
			public int
				currentDirection,
				newDirection,
				ghost = 0,
				grow = 0,
				warp = 0;
			
			public Color color, defaultColor = Color.WHITE;
			ArrayList<Double> abilityStartTimes = new ArrayList<Double>();
			public ArrayList<Cell> cells = new ArrayList<Cell>();
			public ArrayList<Timer> abilityTimers = new ArrayList<Timer>();
			public Timer runTimer = new Timer(defaultSpeed, new MoveAction());

			public Snake() {
				color = defaultColor;
				Cell startingCell = openCells.get(random.nextInt(openCells.size()));
				cells.add(startingCell);
				openCells.remove(startingCell);
				startingCell.setType(SNAKE);

				for (int i = 1; i < startingSize; i++) {
					startingCell = gameBoard[startingCell.getY()][startingCell.getX()];
					cells.add(startingCell);
					openCells.remove(startingCell);
					startingCell.setType(SNAKE);
				}
			}

			public void eatSpecialPod(int podType) {
				switch (pod.getType()) {
				case Pod.GHOST:
					ghost++;
					color = GHOST_COLOR;
					abilityTimers.add(new Timer(abilityDuration, new disableGhostAbility()));
					break;
				case Pod.SPEED:
					runTimer.setDelay(runTimer.getDelay()/2);
					color = SPEED_COLOR;
					abilityTimers.add(new Timer(abilityDuration, new disableSpeedAbility()));
					break;
				case Pod.SLOW:
					runTimer.setDelay(runTimer.getDelay()*2);
					color = SLOW_COLOR;
					abilityTimers.add(new Timer(abilityDuration, new disableSlowAbility()));
					break;
				case Pod.GROW:
					grow++;
					color = GROW_COLOR;
					abilityTimers.add(new Timer(abilityDuration, new disableGrowAbility()));
					break;
				case Pod.WARP:
					warp++;
					color = WARP_COLOR;
					abilityTimers.add(new Timer(abilityDuration, new disableWarpAbility()));
					break;
				}
				abilityStartTimes.add((double)System.currentTimeMillis());
				abilityTimers.get(abilityTimers.size()-1).start();
			}
			private class disableGhostAbility implements ActionListener { 	// Disable ghost pod effects
				@Override
				public void actionPerformed(ActionEvent e) {
					shedSkin();
					ghost--;
				}
			}
			private class disableSpeedAbility implements ActionListener {	// Disable speed pod effects
				@Override
				public void actionPerformed(ActionEvent e) {
					shedSkin();
					runTimer.setDelay(runTimer.getDelay()*2);
				}
			}
			private class disableSlowAbility implements ActionListener {	// Disable slow pod effects
				@Override
				public void actionPerformed(ActionEvent e) {
					shedSkin();
					runTimer.setDelay(runTimer.getDelay()/2);
				}
			}
			private class disableGrowAbility implements ActionListener {	// Disable grow pod effects
				@Override
				public void actionPerformed(ActionEvent e) {
					shedSkin();
					grow--;
				}
			}
			private class disableWarpAbility implements ActionListener {	// Disable warp pod effects
				@Override
				public void actionPerformed(ActionEvent e) {
					shedSkin();
					warp--;
				}
			}
			private void shedSkin() {										// Removes ability
				abilityTimers.get(0).stop();
				abilityTimers.remove(0);
				abilityStartTimes.remove(0);
				if (abilityTimers.size() <= 0) {
					color = defaultColor;
					repaint();
				}
			}

			// SNAKE MOVEMENT
			protected int getDirection() {
				if (currentDirection == 0 && newDirection != 0) {
					int playersActive = 0;
					for (int i = 0; i < numPlayers; i++) {
						if (snakes.get(i).currentDirection != 0 || snakes.get(i).newDirection != 0) playersActive++;
						if (numPlayers - playersActive > 0 && playersActive == 1) {
							multiplayerCountdown = 5;
							multiplayerStartTimer.start();
						}
					}
					for (int i = numPlayers-1; i < snakes.size(); i++)
						if (snakes.get(i).alive) snakes.get(i).runTimer.start();
				}
				switch (newDirection) {
				case UP:
					if (currentDirection != DOWN) 
						return newDirection;
					break;
				case DOWN:
					if (currentDirection != UP) 
						return newDirection;
					break;
				case LEFT:
					if (currentDirection != RIGHT) 
						return newDirection;
					break;
				case RIGHT:
					if (currentDirection != LEFT) 
						return newDirection;
					break;
				}
				return 0;
			}
			private class MoveAction implements ActionListener {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (getDirection() != 0) currentDirection = getDirection();
					if (newDirection == 0 && currentDirection == 0) return;

					// Add cells head in current direction
					Cell head = cells.get(0);
					switch (currentDirection) {
					case UP:
						if (head.getY() == 0) {
							if (warp > 0) head = gameBoard[gameBoard.length-1][head.getX()];
							else { alive = false; endGame(); return; }
						}
						else head = gameBoard[head.getY()-1][head.getX()];
						break;
					case DOWN:
						if (head.getY() == gameBoard.length-1) {
							if (warp > 0) head = gameBoard[0][head.getX()];
							else { alive = false; endGame(); return; }
						}
						else head = gameBoard[head.getY()+1][head.getX()];
						break;
					case LEFT:
						if (head.getX() == 0) {
							if (warp > 0) head = gameBoard[head.getY()][gameBoard[0].length-1];
							else { alive = false; endGame(); return; }
						}
						else head = gameBoard[head.getY()][head.getX()-1];
						break;
					case RIGHT:
						if (head.getX() == gameBoard[0].length-1) {
							if (warp > 0)head = gameBoard[head.getY()][0];
							else { alive = false; endGame(); return; }
						}
						else head = gameBoard[head.getY()][head.getX()+1];
						break;
					}

					// Remove tail or eat pod
					openCells.remove(head);
					Cell tail = cells.get(cells.size()-1);
					if (head.getType() == SNAKE) {		// Snake runs into itself
						if (ghost <= 0) {
							alive = false;
							endGame();
							return;
						}
						cells.remove(cells.size()-1);
						if (!cells.contains(tail)) {
							tail.setType(OPEN);
							openCells.add(tail);
						}
					}
					else if (head.getType() == OPEN) {	// Normal movement
						cells.remove(cells.size()-1);
						boolean coveredSnakeCell = false;
						for (int i = 0; i < snakes.size(); i++) {
							if (snakes.get(i).cells.contains(tail)) {
								coveredSnakeCell = true;
								break;
							}
						}
						if (!coveredSnakeCell) {
							tail.setType(OPEN);
							openCells.add(tail);
						}
					}
					else {								// Snake eats pod
						if (pod.getType() != Pod.NORMAL) eatSpecialPod(pod.getType());
						for (int i = 0; i < grow; i++) cells.add(tail);
						pod = new Pod();
					}
					cells.add(0, head);
					head.setType(SNAKE);
					repaint();
				}
			}
		}

		public class AiSnake extends Snake {

			public AiSnake() {
				super();
				if (numPlayers == 0) runTimer.setDelay(runTimer.getDelay()/2);
				
				double brightnessMultiplier = 1;
				if (numAi > 1) brightnessMultiplier = 1-Math.random()*0.8;
				color = defaultColor = new Color((int)(AI_COLOR.getRed()*brightnessMultiplier), (int)(AI_COLOR.getGreen()*brightnessMultiplier), (int)(AI_COLOR.getBlue()*brightnessMultiplier));
			}

			@Override
			protected int getDirection() {
				int snakeX = cells.get(0).getX();
				int snakeY = cells.get(0).getY();
				int podX = pod.getCell().getX();
				int podY = pod.getCell().getY();
				
				boolean[] heuristicDirections = { false, false, false, false };
				boolean[] wallCollision = { true, true, true, true };
				int[] collisionDistance = { 0, 0, 0, 0 };
				
				// Get heuristic directions
				if (snakeY > podY) heuristicDirections[UP-1] = true;
				if (snakeY < podY) heuristicDirections[DOWN-1] = true;
				if (snakeX > podX) heuristicDirections[LEFT-1] = true;
				if (snakeX < podX) heuristicDirections[RIGHT-1] = true;
				
				// CALCULATE COLLISIONS
				// Calculate north collision
				if (snakeY > 0) {
					for (int i = snakeY-1; i >= 0; i--) {
						if (gameBoard[i][snakeX].getType() == SNAKE) {
							wallCollision[UP-1] = false;
							break;
						}
						else collisionDistance[UP-1]++;
					}
				}
				// Calculate south collision
				if (snakeY < gameBoard.length-1) {
					for (int i = snakeY+1; i < gameBoard.length; i++) {
						if (gameBoard[i][snakeX].getType() == SNAKE) {
							wallCollision[DOWN-1] = false;
							break;
						}
						else collisionDistance[DOWN-1]++;
					}
				}
				// Calculate west collision
				if (snakeX > 0) {
					for (int i = snakeX-1; i >= 0; i--) {
						if (gameBoard[snakeY][i].getType() == SNAKE) {
							wallCollision[LEFT-1] = false;
							break;
						}
						else collisionDistance[LEFT-1]++;
					}
				}
				// Calculate east collision
				if (snakeX < gameBoard[0].length-1) {
					for (int i = snakeX+1; i < gameBoard[0].length; i++) {
						if (gameBoard[snakeY][i].getType() == SNAKE) {
							wallCollision[RIGHT-1] = false;
							break;
						}
						else collisionDistance[RIGHT-1]++;
					}
				}
				if (ghost > 0) {
					if (currentDirection != DOWN) collisionDistance[0]++;
					if (currentDirection != UP) collisionDistance[1]++;
					if (currentDirection != RIGHT) collisionDistance[2]++;
					if (currentDirection != LEFT) collisionDistance[3]++;
				}
				
				// DIRECTION DECISION PROCESS
				ArrayList<Integer> directionList = new ArrayList<Integer>();
				
				// Retreat from corners
				for (int i = 0; i < collisionDistance.length; i++)
					if (collisionDistance[i] != 0) directionList.add(i+1);
				
				if (directionList.size() == 1) return directionList.get(0);
				
				// Obstacle encountered
				switch (currentDirection) {
				case UP:
					if (collisionDistance[UP-1] > 0 && !(podY > snakeY && podX == snakeX)) break;
				case DOWN:
					if (collisionDistance[DOWN-1] > 0 && !(podY < snakeY && podX == snakeX)) break;
					
					if (wallCollision[LEFT-1] && !wallCollision[RIGHT-1] && snakeX != 0) return LEFT;
					else if (!wallCollision[LEFT-1] && wallCollision[RIGHT-1] && snakeX != gameBoard[0].length-1) return RIGHT;
					else if (wallCollision[LEFT-1] && wallCollision[RIGHT-1]) {
						if (heuristicDirections[LEFT-1]) return LEFT;
						else if (heuristicDirections[RIGHT-1]) return RIGHT;
						else {
							if (snakeX == 0) return RIGHT;
							else if (snakeX == gameBoard[0].length-1) return LEFT;
							else return (random.nextInt() % 2 == 0) ? LEFT : RIGHT;
						}
					}
					else {
						if (collisionDistance[LEFT-1] > collisionDistance[RIGHT-1]) return LEFT;
						else if (collisionDistance[LEFT-1] < collisionDistance[RIGHT-1]) return RIGHT;
						else return (random.nextInt() % 2 == 0) ? LEFT : RIGHT;
					}
					
				case LEFT:
					if (collisionDistance[LEFT-1] > 0 && !(podY == snakeY && podX > snakeX)) break;
				case RIGHT:
					if (collisionDistance[RIGHT-1] > 0 && !(podY == snakeY && podX < snakeX)) break;
					
					if (wallCollision[UP-1] && !wallCollision[DOWN-1] && snakeY != 0) return UP;
					else if (!wallCollision[UP-1] && wallCollision[DOWN-1] && snakeY != gameBoard.length-1) return DOWN;
					else if (wallCollision[UP-1] && wallCollision[DOWN-1]) {
						if (heuristicDirections[UP-1]) return UP;
						else if (heuristicDirections[DOWN-1]) return DOWN;
						else {
							if (snakeY == 0) return DOWN;
							else if (snakeY == gameBoard.length-1) return UP;
							else return (random.nextInt() % 2 == 0) ? UP : DOWN;
						}
					}
					else {
						if (collisionDistance[UP-1] > collisionDistance[DOWN-1]) return UP;
						else if (collisionDistance[UP-1] < collisionDistance[DOWN-1]) return DOWN;
						else return (random.nextInt() % 2 == 0) ? UP : DOWN;
					}
					
				}
				
				// Valid heuristic direction(s)
				directionList.clear();
				for (int i = 0; i < heuristicDirections.length; i++)
					if (heuristicDirections[i] && collisionDistance[i] > 0) directionList.add(i+1);
				
				if (directionList.size() == 1)  return directionList.get(0);
				else if (directionList.size() == 2) {
					if (currentDirection == directionList.get(0)) return directionList.get(0);
					else if (currentDirection == directionList.get(1)) return directionList.get(1);
					else return (random.nextInt() % 2 == 0) ? directionList.get(0) : directionList.get(1);
				}
				
				return currentDirection;
			}
			
		}

		public class Pod {
			public static final int
				NORMAL = 1,
				GHOST  = 2,
				SPEED  = 3,
				SLOW   = 4,
				GROW   = 5,
				WARP   = 6;
			public static final int
				SIZE1 = 1,
				SIZE2 = 2,
				SIZE3 = 3,
				SIZE4 = 4,
				SIZE5 = 5,
				SIZE6 = 6,
				SIZE7 = 7,
				SIZE8 = 8;

			private int type, size = SIZE1;
			private Color color;
			private Cell cell;

			public Pod() {
				generatePod();
			}

			public int getType() {
				return type;
			}
			public Color getColor() {
				return color;
			}
			public Cell getCell() {
				return cell;
			}
			public void setColor (Color newColor) {
				color = newColor;
			}
			public void resize() {
				switch (size) {
				case SIZE1:
					size = SIZE2;
					break;
				case SIZE2:
					size = SIZE3;
					break;
				case SIZE3:
					size = SIZE4;
					break;
				case SIZE4:
					size = SIZE5;
					break;
				case SIZE5:
					size = SIZE6;
					break;
				case SIZE6:
					size = SIZE7;
					break;
				case SIZE7:
					size = SIZE8;
					break;
				case SIZE8:
					size = SIZE1;
					break;
				}
			}
			private void generatePod() {
				cell = openCells.get(random.nextInt(openCells.size()));
				cell.setType(POD);
				if (numPlayers == 0) {
					type = NORMAL;
					color = NORMAL_COLOR;
				}
				else {
					switch ((int)(Math.random()*20)) {
					case 1:
						type = GHOST;
						color = GHOST_COLOR;
						break;
					case 2:
						type = SPEED;
						color = SPEED_COLOR;
						break;
					case 3:
						type = SLOW;
						color = SLOW_COLOR;
						break;
					case 4:
						type = GROW;
						color = GROW_COLOR;
						break;
					case 5:
						type = WARP;
						color = WARP_COLOR;
						break;
					default:
						type = NORMAL;
						color = NORMAL_COLOR;
					}
				}
				if (type != NORMAL && (int)(Math.random()*10) == 0) color = MYSTERY_COLOR;
				repaint();
			}
		}

		// CONSTRUCTOR
		public SnakePanel(int rows, int columns) {
			// Set up game panel
			gameBoard = new Cell[rows][columns];
			setLayout(null);
			setPreferredSize(new Dimension(cellSize*columns, cellSize*rows));
			add(textField);

			// Set up KeyListener
			addKeyListener(MainMenuKeyListener);
			setFocusable(true);
			setFocusTraversalKeysEnabled(false);

			// Set up defaultFont
			defaultFont = new Font("LucidaGrande", Font.PLAIN, 1);
/*			try {
				defaultFont = Font.createFont(Font.TRUETYPE_FONT, new File("LucidaGrande.ttc")).deriveFont(12f);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("LucidaGrande.ttc")));
			} catch (FontFormatException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
*/			
			// Set up leaderboards file
			try {
				leaderboardsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Setup multiplayer countdown
			multiplayerStartTimer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					multiplayerCountdown--;
					if (multiplayerCountdown <= 0) {
						for (int i = 0; i < numPlayers; i++) {
							if (snakes.get(i).currentDirection == 0) {
								snakes.get(i).alive = false;
								endGame();
							}
						}
						multiplayerStartTimer.stop();
					}
					repaint();
				}
			});
			
			// Set up menu animations
			gameState = MAIN_MENU;
			menuTimer = new Timer(defaultSpeed*2, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					repaint();
				}
			});
			menuTimer.start();
		}

		// START & END GAME METHODS
		private void createNewGame() {
			// Reset grid
			openCells.clear();
			for (int y = 0; y < gameBoard.length; y++) {
				for (int x = 0; x < gameBoard[0].length; x++) {
					Cell c = new Cell(y, x, OPEN);
					gameBoard[y][x] = c;
					openCells.add(c);
				}
			}

			// Reset variables
			gameState = RUNNING;
			newHighScore = false;
			highscoreAdded = false;
			textField.setSize(new Dimension(0, 0));
			snakes.clear();
			
			// Create new snake objects
			for (int i = 0; i < numPlayers; i++) {
				snakes.add(new Snake());
				multiplayerPoints.add(0);
			}
			for (int i = 0; i < numAi; i++) {
				Snake aiSnake = new AiSnake();
				aiSnake.runTimer.setInitialDelay(i);
				snakes.add(aiSnake);
				multiplayerPoints.add(0);
				
			}

			// Start game
			pod = new Pod();
			if (numPlayers > 0) {
				for (int i = 0; i < numPlayers; i++) 
					snakes.get(i).runTimer.start();
			}
			else {
				for (int i = 0; i < snakes.size(); i++) 
					snakes.get(i).runTimer.start();
			}
		}
		private void endGame() {
			int numAlive = 0;
			for (int i = 0; i < snakes.size(); i++) {
				if (snakes.get(i).alive == true) numAlive++;
				else {
					snakes.get(i).runTimer.stop();
					for (int j = 0; j < snakes.get(i).abilityTimers.size(); j++) 
						snakes.get(i).abilityTimers.get(j).stop();
					snakes.get(i).abilityTimers.clear();
					snakes.get(i).color = DEAD_COLOR;
				}
			}
			if (numAlive <= 1) {
				multiplayerStartTimer.stop();
				for (int i = 0; i < snakes.size(); i++) {
					if (snakes.get(i).alive == true) {
						snakes.get(i).runTimer.stop();
						for (int j = 0; j < snakes.get(i).abilityTimers.size(); j++) 
							snakes.get(i).abilityTimers.get(j).stop();
						break;
					}
				}
				gameState = OVER;
				repaint();
			}
		}
		private void storeLeaderboards(ArrayList<String[]> table, String lastEnteredName) {
			try {
				PrintWriter writer = new PrintWriter(leaderboardsFile);
				for (int i = 0; i < table.size(); i++) {
					String line = table.get(i)[0]+","+table.get(i)[1];
					writer.write(line+"\n");
				}
				writer.write("$$"+lastEnteredName+",0");
				writer.close();
			} catch (FileNotFoundException e) {
				System.out.println("Leaderboards file not found.");
				e.printStackTrace();
			}
		}

		// KEY LISTENERS
		KeyListener MainMenuKeyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_W:
					if (menuSelection != MAIN_MENU_SINGLEPLAYER) menuSelection--;
					repaint();
					break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_S:
					if (menuSelection != MAIN_MENU_QUIT) menuSelection++;
					repaint();
					break;
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_SPACE:
					menuTimer.stop();
					switch (menuSelection) {
					case MAIN_MENU_SINGLEPLAYER:
						numPlayers = 1;
						numAi = 0;
						removeKeyListener(MainMenuKeyListener);
						addKeyListener(GameKeyListener);
						createNewGame();
						break;
					case MAIN_MENU_MULTIPLAYER:
						gameState = MULTIPLAYER_MENU;
						removeKeyListener(MainMenuKeyListener);
						addKeyListener(MultiplayerMenuKeyListener);
						numPlayers = 2;
						numAi = 0;
						repaint();
						break; 
					case MAIN_MENU_AI:
						numPlayers = 0;
						numAi = 1;
						removeKeyListener(MainMenuKeyListener);
						addKeyListener(GameKeyListener);
						createNewGame();
						break;
					case MAIN_MENU_QUIT:
						System.exit(0);
						break;
					}
				}
			}

			@Override
			public void keyTyped(KeyEvent e) { return; }
			@Override
			public void keyReleased(KeyEvent e) { return; }
		};
		KeyListener MultiplayerMenuKeyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_W:
					if (multiplayerSelection == MULTIPLAYER_MENU_PLAYERS) {
						if (numPlayers < maxPlayers) {
							numPlayers++;
							if (numPlayers + numAi > maxSnakes) numAi--;
						}
					}
					else if (multiplayerSelection == MULTIPLAYER_MAIN_MENU_AIS) {
						if (numAi < maxAi) {
							numAi++;
							if (numPlayers + numAi > maxSnakes) numPlayers--;
						}
					}
					repaint();
					break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_S:
					if (multiplayerSelection == MULTIPLAYER_MENU_PLAYERS) {
						if (numPlayers > 0) {
							numPlayers--;
							if (numPlayers + numAi < 2) numAi++;
						}
					}
					else if (multiplayerSelection == MULTIPLAYER_MAIN_MENU_AIS) {
						if (numAi > 0) {
							numAi--;
							if (numPlayers + numAi < 2) numPlayers++;
						}
					}
					repaint();
					break;
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_A:
					if (multiplayerSelection == MULTIPLAYER_MAIN_MENU_AIS) multiplayerSelection = MULTIPLAYER_MENU_PLAYERS;
					repaint();
					break;
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_D:
					if (multiplayerSelection == MULTIPLAYER_MENU_PLAYERS) multiplayerSelection = MULTIPLAYER_MAIN_MENU_AIS;
					repaint();
					break;
				case KeyEvent.VK_ENTER:
				case KeyEvent.VK_SPACE:
					removeKeyListener(MultiplayerMenuKeyListener);
					addKeyListener(GameKeyListener);
					createNewGame();
					break;
				case KeyEvent.VK_BACK_SPACE:
					gameState = MAIN_MENU;
					removeKeyListener(MultiplayerMenuKeyListener);
					addKeyListener(MainMenuKeyListener);
					menuTimer.start();
					repaint();
					break;
				}
			}
			
			@Override
			public void keyTyped(KeyEvent e) { return; }
			@Override
			public void keyReleased(KeyEvent e) { return; }
		};
		KeyListener GameKeyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if (numPlayers > 1) {
						if (snakes.get(1).runTimer.isRunning()) {
							snakes.get(1).newDirection = UP;
							break;
						}
						break;
					}
				case KeyEvent.VK_W:
					if (snakes.get(0).runTimer.isRunning() && numPlayers != 0) snakes.get(0).newDirection = UP;
					break;
				case KeyEvent.VK_DOWN:
					if (numPlayers > 1) {
						if (snakes.get(1).runTimer.isRunning()) {
							snakes.get(1).newDirection = DOWN;
							break;
						}
						break;
					}
				case KeyEvent.VK_S:
					if (snakes.get(0).runTimer.isRunning() && numPlayers != 0) snakes.get(0).newDirection = DOWN;
					break;
				case KeyEvent.VK_LEFT:
					if (numPlayers > 1) {
						if (snakes.get(1).runTimer.isRunning()) {
							snakes.get(1).newDirection = LEFT;
							break;
						}
						break;
					}
				case KeyEvent.VK_A:
					if (snakes.get(0).runTimer.isRunning() && numPlayers != 0) snakes.get(0).newDirection = LEFT;
					break;
				case KeyEvent.VK_RIGHT:
					if (numPlayers > 1) {
						if (snakes.get(1).runTimer.isRunning()) {
							snakes.get(1).newDirection = RIGHT;
							break;
						}
						break;
					}
				case KeyEvent.VK_D:
					if (snakes.get(0).runTimer.isRunning() && numPlayers != 0) snakes.get(0).newDirection = RIGHT;
					break;
				case KeyEvent.VK_SPACE:
					if (gameState == RUNNING || gameState == PAUSE) {
						if (gameState == RUNNING) {
							gameState = PAUSE;
							for (int i = 0; i < snakes.size(); i++) {
								snakes.get(i).runTimer.stop();
								pauseTime = System.currentTimeMillis();
								for (int j = 0; j < snakes.get(i).abilityTimers.size(); j++) {
									snakes.get(i).abilityTimers.get(j).stop();
									snakes.get(i).abilityTimers.get(j).setInitialDelay((int)(abilityDuration-(pauseTime-snakes.get(i).abilityStartTimes.get(j))));
								}
							}
							repaint();
						}
						else {
							gameState = RUNNING;
							for (int i = 0; i < snakes.size(); i++) {
								if (snakes.get(i).alive) snakes.get(i).runTimer.start();
								for (int j = 0; j < snakes.get(i).abilityTimers.size(); j++) {
									snakes.get(i).abilityTimers.get(j).start();
									snakes.get(i).abilityStartTimes.set(j, System.currentTimeMillis()-(pauseTime-snakes.get(i).abilityStartTimes.get(j)));
								}
							}
							repaint();
						}
					}
					break;
				case KeyEvent.VK_ENTER:
					if (gameState == OVER) { 
						if (textField.getActionListeners().length > 0) textField.removeActionListener(textField.getActionListeners()[0]);
						createNewGame();
					}
					break;
				case KeyEvent.VK_BACK_SPACE:
					if (gameState == OVER || gameState == PAUSE) {
						gameState = MAIN_MENU;
						textField.setSize(new Dimension(0, 0));
						if (textField.getActionListeners().length > 0) textField.removeActionListener(textField.getActionListeners()[0]);
						multiplayerPoints.clear();
						removeKeyListener(GameKeyListener);
						addKeyListener(MainMenuKeyListener);
						menuTimer.setInitialDelay(0);
						menuTimer.start();
					}
					break;
				}
			}

			@Override
			public void keyTyped(KeyEvent e) { return; }
			@Override
			public void keyReleased(KeyEvent e) { return; }
		};

		// PAINT METHODS
		private void updatePopupColor() {
			if (snakes.size() == 0) return;
			switch ((int)(Math.random()*5)) {
			case 0:	// Red
				POPUP_COLOR = new Color(50, 0, 10, 200);
				if (snakes.get(0).color == SPEED_COLOR || snakes.get(snakes.size()-1).color == SPEED_COLOR || pod.getColor() == SPEED_COLOR)
					updatePopupColor();
				break;
			case 1:	// Green
				POPUP_COLOR = new Color(0, 25, 10, 200);
				if (snakes.get(0).color == GROW_COLOR || snakes.get(snakes.size()-1).color == GROW_COLOR || pod.getColor() == GROW_COLOR)
					updatePopupColor();
				break;
			case 2:	// Blue
				POPUP_COLOR = new Color(0, 0, 40, 200);
				if (snakes.get(0).color == SLOW_COLOR || snakes.get(snakes.size()-1).color == SLOW_COLOR || pod.getColor() == SLOW_COLOR)
					updatePopupColor();
				break;
			case 3:	// Gray
				POPUP_COLOR = new Color(20, 20, 20, 200);
				if (snakes.get(0).color == GHOST_COLOR || snakes.get(snakes.size()-1).color == GHOST_COLOR || pod.getColor() == GHOST_COLOR)
					updatePopupColor();
				break;
			case 4: // Purple
				POPUP_COLOR = new Color(30, 0, 30, 200);
				if (pod.getColor() == MYSTERY_COLOR)
					updatePopupColor();
				break;
			}
		}
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Full screens
			switch (gameState) {

			// Paint game grid
			case RUNNING:
				pod.resize();
			case PAUSE:
			case OVER:
				for (int y = 0; y < gameBoard.length; y++) {
			        for (int x = 0; x < gameBoard[0].length; x++) {
			        	g.setColor(OPEN_COLOR);
		        		g.fillRect(x*cellSize, y*cellSize, cellSize, cellSize);
			        	switch (gameBoard[y][x].getType()) {
			        	case SNAKE:
			        		ArrayList<Snake> snakesInCell = new ArrayList<Snake>();
			        		for (int i = 0; i < snakes.size(); i++) {
			        			if (snakes.get(i).cells.contains(gameBoard[y][x])) snakesInCell.add(snakes.get(i));
			        		}
		        			g.setColor(snakesInCell.get(snakesInCell.size()-1).color);
		        			for (int i = 0; i < snakesInCell.size(); i++) {
		        				if (snakesInCell.get(i).ghost > 0) g.setColor(snakesInCell.get(i).color);
		        			}
			        		g.fillRect(x*cellSize+1, y*cellSize+1, cellSize-2, cellSize-2);
			        		break;
			        	case POD:
			        		g.setColor(pod.color);
			        		switch (pod.size) {
			        		case Pod.SIZE1:
			        			g.fillRect(x*cellSize+cellSize/4+2, y*cellSize+cellSize/4+2, cellSize/2-4, cellSize/2-4);
			        			break;
			        		case Pod.SIZE2:
			        		case Pod.SIZE8:
			        			g.fillRect(x*cellSize+cellSize/4+1, y*cellSize+cellSize/4+1, cellSize/2-2, cellSize/2-2);
			        			break;
			        		case Pod.SIZE3:
			        		case Pod.SIZE7:
			        			g.fillRect(x*cellSize+cellSize/4, y*cellSize+cellSize/4, cellSize/2, cellSize/2);
			        			break;
			        		case Pod.SIZE4:
			        		case Pod.SIZE6:
			        			g.fillRect(x*cellSize+cellSize/4-1, y*cellSize+cellSize/4-1, cellSize/2+2, cellSize/2+2);
			        			break;
			        		case Pod.SIZE5:
			        			g.fillRect(x*cellSize+cellSize/4-2, y*cellSize+cellSize/4-2, cellSize/2+4, cellSize/2+4);
			        			break;
			        		default:
			        			g.fillRect(x*cellSize+cellSize/4, y*cellSize+cellSize/4, cellSize/2, cellSize/2);
			        		}
			        		break;
			        	default:
			        		g.setColor(OPEN_COLOR);
			        		g.fillRect(x*cellSize, y*cellSize, cellSize, cellSize);
			        	}
			        }
				}

				// Draws score strings
				if (numPlayers + numAi > 1) {
					for (int i = 0; i < numPlayers; i++) {
						g.setColor(snakes.get(i).color);
						g.drawString("Player "+(i+1)+":", 5, 16*(i+1));
						g.drawString(""+snakes.get(i).cells.size(), 63, 16*(i+1));
					}
					for (int i = 0; i < numAi; i++) {
						g.setColor(snakes.get(numPlayers+i).color);
						g.drawString("CPU "+(i+1)+":", 5, 16*(numPlayers+i+1));
						g.drawString(""+snakes.get(numPlayers+i).cells.size(), 63, 16*(numPlayers+i+1));
					}
				}
				else {
					if (numPlayers == 0) {
						g.setColor(Color.MAGENTA);
						g.drawString(""+snakes.get(0).cells.size(), 4, 15);
					}
					else if (gameBoard[0][0].getType() != POD) {
						g.setColor(Color.BLACK);
						g.fillRect(4, 4, 8*((int)(Math.log10(snakes.get(0).cells.size()))+1), 12);
						g.setColor(snakes.get(0).color);
					}
					else {
						g.setColor(new Color(255-pod.getColor().getRed(),255-pod.getColor().getGreen(),255-pod.getColor().getBlue()));
					}
					g.drawString(""+snakes.get(0).cells.size(), 4, 15);
				}

				// Draw initial snake identification for multiplayer
				if (numPlayers + numAi > 1) {
					g.setColor(Color.BLACK);
					g.setFont(defaultFont.deriveFont(Font.BOLD, 15));
					for (int i = 0; i < numPlayers; i++) {
						if (snakes.get(i).newDirection == 0 && snakes.get(i).currentDirection == 0 && snakes.get(i).runTimer.isRunning()) {
							if (multiplayerStartTimer.isRunning()) {
								g.setColor(Color.RED);
								g.drawString(""+multiplayerCountdown, snakes.get(i).cells.get(0).getX()*cellSize+5, snakes.get(i).cells.get(0).getY()*cellSize+16);
							}
							else 
								g.drawString(""+(i+1), snakes.get(i).cells.get(0).getX()*cellSize+5, snakes.get(i).cells.get(0).getY()*cellSize+16);
						}		
					}
					g.setColor(Color.BLACK);
					g.setFont(defaultFont.deriveFont(Font.PLAIN, 15));
					for (int i = 0; i < numAi; i++) {
						if (snakes.get(i).newDirection == 0 && snakes.get(numPlayers+i).currentDirection == 0)
							g.drawString(""+(i+1), snakes.get(numPlayers+i).cells.get(0).getX()*cellSize+5-5*(int)(Math.log10(i+1)), snakes.get(numPlayers+i).cells.get(0).getY()*cellSize+16);
					}
				}

				break;

			// Paint menu screen
			case MAIN_MENU:
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
				
				// Draw version info in bottom left corner
				g.setColor(Color.WHITE);
				g.setFont(defaultFont.deriveFont(Font.PLAIN, 15));
				g.drawString(infoString, 4, this.getHeight()-4);

				// Draw "Super" title
				g.setColor(GHOST_COLOR);
				g.setFont(defaultFont.deriveFont(Font.BOLD, 30));
				g.drawString("S   U   P   E   R", this.getWidth()/2-107, this.getHeight()/2-215);

				// Draw "Snake" title
				g.setFont(defaultFont.deriveFont(Font.BOLD, 150));
				g.setColor(SLOW_COLOR);
				g.drawString("S", this.getWidth()/2-234, this.getHeight()/2-95);
				g.setColor(GROW_COLOR);
				g.drawString("N", this.getWidth()/2-161, this.getHeight()/2-95);
				g.setColor(WARP_COLOR);
				g.drawString("A", this.getWidth()/2-55, this.getHeight()/2-95);
				g.setColor(SPEED_COLOR);
				g.drawString("K", this.getWidth()/2+47, this.getHeight()/2-95);
				g.setColor(MYSTERY_COLOR);
				g.drawString("E", this.getWidth()/2+144, this.getHeight()/2-95);

				// Draw menu items
				g.setColor(Color.WHITE);
				g.setFont(defaultFont.deriveFont(Font.BOLD, 38));
				g.drawString("Singleplayer", this.getWidth()/2-119, this.getHeight()/2+60);
				g.drawString("Multiplayer", this.getWidth()/2-109, this.getHeight()/2+110);
				g.drawString("Snake AI", this.getWidth()/2-83, this.getHeight()/2+160);
				g.drawString("Quit", this.getWidth()/2-42, this.getHeight()/2+210);

				// Draw selection icon
				g.setColor(Color.CYAN);
				int[] iconPos = new int[2];
				switch (menuSelection) {
				case MAIN_MENU_SINGLEPLAYER:
					iconPos[0] = this.getWidth()/2-119;
					iconPos[1] = this.getHeight()/2+60;
					break;
				case MAIN_MENU_MULTIPLAYER:
					iconPos[0] = this.getWidth()/2-109;
					iconPos[1] = this.getHeight()/2+110;
					break;
				case MAIN_MENU_AI:
					iconPos[0] = this.getWidth()/2-83;
					iconPos[1] = this.getHeight()/2+160;
					break;
				case MAIN_MENU_QUIT:
					iconPos[0] = this.getWidth()/2-42;
					iconPos[1] = this.getHeight()/2+210;
					break;
				}
				iconPos[0] -= 27; // x-offset
				iconPos[1] -= 13; // y-offset
				int[][] snakeIcon = {{0, 0, 0, 0, 0},{0, 0, 0, 0, 0}};
				int iconSize = 12;
				switch (iconFrame%10) {
				case 9: snakeIcon = new int[][] {{0, 0, 0, 0, 0},{1, 1, 1, 1, 1}}; break;
				case 8: snakeIcon = new int[][] {{0, 0, 0, 0, 1},{0, 1, 1, 1, 1}}; break;
				case 7: snakeIcon = new int[][] {{0, 0, 0, 1, 1},{0, 1, 1, 1, 0}}; break;
				case 6: snakeIcon = new int[][] {{0, 0, 1, 1, 1},{0, 1, 1, 0, 0}}; break;
				case 5: snakeIcon = new int[][] {{0, 1, 1, 1, 1},{0, 1, 0, 0, 0}}; break;
				case 4: snakeIcon = new int[][] {{1, 1, 1, 1, 1},{0, 0, 0, 0, 0}}; break;
				case 3: snakeIcon = new int[][] {{0, 1, 1, 1, 1},{0, 0, 0, 0, 1}}; break;
				case 2: snakeIcon = new int[][] {{0, 1, 1, 1, 0},{0, 0, 0, 1, 1}}; break;
				case 1: snakeIcon = new int[][] {{0, 1, 1, 0, 0},{0, 0, 1, 1, 1}}; break;
				case 0: snakeIcon = new int[][] {{0, 1, 0, 0, 0},{0, 1, 1, 1, 1}}; break;
				}
				iconFrame++;
				for (int row = 0; row < 2; row++) {
					for (int col = 0; col < 5; col++) {
						g.fillRect(iconPos[0]-(iconSize+2)*col, iconPos[1]-(iconSize+2)*row, iconSize*snakeIcon[row][col], iconSize*snakeIcon[row][col]);
					}
				}
				break;
				
			// Paint multiplayer menu screen
			case MULTIPLAYER_MENU:
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
				
				// Draw "Multiplayer" title
				g.setFont(defaultFont.deriveFont(Font.BOLD, 50));
				g.setColor(Color.LIGHT_GRAY);
				g.drawString("MULTIPLAYER", this.getWidth()/2-261, this.getHeight()/2-180);
				g.setColor(Color.DARK_GRAY);
				g.drawString("MENU", this.getWidth()/2+112, this.getHeight()/2-180);
				
				// Draw selection headings
				g.setFont(defaultFont.deriveFont(Font.BOLD, 35));
				g.setColor(Color.WHITE);
				g.drawString("PLAYERS", this.getWidth()/2-204, this.getHeight()/2-60);
				g.drawString("CPUs", this.getWidth()/2+79, this.getHeight()/2-60);
				
				// Draw max value sub-heading
				g.setFont(defaultFont.deriveFont(Font.PLAIN, 15));
				g.drawString("[Max " + maxPlayers + "]", this.getWidth()/2-152-((int)(Math.log10(maxPlayers))*8)/2, this.getHeight()/2-40);
				g.drawString("[Max " + maxAi + "]", this.getWidth()/2+99-((int)(Math.log10(maxAi))*8)/2, this.getHeight()/2-40);
				
				// Draw selected values
				g.setFont(defaultFont.deriveFont(Font.PLAIN, 120));
				g.drawString(""+numPlayers, this.getWidth()/2-163-38*(int)(Math.log10(numPlayers)), this.getHeight()/2+100);
				g.drawString(""+numAi, this.getWidth()/2+87-38*(int)(Math.log10(numAi)), this.getHeight()/2+100);
				
				// Draw selection icon
				if (multiplayerSelection == MULTIPLAYER_MENU_PLAYERS) g.fillRect(this.getWidth()/2-161, this.getHeight()/2+115, 73, 10);
				else g.fillRect(this.getWidth()/2+89, this.getHeight()/2+115, 73, 10);
				
				// Draw keyboard controls
				g.setFont(defaultFont.deriveFont(Font.PLAIN, 18));
				g.drawString("Press ENTER or SPACE to start the game", this.getWidth()/2-173,this.getHeight()-80);
				g.drawString("Press BACKSPACE to return to main menu", this.getWidth()/2-180,this.getHeight()-55);
				
				break;

			}	// End of first switch (gameState)

			// Pop-up screens
			updatePopupColor();
			switch (gameState) {

			// Game over
			case OVER:
				int width, height;
				// Multiplayer
				if (numPlayers + numAi > 1) {
					width = 400;
					height = 180+(snakes.size()-1)*18;

					// Fill box
					g.setColor(POPUP_COLOR);
					g.fillRect((int)((this.getSize().getWidth()-width)/2), (int)((this.getSize().getHeight()-height)/2), width, height);

					// Draw "Game Over" text
					g.setColor(Color.WHITE);
					g.setFont(defaultFont.deriveFont(Font.BOLD, 30));
					g.drawString("Game Over", this.getWidth()/2-86, this.getHeight()/2-height/2+50);
					
					// Draw winner text
					g.setFont(defaultFont.deriveFont(Font.PLAIN, 20));
					String winnerText = "All players died!";
					for (int i = 0; i < snakes.size(); i++) {
						if (snakes.get(i).alive) {
							if (i < numPlayers) { 
								winnerText = "Player "+(i+1)+" won!";
								g.drawString(winnerText, this.getWidth()/2-63, this.getHeight()/2-height/2+80);
							}
							else {
								winnerText = "CPU "+(i+1-numPlayers)+" won!";
								g.drawString(winnerText, this.getWidth()/2-54, this.getHeight()/2-height/2+80);
							}
							
							multiplayerPoints.set(i, multiplayerPoints.get(i)+1);
							break;
						}
					}
					
					// Draw scores
					g.setFont(defaultFont.deriveFont(Font.ITALIC, 15));
					for (int i = 0; i < numPlayers; i++) {
						g.drawString("Player "+(i+1)+": ", this.getWidth()/2-39, this.getHeight()/2-height/2+113+18*i);
						g.drawString(""+multiplayerPoints.get(i), this.getWidth()/2+28, this.getHeight()/2-height/2+113+18*i);
					}
					for (int i = 0; i < numAi; i++) {
						g.drawString("CPU "+(i+1)+": ", this.getWidth()/2-39, this.getHeight()/2-height/2+113+18*(numPlayers+i));
						g.drawString(""+multiplayerPoints.get(numPlayers+i), this.getWidth()/2+28, this.getHeight()/2-height/2+113+18*(numPlayers+i));
					}
				}
				// Single AI
				else if (numPlayers == 0 && numAi == 1) {
					width = 400;
					height = 200;
					int score = snakes.get(0).cells.size();

					// Fill box
					g.setColor(POPUP_COLOR);
					g.fillRect((int)((this.getSize().getWidth()-width)/2), (int)((this.getSize().getHeight()-height)/2), width, height);

					// Draw text
					g.setColor(Color.WHITE);
					g.setFont(defaultFont.deriveFont(Font.BOLD, 40));
					g.drawString("Game Over", this.getWidth()/2-114, this.getHeight()/2-36);
					g.setFont(defaultFont.deriveFont(Font.PLAIN, 30));
					g.drawString("The AI got a score of "+score, this.getWidth()/2-153-((int)(Math.log10(score)+1)*20)/2, this.getHeight()/2-height/2+115);
				}
				// Singleplayer
				else {
					width = 400;
					height = 550;
					int score = snakes.get(0).cells.size();

					// Fill box
					g.setColor(POPUP_COLOR);
					g.fillRect((int)((this.getSize().getWidth()-width)/2), (int)((this.getSize().getHeight()-height)/2), width, height);

					// Iterate through high scores text file
					ArrayList<String[]> leaderboardsTable = new ArrayList<String[]>();
					String newLeaderboardName = "ENTER NAME";
					try {
						Scanner inputLeaderboards = new Scanner(leaderboardsFile);

						while (inputLeaderboards.hasNextLine()) {
							String line = inputLeaderboards.nextLine();
							Scanner lineReader = new Scanner(line);

							if (line.substring(0, 2).equals("$$")) {
								newLeaderboardName = line.substring(2, line.indexOf(','));
							}
							else {
								String leaderboardName = line.substring(0, line.indexOf(','));
								String leaderboardScore = line.substring(line.indexOf(',')+1);

								leaderboardsTable.add(new String[] { leaderboardName, leaderboardScore });
							}
							lineReader.close();
						}

						inputLeaderboards.close();
					} catch (FileNotFoundException e) {
						System.out.println("Leaderboards File not found.");
						e.printStackTrace();
					}

					if (!highscoreAdded) {
						// Add current score to leader board array
						newLeaderboardIndex = leaderboardsTable.size();
						for (int i = leaderboardsTable.size()-1; i >= 0; i--) {
							if (Integer.parseInt(leaderboardsTable.get(i)[1]) == score) {
								newLeaderboardIndex = i+1;
								break;
							}
							else if (Integer.parseInt(leaderboardsTable.get(i)[1]) < score)
								newLeaderboardIndex = i;
						}
						leaderboardsTable.add(newLeaderboardIndex, new String[] { newLeaderboardName, ""+score });
						if (leaderboardsTable.size() > 10) leaderboardsTable.remove(leaderboardsTable.size()-1);
						storeLeaderboards(leaderboardsTable, newLeaderboardName);

						// Create text field for new high score name
						if (newLeaderboardIndex < 10) {
							newHighScore = true;
							JLabel leaderboardNameLabel = new JLabel(newLeaderboardName);
							textField.setText(newLeaderboardName);
							textField.setBounds(this.getWidth()/2-width/2+width/6, this.getHeight()/2+height/2-85, 2*width/3, 20);
							textField.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									if (textField.getText().length() > 12) textField.setText(textField.getText().substring(0, 12));
									leaderboardNameLabel.setText(textField.getText());
									leaderboardsTable.set(newLeaderboardIndex, new String[] { leaderboardNameLabel.getText(), ""+score });
									storeLeaderboards(leaderboardsTable, leaderboardNameLabel.getText());
									repaint();
								}
							});
						}
						highscoreAdded = true;
					}

					// Draw text
					g.setColor(Color.WHITE);
					g.setFont(defaultFont.deriveFont(Font.BOLD, 40));
					g.drawString("Game Over", this.getWidth()/2-113, this.getHeight()/2-height/2+70);
					g.setFont(defaultFont.deriveFont(Font.PLAIN, 30));
					g.drawString("You got a score of "+score, this.getWidth()/2-133-((int)(Math.log10(score)+1)*20)/2, this.getHeight()/2-height/2+110);
					if (newHighScore) g.drawString("New High Score!", this.getWidth()/2-117, this.getHeight()/2-height/2+180);
					else g.drawString("Leaderboards", this.getWidth()/2-99, this.getHeight()/2-height/2+180);

					// Draw high scores table
					g.setFont(new Font("American Typewriter", Font.PLAIN, 20));
					for (int i = 0; i < leaderboardsTable.size(); i++) {
						String tableEntry;
						// Add name
						if (i == newLeaderboardIndex) {
							g.setColor(new Color(8, 163, 244));
							tableEntry = newLeaderboardName;
						}
						else tableEntry = leaderboardsTable.get(i)[0];
						g.drawString(tableEntry, this.getWidth()/2-width/2+60, this.getHeight()/2-height/2+225+25*i);
						// Add score
						tableEntry = leaderboardsTable.get(i)[1];
						g.drawString(tableEntry, this.getWidth()/2+width/2-100,this.getHeight()/2-height/2+225+25*i);
						g.setColor(Color.WHITE);
					}

				}
				g.setFont(defaultFont.deriveFont(Font.PLAIN, 15));
				g.drawString("Press ENTER to play again", this.getWidth()/2-91,this.getHeight()/2+height/2-34);
				g.drawString("Press BACKSPACE to return to menu", this.getWidth()/2-129,this.getHeight()/2+height/2-13);
				break;

			// Game paused
			case PAUSE:
				width = 350;
				height = 150;

				// Fill box
				g.setColor(POPUP_COLOR);
				g.fillRect((int)((this.getSize().getWidth()-width)/2), (int)((this.getSize().getHeight()-height)/2), width, height);

				// Draw text
				g.setColor(Color.WHITE);
				g.setFont(defaultFont.deriveFont(Font.BOLD, 30));
				g.drawString("Game Paused", this.getWidth()/2-102, this.getHeight()/2-6);
				g.setFont(defaultFont.deriveFont(Font.PLAIN, 15));
				g.drawString("Press SPACE to resume game", this.getWidth()/2-103,this.getHeight()/2+height/2-34);
				g.drawString("Press BACKSPACE to return to menu", this.getWidth()/2-129,this.getHeight()/2+height/2-13);

				break;

			}	// End of second switch (gameState)

		}	// End of paintComponent() method

	}	// End of SnakePanel class

}