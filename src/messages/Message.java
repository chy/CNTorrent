package messages;

import java.nio.ByteBuffer;

public abstract class Message
{

	public final int senderID; 
	public final int receiverID; 

	public Message(int senderID, int receiverID)
	{
		this.senderID = senderID;	
		this.receiverID = receiverID; 
	}

	public static Message decodeMessage(String messageString, int senderID, int receiverID)
	{
		byte[] bytes = messageString.getBytes();
		byte[] messageTypeBytes = new byte[] { bytes[4] };
		ByteBuffer messageTypeByteBuffer = ByteBuffer.wrap(messageTypeBytes);
		int messageType = messageTypeByteBuffer.getInt();
		byte[] payloadBytes = null;
		int pieceIndex = -1;
		if (messageType == 4 || messageType == 6 || messageType == 7)
		{
			payloadBytes = new byte[4];
			System.arraycopy(bytes, 5, payloadBytes, 0, 4);
			ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payloadBytes);
			pieceIndex = payloadByteBuffer.getInt();
		}
		else if (messageType == 5)
		{
			int length = bytes.length - 5;
			payloadBytes = new byte[length];
			System.arraycopy(bytes, 5, payloadBytes, 0, length);
		}
		switch (messageType)
		{
		case 0: return new Choke(senderID, receiverID);
		case 1: return new Unchoke(senderID, receiverID);
		case 2: return new Interested(senderID, receiverID);
		case 3: return new NotInterested(senderID, receiverID);
		case 4: return new Have(senderID, receiverID, pieceIndex);
		case 5: return new BitfieldMessage(senderID, receiverID, payloadBytes);
		case 6: return new Request(senderID, receiverID, pieceIndex);
		case 7: return new Piece(senderID, receiverID, pieceIndex);
		default: throw new RuntimeException("Cannot decode message with value " + messageType);
		}
	}

	public abstract String encodeMessage();

	public abstract void handle();

}
