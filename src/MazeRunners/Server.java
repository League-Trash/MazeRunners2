package MazeRunners;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
	//Rate in ms at which the Server updates the clients
	private final int TICKRATE = 1;
	
	private static ArrayList<Session> clients = new ArrayList<Session>();
	private static Point[] players;
	private static double[] times = new double[4];
 	private static int finished = 0;//number of players who have finished the maze
	private static int livecount = 0;//number of players still connected to the server
	private Maze maze;
	public Server(){

		
		players = new Point[4];
		
		
		ServerSocket ss=null;
		try {
			ss = new ServerSocket(1338);
			System.out.println("Server ready");
			while(true)
				new Thread(new Session(ss.accept())).start();//spin off incoming connections as Session threads
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	//Manages individual connections to clients
	private class Session implements Runnable{
		private Socket sock;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		private int id;
		public Session(Socket s){
			sock=s;
			try {
				out=new ObjectOutputStream(sock.getOutputStream());
				in=new ObjectInputStream(sock.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			clients.add(this);
		}
        
        //main loop for each client
		@Override
		public void run() {
			while(true) {
				synchronized(clients) {
					id=livecount;
					livecount++;
					//wait until four players have joined
					if(clients.size() < 4) {
						try {
							clients.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						clients.notifyAll();
					}
					
				//prep and send Maze
					if (maze==null) {
						maze = new Maze(25,25);
						maze.generateMaze();
					}
				}
				try {
					maze.setPlayerid(id);
					out.writeObject(maze);
				} catch (IOException e) {//if a player disconnects
					livecount--;
					clients.remove(this);
					return;
				}
				
				double average = 0;
				int size = 0;
				while(finished<livecount){//while players are still in the maze
					//get location of opponent players
					Point[] rivals = new Point[3];
					int bump = 0;
					for(int i =0; i< rivals.length; i++) {
						if (i==id) {
							bump =1;
						}
						if (players[i+bump]==null)
							rivals[i] = new Point(0,0);
						else
							rivals[i] = players[i+bump];
					}
					
					try {
						out.writeObject(rivals);//send opponent locations
						players[id] = (Point)in.readObject();//recieve location of player
						if(players[id].getX() == -999) {//check for "beat the maze" flag
							if (times[id]==0)
								finished++;
							times[id] = players[id].getY()/10.0;//read in time
						}
						
						Thread.sleep(TICKRATE);
					} catch (Exception e) {//if a player disconnects
						livecount--;
						clients.remove(this);
						return;
					}
					
				}
				
				synchronized(times) {
				try {
					out.writeObject(times);
				} catch (IOException e) {//if a player disconnects
					livecount--;
					clients.remove(this);
					return;
				}
				livecount--;
				if (livecount>0)
					try {
						times.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				else
					times.notifyAll();
				}
				times[id]=0;
				maze=null;
			}
		}
	}
	//main for starting the server
	public static void main(String args[]) {
		new Server();
	}
}
