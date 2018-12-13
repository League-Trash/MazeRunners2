package MazeRunners;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Client implements Runnable {
	private static final String SERVERIP = "localhost";
	private static final int CHECKSERVERTIME =3000;//time in milliseconds between attempts to reach the server
	
	private JFrame frame;
	private JLabel label;//displays waiting message
	private Maze maze;//the current maze being played on
	private ObjectOutputStream out;//used to send objects to the server
	private ObjectInputStream in;//used to read objects in from the server
	private static BufferedImage cursor_image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);//the icon the program uses
	private int threadCount = 0;//simple flag, 1 is the network flag, 2 is the timer thread. used in run()
	
	public Client() {
		
		//setup gui
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("src/titleIcon.png"));
		} catch (IOException e){
			System.out.println("File \"titleIcon.png\" not found.");
		}
		label = new JLabel("Waiting for players...");
		label.setHorizontalAlignment(label.CENTER);
		frame = new JFrame();
		frame.setSize(525, 570);
		frame.setTitle("Maze Game!");
		frame.setIconImage(img);
		frame.setLocation(450, 200);
		frame.add(label);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		//Try to connect to server
		while(true)
			try {
				connect();//connects to the server, reads in maze 
				break;
			} catch (IOException | ClassNotFoundException e) {
				
				System.out.println("Connection failed");
					
				try {
					Thread.sleep(CHECKSERVERTIME);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
			}
		
		//setup mouse controls
		maze.addMouseMotionListener(new MouseMotionAdapter() {
	        public void mouseMoved(MouseEvent me)
	        {
	          maze.setcoords(me.getX(), me.getY());
	          maze.repaint();
	        }
	      });
  	    Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursor_image, new Point(0, 0), "My Cursor");
  	    frame.setLocationRelativeTo(null);
  	    frame.setCursor(cursor);
  	    
		label.setVisible(false);
		frame.add(maze);
		maze.repaint();
		frame.setSize(525, 570);
		frame.setVisible(true);
	}
	
	//Commences connection with Server, begin network and timer threads (only called by constructor)
	private void connect() throws UnknownHostException, IOException, ClassNotFoundException {
		Socket socket = new Socket(SERVERIP, 1338);
        System.out.print("connection ");
        out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Ok");
        in = new ObjectInputStream(socket.getInputStream());
        maze = (Maze)in.readObject();
        new Thread(this).start();
        new Thread(this).start();
	}
	
	//show the players times after the race
	public void displayLeaderboard(double[] times) {
		maze.setFinalTimes(times);
		maze.repaint();
	}
	
	//handles networking OR the timer depending the thread
	@Override
	public void run() {
		threadCount++;//thread flag
		//the network section
		if(threadCount == 1) {
			while (true)
			{
					try {
						Object message = in.readObject();
						if(maze.isDone()) {//if the player reaches the finish
							try {
							    //transmit a "point" containing a special flag and the player's time
								out.writeObject(new Point(-999,(int) (maze.getTime()*10)));
							} catch (IOException e) {
								
								e.printStackTrace();
							}
						}
						//if message is a point array then it's the position of the other players
						if (message instanceof Point[]) {
							maze.setRivals((Point[])message);
							out.writeObject(new Point(maze.getMazeX(),maze.getMazeY()));//send player's location
						}
						//if message is a double array then it's the list of times (and the race is over)
						else if(message instanceof double[]) {
							double[] times = (double[]) message;
							displayLeaderboard(times);
							reset();
							break;//end thread
						}
						
						
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
						break;
					}
				
			}
		}
		//the timer section
		else if(threadCount == 2) {
			while(!maze.isDone()) {
				
				try {
					maze.updateTimer();
					maze.repaint();
					Thread.sleep(100);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//concludes current game and begins the next one
	private void reset() {
		try {
			Thread.sleep(10000);//wait 10 seconds to show player times
			frame.remove(maze);
			label.setVisible(true);//show waiting message
			
			//setup new maze
			maze = (Maze) in.readObject();
			maze.addMouseMotionListener(new MouseMotionAdapter() {
		        public void mouseMoved(MouseEvent me)
		        {
		          maze.setcoords(me.getX(), me.getY());
		          maze.repaint();
		        }
		      });
	        frame.add(maze);
	        frame.setVisible(true);
	        maze.setVisible(true);
	        label.setVisible(false);
	        
	        //start new network and timer threads
	        threadCount = 0;
	        new Thread(this).start();
	        new Thread(this).start();
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
			e.printStackTrace();
		} 
	}
	
	//main for starting the client
	public static void main (String args[]) {
		new Client();
	}
}
