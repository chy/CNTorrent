package messages;

import peers.NeighborPeer;
import peers.Peer;
import util.Bitfield;

public class BitfieldMessage extends Message
{

	private Bitfield senderBitfield;

	public BitfieldMessage(int senderID, int receiverID, byte[] bitfieldBytes)
	{
		super(senderID, receiverID);
		senderBitfield = new Bitfield(bitfieldBytes);
	}

	public BitfieldMessage(int senderID, int receiverID, Bitfield senderBitfield)
	{
		super(senderID, receiverID);
		this.senderBitfield = senderBitfield;
	}

	@Override
	public void handle()
	{
		// If (B ^ ~ A) != 0, send interested to host B
		// Else, send not interested
		// update interested, as well
		NeighborPeer neighborPeer = Peer.peers.get(this.senderID);
		neighborPeer.establishConnection(); // connect to peer

		Bitfield myBitfield = Peer.bitfield;

		for (int i = 0; i < Peer.numPieces; i++)
		{
			if (senderBitfield.hasPiece(i) && !myBitfield.hasPiece(i))
			{
				// we are interested
				neighborPeer.amInterested = true;
			}
			if (!senderBitfield.hasPiece(i) && myBitfield.hasPiece(i))
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
