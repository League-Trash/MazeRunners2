package MazeRunners;
import java.io.Serializable;

public class Node implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -40690401562303807L;
	private int row;
	private int col;
	private boolean visited;
	
	//these represent walls connected to the nodes
	private boolean north;
	private boolean south;
	private boolean east;
	private boolean west;
	
	public Node(int r, int c, boolean v) {
		row = r;
		col = c;
		visited = v;
		north = true;
		south = true;
		east = true;
		west = true;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public boolean isNorth() {
		return north;
	}

	public void setNorth(boolean north) {
		this.north = north;
	}

	public boolean isSouth() {
		return south;
	}

	public void setSouth(boolean south) {
		this.south = south;
	}

	public boolean isEast() {
		return east;
	}

	public void setEast(boolean east) {
		this.east = east;
	}

	public boolean isWest() {
		return west;
	}

	public void setWest(boolean west) {
		this.west = west;
	}


}
