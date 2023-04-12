import java.util.ArrayList;
import java.util.Random;
import java.util.List;

public class RandomLinkCode extends Greetings
{
    public static List<String> users = new ArrayList<String>();
    public static List<String> storedLinkCodes = new ArrayList<String>();

    /**
     * Returns a {@code String} that contains formatted, randomly generated
     * code with given digits;
     * <p>
     * Intended for a maximum of 20 digits. Anything greater than 20 will result 
     * in a random {@code long} with random digits. In the master build, a check
     * has been implemented which will send a message telling the user that digits
     * larger than 20 are not supported.
     * <p>
     * Formatting rules apply in the following order:
     * <ul>
     * <li>{@code digits} = multiple of 5, add a space every 5 digits.
     * </li>
     * <li>{@code digits} = multiple of 4, add a space every 4 digits.
     * </li>
     * <li>{@code digits} = multiple of 3, add a space every 3 digits.
     * </li>
     * <li>If passed prime numbers smaller than 20, no formatting.
     * @param digits The number of digits specified by the user.
     * @param sender The username of the user who requested a code.
     * @return A string that contains formatted randomly generated code.
     * @since FrostySpirit 1.3.0
     */
    public String generateLinkCode(int digits, String sender)
	{
        Random random = new Random();
        String linkCode = "";
		if(digits % 5 == 0)
		{
            linkCode = String.format("%05d", random.nextInt(99999));
            for(int i = 1; i < (digits / 5); i++)
                linkCode += " " + String.format("%05d", random.nextInt(99999));
            saveLinkCode(linkCode, sender);
            return linkCode;
		}

		else if(digits % 4 == 0)
		{
            linkCode = String.format("%04d", random.nextInt(9999));
            for(int i = 1; i < (digits / 4); i++)
                linkCode += " " + String.format("%04d", random.nextInt(9999));
            saveLinkCode(linkCode, sender);
			return linkCode;
		}

		else if(digits % 3 == 0)
		{
            linkCode = String.format("%03d", random.nextInt(999));
            for(int i = 1; i < (digits / 3); i++)
                linkCode += " " + String.format("%03d", random.nextInt(999));
            saveLinkCode(linkCode, sender);
			return linkCode;
		}

        else if(digits == 11 || digits == 13 || digits == 17 || digits == 19)   // Special cases for prime numbers (little assholes).
        {
            for(int i = 1; i <= digits; i++)
                linkCode += random.nextInt(9);
            saveLinkCode(linkCode, sender);
            return linkCode;
        }

        return String.format("%" + 0 + digits + "d", random.nextInt((int) Math.pow(10, digits) - 1));    // For prime numbers smaller or equal to 7.
    }
    
    /**
     * This method stores the last generated passcode for a user.
     * @param linkCode The generated passcode.
     * @param sender The user who generated the passcode.
     * @since FrostySpirit 1.3.0
     */
    public void saveLinkCode(String linkCode, String sender)
    {
        if(users.stream().anyMatch(sender::contains))   //  If a user already has generated a code in a session
            storedLinkCodes.set(users.indexOf(sender), linkCode);   //  Update the corresponding code in the list
        else    //  Otherwise, create a new element
        {
            users.add(sender);
            storedLinkCodes.add(linkCode);
        }
    }

    /**
     * This method sends a user's last stored passcode to the chat.
     * @param sender The user who is requesting their last passcode.
     * Used to look up the index of their corresponding stored code.
     * @since FrostySpirit 1.3.0
     */
    public void showLastLinkCode(String sender)
    {
        if(!users.stream().anyMatch(sender::contains))
            sendMessage(channel, "My memory gets lost every time I restart. BigBrother You need to generate a new code for this session first.");
        else
            sendMessage(channel, storedLinkCodes.get(users.indexOf(sender)) + " was your last passcode, " + sender + ".");
    }
}