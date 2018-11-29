package MazeRunners;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

public class Maze extends Canvas{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 5491802318380000478L;
	private int rows;
	private int columns;
	
	ArrayList<Node[][]> maps = new ArrayList<Node[][]>(); 
	
	public Maze(int r, int c) {
		rows = r;
		columns = c;
	}
	
	public void generateMaze() {
		//-1: unvisited, 0: empty, 1: wall
		Node[][] maze = new Node[rows][columns];
		Random rnd = new Random();
		Stack<Node> stack = new Stack<Node>();
		for(int i = 0; i<maze.length; i++) {
			for(int j = 0; j<maze[i].length; j++) {
				maze[i][j] = new Node(i,j,false);
			}
		}
		maze[0][0].setVisited(true); //visited
		int curRow = 0;
		int curCol = 0;
		int newRow;
		int newCol;
		int choice;
		while(hasUnvisited(maze)) {
			if(hasUnvisitedNeighbor(maze, curRow, curCol)) {
				while(true) {
					newRow = curRow;
					newCol = curCol;
					choice = rnd.nextInt(2);
					if(choice == 0)
						newRow = curRow + rnd.nextInt(3) - 1;
					else
						newCol = curCol + rnd.nextInt(3) - 1;
					
					//new node is actually a neighbor, and nothing is out of bounds
					if((newRow != curRow || newCol != curCol) && newRow >= 0 && newCol >= 0 && newRow < rows && newCol < columns) {
						if(!maze[newRow][newCol].isVisited()) {
							//System.out.println("Current: " + curRow + "," + curCol + " New: " + newRow + "," + newCol + "\n");
							break;
						}
					}
				}
				stack.push(maze[curRow][curCol]);
				if(newRow > curRow) {
					maze[curRow][curCol].setSouth(false);
					maze[newRow][newCol].setNorth(false);
				}
				else if(newRow < curRow) {
					maze[curRow][curCol].setNorth(false);
					maze[newRow][newCol].setSouth(false);
				}
				else if(newCol > curCol){
					maze[curRow][curCol].setEast(false);
					maze[newRow][newCol].setWest(false);
				}
				else {
					maze[curRow][curCol].setWest(false);
					maze[newRow][newCol].setEast(false);
				}
				curRow = newRow;
				curCol = newCol;
				maze[curRow][curCol].setVisited(true);
			}
			else if(!stack.isEmpty()) {
				curRow = stack.peek().getRow();
				curCol = stack.peek().getCol();
				stack.pop();
			}
		}
		maze[0][0].setWest(false);
		
		maze[rows-1][columns-1].setEast(false);
		
		maps.add(maze);
	}
	
	public boolean hasUnvisited(Node[][] maze) {
		for(int i = 0; i<maze.length; i++) {
			for(int j = 0; j<maze[i].length; j++) {
				if(!maze[i][j].isVisited())
					return true;
			}
		}
		return false;
	}
	
	public boolean hasUnvisitedNeighbor(Node[][] maze, int row, int col) {
		
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
		return false;
	}
	
	public void paint(Graphics g) {
		Node[][] maze = maps.get(0);
		for(int i=0; i<rows; i++) {
			//System.out.println();
			for(int j=0; j<columns; j++) {
				//System.out.print(maze[i][j].isNorth() + "" + maze[i][j].isEast() + maze[i][j].isSouth() + maze[i][j].isWest() + "   ");
			}
		}
		int x=10,y=10;
		g.setColor(Color.RED);
		g.fillRect(x, y, 20, 20);
		g.setColor(Color.GREEN);
		g.fillRect((20*rows)-10, (20*columns)-10, 23, 20);
		g.setColor(Color.BLACK);
		
		for(int i=0; i<rows; i++) {
			x = 10;
			for(int j=0; j<columns; j++) {
				if(maze[i][j].isNorth()) {
					g.fillRect(x, y, 23, 3);
				}
				if(maze[i][j].isSouth()) {
					g.fillRect(x, y+20, 23, 3);
				}
				if(maze[i][j].isEast()) {
					g.fillRect(x+20, y, 3, 23);
				}
				if(maze[i][j].isWest()) {
					g.fillRect(x, y, 3, 23);
				}
				x += 20;
			}
			y += 20;
		}
	}
}
