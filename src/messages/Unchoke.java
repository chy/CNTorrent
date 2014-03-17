package messages;

import java.util.Random;

import peers.NeighborPeer;
import peers.Peer;
import util.Bitfield;

public class Unchoke extends Message
{

	public Unchoke(int senderID, int receiverID)
	{
		super(senderID, receiverID);
	}

	@Override
	public void handle()
	{
		// If interested in peer that unchoked you, send request for a random
		// piece they have that you don't

		NeighborPeer unchokingPeer = Peer.peers.get(this.senderID);
		unchokingPeer.peerChoking = true;

		if (unchokingPeer.amInterested)
		{
			Bitfield unchokingBitfield = unchokingPeer.bitfield;
			Bitfield myBitfield = Peer.bitfield;

			Random random = new Random();

			int pieceIndex = random.nextInt(Peer.numPieces);
			while (!(unchokingBitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex)))
			{
				pieceIndex = random.nextInt(Peer.numPieces);
			}

			Request requestMessage = new Request(receiverID, senderID, pieceIndex);
			Peer.sendMessage(requestMessage);
		}
	}

}
