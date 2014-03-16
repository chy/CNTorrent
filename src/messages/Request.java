package messages;

import peers.NeighborPeer;
import peers.Peer;

public class Request extends Message {
	
	public int pieceIndex; //index of the requested piece 
	
	public Request(int senderID, int receiverID){
		super(senderID, receiverID);
	}
	
	@Override
	public void handle() {
		// If sending host is unchoked, send the requested data
		NeighborPeer peer = Peer.peers.get(this.senderID); 
		boolean [] myBitfield = Peer.bitfield;
		
		if(!peer.amChoking && myBitfield[pieceIndex]){ 
			//if we're not choking them and have the requested piece, send it
			Piece pieceMessage = new Piece(receiverID, senderID, pieceIndex);
			
			Peer.sendMessage(pieceMessage);
		}
		
		
	}

}
