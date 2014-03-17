package messages;

import peers.NeighborPeer;
import peers.Peer;

public class Bitfield extends Message
{

	public Bitfield(int senderID, int receiverID)
	{
		super(senderID, receiverID);
	}

	@Override
	public void handle()
	{
		// If (B ^ ~ A) != 0, send interested to host B
		// Else, send not interested
		// update interested, as well
		NeighborPeer neighborPeer = Peer.peers.get(this.senderID);
		neighborPeer.establishConnection(); // connect to peer

		boolean[] unchokingBitfield = neighborPeer.bitfield;
		boolean[] myBitfield = Peer.bitfield;

		for (int i = 0; i < myBitfield.length; i++)
		{
			if (unchokingBitfield[i] && !myBitfield[i])
			{
				// interested
				neighborPeer.amInterested = true;
			}
			if (!unchokingBitfield[i] && myBitfield[i])
			{
				// peer is interested
				neighborPeer.peerInterested = true;
			}
		}

		if (neighborPeer.amInterested)
		{ // send interested
			Message interestedMessage = new Interested(receiverID, senderID);
			Peer.sendMessage(interestedMessage);
		}
		else
		{ // send not interested
			Message notInterestedMessage = new NotInterested(receiverID, senderID);
			Peer.sendMessage(notInterestedMessage);
		}

	}

}
