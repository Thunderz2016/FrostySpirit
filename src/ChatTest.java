public class ChatTest extends FilesProcess {
    public String channel = Config.channel;
    public String testMsg;
    private String anotherTestMsg;
    private String longMsg;
    

    public void send() {
        System.out.println("ChatTest has been called.");
        this.testMsg = "Test message.";
        this.anotherTestMsg = "Another test message.";
        this.longMsg = "This is a long message.";
        sendMessage(channel, testMsg);
        sendMessage("#thunderz2016", anotherTestMsg);
        sendMessage(channel, longMsg);
    }
}

