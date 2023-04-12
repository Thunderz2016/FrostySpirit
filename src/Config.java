import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
//import java.nio.file.Files;
import java.util.Scanner;


public class Config {

	public static String currentGame = "A Game";

	public static String callBot = "@frostyspirit_irc";

	public static String channel = "#thunderz2016";

	public static Boolean debugMode = false;
	public static Boolean debugWithFC = false;
	public static Boolean debugWithWelcome = false;
	public static Boolean crashed = false;

	public static File cache = new File("TextFiles" + File.separator + "cache.dat");

	public static void main(String[] args) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				System.out.println("[Terminal] Exiting. Clearing session cache...");
				cache.delete();
			}
		}, "Shutdown-thread"));

		

		System.out.println("FrostySpirit Version " + TwitchBot.FSVERSION + " (" + TwitchBot.BUILDNUM + ", Java "
				+ System.getProperty("java.version") + ")");
		checkEncoding();
		crashRecovery();
		isDebugging();

		//define the bot
		TwitchBot bot = new TwitchBot();
		bot.setVerbose(true);
		
		connect(bot, "irc.twitch.tv", 6667, "oauth:");

	}

	public static void connect(TwitchBot bot, String server, int port, String password) throws Exception {
		Scanner read = new Scanner(System.in);
		
		try {
			System.out.println("[Terminal] Connecting to " + server + ":" + port); 
			bot.connect(server, port, password);
			bot.joinChannel(channel);
			bot.getName();
	
		} catch (ConnectException e) {
			System.out.println("\n" + e);
			System.out.println("[Terminal] Cannot connect to IRC server: " + server);
			
			int option = 0;
			do {
				System.out.println("Next step? [1] Retry [2] Specify a different server [3] Skip, control Ori offline [4] Quit");
				option = read.nextInt();
				
				switch(option) {
					case 1: {
						System.out.println("[Terminal] Retrying connection to " + server + "\n");
						connect(bot, server, port, password);
					}
					break;
					case 2: {
						System.out.print("[Terminal] Enter an IRC server:");
						server = read.nextLine();
						System.out.print("[Terminal] Enter the port number of the IRC server:");
						port = read.nextInt();
						System.out.print("[Terminal] Enter the password for the server:");
						password = read.nextLine();
						System.out.println("[Terminal] Connecting to IRC server: " + server + ":" + port + ":" + password);
						bot.connect(server, port, password);
					}
					break;
					case 3: {
						bot.FC.dispose();
						//FrostyCanvasOffline fco = new FrostyCanvasOffline("Animation_01_Idle_Edit0Final_Green.gif");
						FrostyCanvasOffline.run();
						//System.out.println("[Terminal] FrostySpirit is now running offline. Type in the animation name below to control.");
					}
					break;
					case 4: System.exit(0);
					break;
				}

			} while(!read.hasNextInt() || option < 0 || option > 5);
			read.close();
		}
	
	}

	public static void isDebugging() throws FileNotFoundException, InterruptedException
	{
		String debug = checkDebugStatus();
		if(debug != null)
		{
			debugMode = true;
			if(debug.contains("fc"))
				debugWithFC = true;
			if(debug.contains("wc"))
				debugWithWelcome = true;
		}
		
		if(debugMode)
		{
			System.out.println("[Terminal] CAUTION! DEBUG MODE ENABLED! Some features may be unavailable. Press Ctrl+C to terminate the program anytime.");
			if(debugWithFC)
				System.out.println("[Terminal] Force-enabled feature: Ori Stream Pet (FrostyCanvas).");
			if(debugWithWelcome)
				System.out.println("[Terminal] Force-enabled feature: \"Connected\" Chat Notification.");
			System.out.println("[Terminal] FrostySpirit will launch in 3 seconds...");
			Thread.sleep(3000);
		}
	}
	
	public static String checkDebugStatus() throws FileNotFoundException
	{
		File DebugStatusFile = new File("TextFiles" + System.getProperty("file.separator") + "debugging.txt");

		if (!DebugStatusFile.isFile())
			return null;

		else
		{
			Scanner readDebugStatus = new Scanner(DebugStatusFile);
			String debugExtra = "";
			if(readDebugStatus.hasNextLine())
			{			
				debugExtra = readDebugStatus.nextLine();
				readDebugStatus.close();
			}
			return debugExtra;
		}
	}

	public static void checkEncoding()
	{
		String encoding = System.getProperty("file.encoding");
		System.out.println("[Terminal] Current File Encoding: " + encoding);
		if(!encoding.equalsIgnoreCase("UTF-8"))
		{
			System.out.println("WARNING: The JVM is not using UTF-8 encoding. Unexpected errors may occur!!");
			System.out.println("Please use the \"-Dfile.encoding=utf-8\" attribute to launch the JAR file.");
			System.out.println("To ignore this warning and continue, press Enter; otherwise, press Ctrl+C.");

			Scanner enter = new Scanner(System.in);
			enter.nextLine();
			enter.close();
		}

	}

	public static void crashRecovery() throws IOException, InterruptedException {
		if(cache.createNewFile())
			System.out.println("[Terminal] Crash recovery cache created. Path: " + cache.getAbsolutePath());
		else {
			System.out.println("[Terminal] It seemed like FrostySpirit did not shut down correctly last time. Restore last session? (Y/N)");
			Scanner read = new Scanner(System.in);

			if(read.nextLine().equalsIgnoreCase("y")) {
				crashed = true;		// Signal that FrostySpirit has recovered data from last session
				Scanner readFile = new Scanner(cache);
				while(readFile.hasNextLine()) {
					TwitchBot.usersGreeted.add(readFile.nextLine());
				}

				System.out.println("[Terminal] Recovered from last session. Launching...");
				readFile.close();
				Thread.sleep(1500);

			} else if(read.nextLine().equalsIgnoreCase("n")) {
				System.out.println("[Terminal] Launching a new session...");
				Thread.sleep(1000);
			}
			read.close();
		}
	}		
		
} 
