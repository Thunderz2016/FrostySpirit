import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class FrostyQueue extends FilesProcess
{
    public static String channel = Config.channel;
    
    public static List<String> queue = new ArrayList<String>();   // Create queue List
    
    public static List<String> responses = new ArrayList<String>();     // Create List for random response

    public static Random random = new Random();
    
    public static int q = 0;    // Counter / position of the queue

    /**
     * This method adds the user who triggered the 
     * "!join" command to the queue list.
     * <p>
     * If the user is already in the queue, sends a
     * notification with their position in the queue instead.
     * @param sender The user being added to the queue.
     */
    public void addUserToQueue(String sender)
    {        
        if(!queue.stream().anyMatch(sender::contains))    // If user is not in the queue
        {
            queue.add(sender);  // Add the user who triggers "!join" command to the queue
            String notice = sender + ", you have joined the queue. ";
            if(q == 0)
                notice += "You are the first in the queue! (#1)";
            else if(q == 1)
                notice += "There is only one person ahead of you. (#2)";
            else
                notice += "There are currently " + q + " people ahead of you. (#" + (q + 1) + ")";
            sendMessage(channel, notice);
            q++;
        }

        else
        {
            String notice = sender + ", you're already in the queue. ";
            if(queue.indexOf(sender) == 0)
                notice += "You'll be the next. (#1)";
            else if(queue.indexOf(sender) == 1)
                notice += "There is only one person ahead of you. (#2)";
            else
                notice += "There are currently " + queue.indexOf(sender) + " people ahead of you. (#" + (queue.indexOf(sender) + 1) + ")";
            sendMessage(channel, notice);
        }
    }

    /**
     * This method removes the user who triggered the 
     * "!leave" command from the queue list.
     * 
     * @param sender The user being removed from the queue.
     */
    public void removeUserFromQueue(String sender)
    {
        if(queue.stream().anyMatch(sender::contains)) // If the user is indeed in the queue
        {
            queue.remove(sender); q--;  // Remove the user who triggers "!leave" command from the queue
            sendMessage(channel, sender + " has left the queue. See you around...");
        }
    }

    /**
     * This method picks the first user in the queue list, 
     * sends a notification to the chat, then removes the 
     * picked user from the queue list.
     * 
     * @since FrostySpirit 1.2.0
     */
    public void pickNext()
    {
        if(queue.isEmpty())
            sendMessage(channel, "[System] The queue is empty.");

        else
        {
            String next = queue.get(0); // Get the next user in the queue
            queue.remove(next); // Remove picked user from the queue
            sendNotice(next);
            q--;
        }
    }

    /**
     * This method picks a random user from the queue list, sends
     * a notification to the chat, then removes that user from
     * the queue list.
     * 
     * @since FrostySpirit 1.2.0
     */
    public void pickRandom()
    {
        if(queue.isEmpty())
            sendMessage(channel, "[System] The queue is empty.");

        else
        {
            String next = queue.get(random.nextInt(q)); // Get the corresponding user of random queue position
            queue.remove(next); // Remove picked user from the queue
            sendNotice(next);
            q--;
        }
    }

    /**
     * This method sends the content of the queue list to the chat.
     * @since FrostySpirit 1.2.0
     */
    public void showQueue()
    {
        if(q == 0)  // If the counter is 0 (possibly no one is in the queue)
            sendMessage(channel, "There's currently nobody in the queue. Type \"!join\" to be the first!");
        else
            sendMessage(channel, "We currently have these people in the queue (from first to last): " + queue);   // Show the queue
    }

    /**
     * This method sends the position of a user in the queue.
     * 
     * @param sender The user who is being determined position for.
     * @since FrostySpirit 1.2.0
     */
    public void showPos(String sender)
    {
        if(queue.stream().anyMatch(sender::contains))
        {
            int position = queue.indexOf(sender);
            sendMessage(channel, sender + ", your position in the queue is #" + (position + 1) + ".");
        }
        
        else
            sendMessage(channel, sender + ", you are currently not in the queue. Type \"!join\" to join first.");
    }

    /**
     * This method sends randomized notification to a user in 
     * the queue who just got picked.
     * 
     * @param next The picked next user in the queue.
     * @since FrostySpirit 1.2.0
     */
    public void sendNotice(String next)
    {
        int n = 0;
        responses.clear();
        responses.add("[Queue] " + next + ", you are next. Come on in!"); n++;
        responses.add("[Queue] The next participant is " + next + "! Good luck, have fun!"); n++;
        responses.add("[Queue] It's your turn, " + next + "! Hop right in!"); n++;
        int listPosition = random.nextInt(n);
        sendMessage(channel, responses.get(listPosition));
    }
}