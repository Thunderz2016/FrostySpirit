import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Timer;

public class StreamPet extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //  All-OS compatible GIF file paths
    public static String idleGif = "OriAnim" + System.getProperty("file.separator") + "Animation_01_Idle_Edit0Final_Green.gif";
    public static String waveGif = "OriAnim" + System.getProperty("file.separator") + "Animation_02_Wave_Edit0Final_Green.gif";
    public static String scaredGif = "OriAnim" + System.getProperty("file.separator") + "Animation_03_Scare_Edit0Final_Green.gif";
    public static String petGif = "OriAnim" + System.getProperty("file.separator") + "Animation_04_Hand_Edit0Final_Green.gif";
    public static String eatGif = "OriAnim" + System.getProperty("file.separator") + "Animation_05_Apple_Edit02_Green.gif";
    public static String hiddenGif = "OriAnim" + System.getProperty("file.separator") + "green.gif";
    public static String boxGif = "OriAnim" + System.getProperty("file.separator") + "WhiteBox.gif";
    
    JPanel contentPane;
    JLabel imageLabel = new JLabel();
    
    public StreamPet(String imageName) {
        try 
        {
            TwitchBot.oriRunning = true;
            contentPane = (JPanel) getContentPane();
            contentPane.setLayout(new BorderLayout());
            setSize(new Dimension(1920, 980));
            setTitle("FrostySpirit " + TwitchBot.FSVERSION +  " - Ori Stream Pet - Idle");
            // add the image label
            ImageIcon ii = new ImageIcon(this.getClass().getResource(imageName));
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("TaskbarIcon.png")));
            imageLabel.setIcon(ii);
            contentPane.add(imageLabel, java.awt.BorderLayout.CENTER);
            // display target GIF
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void changeGif(String imageName, String status) throws IOException
    {
        File file = new File(imageName);
        ImageIcon replacement = new ImageIcon(file.toURI().toURL());
        imageLabel.setIcon(replacement);
        setTitle("FrostySpirit " + TwitchBot.FSVERSION + " - Ori Stream Pet - " + status);
    }

    /**
	 * Return to Ori's idle animation after the duration of 
	 * the previously playing animation.
	 * @param duration The duration of the last animation, also the
	 * time in millisecond before returning to the idle animation.
	 */
	public void returnIdle(int duration)	// Return to the idle animation after a given time in milliseconds
	{
        new Timer().schedule(
			new TimerTask()
			{
				@Override
				public void run() {
					try {
                        if (TwitchBot.isBox) {
                            changeGif(boxGif, "B O X");
                        } else {
                            changeGif(idleGif, "Idle");
                        }
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, duration);
        }
    
    public void returnIdle(int duration, Boolean isBox) {
        new Timer().schedule(
			new TimerTask()
			{
				@Override
				public void run() {
					try {
                            changeGif(idleGif, "Idle");
                    } catch (IOException e) {
						e.printStackTrace();
					}
                    TwitchBot.isBox = false;
				}
			}, duration);
        }
}