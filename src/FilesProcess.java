import java.io.File;
import java.util.Scanner;
import java.util.Set;

import org.jibble.pircbot.PircBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class FilesProcess extends PircBot {
        
	File alltimeGreetedUsersFile = new File("TextFiles" + System.getProperty("file.separator") + "allTimeGreetedUsers.txt");
	File frequentViewersFile = (new File("TextFiles" + System.getProperty("file.separator") + "frequentViewers.txt"));
	File shortNameFile = new File("TextFiles" + File.separator + "shortNames.txt");

	public StreamPet FC;	// Declare placeholder for Stream Pet, may be modified by other classes
	//public SaveGuard saveGuard = new SaveGuard("TextFiles" + File.separator + "saveProfiles.txt", "TextFiles" + File.separator + "targetPaths");


    public static List<String> frequentViewers = new ArrayList<String>();	//	Create list for frequent viewers
	public static Set<String> allTimeGreetedUsers = new HashSet<String>();	//	Create list for all time greeted users
	public static List<String> optOutGreeting = new ArrayList<String>();	// Create list for users who don't want greetings

	public static Map<String, String> shortNames = new HashMap<String, String>();

	public static String cDrivePath = getCDrivePath();

	public static String getCDrivePath()
	{
		if(System.getProperty("os.name").contains("Linux"))
			return "~/.wine/drive_c/";
		else
			return "C:/";
	}

	    /**
	 * Returns the short name of a regular.
	 * <p>
	 * If a sender is not a regular, return their entire
	 * username instead.
	 * @param sender The sender of the message.
	 * @return The short name of a regular or the username of 
	 * a non-regular sender.
     * @throws FileNotFoundException
	 */
	public String getShortName(String sender)
	{
        Map<String, String> shortNames = null;
        try {
            shortNames = new FilesProcess().readShortNames();
        } catch (FileNotFoundException e) {
            System.out.println("[ERROR] The file containing short names (shortNames.txt) is missing.");
            e.printStackTrace();
        }
        
        if(!shortNames.containsKey(sender)) // If the sender is not in the Hashmap
            return sender;
        else
            return shortNames.get(sender);  
        
	}

    /**
	 * Read from Snaz's ChronoUp.txt file which contains
	 * the up time of the stream.
	 * @return The literal content of ChronoUp.txt in String
	 * @throws FileNotFoundException
	 */
	public static String getUpTime() throws FileNotFoundException	
	{
		File file = new File(cDrivePath + "Snaz/TextFiles/ChronoUp.txt");		//Define path for ChronoUp.txt
		Scanner scan = new Scanner(file);		//Scanner for reading text file

		String upTimeRaw = scan.nextLine();
		scan.close();
		return upTimeRaw;		//return the content of ChronoUp.txt
	}

	/**
	 * Read from Streamlabs Chatbot's CurrentSong.txt file for 
	 * current requested song playing.
	 * @return The content of CurrentSong.txt in String
	 * @throws FileNotFoundException
	 */
	public static String getCurrentSong() throws FileNotFoundException
	{
		File file = new File(System.getProperty("user.home") + "/AppData/Roaming/Streamlabs/Streamlabs Chatbot/Services/Twitch/Files/CurrentSong.txt");	// Define path for CurrentSong.txt
		Scanner scan = new Scanner(file);	// Scanner for reading text file

		String currentSong = scan.nextLine();
		scan.close();
		return currentSong;	// Return the content of CurrentSong.txt
	}

	/**
	 * Returns a String that contains the username of the user who
	 * requested the song that's playing.
	 * @return A String that contains the username of whoever the current
	 * song is requested by.
	 * @throws FileNotFoundException
	 */
	public static String getRequestedBy() throws FileNotFoundException
	{
		File file = new File(System.getProperty("user.home") + "/AppData/Roaming/Streamlabs/Streamlabs Chatbot/Services/Twitch/Files/RequestedBy.txt");	// Define the path for RequestedBy.txt
		Scanner scan = new Scanner(file);	// Scanner for reading text file

		String requestedBy = scan.nextLine();
		scan.close();
		
		return requestedBy;
    }
	
	/**
	 * Reads from frequentViewers.txt, adds all lines to {@code List<String> frequentViewers}
	 * and allTimeGreetedUsers.txt, adds all lines to {@code List<String> allTimeGreetedUsers}.
	 * <p>
	 * Usage for each file and list:
	 * <ul>
	 * <li> Frequent Viewers list is used to determine whether or not a user is possible to 
	 * be aware that FrostySpirit is a bot.
	 * </li>
	 * <li> All-time greeted users list is used to serve similar purposes. Two files will 
	 * eventually merge and only one will be kept in future versions.
	 * </li>
	 * <li> If a user is probably not aware that FrostySpirit is a bot, FrostySpirit will send
	 * a greeting message after 4-7 seconds(random) to achieve a more humanized approach. Otherwise,
	 * a greeting message will be sent immediately upon a user's first message of the session.
	 * @since FrostySpirit 1.0.7b
	 * @throws FileNotFoundException
	 */

    public void readFrequentViewers() throws FileNotFoundException
	{
		//Scanner readFreqViewers = new Scanner(new File("C:\\Users\\Vincent\\Documents\\CSC\\TwitchBot\\FrostySpirit_IRC\\src\\frequentViewers.txt"));	//	Read the txt file that contains frequent viewers' usernames
		Scanner readFreqViewers = new Scanner(new File(frequentViewersFile.getAbsolutePath()));
		Scanner readGreetedViewers = new Scanner(alltimeGreetedUsersFile);

		while(readGreetedViewers.hasNextLine())
		{
			String greetedUser = readGreetedViewers.nextLine();    	//	String for a single greeted viewer
			allTimeGreetedUsers.add(greetedUser);	// Add that viewer to the list
		}
		
		//System.out.println("[System] Frequent Viewers list successfully loaded. " + frequentViewers);
		readGreetedViewers.close();
		readFreqViewers.close();
	}

	public void readOptOutGreeting() throws FileNotFoundException {
		Scanner read = new Scanner(new File("TextFiles" + File.separator + "greetingOptOut.txt"));
		while(read.hasNextLine()) {
			optOutGreeting.add(read.nextLine());
		}
	}

	public Map<String, String> readShortNames() throws FileNotFoundException {
		Scanner read = new Scanner(new File("TextFiles" + File.separator + "shortNames.txt"));
		while(read.hasNextLine()) {
			String current = read.nextLine();
			String token[] = current.split(":");
			shortNames.put(token[0], token[1]);
		}
		
		return shortNames;
	}

	public void recordNewShortName(String user, String shortName) throws IOException {
		FileWriter write = new FileWriter(shortNameFile, true);
		if(shortNames.containsKey(user)) {
			shortNames.replace(user, shortName);
			List<String> fileContent = new ArrayList<>(Files.readAllLines(shortNameFile.toPath()));	// Store every line to a list
			for(int i = 0; i < fileContent.size(); i++) {
				if(fileContent.get(i).contains(user)) {	// If the current line is the one defines the current user
					fileContent.set(i, user + ":" + shortName);	// Replace with their new desired nickname
				}
			}
			Files.write(shortNameFile.toPath(), fileContent);
		}
		else {
			shortNames.put(user, shortName);
			write.append(user + ":" + shortName + System.lineSeparator());
		}
		write.close();
	}

	public void removeShortName(String user) throws IOException {
		if(!shortNames.containsKey(user))
			sendMessage(Config.channel, user + ", I don't think you have told me your nickname yet.");
		else {
			shortNames.remove(user);
			List<String> fileContent = new ArrayList<>(Files.readAllLines(shortNameFile.toPath()));
			for(int i = 0; i < fileContent.size(); i++) {
				if(fileContent.get(i).contains(user))
					fileContent.remove(i);
			}
			Files.write(shortNameFile.toPath(), fileContent);
			sendMessage(Config.channel, user + ", I have successfully removed your nickname. I'll call you by your full username from now on.");
		}
	}
}