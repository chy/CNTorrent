package messages;

import java.nio.ByteBuffer;

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
		logPeer = new Peer(receiverID); //create peer object based on receiver ID so log method in Peer class will write to the receiver ID's log file

	}

	@Override
	public void handle() 
	{
		/*
		 * notify all neighbors that you have it (so they can keep your bitfield updated)
		 * have a piece index and set that index to true in the peer's bitfield that has sent that particular
		 * piece. senderID is given. check if all of the indices in bitfield is set to true, if so decrement numUnFinishedPeers(Peers class)
		 */
		System.out.println("have message " + senderID + " -> " + receiverID + " : " + pieceIndex);
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
		
		logPeer.log("receive_have",receiverID,senderID,pieceIndex, 0); //handles log file: Peer, receiverID received a have message from Peer, senderID, for the Piece, pieceIndex

	}
	
	public String encodeMessage()
	{					
		byte [] payload = (ByteBuffer.allocate(4)).putInt(pieceIndex).array(); // 4-byte index of the file piece
		byte [] length = (ByteBuffer.allocate(4)).putInt(payload.length).array(); 
		
		byte [] message = new byte[5+payload.length];
		message[4] = 4;// type have
		
		System.arraycopy(length, 0, message, 0, 4); 
		System.arraycopy(payload, 0, message, 5, payload.length); 
		
		return new String(message); 
	}
}
