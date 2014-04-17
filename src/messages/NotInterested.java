package messages;

import java.nio.ByteBuffer;

public class NotInterested extends Message {

	public NotInterested (int senderID, int receiverID){
		super(senderID, receiverID);
	}
	public void handle()
	{
		
		
	}
	public String encodeMessage()
	{
		byte [] length = (ByteBuffer.allocate(4)).putInt(0).array(); 
		
		byte [] message = new byte[5];
		message[4] = 3;// type not interested
		
		System.arraycopy(length, 0, message, 0, 4); 
		
		return new String(message); 
	}	
}
