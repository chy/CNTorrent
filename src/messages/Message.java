package messages;

public abstract class Message
{
	public final int senderID; 
	public final int receiverID; 
	
	public Message(int senderID, int receiverID){
		this.senderID = senderID;	
		this.receiverID = receiverID; 
	}
	
	public static void parseMessage(String[] data)
	{
		
	}

	public abstract void handle();

}
