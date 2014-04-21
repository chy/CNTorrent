package messages;

import java.nio.ByteBuffer;

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
		System.out.println("request message " + senderID + " -> " + receiverID + " : " + pieceIndex);
		
		NeighborPeer peer = Peer.peers.get(this.senderID);
		Bitfield myBitfield = Peer.bitfield;

		if (!peer.amChoking && myBitfield.hasPiece(pieceIndex))
		{
			// if we're not choking them and have the requested piece, send it
			Piece pieceMessage = new Piece(receiverID, senderID, pieceIndex);
			Peer.sendMessage(pieceMessage);
		}
	}
	public String encodeMessage()
	{
		byte [] payload = (ByteBuffer.allocate(4)).putInt(pieceIndex).array(); // 4-byte index of the file piece
		byte [] length = (ByteBuffer.allocate(4)).putInt(payload.length).array(); 
		
		byte [] message = new byte[5+payload.length];
		message[4] = 6;// type request
		
		System.arraycopy(length, 0, message, 0, 4); 
		System.arraycopy(payload, 0, message, 5, payload.length); 
		
		return new String(message); 
		
	}
}
