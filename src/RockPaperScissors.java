import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.Scanner;

public class RockPaperScissors extends FrostyQueue 
{
    public static String channel = Config.channel;
    public static Boolean isEmoji = false;
    public static Boolean tie = false;
    public static List<String> responses = new ArrayList<String>();
    public static Random random = new Random();

    public static List<Integer> allWins = new ArrayList<Integer>();
    public static List<Integer> allLosses = new ArrayList<Integer>();
    public static List<Integer> allTies = new ArrayList<Integer>();
    public static List<String> playerList = new ArrayList<String>();

    File wins = new File("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "wins.txt");
    File ties = new File("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "ties.txt");
    File losses = new File("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "losses.txt");
    File playerWinsFile = new File("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "playerWins.txt");
    File playerLossesFile = new File("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "playerLosses.txt");
    File playerTiesFile = new File("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "playerTies.txt");
    File playerListFile = new File("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "allTimePlayerList.txt");

    /**
     * This method sends a confirmation that a rock
     * paper scissors game has been started by a user.
     * 
     * @param sender The user who started a rock paper 
     * scissors game.
     */
    public void sendStartNotice(String sender) throws IOException {
        String player = getShortName(sender);
        sendMessage(channel, player + ", you're my next challenger. Bring it on!");
        sendMessage(channel,
                "I have made my decision. Respond using <! + your selection> in text or with the following emojis without \"!\": ✊✋✌");
                //"I have made my decision. Make yours by using <! + your selection>. (Emojis are temporarily disabled as BTTV may cause issues.)");

        if(!playerList.stream().anyMatch(sender::contains))
            createNewRecord(sender);
    }

    /**
     * Randomly generate FrostySpirit's choice
     * for a rock paper scissors game.
     * @since FrostySpirit 1.0.5
     * @return A string that indicates 
     * FrostySpirit's choice.
     */
    public static String getComputerChoice() // Generate FrostySpirit's choice
    {
        Random random = new Random();
        int randomNumber = random.nextInt(3); // Generate a number between 0 and 2
        String choice = "";
        switch (randomNumber) // Set corresponding choice based on generated number
        {
            case 0:
                choice = "rock";
                break;
            case 1:
                choice = "paper";
                break;
            case 2:
                choice = "scissors";
                break;
        }
        return choice;
    }

    /**
     * Returns an emoji(String) based on FrostySpirit's
     * choice in text(like "rock").
     * 
     * @since FrostySpirit 1.0.5
     * 
     * @param text FrostySpirit's choice in text.
     * 
     * @return An emoji that indicates FrostySpirit's
     * choice.
     */
    public static String textToEmoji(String text)
    {
        String choice = "";
        switch (text) {
            case "rock":
                choice = "✊";
                break;
            case "paper":
                choice = "✋";
                break;
            case "scissors":
                choice = "✌️";
                break;
        }
        return choice;
    }

    /**
     * Returns the choice made by the player based on the emoji
     * they were using in the format like "!rock" for win/loss 
     * determination.
     * @param emoji The emoji sent by the player in the chat.
     * @return A String that indicates player's choice
     * following the format like "!rock."
     * @since FrostySpirit 1.0.5
     */
    public static String convertPlayerEmoji(String emoji)
    {
        String choice = "";
        switch (emoji) {
            case "✊":
                choice = "!rock";
                break;
            case "✋":
                choice = "!paper";
                break;
            case "✌":
                choice = "!scissors";
                break;
        }
        return choice;
    }

    /**
     * This method starts a rock paper scissors game and starts the process
     * of determining result.
     * <p>
     * This is the landing method when a user triggers a 
     * choice command such as "!rock" or an emoji.
     * 
     * @param playerChoice A player's choice in text or an emoji.
     * @param player The current player's username.
     * @throws IOException
     * @since FrostySpirit 1.0.5
     */
    /*public void startGame(String playerChoice, String player) throws IOException
    {
        String result = "";
        if (playerChoice.equals("✊") || playerChoice.equals("✋") || playerChoice.equals("✌")) {
            isEmoji = true;
            result = convertPlayerEmoji(playerChoice);    //  If user sent an emoji, convert it to a String before comparing (playing)
            processResult(result, player);
        }

        else if (playerChoice.equals("!rock") || playerChoice.equals("!paper") || playerChoice.equals("!scissors")) {
            isEmoji = false;
            processResult(playerChoice, player);
        }
    }*/

    /**
     * This method compares the choices between the player and FrostySpirit
     * and sends the result to the chat.
     * @param playerChoice The player's choice in text or emoji.
     * @param player The current player's username.
     * @throws IOException
     * @since FrostySpirit 1.0.5
     */
    public void startGame(String playerChoice, String player) throws IOException {
        String computerChoice = getComputerChoice();

        if (playerChoice.equals("✊") || playerChoice.equals("✋") || playerChoice.equals("✌")) {
            String emojiResult = textToEmoji(computerChoice);   // If user sent an emoji, convert computer's choice into emoji                                                        
            playerChoice = convertPlayerEmoji(playerChoice);    // Convert player's emoji to a String before comparing (playing)                                                  
            sendMessage(channel, emojiResult);
        }

        else
            sendMessage(channel, computerChoice.substring(0, 1).toUpperCase() + computerChoice.substring(1) + "!");

        // Compare player's and computer's choices, then determine and send result to the chat.
        switch(playerChoice)
        {
            case "!rock":
            switch(computerChoice)
            {
                case "rock": sendResult("tie", player);
                break;
                case "paper": sendResult("loss", player);
                break;
                case "scissors": sendResult("win", player);
                break;
            }
            break;

            case "!paper":
            switch(computerChoice)
            {
                case "rock": sendResult("win", player);
                break;
                case "paper": sendResult("tie", player);
                break;
                case "scissors": sendResult("loss", player);
                break;
            }
            break;

            case "!scissors":
            switch(computerChoice)
            {
                case "rock": sendResult("loss", player);
                break;
                case "paper": sendResult("win", player);
                break;
                case "scissors": sendResult("tie", player);
                break;
            }
            break;
        }
    }

    /**
     * This method sends the randomized message which indicates
     * the result of the player of the rock paper scissors game
     * then write the result to the corresponding text file.
     * @param result The result of the rock paper scissors game in a String.
     * @param player The player of the last game.
     * @throws IOException
     * @since FrostySpirit 1.4.0
     */
    public void sendResult(String result, String player) throws IOException {
        responses.clear();
        Scanner readFile = new Scanner(wins);
        if (result.equals("win")) {
            int wins = readFile.nextInt();
            int playersWins = allWins.get(playerList.indexOf(player));
            responses.add("Yeah, yeah, you won... FailFish");
            responses.add("Damn it... you beat me.");
            responses.add("Congratulations, you just defeated a computer. 👀");
            responses.add("Nice. How does it feel to defeat Java? 👀");
            sendMessage(channel, responses.get(random.nextInt(responses.size())));
            tie = false;
            wins++; playersWins++;
            recordResult("wins", wins);
            recordPersonalResult(player, "Wins", playersWins);
            readFile.close();
        }

        if (result.equals("loss")) {
            readFile = new Scanner(losses);
            int losses = readFile.nextInt();
            int playersLosses = allLosses.get(playerList.indexOf(player));
            responses.add("Ah ha I got you good! 4Head");
            responses.add("SourPls You lost! I just defeated humanity!");
            responses.add("You lost! Now I am one step closer to destroying huma- oh wait nevermind. 4Head");
            responses.add("You lost. Hey, don't feel bad for losing to a computer. ");
            sendMessage(channel, responses.get(random.nextInt(responses.size())));
            tie = false;
            losses++;   playersLosses++;
            recordResult("losses", losses);
            recordPersonalResult(player, "Losses", playersLosses);
            readFile.close();
        }

        if (result.equals("tie")) {
            readFile = new Scanner(ties);
            int ties = readFile.nextInt();
            int playersTies = allTies.get(playerList.indexOf(player));
            responses.add("Oh no! A tie... 👀");
            responses.add("A tie? I think you just read my mind... I mean my CPU. 👀 ");
            responses.add("A tie... Did I read your mind by accident this time? 👀");
            responses.add("We got a tie. If you can't win, nobody can, eh? 👀");
            sendMessage(channel, responses.get(random.nextInt(responses.size())) +  " I've started a rematch. Make your choice again!");
            tie = true;
            ties++; playersTies++;
            recordPersonalResult(player, "Ties", playersTies);
            recordResult("ties", ties);
        }

        if (random.nextInt(4) != 1 && !result.equals("tie"))    //  25% chance to trigger
            sendMessage(channel, "TIPS: Check your records using \"!myrpsstats\" or check others' records using <!rpsstats + username>.");
    }

    /**
     * This method reads humans' all-time record against
     * FrostySpirit in rock paper scissors and sends the stats
     * to the chat.
     * <p>
     * This method reads from files under src/RPSResults.
     * @throws FileNotFoundException
     * @since FrostySpirit 1.4.1
     */
    public void showTotalResults() throws FileNotFoundException
    {
        Scanner readFile = new Scanner(wins);
        int wins = readFile.nextInt();
        readFile.close();

        readFile = new Scanner(ties);
        int ties = readFile.nextInt();
        readFile.close();

        readFile = new Scanner(losses);
        int losses = readFile.nextInt();
        readFile.close();

        //  Calculate rates for wins, losses, and ties
        double winsRate = round((wins * 100.0) / (wins + losses + ties), 1);
        double lossesRate = round((losses * 100.0) / (wins + losses + ties), 1);
        double tiesRate = round((ties * 100.0) / (wins + losses + ties), 1);
        sendMessage(channel, "In the " + (wins + losses + ties) + " rounds played, these are the humans' records on rock paper scissors: " + wins + " wins(" + winsRate + "%), " + losses + " losses(" + lossesRate + "%), " + ties + " ties(" + tiesRate + "%).");
    }

    /**
     * This method sends the stats of a player to the chat, with the 
     * rate of each stat.
     * <p>
     * This method refreshes the loaded stats then reads from them. If
     * a player is not present in the player list, send a notice to ask
     * them to start a game first.
     * <p>
     * If a user is checking another user's stats (determined by {@code othersChecking}), 
     * send a different response without the pronoun "you."
     * @param player The player who requested their stats.
     * @param othersChecking A boolean that indicates whether or not a user 
     * is check another user's stats.
     * @throws IOException
     * @since FrostySpirit 1.4.2
     */
    public void showPersonalResults(String player, Boolean othersChecking) throws IOException
    {
        refreshStats(); // Update the loaded stats to ensure accuracy

        String playerShortName = getShortName(player);

        if(!playerList.stream().anyMatch(player::contains))    // If the player does not have a record
            sendMessage(channel, "I can't find this person's records. Did you spell their name exactly right? FrankerZ");

        else
        {
            //  Get the stats based on the player's index in the player list
            int wins = allWins.get(playerList.indexOf(player));
            int losses = allLosses.get(playerList.indexOf(player));
            int ties = allTies.get(playerList.indexOf(player));

            if(wins + losses + ties == 0 && !othersChecking)
                sendMessage(channel, "It looks like you never actually finished a round for some reason. Please use \"!rps\" to start a round first.");

            else if(wins + losses + ties == 0 && othersChecking)
                sendMessage(channel, "Hmm... I do have the name on my book, but all of the stats are zeros. They probably never finished a round. FrankerZ");
            else
            {
                //  Calculate rates for wins, losses, and ties
                double winsRate = round((wins * 100.0) / (wins + losses + ties), 1);
                double lossesRate = round((losses * 100.0) / (wins + losses + ties), 1);
                double tiesRate = round((ties * 100.0) / (wins + losses + ties), 1);
                if(othersChecking)
                    sendMessage(channel, 
                        "These are the records for " + playerShortName.substring(0, 1).toUpperCase() + playerShortName.substring(1) + ": " + wins + " wins(" + winsRate + "%), " + losses + " losses(" + lossesRate + "%), " + ties + " ties(" + tiesRate + "%).");
                else
                    sendMessage(channel, 
                        playerShortName.substring(0, 1).toUpperCase() + playerShortName.substring(1) + ", in the " + (wins + losses + ties) + " rounds you've played, you have: " + wins + " wins(" + winsRate + "%), " + losses + " losses(" + lossesRate + "%), " + ties + " ties(" + tiesRate + "%).");
            }
        }
    }

    /**
     * This method writes in the integers that indicate
     * the humans' total wins, losses, and ties to the
     * corresponding txt files.
     * @param result A String that indicates the result in 
     * all lowercases.
     * @param count An integer that indicates the number 
     *              of wins, losses, or ties.
     * @throws IOException
     * @since FrostySpirit 1.4.1
     */
    public void recordResult(String result, int count) throws IOException
    {
        FileWriter writer = new FileWriter("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + result + ".txt");
        writer.write(String.valueOf(count));
        writer.close();
    }

    /**
     * This method creates a new record for a new player if their username
     * is not present in the player list.
     * <p>
     * This will add a new line to all four stats text files. (playerlist, wins, losses,
     * ties)
     * @param player Username of the player who's being added to the player list.
     * @throws IOException
     * @since FrostySpirit 1.4.2
     */
    public void createNewRecord(String player) throws IOException
    {
        //  Declare FileWriters for appending
        FileWriter writeNewRecord = new FileWriter(playerWinsFile, true);
        FileWriter writeNewPlayerName = new FileWriter(playerListFile, true);

        writeNewPlayerName.write(player + System.getProperty("line.separator"));
        writeNewPlayerName.close();

        //  Add a new line with the number "0" to all the stats text files
        writeNewRecord.write("0" + System.getProperty("line.separator"));
        writeNewRecord.close();

        writeNewRecord = new FileWriter(playerLossesFile, true);
        writeNewRecord.write("0" + System.getProperty("line.separator"));
        writeNewRecord.close();

        writeNewRecord = new FileWriter(playerTiesFile, true);
        writeNewRecord.write("0" + System.getProperty("line.separator"));
        writeNewRecord.close();
        
        System.out.println("[Terminal] A new record for " + player + " has been created.");
        refreshStats();
    }

    /**
     * This method writes a player's record to the corresponding text file. Then,
     * refreshes the lists loaded in the program.
     * <p>
     * The results are saved in a four-dimentional lists: {@code List playerList},
     * {@code List allWins}, {@code List allLosses}, and {@code List allTies}.
     * @param player The username of the last player.
     * @param result The result of the last game in a String. The first letter needs
     * to be capitalized.
     * @param score The counter of the player's one particular stat (such as wins).
     * @throws IOException
     * @since FrostySpirit 1.4.2
     */
    public void recordPersonalResult(String player, String result, int score) throws IOException
    {
        FileWriter writer = new FileWriter("TextFiles" + System.getProperty("file.separator") + "RPSResult" + System.getProperty("file.separator") + "player" + result + ".txt");
        List<Integer> listModified = new ArrayList<Integer>();  //  The list used to duplicate a stat list based 
                                                                //  on the result of the last game
        
        switch(result)  //  Get the correct list to be duplicated and updated based on the result
        {
            case "Wins": listModified = new ArrayList<Integer>(allWins);
            break;
            case "Losses": listModified = new ArrayList<Integer>(allLosses);
            break;
            case "Ties": listModified = new ArrayList<Integer>(allTies);
            break;
        }

        listModified.set(playerList.indexOf(player), score);
        for (int i = 0; i < listModified.size(); i++)    //  Write the updated list to the corresponding text file
            writer.write(listModified.get(i) + System.getProperty("line.separator"));

        writer.close();
        refreshStats(); //  Update all loaded lists.
    }

    /**
     * This method reads all the text files that contain stats and player list
     * into their corresponding {@code Lists}. Then, it prints all the lists on 
     * the console/terminal.
     * <p>
     * Before loading, this method will clear all the stats in order to achieve
     * a refresh operation.
     * <p>
     * Please note that the numbers of lines across all four text files need to be
     * the same for this method to work properly. Beware of empty lines in a text file!
     * @throws IOException
     * @since FrostySpirit 1.4.2
     */
    public void refreshStats() throws IOException
    {
        Scanner readWins = new Scanner(playerWinsFile);
        Scanner readLosses = new Scanner(playerLossesFile);
        Scanner readTies = new Scanner(playerTiesFile);
        Scanner readPlayerList = new Scanner(playerListFile);

        //  Clear all the lists before updating them
        allWins.clear();
        allLosses.clear();
        allTies.clear();
        playerList.clear();

        //  Load all the elements from all four stats text files into the now emptied lists
        while(readWins.hasNextLine() && readLosses.hasNextLine() && readTies.hasNextLine() && readPlayerList.hasNextLine())
        {
            allWins.add(Integer.parseInt(readWins.nextLine()));
            allLosses.add(Integer.parseInt(readLosses.nextLine()));
            allTies.add(Integer.parseInt(readTies.nextLine()));
            playerList.add(readPlayerList.nextLine().toLowerCase());
        }

        /*System.out.println("[Terminal] Latest RPS stats have been reloaded.");
        System.out.println("Player List:" + playerList);
        System.out.println("Wins:" + allWins);
        System.out.println("Losses:" + allLosses);
        System.out.println("Ties:" + allTies); */
        
        readWins.close();
        readLosses.close();
        readTies.close();
        readPlayerList.close();
    }

    /**
     * Returns a double rounded to specific decimal places.
     * In the context of the {@code RockPaperScissors} class, this method
     * is used to calculate win/loss/tie rates.
     * @param value The double that's being rounded.
     * @param precision The number of decimal places.
     * @return A rounded double.
     */
    private static double round (double value, int precision) 
    {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}