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

	public static Message decodeMessage(String messageString)
	{
		// TODO return a Message object based on the header info of messageString
		return null;
	}

	public String encodeMessage()
	{
		// TODO return a String from this Message object that follows the BitTorrent protocol
		return null;
	}

	public abstract void handle();

}
