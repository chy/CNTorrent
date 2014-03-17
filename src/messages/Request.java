package messages;

import peers.NeighborPeer;
import peers.Peer;
import util.Bitfield;

public class Request extends Message
{

	public int pieceIndex; // index of the requested piece

	public Request(int senderID, int receiverID, int pieceIndex)
	{
		super(senderID, receiverID);
		this.pieceIndex = pieceIndex;
	}

	@Override
	public void handle()
	{
		// if sending host is unchoked, send the requested data
		NeighborPeer peer = Peer.peers.get(this.senderID);
		Bitfield myBitfield = Peer.bitfield;

		if (!peer.amChoking && myBitfield.hasPiece(pieceIndex))
		{
			// if we're not choking them and have the requested piece, send it
			Piece pieceMessage = new Piece(receiverID, senderID, pieceIndex);
			Peer.sendMessage(pieceMessage);
		}
	}

}
