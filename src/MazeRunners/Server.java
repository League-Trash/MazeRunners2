package MazeRunners;
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
	private String password;
	private static ArrayList<Session> players = new ArrayList<Session>();
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
		
		
		
		maze = new Maze(25,25);
		maze.generateMaze();
		
		ServerSocket ss=null;
		try {
			ss = new ServerSocket(1338);
			while(true)
				new Thread(new Session(ss.accept())).start();
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private class Session implements Runnable{
		private Socket sock;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		public Session(Socket s){
			sock=s;
			try {
				out=new ObjectOutputStream(sock.getOutputStream());
				in=new ObjectInputStream(sock.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			players.add(this);
		}

		@Override
		public void run() {
			
			synchronized(players) {
				if(players.size() < 4) {
					try {
						players.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					players.notifyAll();
				}
			}
			try {
				out.writeObject(maze);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	public static void main(String args[]) {
		new Server();
	}
}
