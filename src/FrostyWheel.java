import java.util.*;
//import org.jibble.pircbot.*;

public class FrostyWheel extends RandomLinkCode
{
    public static Random rand = new Random();

	public static String channel = Config.channel;
    
    public static List<String> responses = new ArrayList<String>(); //Creates list for random responses

    public static Random random = new Random();

    public void frostyRaffle()
    {
        Scanner read = new Scanner(System.in);
		List<String> entries = new ArrayList<String>(); //creates list for entries
        String entry  = ""; //placeholder for entries			
        int n = 0; //counter for response randomizer
		int entryCount = 0; //counter for entries
		String again = "n";
		String confirm = "n";
			
			responses.clear();
			responses.add("Ah yes! The good ol' raffle time, never gets old. Let's do this.");
			n++;
			responses.add("We're doing the raffle box again? Heck yes! Now I am excited.");
			n++;
			responses.add("Feeling lucky or having trouble deciding? Raffle Box comes to rescue!");
			n++;
			int listPosition = rand.nextInt(n); //randomized position
			sendMessage(channel, responses.get(listPosition)); //Output randomly picked initial response
			sendMessage(channel, "Now we wait while Thunderz fills in the entries...");
					
			System.out.println("[Terminal] Please enter the entries below. Type '/end' to finish."); //prompt Caster to enter entries in the terminal 
			while(!entry.equalsIgnoreCase("/end")) //Keep receiving entries until "/end" is entered.
			{
				{
					entry = read.nextLine();    //Receive entries from the Caster.
					if(!entry.equalsIgnoreCase("/end"))
					{
						{
							entries.add(entry); //Add input to list
							entryCount++; //Increment counter for entries
						}
					}

					while(entryCount==0 && entry.equalsIgnoreCase("/end")) //If no entries have been entered, tell Caster to try again
					{
						System.out.println("[Terminal] Cannot start the raffle. No entries have been received.");
						System.out.println("[Terminal] Please try again below with at least one entry. Type \"/end\" to finish.");
						entry = read.nextLine();
						if(!entry.equalsIgnoreCase("/end"))	//Add all input to the list "entries" besides "/end"
						{
							entries.add(entry);
							entryCount++;

							while(!entry.equalsIgnoreCase("/end")) //Keep receiving entries until "/end" is entered
							{
								entry = read.nextLine();
								if(!entry.equalsIgnoreCase("/end"))
									entries.add(entry); //If the input is NOT "/end", add input to the entries list
							}
							break;
						}
					}
				}
			}

			sendMessage(channel, "Got it. Let's see what we'll get this time... The entries are the following: " + entries); 	//Send confirmation and entries to chat
			do //Output results to terminal and chat; ask for a re-draw afterwards
			{ 
				System.out.println("[Terminal] The raffle is now ready. Type 'y' to start; or any other character to abandon.");
				confirm = read.nextLine(); //Prompt the Caster to start drawing
					if(confirm.equalsIgnoreCase("y"))
					{
						listPosition = rand.nextInt(entryCount); //randomize entries.
						sendMessage(channel, "Our winner is... " + entries.get(listPosition) + "!"); //Output result
						System.out.println("[Terminal] The result is " + entries.get(listPosition) + ". Type 'y' to initiate a re-draw; or any other character to conclude this raffle.");
						again = read.nextLine(); //Receive input for whether or not to do a re-draw

						if (again.equalsIgnoreCase("y") && confirm.equalsIgnoreCase("y")) //if the response is "y", send confirmation to terminal and chat, then go back to the beginning of the loop
						{
							System.out.println("[Terminal] ********A re-draw has started.********");
							sendMessage(channel, "A re-draw has been initiated. Better luck this time!");
						}
					}
			}while(again.equalsIgnoreCase("y")); 

			
			
			if(entryCount != 0 && confirm.equalsIgnoreCase("y")) //If entries have been entered and Caster did not abandon the raffle, send end confirmation
			{
				sendMessage(channel, "That brings us to the end of this raffle! I hope you guys enjoyed it as much as I did! :D");
				System.out.println("[Terminal] The raffle is now concluded.");
			}

			if(!confirm.equalsIgnoreCase("y")) //Send confirmation if Caster abandons the raffle
			{
				sendMessage(channel, "Well... I guess Thunderz changed his mind. This raffle has been abandoned. :/");
				System.out.println("[Terminal] Raffle abandoned. Returning to idle state...");
			}
					
			read.close();
			
	}

}

