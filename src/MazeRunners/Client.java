package MazeRunners;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Client implements Runnable {
	private String serverip; 
	private String password;
	private final int MAXTRY=3;
	private JLabel label;
	private Maze maze;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	public Client() {
		Scanner keysc = null;
		try {
			keysc = new Scanner(new File("ClientSecret.config"));
			serverip=keysc.nextLine();
			password=keysc.nextLine();
		} catch (FileNotFoundException e) {
			System.out.println("WARNING, ClientSecret not found, using default connection configs.");
			serverip = "localhost";
			password = "asdf";
		}
		
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("src/titleIcon.png"));
		} catch (IOException e){
			System.out.println("File \"titleIcon.png\" not found.");
		}
		label = new JLabel("Waiting for players...");
		label.setHorizontalAlignment(label.CENTER);
		JFrame frame = new JFrame();
		frame.setSize(525, 570);
		frame.setTitle("Maze Game!");
		frame.setIconImage(img);
		frame.setLocation(450, 200);
		frame.add(label);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		int tries = 0;
		while(true)
			try {
				connect();
				break;
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				tries++;
				if (tries==MAXTRY)
				{
					System.out.println("RIP Connection");
					System.exit(1);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
			}
		label.setVisible(false);
		frame.add(maze);
		maze.repaint();
		frame.setSize(525, 569);
	}
	
	private void connect() throws UnknownHostException, IOException, ClassNotFoundException {
		Socket socket = new Socket(serverip, 1338);
        System.out.print("connection ");
        out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Ok");
        //out.writeObject(password);
        in = new ObjectInputStream(socket.getInputStream());
        maze = (Maze)in.readObject();
        new Thread(this).start();
	}
	
	@Override
	public void run() {
		while (true)
		{
			try {
				Point[] rivals = (Point[])in.readObject();
				out.writeObject(new Point((int)(System.currentTimeMillis()%5000L),0));//TODO Plug player location in here
				//TODO Paint location of Rivals
				//System.out.println("Client chatting!");
				
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				break;
			}
			
		}
	}
	
	public static void main (String args[]) {
		new Client();
	}
}
