package messages;

import java.nio.ByteBuffer;

import peers.Peer;

public class Handshake extends Message
{

	public int socketID;

	public Handshake(int senderID, int receiverID, int socketID)
	{
		super(senderID, receiverID);
		this.socketID = socketID;
	}

	@Override
	public void handle()
	{
		// link socket with sender ID
		Peer.linkSocket(socketID, senderID);

		// send bitfield message
		Peer.sendMessage(new BitfieldMessage(receiverID, senderID, Peer.bitfield));
	}

	@Override
	public String encodeMessage()
	{
		// 4-byte index of the file piece
		byte[] payload = (ByteBuffer.allocate(4)).putInt(senderID).array();
		byte[] length = (ByteBuffer.allocate(4)).putInt(payload.length).array();

		byte[] message = new byte[5 + payload.length];
		message[4] = 8;// type handshake

		System.arraycopy(length, 0, message, 0, 4);
		System.arraycopy(payload, 0, message, 5, payload.length);

		return new String(message);
	}

}
