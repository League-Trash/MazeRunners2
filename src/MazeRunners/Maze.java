package MazeRunners;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

/**
 * Matthew Manoly, Steven Proctor, Zachary Taylor, Sinclair Fuh, Derek Windahl
 * Maze.java
 * 12/13/18
 * Generates a maze and then paints it, as well as the player/mouse objects, the rival objects, player id and timer, and final times.
 * Also detects when a player has collided with a wall, or finished the game. Prevents the player from moving their player until they
 * move it away from the wall. 
 */

public class Maze extends Canvas{
		
	private static final long serialVersionUID = 5491802318380000478L;

	private int rows; //number of maze rows
	private int columns; //number of maze columns
	private int mouseX; //x position of the mouse
	private int mouseY; //y position of the mouse
	private int startX = 15; //x position of the player
	private int startY = 15; //y position of the player
	private double time = 0; //player timer
	private double[] finalTimes = new double[4]; //all player's finishing times
	private boolean isPlaying = false; //true if player is playing
	private boolean isDone = false; //true if player has reached the end
	private boolean allDone = false; //true if all players have reached the end
	private Point[] rivals; //Point array of rival's coordinates
	private int playerid; //player id

	ArrayList<Node[][]> maps = new ArrayList<Node[][]>(); //arraylist of generated maps
	
	//constructor
	public Maze(int r, int c) {
		rows = r;
		columns = c;
	}
	
	//Generates a maze using depth-first search maze generation algorithm and adds it to the ArrayList
	//Credit to http://www.algostructure.com/specials/maze.php for the algorithm
	public void generateMaze() {
		Node[][] maze = new Node[rows][columns]; //maze represented as 2d grid of nodes
		Random rnd = new Random();
		Stack<Node> stack = new Stack<Node>(); //stack of Nodes
		for(int i = 0; i<maze.length; i++) {
			for(int j = 0; j<maze[i].length; j++) {
				maze[i][j] = new Node(i,j,false); //Create nodes at every index with visited status set to false
			}
		}
		maze[0][0].setVisited(true); //Starting node set to visited
		int curRow = 0; //current node row
		int curCol = 0; //current node column
		int newRow; //new node row
		int newCol; //new node column
		//while there are unvisited nodes
		while(hasUnvisited(maze)) {
			if(hasUnvisitedNeighbor(maze, curRow, curCol)) { //if node has an unvisited neighbor
				//select a random neighbor
				while(true) {
					newRow = curRow;
					newCol = curCol;
					if(rnd.nextInt(2) == 0)
						newRow = curRow + rnd.nextInt(3) - 1;
					else
						newCol = curCol + rnd.nextInt(3) - 1;
					
					//new node is actually a neighbor, and nothing is out of bounds
					if((newRow != curRow || newCol != curCol) && newRow >= 0 && newCol >= 0 && newRow < rows && newCol < columns) {
						if(!maze[newRow][newCol].isVisited()) {
							break;
						}
					}
				}
				//push the current node to the stack
				stack.push(maze[curRow][curCol]);
				//remove the wall between the current node and the new node
				if(newRow > curRow) { //if new node is below current node
					maze[curRow][curCol].setSouth(false);
					maze[newRow][newCol].setNorth(false);
				}
				else if(newRow < curRow) { //if new node is above current node
					maze[curRow][curCol].setNorth(false);
					maze[newRow][newCol].setSouth(false);
				}
				else if(newCol > curCol){ //if new node is to the right
					maze[curRow][curCol].setEast(false);
					maze[newRow][newCol].setWest(false);
				}
				else { //if new node is to the left
					maze[curRow][curCol].setWest(false);
					maze[newRow][newCol].setEast(false);
				}
				//make the new node the current node
				curRow = newRow; 
				curCol = newCol;
				maze[curRow][curCol].setVisited(true); //mark node as visited
			}
			else if(!stack.isEmpty()) { //if no unvisited neighbor and stack is not empty
				//pop a node from the stack and make it the current node
				curRow = stack.peek().getRow();
				curCol = stack.peek().getCol();
				stack.pop();
			}
		}
		//set up start and finish
		maze[0][0].setWest(false);
		
		maze[rows-1][columns-1].setEast(false);
		
		maps.add(maze); //add maze to array list
	}
	
	//parameters: maze grid of nodes
	//return true if there are unvisited nodes, false if not
	public boolean hasUnvisited(Node[][] maze) {
		for(int i = 0; i<maze.length; i++) {
			for(int j = 0; j<maze[i].length; j++) {
				if(!maze[i][j].isVisited())
					return true;
			}
		}
		return false;
	}
	
	//parameters: maze grid of nodes, rew of current node, column of current node
	//return true if there are unvisited neighbors, false if not
	public boolean hasUnvisitedNeighbor(Node[][] maze, int row, int col) {
		
		//checks all neighbors and returns true if any is found
		//nested ifs are for outofbounds prevention
		if(col < maze[row].length-1) {
			if(!maze[row][col+1].isVisited()) {
				return true;
			}
		}
		if(row < maze.length-1) {
			if(!maze[row+1][col].isVisited()) {
				return true;
			}
		}
		if(row > 0) {
			if(!maze[row-1][col].isVisited()) {
				return true;
			}
		}
		if(col > 0) {
			if(!maze[row][col-1].isVisited()) {
				return true;
			}
		}
		//nothing was unvisited, return false
		return false;
	}
	//parameters: x coordinate of mouse, y coordinate of mouse
	public void setcoords(int x, int y) {
		this.mouseX = x;
		this.mouseY = y;
	}
	
	//return mouse position
	public int getX() {
		return this.mouseX;
	}
	
	//return mouse position
	public int getY() {
		return this.mouseY;
	}
	
	//return true if mouse is within 2 pixels of the player position
	public boolean atStart () {
		if (2 > Math.abs(mouseX - startX) && 2 > Math.abs(mouseY - startY)){
			return true;
		} else {
			return false;
		}
	} 
	//parameters: x coordinate of mouse, y coordinate of mouse
	//return -1 if player reached the finish, 1-4 if player hit a wall, and 0 if player hits nothing
	public int checkState(int x, int y) {
		Node[][] maze = maps.get(0);
		int trueX = Math.floorDiv(x-10, 20); //node position of mouse
		int trueY = Math.floorDiv(y-10, 20); 
		
		if(trueX == maze.length-1 && trueY == maze.length-1) { //if player reached the finish
			return -1;
		}
		
		if (trueX < 0 || trueX > maze.length-1 || trueY < 0 || trueY > maze.length-1) { //if player got out of bounds
			return 5;
		}
		
		if (maze[trueY][trueX].isNorth()) { //if player hit a north wall
			if (y <= (trueY)*20 + 12) {
				return 1;
			}
		}
		
		if (maze[trueY][trueX].isSouth()) { //if player hit a south wall
			if (y+8 >= (trueY+1)*20 + 8) {
				return 2;
			}
		}
		
		if (maze[trueY][trueX].isWest()) { //if player hit a west wall
			if (x <= (trueX)*20 + 12) {
				return 3;
			}
		}
		
		if (maze[trueY][trueX].isEast()) { //if player hit an east wall
			if (x+8 >= (trueX+1)*20 + 8) {
				return 4;
			}
		}
		
		return 0; //if player has not finished or collided with anything 
	}
	
	//paints everything being displayed onto a canvas
	public void paint(Graphics g) {
		if(!allDone) { //if not all players have finished
			Node[][] maze = maps.get(0);
			g.setColor(Color.BLACK);
			g.drawString("Player " + (playerid+1) + ": " + ((int)(time*10)/10.0) + " seconds", 230, 530); //display player id and time
			int x=10,y=10;
			g.setColor(Color.GREEN);
			g.fillRect((20*rows)-10, (20*columns)-10, 23, 20); //draw finish line
			
			if (!isPlaying) { //if player is not playing (generally after hitting a wall)
				//draw player object separated from mouse object
				g.setColor(Color.blue);
			    g.fillOval(startX, startY, 10, 10);
			    //make mouse object transparent
			    g.setColor(new Color(200, 0, 0, 50));
			    g.fillOval(mouseX, mouseY, 10, 10);
			} else { //if player is playing
				//just draw mouse object
				g.setColor(Color.red);
			    g.fillOval(mouseX, mouseY, 10, 10);
			}
			
			if(rivals != null) { //if rivals exist
				Color[] c = {new Color(255,200,0,65),new Color(0,255,0,100),new Color(0,255,255,100)}; //list of possible rival colors
				for(int i =0; i < rivals.length; i++) { //draw each rival at their current position
					g.setColor(new Color(7,8,9));
					g.setColor(c[i%rivals.length]);
					g.fillOval(rivals[i].x, rivals[i].y, 10, 10);
				}
			}
			
			int ColValue = this.checkState(mouseX, mouseY); //status of player collision
			
			if (ColValue > 0 && isPlaying) { //if player has hit a wall
				isPlaying = false; //player is not playing
				switch (ColValue) { //set coordinate values for player object
				case 1:
					startX = mouseX;
					startY = mouseY+2;
					break;
				case 2:
					startX = mouseX;
					startY = mouseY-2;
					break;
				case 3:
					startX = mouseX+2;
					startY = mouseY;
					break;
				case 4:
					startX = mouseX-2;
					startY = mouseY;
					break;
				case 5: 
					startX = 15;
					startY = 15;
					break;
				} 
				
			}else if(ColValue == -1 && isPlaying) { //else if player reached the finish
				isPlaying = false; //player is not playing
				isDone = true; //player is done
				startX = mouseX; //player object will remain in the finish area
				startY = mouseY;
				//TODO send final time to server
			}
			else if (this.atStart() && !isDone) { //else if mouse is in the same location as player
				isPlaying = true;
			}
			
			g.setColor(Color.BLACK);
			
			//draw the maze
			for(int i=0; i<rows; i++) {
				x = 10; //set x coord to top row
				for(int j=0; j<columns; j++) {
					if(maze[i][j].isNorth()) {
						g.fillRect(x, y, 23, 3); //draw north wall
					}
					if(maze[i][j].isSouth()) {
						g.fillRect(x, y+20, 23, 3); //draw south wall
					}
					if(maze[i][j].isEast()) {
						g.fillRect(x+20, y, 3, 23); //draw east wall
					}
					if(maze[i][j].isWest()) {
						g.fillRect(x, y, 3, 23); //draw west wall
					}
					x += 20; //increment x coord to one column right
				}
				y += 20; //increment y coord one row lower
			}
		}else { //if all players are finished
			g.setColor(Color.BLACK);
			//draw all players final times
			for(int i=0; i<finalTimes.length; i++) {
				g.drawString("Player " + (i+1) + ": " + ((int)(finalTimes[i]*10)/10.0) + " seconds", 200, (i*50) + 100);
			}
		}
	}
	
	//increase player timer by 0.1 seconds
	public void updateTimer() {
		time+=0.1;
	}
	
	//return player timer
	public double getTime() {
		return time;
	}
	
	//return player x position
	public int getMazeX() {
		return startX;
	}

	//return player y position
	public int getMazeY() {
		return startY;
	}

	//set Point array of rivals
	public void setRivals(Point[] rivals) {
		this.rivals = rivals;
	}
	
	//return if player is done
	public boolean isDone() {
		return isDone;
	}
	
	//set array of final times and all players have finished
	public void setFinalTimes(double[] times) {
		finalTimes = times;
		allDone = true;
	}
	
	//set player id
	public void setPlayerid(int id) {
		playerid=id;
	}

}
