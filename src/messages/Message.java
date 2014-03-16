package messages;

public abstract class Message
{

	public final int senderID; 
	public final int receiverID; 

	public Message(int senderID, int receiverID)
	{
		this.senderID = senderID;	
		this.receiverID = receiverID; 
	}

	public static Message parseMessage(String messageString)
	{
		// TODO return a Message object based on the header info of messageString
		// use MessageType.getMessageType()
		return null;
	}

	public abstract void handle();

}
