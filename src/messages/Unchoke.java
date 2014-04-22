package messages;

import java.nio.ByteBuffer;
import java.util.Random;

import peers.NeighborPeer;
import peers.Peer;
import util.Bitfield;

public class Unchoke extends Message
{

	public Unchoke(int senderID, int receiverID)
	{
		super(senderID, receiverID);
		logPeer = new Peer(receiverID); //create peer object based on receiver ID so log method in Peer class will write to the receiver ID's log file

	}

	@Override
	public void handle()
	{
		// If interested in peer that unchoked you, send request for a random
		// piece they have that you don't
		System.out.println("Unchoke message " + senderID + " -> " + receiverID);
		
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
		
		logPeer.log("unchoke", receiverID, senderID); //handles log file: Peer, receiverID is unchoked by Peer, senderID

	}
	public String encodeMessage()
	{
		byte [] length = (ByteBuffer.allocate(4)).putInt(0).array(); 
		
		byte [] message = new byte[5];
		message[4] = 1;// type unchoke
		
		System.arraycopy(length, 0, message, 0, 4); 
		
		return new String(message); 
	}
	
	public int getReceiverID()
	{
		return receiverID;
	}
	
	public int getSenderID()
	{
		return senderID;
	}
	
}
