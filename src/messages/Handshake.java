package messages;

import peers.NeighborPeer;
import peers.Peer;

public class Handshake extends Message
{

	public Handshake(int senderID, int receiverID)
	{
		super(senderID, receiverID);
	}

	@Override
	public void handle()
	{
		NeighborPeer neighborPeer = Peer.peers.get(senderID);
		if (!neighborPeer.amConnected)
			throw new RuntimeException("Received a handshake message from a peer that we are not connected to");

		Peer.sendMessage(new BitField(receiverID, senderID));
	}

}
