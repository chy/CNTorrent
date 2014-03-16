package messages;

public abstract class Message
{
	public int senderID; 
	
	public Message(int senderID){
		this.senderID = senderID;		
	}
	
	public static void parseMessage(String[] data)
	{
		
	}

	public abstract void handle();

}
