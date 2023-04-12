
import java.util.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.jibble.pircbot.IrcException;

import java.io.File;
//import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
//import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.awt.event.*;

public class TwitchBot extends FrostyWheel {
	public static final String FSVERSION = "1.7.5";
	public static final String BUILDNUM = "Build 20220216";

	Boolean gameOngoing = false; // Determine if a rock paper scissors game is ongoing
	static Boolean oriRunning = false; // Determine if Ori(Stream Pet) is running
	public static Boolean msgNoti = false;	// Whether of not to play a sound on message
	public static Boolean isBox = false;	// Wehtehr or not Stream Pet is a box

	String channel = Config.channel;

	public static Random random = new Random(); // Declares a randomizer "random"

	public static List<String> responses = new ArrayList<String>(); // Create list for random responses
	public static Set<String> usersGreeted = new HashSet<String>(); // Create list for greeted users in a session
	public static List<String> usersLurking = new ArrayList<String>(); // Create list for users who are lurking (!lurk)
	public static List<String> greetQueue = new ArrayList<String>(); // The list of all the new users showed up during
																		// the delay timeframe

	public String user = ""; // placeholder for short names of regulars
	public String disconnect = "n"; // Confirmation String for disconnecting in hibernation mode
	public int digit;
	public String randomLinkCode = "";
	public String confirm = "";
	public String reconnect = "";
	public String player = ""; // Determine the player of the current ongoing rock paper scissors game
	public String ioexcep = "[Error] FrostyCanvas encountered a problem. (IOException) Last operation abandoned.";


	// public FrostyCanvas FC;

	RockPaperScissors RPS = new RockPaperScissors(); // Launch a rock paper scissors game instance
	SaveGuard saveGuard = new SaveGuard("TextFiles" + File.separator + "saveProfiles.txt", "TextFiles" + File.separator + "targetPaths.txt");
	ChatTest chatTest = new ChatTest();

	public TwitchBot() {

		this.setName("frostyspirit_irc");

		this.isConnected();

		if (!Config.debugMode || Config.debugWithFC) {
			FC = new StreamPet("Animation_01_Idle_Edit0Final_Green.gif"); // Launch FrostyCanvas with the idle
																			// animation
			FC.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					TwitchBot.oriRunning = false;
					System.out.println(
							"[Terminal] You've closed the Stream Pet window. Use the \"\\summon\" command in chat to relaunch it.");
					sendMessage(channel,
							"[System] Ori's render window closed by Caster. FrostySpirit will now operate in text-only mode.");
				}
			});
		}
	}

	// Sends a connected message.
	public void onConnect() {
		if (!Config.debugMode || Config.debugWithWelcome) {
			if(Config.crashed)
				sendMessage(Config.channel, "[System] FrostySpirit has recovered from last unexpected shutdown. Resuming last session.");
			else
				sendMessage(Config.channel,
					"[System] FrostySpirit is now connected and listening to the chat. Go talk to her!");
		}

		// Get the usernames of frequent viewers (include but not limited to regulars)
		// This is for determining the delay before greeting
		try {
			readOptOutGreeting();
			readFrequentViewers();
			refreshStats();
			saveGuard.loadProfiles(false);
		} catch (IOException e) {
			e.printStackTrace();
			sendMessage(channel, "[ERROR] Unable to import RPS stats and/or frequent viewer list.");
		}

	}

	public void onDisconnect() {
		// sendMessage(channel, "[System] FrostySpirit has left the chat.");
		System.out.println(
				"[Terminal] The bot has now left the chat. Ori may still be present until you close the terminal.");
		System.out.println("[Terminal] To reconnect, please press Enter.");
		Scanner reconnect = new Scanner(System.in);
		reconnect.nextLine();
		try {
			reconnect();
			this.joinChannel(channel);
		} catch (IOException | IrcException e) {
			System.out.println("[Terminal] Cannot reconnect. See stack trace for details.");
			e.printStackTrace();
		}
		reconnect.close();
	}

	// All commands and their responses
	public void onMessage(String channel, String sender, String login, String hostname, String message) {

		// Play a sound on message (toggleable)
		if(msgNoti && !sender.equalsIgnoreCase("frostyspirit_irc")) {
			File msgSound = new File("msgnoti.wav");
			try {
				Clip clip = AudioSystem.getClip();
				clip.open(AudioSystem.getAudioInputStream(msgSound));
				clip.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		user = getShortName(sender); // Replace a regular's username with their corresponding short name

		// If the user is not in the greeted user list and it's not the Caster themself
		if (!usersGreeted.stream().anyMatch(sender::contains) && !FilesProcess.optOutGreeting.stream().anyMatch(sender::contains)) 																														// list
		{
			// If the sender is not a frequent viewer (possibly isn't aware that
			// FrostySpirit is a bot),
			// greet sender after 4-6 seconds
			if (!allTimeGreetedUsers.stream().anyMatch(sender::contains) && !FilesProcess.optOutGreeting.stream().anyMatch(sender::contains)) {
				try {
					strangerGreetingsTimer(sender);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else // Otherwise(the sender is a frequent viewer and aware that FrostySpirit is a
					// bot),
					// greet sender immediately on their first message
			{
				try {
					sendRegularGreetings(user);
					if(user.equals(sender) && new Random().nextInt(5) != 0)
						sendMessage(channel, "TIPS: Want me to call you by a different name? Try <!nickname + your preferred name>!");
				} catch (ParseException e) {
					e.printStackTrace();
				}
				usersGreeted.add(sender);
				try {
					FileWriter cacheUser = new FileWriter(Config.cache, true);
					cacheUser.write(sender + System.lineSeparator());
					cacheUser.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		if (usersLurking.stream().anyMatch(sender::contains)) // If the user was lurking before the message
		{
			usersLurking.remove(sender);
			sendMessage(channel, "Welcome back, " + user + "! Hope you enjoyed the lurk."); // Send welcome back message

			try {
				FC.changeGif(StreamPet.waveGif, "Waving");
			} catch (IOException e) {
				e.printStackTrace();
			}
			FC.returnIdle(6800); // After 6.8 seconds, return to idle animation
		}

		// Perform command check only if the message seems like a command
		if (message.contains("\\") || message.contains("!") || message.contains(Config.callBot) || gameOngoing) 
		{
			if (message.startsWith("\\halt") && sender.equalsIgnoreCase("thunderz2016")) {
				sendMessage(channel, "[System] All threads halted! Shutting down...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}

			else if (message.contains("!lurk")) {
				if (usersLurking.stream().anyMatch(sender::contains)) // If the user isn't already lurking
				{
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							usersLurking.add(sender); // Add the user to the lurking users list after 7 minutes
						}
					}, 420000);
				}

				sendMessage(channel, user + " is now lurking. Take care, we'll miss you... 👋");
			}

			else if (message.startsWith("\\version")) {
				sendMessage(channel,
						"[System] FrostySpirit " + FSVERSION + " (" + BUILDNUM + "; PircBot " + VERSION + ", Java "
								+ System.getProperty("java.version") + ", Host OS: " + System.getProperty("os.name")
								+ " (" + System.getProperty("os.version") + "))");
			}

			else if (message.startsWith("\\summon") && !oriRunning) {
				FC = new StreamPet("Animation_01_Idle_Edit0Final_Green.gif");
				FC.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						TwitchBot.oriRunning = false;
						System.out.println(
								"[Terminal] You've closed the FrostyCanvas window. Use the \"\\summon\" command in chat to relaunch it.");
						sendMessage(channel, "[System] Ori's render window is closed. Entering text-only mode.");
					}
				});
				oriRunning = true;
				sendMessage(channel, "[System] Ori has been summoned.");
			}

			else if (message.startsWith("\\exitori") && oriRunning) {
				FC.dispose();
				oriRunning = false;
				sendMessage(channel, "[System] Ori has stopped rendering. FrostySpirit will now enter text-only mode.");
				System.out.println("[Terminal] Stream Pet (\"OpenJDK Platform binary\" process) is now closed.");
			}

			else if (message.startsWith("\\hidden") && oriRunning
					&& (sender.equalsIgnoreCase("thunderz2016") || sender.equalsIgnoreCase("john_the_pizza_eater"))) {
				try {
					FC.changeGif(StreamPet.hiddenGif, "Hidden");
					sendMessage(channel, "[System] Ori is now hidden.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Override animation to idle
			else if (message.startsWith("\\idle") && oriRunning
					&& (sender.equalsIgnoreCase("thunderz2016") || sender.equalsIgnoreCase("john_the_pizza_eater"))) {
				try {
					FC.changeGif(StreamPet.idleGif, "Idle");
					sendMessage(channel, "[System] Ori status overridden: Idle");
				} catch (IOException e) {
					e.printStackTrace();
					sendMessage(channel, ioexcep);
				}
			}

			// Override animation to "Scared"
			else if (message.startsWith("\\scared") && oriRunning
					&& (sender.equalsIgnoreCase("thunderz2016") || sender.equalsIgnoreCase("john_the_pizza_eater"))) {
				try {
					FC.changeGif(StreamPet.scaredGif, "Scared");
					sendMessage(channel, "[System] Ori status overridden: \"Scared\"");
				} catch (IOException e) {
					e.printStackTrace();
					sendMessage(channel,
							"[Error] FrostyCanvas encountered a problem. (IOException) Last operation abandoned.");
				}
				FC.returnIdle(12500); // 12 seconds later, return to idle animation
			}

			else if (message.startsWith("\\box") || message.startsWith("\\b o x")
					&& ((sender.equalsIgnoreCase("thunderz2016") || (sender.equalsIgnoreCase("john_the_pizza_eater"))))
					&& oriRunning) {
				isBox = true;
				responses.clear();
				responses.add("Ori status overri- Oh GOD not again!?");
				responses.add("OK who the heck is it this time!? DansGame");
				responses.add("What now!? DansGame");
				responses.add("Wait no- monkaS");
				responses.add("DansGame DansGame DansGame");
				String response = responses.get(random.nextInt(responses.size()));

				try {
					FC.changeGif(StreamPet.boxGif, "B O X");
					sendMessage(channel, response);
				} catch (IOException e) {
					e.printStackTrace();
					sendMessage(channel, ioexcep);
				}
				FC.returnIdle(300000, true); // 5 minutes later, return to idle animation
			}

			// Override animation to "Waving"
			else if (message.startsWith("\\wave") && oriRunning
					&& (sender.equalsIgnoreCase("thunderz2016") || sender.equalsIgnoreCase("john_the_pizza_eater"))) {
				try {
					FC.changeGif(StreamPet.waveGif, "Waving");
					sendMessage(channel, "[System] Ori status overridden: \"Waving\"");
				} catch (IOException e) {
					e.printStackTrace();
					sendMessage(channel, ioexcep);
				}
				FC.returnIdle(6800); // 6.8 seconds later, return to idle animation
			}

			else if (message.startsWith("\\next")
					&& (sender.equalsIgnoreCase("thunderz2016") || sender.equalsIgnoreCase("john_the_pizza_eater"))) {
				pickNext(); // Pick the next user in the queue
			}

			else if (message.startsWith("\\nextrandom")
					&& (sender.equalsIgnoreCase("thunderz2016") || sender.equalsIgnoreCase("john_the_pizza_eater"))) {
				pickRandom(); // Pick a random user in the queue
			}

			else if (message.startsWith("!pet") && oriRunning) {
				try {
					// FC.changeGif("C:\\Users\\Vincent\\Pictures\\FrostySpirit\\Animation_04_Hand_Edit0Final_Green.gif",
					// "Pet");
					FC.changeGif(StreamPet.petGif, "Pet");
				} catch (IOException e) {
					e.printStackTrace();
					sendMessage(channel,
							"[Error] FrostyCanvas encountered a problem. (IOException) Last operation abandoned.");
				}

				FC.returnIdle(8100); // 8.1 seconds later, return to the idle animation
			}

			else if (message.startsWith("!treat")) {
				try {
					FC.changeGif(StreamPet.eatGif, "Treat");
				} catch (IOException e) {
					e.printStackTrace();
				}

				FC.returnIdle(14000); // 14 seconds later, return to the idle animation
			}

			else if (message.startsWith("!nickname")) {
				String tokens[] = message.split(" ");
				if(message.equalsIgnoreCase("!nickname")) {
					if(FilesProcess.shortNames.containsKey(sender)) {
						sendMessage(channel, sender + ", your nickname is " + user);
					} else {
						sendMessage(channel, sender + ", you don't have a nickname yet. Use <!nickname nickname> to get one.");
					}
				} else if (tokens.length == 1) {
					sendMessage(channel, "[Error] Invalid format. Usage: <!nickname nickname> <!nickname -remove>");
				}  else if (tokens[1].equalsIgnoreCase("-remove")) {
					try {
						removeShortName(sender);
					} catch (IOException e) {
						sendMessage(channel, "[Error] Internal error. Action abandoned.");
						e.printStackTrace();
					}
				} else if (tokens.length > 1) {
					sendMessage(channel, "Got it. I will call you " + tokens[1] + " from now on.");
					try {
						recordNewShortName(sender, tokens[1]);
					} catch (IOException e) {
						sendMessage(channel, "[Error] Internal error occurred while recording nickname.");
						e.printStackTrace();
					}
				} 
			}

			else if (message.startsWith("\\resetqueue") && (sender.equalsIgnoreCase("thunderz2016"))) {
				queue.clear();
				sendMessage(channel, "[System] The queue has been reset.");
			}

			else if (message.startsWith("\\nextrandom") && sender.equalsIgnoreCase("thunderz2016")) {
				pickRandom(); // Randomly pick the next user from the queue
			}

			else if (message.startsWith("!queue")
					|| message.equalsIgnoreCase(Config.callBot + " who is in the queue")) {
				showQueue(); // Send the list of users in the queue to chat
			}

			else if (message.startsWith("!join")) {
				addUserToQueue(user); // Add the user who triggered the command to queue
			}

			else if (message.startsWith("!leave")) {
				removeUserFromQueue(user); // Remove user who triggered this command from queue
			}

			else if (message.equalsIgnoreCase(Config.callBot + " What can you do")) {
				sendMessage(channel,
						"Thank you for your interest! I will get a manual when I learn more cool and useful stuff. Please give me more time!");
			}

			else if (message.equalsIgnoreCase(Config.callBot + " What time is it")) {
				String time = new java.util.Date().toString();
				sendMessage(channel, "It's currently " + time + "! (Java.util.Date)");
			}

			else if (message.startsWith("!test")) {
				chatTest.send();
			}

			else if (message.startsWith("!winston") || message.startsWith("!soclose")) {
				responses.clear();
				responses.add("Jesus Christ... there goes my ears again.");
				responses.add("Oh my God here comes Vixx again... monkaS");
				responses.add("You guys sure do like to blow my eardrums up, eh? NotLikeThis");
				responses.add("So... is this the angriest man alive? 👀");
				try {
					FC.changeGif(StreamPet.scaredGif, "Scared");
					sendMessage(channel, responses.get(random.nextInt(responses.size())));
				} catch (IOException e) {
					e.printStackTrace();
				}
				FC.returnIdle(13000); // 13 seconds later, return to idle animation
			}

			else if (message.startsWith("!uptime")
					|| (message.equalsIgnoreCase(Config.callBot + " How long have we been streaming"))) {
				String upTimeRaw = ""; // Placeholder for not-converted original lines from ChronoUp.txt generated by
										// Snaz
				try {
					upTimeRaw = getUpTime(); // Read from ChronoUp.txt generated by Snaz
				} catch (FileNotFoundException e) {
					sendMessage(channel, "[System] Uptime currently unavailable. (FileNotFoundException)");
					e.printStackTrace();
				}

				// Get character for each digit in the uptime text file from Snaz
				char firstH = upTimeRaw.charAt(0);
				char secondH = upTimeRaw.charAt(1);
				char firstM = upTimeRaw.charAt(3);
				char secondM = upTimeRaw.charAt(4);
				char firstS = upTimeRaw.charAt(6);
				char secondS = upTimeRaw.charAt(7);

				if (secondH >= '0' && secondH <= '1' && firstH == '0') // Response for uptime under 2 hours
					sendMessage(channel, "We've been live for " + secondH + " hour, " + firstM + secondM
							+ " minutes, and " + firstS + secondS + " seconds. We are still just getting warmed up!");
				if (secondH >= '2' && secondH <= '4' && firstH == '0') // Response for uptime between 2-4 hours
					sendMessage(channel, "Things are just starting to get exciting! We've been live for " + secondH
							+ " hours, " + firstM + secondM + " minutes, and " + firstS + secondS + " seconds.");
				if (secondH >= '5' && secondH <= '8' && firstH == '0') // Response for uptime between 5-8 hours
					sendMessage(channel,
							"Hoo boy... it looks like we are on a marathon this time...👀 We've been live for "
									+ secondH + " hours, " + firstM + secondM + " minutes, and " + firstS + secondS
									+ " seconds.");
				if (secondH >= '9' || firstH >= '1') // Response for uptime more than 9 hours
					sendMessage(channel, "We are currently going on a record-breaking " + firstH + secondH + " hours, "
							+ firstM + secondM + " minutes, and " + firstS + secondS + " seconds!! PogChamp");
			}

			else if (message.startsWith("!fc")
					|| message.equalsIgnoreCase(Config.callBot + " show me the friend code")) {
				responses.clear();
				responses.add("Grouping up? The friend code is 0104-1188-8332. Have fun!");
				responses.add(
						"Anything is more fun with friends eh? The Friend code is 0104-1188-8332. Good luck, have fun!");
				responses.add(
						"Not sure if you guys are going to blow something up this time... but here you go: 0104-1188-8332.");
				responses.add("The Friend Code is 0104-1188-8332. Welcome to the abyss...");
				sendMessage(channel, responses.get(random.nextInt(responses.size()))); // Sends randomly picked response
																						// to chat
			}

			// This command flips a coin
			else if (message.equalsIgnoreCase(Config.callBot + "!coin")) {
				responses.clear();
				List<String> coin = new ArrayList<String>(); // Create list for coin sides
				coin.add("Tails");
				coin.add("Heads");
				int coinSide = random.nextInt(coin.size()); // Get result for the coin flip

				responses.add("It's " + coin.get(coinSide) + " this time.");
				responses.add(coin.get(coinSide) + ".");
				responses.add("You got " + coin.get(coinSide) + ".");
				sendMessage(channel, responses.get(random.nextInt(responses.size()))); // Send result to the chat.
			}

			else if (message.startsWith("!rpsstats")) {
				// Boolean hasPlayerName = message.substring(message.indexOf(" ") + 1) != null;
				// // Check if the string after the space actually contains an int
				String playerName = "";

				if (message.equalsIgnoreCase("!rpsstats")) {
					try {
						showTotalResults();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						sendMessage(channel, "[Error] FileNotFoundException caught while retriving data.");
					}
				}

				else {
					playerName = message.substring(message.indexOf(" ") + 1);
					if (playerName.equalsIgnoreCase("-playerlist"))
						sendMessage(channel, "All-time RPS player list: " + RockPaperScissors.playerList);

					else {
						try {
							showPersonalResults(playerName.toLowerCase(), true);
						} catch (IOException e) {
							e.printStackTrace();
							sendMessage(channel, "[Error] IOException caught while retriving data.");
						}
					}
				}
			}

			else if (message.startsWith("!myrpsstats")) {
				try {
					showPersonalResults(sender, false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// This command will start a rock paper scissors game
			else if (message.startsWith("!rockpaperscissors") || message.startsWith("!rps")) {
				//sendMessage(channel, "[System] Rock paper scissors is temporarily unavailable.");
				if (!gameOngoing) // If there's no game currently in progress
				{
					// RockPaperScissors RPS = new RockPaperScissors();
					player = sender; // Replace sender's username with a regular's short name
					try {
						sendStartNotice(player);
					} catch (IOException e) {
						e.printStackTrace();
					}
					gameOngoing = true;
				}

				else // If a game is in progress
				{
					String playerShortName = getShortName(player);
					if (sender.equals(player)) // If the user who triggers the command is the same as the current player
					{
						sendMessage(channel, playerShortName + ", you're still playing. Make your decision!");
						sendMessage(channel,
								"TIPS: You don't need start a new game (use !rps again) if there's a tie. Just like in real life.");
					} else
						sendMessage(channel, playerShortName + " is currently playing! Please wait for your turn.");
				}

			}

			else if ((message.startsWith("!rock") || message.startsWith("!paper") || message.startsWith("!scissors")
					|| message.startsWith("✊") || message.startsWith("✋") || message.startsWith("✌")) && gameOngoing
					&& sender.equalsIgnoreCase(player)) {
				try {
					startGame(message, sender.toLowerCase());
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (!RockPaperScissors.tie) // If the previous game did not result in a tie
					gameOngoing = false; // end the game (set status to close)
			}

			else if (message.startsWith("\\resetrps")
					&& (sender.equalsIgnoreCase("thunderz2016") || sender.equalsIgnoreCase("john_the_pizza_eater"))
					&& gameOngoing) {
				gameOngoing = false;
				sendMessage(channel, "[System] The rock paper scissors game has been reset.");
			}

			else if (message.startsWith("!dice") || message.equalsIgnoreCase("roll a dice")) {
				int dice = 1 + random.nextInt(6); // Random number between 1-6
				sendMessage(channel, user + ", you rolled a " + dice + ".");
			}

			else if (message.startsWith("!2dice")) {
				int d1 = 1 + random.nextInt(6);
				int d2 = 1 + random.nextInt(6);
				sendMessage(channel, user + ", you rolled " + d1 + " and " + d2 + ". Your total is " + (d1 + d2) + ".");
			}

			else if (message.startsWith("!3dice")) {
				int d1 = 1 + random.nextInt(6);
				int d2 = 1 + random.nextInt(6);
				int d3 = 1 + random.nextInt(6);
				sendMessage(channel, user + ", your rolls are " + d1 + ", " + d2 + ", and " + d3 + ". Your total is "
						+ (d1 + d2 + d3) + ".");
			}

			else if (message.startsWith("\\hibernate") && sender.equalsIgnoreCase("thunderz2016")) {
				Scanner read = new Scanner(System.in);
				sendMessage(channel, "[System] FrostySpirit is now hibernating.");
				System.out.println("[Terminal] Now hibernating.");
				System.out.println("[Terminal] Type 'y' to completely disconnect; or any other character to wake.");
				confirm = read.nextLine();
				while (!confirm.equalsIgnoreCase("y")) {
					confirm = read.nextLine();
				}
				if (confirm.equalsIgnoreCase("y")) {
					sendMessage(channel, "[System] FrostySpirit has resumed from hibernation.");
					System.out.println("[Terminal] Resumed from hibernation.");
				}
				read.close();
			}

			else

			if (message.startsWith("\\disconnect") && (sender.equalsIgnoreCase("thunderz2016"))) {
				sendMessage(channel, "[System] FrostySpirit has left the chat.");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("[Terminal] InterruptedException caught when disconnect.");
					e.printStackTrace();
				}
				disconnect();
			}

			if (message.startsWith("\\msgnoti") && (sender.equalsIgnoreCase("thunderz2016"))) {
				if(!msgNoti) {
					msgNoti = true;
					sendMessage(channel, "[System] Message notification sound enabled.");
				} else {
					msgNoti = false;
					sendMessage(channel, "[System] Message notification sound disabled.");
				}
			}

			else

			if(message.startsWith("!raffle") || message.equalsIgnoreCase(Config.callBot + " let's do a raffle"))
			{
				//This is a private command
				if (sender.equalsIgnoreCase("thunderz2016"))	//Check if the sender is the same as the caster
					frostyRaffle();
				else
				{
					responses.clear();
					responses.add("Easy there, only Thunderz is allowed to start a raffle.");
					responses.add("I apologize, but Thunderz doesn't let anybody else start a raffle excpet him...");
					responses.add("I didn't make the rules, but I can't start a raffle unless Thunderz's the one tells me to.");
					responses.add("Thunderz told me not to start anything unless he's the one calling... there's nothing I can do. I apologize.");
					responses.add("All right it's time for a raf- wait you are not Thunderz? DansGame");
					sendMessage(channel, responses.get(random.nextInt(responses.size()))); //Sends randomly picked response to chat
				}
			}
			
			else if(message.startsWith("!passcode"))
			{
				Boolean isInteger = isInteger(message.substring(message.indexOf(" ") + 1));	// Check if the string after the space actually contains an int
				if(!isInteger)
					sendMessage(channel, "[Error] Invalid format. Usage: !passcode <# of digits> (e.g. !passcode 8)");

				else
				{
					int digits = Integer.parseInt(message.substring(message.indexOf(" ") + 1));
					if(digits > 20)
						sendMessage(channel, "Sorry. My algorithm's too lazy to handle 20+ digits. FrankerZ");
					else if(digits == 0)
						sendMessage(channel, "Zero digit...? I see somebody really wants to see the world burn. CoolStoryBob");
					else
						sendLinkCode(generateLinkCode(digits, user));
				}
			}

			else if(message.startsWith("!lastcode"))
				showLastLinkCode(user);

			else if(message.startsWith("!saveguard")) {
				String tokens[] = message.split(" ");
				try {					
					saveGuard.runSaveGuard(tokens[1], tokens[2]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			else if(message.equalsIgnoreCase(Config.callBot + " fuck you"))
			{
				responses.clear();
				responses.add("Uhm... you sure do have an interesting taste...");
				responses.add("...You do realize my creator is actually a man, right?");
				responses.add("No thanks.");
				responses.add("Don't threaten me with a good time.");
				responses.add("Yeah... whatever girl.");
				responses.add("...I have standards.");
				sendMessage(channel, responses.get(random.nextInt(responses.size())));
			}

			else

			if(message.equalsIgnoreCase(Config.callBot + " what song is this") || message.startsWith("!nowplaying"))
			{
				String currentSong = "";
				String requestedBy = "";
				String correctedUserName = "";
				try {
					currentSong = getCurrentSong();	// Attempt to read from CurrentSong.txt
					requestedBy = getRequestedBy();	// Attempt to read from RequestedBy.txt
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					sendMessage(channel, "[Error] Unable to retrieve information. (FileNotFoundException)");
				}

				correctedUserName = requestedBy.substring(0, requestedBy.length() - 1).toLowerCase();	// Remove the space at the end of the file
				String user = "";
				user = getShortName(correctedUserName); // Replace username with regular's short name
				responses.clear();
				responses.add("This is " + currentSong + ". Requested by " + user + ".");
				responses.add("Currently playing " + currentSong + ", brought to you by " + user + ".");
				responses.add("Ah I know! It's " + currentSong + "! Requested by " + user + "!");
				sendMessage(channel, responses.get(random.nextInt(responses.size())));
			}

			else

			if(message.equalsIgnoreCase(Config.callBot + " who requested this song") || message.startsWith("!requestedby"))
			{
				String requestedBy = "";
				String correctedUserName = "";
				try {
					requestedBy = getRequestedBy();	//Attempt to read from RequestedBy.txt
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				correctedUserName = requestedBy.substring(0, requestedBy.length() - 1);	// Remove the space at the end of the file
				requestedBy = getShortName(correctedUserName.toLowerCase());	// Replace username with regular's short name
				responses.clear();
				responses.add("This song was brought to you by " + requestedBy + ".");
				responses.add("This song was requested by the one and only " + requestedBy + ".");
				responses.add(requestedBy + " requested this song.");
				sendMessage(channel, responses.get(random.nextInt(responses.size())));
			}
		}
	} 

	/**
	 * Send the randomly generated code to the chat.
	 * @param randomLinkCode The code that has been randomly 
	 * generated in the onMessage method.
	 */
	public void sendLinkCode(String randomLinkCode)
	{
		responses.clear();
		responses.add(user + ", your randomized code is " + randomLinkCode + ". No more keyboard-slamming... 👀");
		responses.add("Here's your code, " + user + ": " + randomLinkCode + ". Don't lose it.");
		responses.add(randomLinkCode + " is your code, " + user + ". I made it just for you.");
		responses.add("Your code is " + randomLinkCode + ", " + user + ". The safest passcode in the world! 4Head");
		responses.add("Here you go, the code's " + randomLinkCode + ", " + user + ".");
		sendMessage(channel, responses.get(random.nextInt(responses.size())));
	}
	
	public static boolean isInteger(String str) { 
		try {  
			Integer.parseInt(str);  
			return true;
		} catch(NumberFormatException e) {  
			return false;
		}
	}

	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		
	}
}
