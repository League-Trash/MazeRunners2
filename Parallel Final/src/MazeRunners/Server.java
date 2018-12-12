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
	
	private final int TICKRATE = 1;
	
	private String password;
	private static ArrayList<Session> clients = new ArrayList<Session>();
	private static Point[] players;
	private Maze maze;
	public Server(){
		Scanner keysc = null;
		try {
			keysc = new Scanner(new File("ServerSecret.config"));
			password=keysc.nextLine();
		} catch (FileNotFoundException e) {
			System.out.println("WARNING, ServerSecret not found, using default connection configs.");
			password = "asdf";
		}
		
		
		players = new Point[4];
		maze = new Maze(25,25);
		maze.generateMaze();
		
		ServerSocket ss=null;
		try {
			ss = new ServerSocket(1338);
			for(int i =0; i<4; i++)
				new Thread(new Session(ss.accept(),i)).start();
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private class Session implements Runnable{
		private Socket sock;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		private int id;
		public Session(Socket s, int num){
			id=num;
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

		@Override
		public void run() {
			
			synchronized(clients) {
				if(clients.size() < 4) {
					try {
						clients.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					clients.notifyAll();
				}
			}
			try {
				out.writeObject(maze);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double average = 0;
			int size = 0;
			while(true){
				Point[] rivals = new Point[3];
				int bump = 0;
				for(int i =0; i< rivals.length; i++) {
					if (i==id) {
						bump =1;
					}
					rivals[i] = players[i+bump];
				}
				
				try {
					out.writeObject(rivals);
					players[id] = (Point)in.readObject();
					double lag = ((int)(System.currentTimeMillis()%5000L)-players[id].getX());
					if (lag < 0)
						lag = 5000+lag;
					size++;
					average = (average*(size-1)+lag)/size;
					System.out.println(average);
					Thread.sleep(TICKRATE);
				} catch (IOException | ClassNotFoundException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
				
			}
		}
	}
	
	public static void main(String args[]) {
		new Server();
	}
}
