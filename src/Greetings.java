//import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class Greetings extends RockPaperScissors
{
    private static String allGreets = "";
    private static Timer timer;
    private static Boolean greetTimer = false; // Determine if there's a greet timer running

    /**
	 * Send randomized greeting message to a regular in the chat
	 * when they send their first message of the session.
     * <p>
     * Greeting's content differs in these scenarios (checks perform
     * in the following order. Whatever passes first will execuate):
     * <ul>
     * <li>There is a 1% chance that a regular will get a special 
     * greeting as an easter egg.
     * </li>
	 * <li>If today's date is earlier than or equal to the 7th day 
     * of a month, 
     * greet user with the month's name. (e.g. "Happy April").
     * </li>
     * <li>There's a 50% chance that FrostySpirit will greet user with 
     * whatever day it is today. (e.g. "Happy Friday").
     * </li>
     * </ul>
     * If all checks above failed, send a generic greeting.
	 * 
	 * @since FrostySpirit 1.0
     * @param user The name of the user being greeted.
	 */
	public void sendRegularGreetings(String user) throws ParseException
	{
        LocalDate today = LocalDate.now();
        int currentDay = today.getDayOfMonth();
        int currentMonthNum = today.getMonthValue();
        int currentYear = today.getYear();
        String dateString = String.format("%d-%d-%d", currentYear, currentMonthNum, currentDay);
        Date date = new SimpleDateFormat("yyyy-M-d").parse(dateString);     //  System date for formatting

        int easterEggResponse = random.nextInt(99);  
		if(easterEggResponse == 69) {
            sendMessage(channel, "Hey uh " + user + "... I forgot my charger today. Can I borrow yours? 4Head");
            sendMessage(channel, "Congratulations, " + user + "! You just got the 1% secret greetings!");
        }
            
        else if(currentDay <= 7)    //  If day of month is earlier than the 7th
        {
            String month = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(date);   //  Current month's English name
            // Send new years wishes for the first 7 days of January
            if (month.equalsIgnoreCase("January")) {
                sendMessage(channel, "Hey " + user + "! Happy New Years!");
                return;
            }
            responses.clear();
            responses.add("Hello, " + user + "! Welcome to " + month + ".");
            responses.add("Yo " + user + "! Happy " + month + "!");
            responses.add("Hello and welcome to " + month + ", " + user + "!");
            responses.add("New month, new me! Happy " + month + ", " + user + "!");
            sendMessage(channel, responses.get(random.nextInt(responses.size())));
        }

        else if(random.nextInt(2) != 1) //  50% chance
        {
            String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);   //  String that indicates what day it is today
            responses.clear();
            responses.add("Welcome and happy " + dayOfWeek + ", " + user + "!");
            responses.add("Hey there, " + user + "! Happy " + dayOfWeek + "!");
            sendMessage(channel, responses.get(random.nextInt(responses.size())));
        }

		else
		{
			responses.clear();
			responses.add("Hello there, " + user + "!");
			responses.add("Welcome back, " + user + "!");
			responses.add("How's it going, " + user + "? Welcome!");
			responses.add("We've been expecting you, " + user + "! Hello there!");
			responses.add(user + " KonCha");
			sendMessage(channel, responses.get(random.nextInt(responses.size()))); //Sends randomly picked response to chat
        }
        
        if(TwitchBot.oriRunning)
        {
            try {
                if(TwitchBot.oriRunning)
                    FC.changeGif(StreamPet.waveGif, "Waving");
            } catch (IOException e) {
                e.printStackTrace();
            }
            FC.returnIdle(6800);	// After 6.8 seconds, return to idle animation
        }
        
    }
    
    /**
	 * Send a randomized greeting message to a stranger when they
	 * send their first message of the session. Then, add the new user
	 * to the all-time greeted user list.
	 * @since FrostySpirit 1.3.2
	 * 
	 * @param sender The sender of the message.
	 * 
	 * @throws IOException
	 */
	public void sendStrangerGreetings(String sender) throws IOException
	{
		responses.clear();
		responses.add("Oh hey new faces! Welcome, " + sender + "!");
		responses.add("Hey there " + sender);
		responses.add("Welcome " + sender + "!");
		responses.add("First time eh, " + sender + "? There's snacks in the lobby. 4Head");
		responses.add("Buckle up, " + sender + ". This ride's gonna get crazy. CoolCat");
        sendMessage(channel, responses.get(random.nextInt(responses.size()))); //Sends randomly picked response to chat
		
		allTimeGreetedUsers.add(sender);	//	Add the greeted use to the list of all the users previously greeted

		//	Write the list of all previously greeted users to a file
		/*for(int i = 0; i < allTimeGreetedUsers.size(); i++)
		{
			allTimeGreetedUsersString += allTimeGreetedUsers.get(i) + System.getProperty("line.separator");
        }*/
        
        
        if(TwitchBot.oriRunning)
        {
            try {
                if(TwitchBot.oriRunning)
                    FC.changeGif(StreamPet.waveGif, "Waving");
            } catch (IOException e) {
                e.printStackTrace();
            }
            FC.returnIdle(6800);	// After 6.8 seconds, return to idle animation
        }
    }

    public void strangerGreetingsTimer(String user) throws IOException
	{
		FileWriter recordGreetedUsers = new FileWriter(alltimeGreetedUsersFile, true);
		recordGreetedUsers.write(user + System.lineSeparator());
        recordGreetedUsers.close();

        String newSender = user;	//	Store the name of sender who triggered the greeting to avoid changes during the delay timeframe
		int greetDelay = random.nextInt(2000) + 4000;

		TwitchBot.greetQueue.add(newSender);		// Add sender to the greet queue
		TwitchBot.usersGreeted.add(newSender);	// Add sender to the greeted user list of the session
		
		allGreets = TwitchBot.greetQueue.get(0);	// Get the first sender from the greet queue
		for(int i = 1; i < TwitchBot.greetQueue.size(); i++)	// Then get the rest of the greet queue, separate each name with a comma
			allGreets += ", " + TwitchBot.greetQueue.get(i);
			
		//System.out.println("[Terminal] Greet Queue: " + greetQueue);
		//System.out.println("[Terminal] Greet String: " + allGreets);

		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run() {
				try {
					sendStrangerGreetings(allGreets);						
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					TwitchBot.greetQueue.clear();
					allGreets = "";
					greetTimer = false;
					timer.cancel();
					timer.purge();
				}
			}
		};

		if(!greetTimer)	// If there isn't a timer ongoing
		{
			greetTimer = true;
			timer = new Timer();	// Create a new timer
			timer.schedule(timerTask, greetDelay);	// Schedule for a new greeting delay timer
		} else {	// If a timer has already been started
			timer.cancel(); 
			timer.purge();		// Reset timer
			timer = new Timer();
			System.out.println("[Terminal] Timer reset successfully.");
			timer.schedule(timerTask, greetDelay);
		}
	}
    
    public static String getMonthName(int monthNum)
    {
        switch(monthNum)
        {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
        }
        return "month " + monthNum;
    }
}