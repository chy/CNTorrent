package messages;

import java.nio.ByteBuffer;

public class Handshake extends Message {

	public Handshake(int senderID, int receiverID)
	{
		super(senderID, receiverID);
	}

	@Override
	public void handle() {
		// TODO Auto-generated method stub

	}

	@Override
	public String encodeMessage() {
		{					
			byte [] payload = (ByteBuffer.allocate(4)).putInt(senderID).array(); // 4-byte index of the file piece
			byte [] length = (ByteBuffer.allocate(4)).putInt(payload.length).array(); 
			
			byte [] message = new byte[5+payload.length];
			message[4] = -1;// type handshake
			
			System.arraycopy(length, 0, message, 0, 4); 
			System.arraycopy(payload, 0, message, 5, payload.length); 
			
			return new String(message); 
		}
	}
	
	
}
