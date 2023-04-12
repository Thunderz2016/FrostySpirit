import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FrostyCanvasOffline extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    JPanel contentPane;
    JLabel imageLabel = new JLabel();
    
    public static Boolean fcRunning = true;

    public FrostyCanvasOffline(String imageName) {
        try {
            //setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    fcRunning = false;
                    System.out.println("[Terminal] The FrostyCanvas window has closed. Type \"/run\" to relaunch it.");
                }
            });
            contentPane = (JPanel) getContentPane();
            contentPane.setLayout(new BorderLayout());
            setSize(new Dimension(1920, 1000));
            setTitle("FrostySpirit " + TwitchBot.FSVERSION + " (Offline) - Ori Stream Pet - Idle");
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
        setTitle("FrostySpirit " + TwitchBot.FSVERSION + " (Offline) - Ori Stream Pet - " + status);
        System.out.println("[Terminal] Animation changed: " + status);
    }

    public static void main(String[] args) throws IOException
    {
        run();
    }

    public static void run() throws IOException
    {
        FrostyCanvasOffline FC;
        FC = new FrostyCanvasOffline("Animation_01_Idle_Edit0Final_Green.gif");        
		FC.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				fcRunning = false;
				System.out.println("[Terminal] You've closed the FrostyCanvas window. Type \"/run\" to relaunch it.");
			}
		});
        Scanner read = new Scanner(System.in);
        String newGif = "";
        List<String> validAnim = Arrays.asList("idle", "treat", "pet", "scared", "wave");
        System.out.println("********FrostySpirit " + TwitchBot.FSVERSION + " Local Terminal********");
        System.out.println("Welcome to the FrostyCanvas offline playground!");
        System.out.println("Here you can play around with the animations without using Twitch Chat.");
        System.out.println("To close the FrostyCanvas window, type \"/close\".");
        System.out.println("To change animation, please type its name below. (e.g. \"pet\")");
        do {
            newGif = read.nextLine();
            if (!newGif.equalsIgnoreCase("/end"))
            {
                if (validAnim.stream().anyMatch(newGif::contains)) {
                    if(fcRunning)
                    {
                        String path = getImagePath(newGif);
                        int duration = getDuration(newGif);
                        FC.changeGif(path, newGif.substring(0, 1).toUpperCase() + newGif.substring(1));
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    FC.changeGif(
                                            "OriAnim/Animation_01_idle_Edit0Final_Green.gif",
                                            "Idle");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, duration);
                    }

                    else
                        System.out.println("[Terminal] FrostyCanvas is not currently running. Type \"/run\" to launch it first.");
                }

                else if (newGif.equalsIgnoreCase("/close")) {
                    if (fcRunning) {
                        FC.dispose();
                        fcRunning = false;
                        System.out.println("[Terminal] FrostyCanvas is now closed. Type \"/run\" to launch it again.");
                    }

                    else
                        System.out.println(
                                "[Terminal] FrostyCanvas is not currently running. Type \"/run\" to launch it first.");
                }

                else if (newGif.equalsIgnoreCase("/run")) {
                    new FrostyCanvasOffline("Animation_01_Idle_Edit0Final_Green.gif");
                    fcRunning = true;
                    System.out.println("[Terminal] FrostyCanvas has been launched.");
                }

                else
                    System.out.println("[Terminal] Invalid animation name. Please check the spelling and try again.");
            }
        } while (!newGif.equalsIgnoreCase("/end"));
        System.exit(0);
        read.close();
    }

    public static String getImagePath(String pose) {
        String path = "";
        switch (pose) {
            case "wave":
                path = "OriAnim/Animation_02_Wave_Edit0Final_Green.gif";
                break;
            case "pet":
                path = "OriAnim/Animation_04_Hand_Edit0Final_Green.gif";
                break;
            case "scared":
                path = "OriAnim/Animation_03_Scare_Edit0Final_Green.gif";
                break;
            case "treat":
                path = "OriAnim/Animation_05_Apple_Edit02_Green.gif";
                break;
            case "idle":
                path = "OriAnim/Animation_01_idle_Edit0Final_Green.gif";
                break;
        }
        return path;
    }

    public static int getDuration(String pose) {
        int duration = 0;
        switch (pose) {
            case "wave":
                duration = 6800;
                break;
            case "pet":
                duration = 8000;
                break;
            case "scared":
                duration = 13000;
                break;
            case "treat":
                duration = 14000;
                break;
        }
        return duration;
    }
}
