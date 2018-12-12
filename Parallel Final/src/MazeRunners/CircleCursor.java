package MazeRunners;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class CircleCursor extends JComponent
{
  public int x;
  public int y;
 
  
  public CircleCursor() {
      //this.setBackground(Color.BLUE);
  }

  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    //String s = x + ", " + y;
    g.setColor(Color.red);
    g.fillOval(x-10, y-40, 10, 10);
  }
}
