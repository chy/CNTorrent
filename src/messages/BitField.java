package messages;

import peers.NeighborPeer;
import peers.Peer;

public class BitField extends Message {
	public BitField(int senderID, int receiverID){
		super(senderID, receiverID);
	}
	@Override
	public void handle() {
		// If  (B ^ ~ A) != 0, send interested to host B. Else, send not interested.
		// update interested, as well
		NeighborPeer peer = Peer.peers.get(this.senderID); 
		peer.establishConnection(); // connect to peer

		boolean [] unchokingBitfield = peer.bitfield;
		boolean [] myBitfield = Peer.bitfield;
		
		for(int i = 0; i < myBitfield.length; i++){
			
			if(unchokingBitfield[i] && !myBitfield[i]){
				//interested
				peer.amInterested = true; 
			}
			if(!unchokingBitfield[i] && myBitfield[i]){
				//peer is interested
				peer.peerInterested = true; 
			}
		}
		
		if(peer.amInterested){ //send interested
			Message interestedMessage = new Interested(receiverID, senderID);
			Peer.sendMessage(interestedMessage);			
		}
		else{ //send not interested
			Message notInterestedMessage = new NotInterested(receiverID, senderID);
			Peer.sendMessage(notInterestedMessage);				
		}
		
	}
	

}
