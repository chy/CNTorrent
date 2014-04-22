package messages;

import java.nio.ByteBuffer;

public class Handshake extends Message
{

	public Handshake(int senderID, int receiverID)
	{
		super(senderID, receiverID);
	}

	@Override
	public void handle()
	{
		// not called
	}

	@Override
	public String encodeMessage()
	{
		// 4-byte index of the file piece
		byte[] payload = (ByteBuffer.allocate(4)).putInt(senderID).array();
		byte[] length = (ByteBuffer.allocate(4)).putInt(payload.length + 1).array();

		byte[] message = new byte[5 + payload.length];
		message[4] = 8;// type handshake

		System.arraycopy(length, 0, message, 0, 4);
		System.arraycopy(payload, 0, message, 5, payload.length);

		return new String(message);
	}

}
