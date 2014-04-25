package messages;

import peers.Peer;

import java.nio.ByteBuffer;

public abstract class Message
{

	static final int CHOKE = 0;
	static final int UNCHOKE = 1;
	static final int INTERESTED = 2;
	static final int NOT_INTERESTED = 3;
	static final int HAVE = 4;
	static final int BITFIELD = 5;
	static final int REQUEST = 6;
	static final int PIECE = 7;
	static final int HANDSHAKE = 'O'; // as in the 5th letter of "HELLO"

	public final int senderID; 
	public final int receiverID;
	Peer logPeer; //created so that subclasses of Message will have access to the log method in Peer class

	public Message(int senderID, int receiverID)
	{
		this.senderID = senderID;	
		this.receiverID = receiverID; 
	}

	// length [4 bytes] type [1 byte] payload [other bytes]
	public static Message decodeMessage(String messageString, int senderID, int receiverID)
	{
		System.out.println("Decoding message: " + messageString);
		
		byte[] bytes = messageString.getBytes();
		
		byte messageType = bytes[4]; 
				
		byte[] payloadBytes = null;
		int pieceIndex = -1;

		if (messageType == HAVE || messageType == REQUEST)
		{
			payloadBytes = new byte[4];
			System.arraycopy(bytes, 5, payloadBytes, 0, 4);
			ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payloadBytes);
			pieceIndex = payloadByteBuffer.getInt();
		}
		else if (messageType == BITFIELD || messageType == PIECE)
		{
			int length = bytes.length - 5;
			payloadBytes = new byte[length];
			System.arraycopy(bytes, 5, payloadBytes, 0, length);
		}
		else if (messageType == HANDSHAKE)
		{
			payloadBytes = new byte[4];
			System.arraycopy(bytes, 28, payloadBytes, 0, 4);
			ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payloadBytes);
			senderID = payloadByteBuffer.getInt();
		}

		switch (messageType)
		{
		case CHOKE: return new Choke(senderID, receiverID);
		case UNCHOKE: return new Unchoke(senderID, receiverID);
		case INTERESTED: return new Interested(senderID, receiverID);
		case NOT_INTERESTED: return new NotInterested(senderID, receiverID);
		case HAVE: return new Have(senderID, receiverID, pieceIndex);
		case BITFIELD: return new BitfieldMessage(senderID, receiverID, payloadBytes);
		case REQUEST: return new Request(senderID, receiverID, pieceIndex);
		case PIECE: return new Piece(senderID, receiverID, pieceIndex, payloadBytes);
		case HANDSHAKE: return new Handshake(senderID, receiverID); 
		default: throw new RuntimeException("Cannot decode message with value " + messageType);
		}
	}

	public abstract String encodeMessage();

	public abstract void handle();

	@Override
	public String toString()
	{
		return encodeMessage();
	}

}
