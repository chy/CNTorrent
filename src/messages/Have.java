package messages;

import peers.NeighborPeer;
import peers.Peer;
import util.Bitfield;

public class Have extends Message
{

	public int pieceIndex; // index of the requested piece

	public Have(int senderID, int receiverID, int pieceIndex)
	{
		super(senderID, receiverID);
		this.pieceIndex = pieceIndex; 
	}

	@Override
	public void handle() 
	{
		/*
		 * notify all neighbors that you have it (so they can keep your bitfield updated)
		 * have a piece index and set that index to true in the peer's bitfield that has sent that particular
		 * piece. senderID is given. check if all of the indices in bitfield is set to true, if so decrement numUnFinishedPeers(Peers class)
		 */

		NeighborPeer neighborPeer = Peer.peers.get(this.senderID);

		Bitfield senderBitfield = neighborPeer.bitfield;
		senderBitfield.setPiece(pieceIndex, true);

		boolean senderIsDone = true;
		for (int i = 0; i < Peer.numPieces; i++)
		{
			if (!senderBitfield.hasPiece(i))
			{
				senderIsDone = false;
				break;
			}
		}

		if (senderIsDone)
		{
			Peer.numUnfinishedPeers--;
			neighborPeer.isDone = true;
		}
	}

}
