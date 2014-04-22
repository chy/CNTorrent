package messages;

import java.nio.ByteBuffer;

import peers.Peer;

public class Handshake extends Message
{
	Peer logPeer2;

	public Handshake(int senderID, int receiverID)
	{
		super(senderID, receiverID);
		logPeer = new Peer(receiverID);
		logPeer2 = new Peer(senderID);
	}

	@Override
	public void handle()
	{
		System.out.println("Handshaking " + senderID + " -> " + receiverID);

		// send bitfield message
		Peer.sendMessage(new BitfieldMessage(receiverID, senderID, Peer.bitfield));
		logPeer.log("TCP_to", senderID, receiverID); //handles log, PEER, senderID is connected to PEER, receiverID
		logPeer2.log("TCP_from", receiverID, senderID); //handles log, PEER, receiverID is connected from PEER, senderID
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
