package messages;

import peers.NeighborPeer;
import peers.Peer;

public class Choke extends Message{
	
	public Choke(int senderID, int receiverID){
		super(senderID, receiverID);
	}
	
	@Override
	public void handle() 
	{
		/*
		 * Update variables: 
		 * 	you are now choked by this peer
		 * 
		 */
		NeighborPeer chokingPeer = Peer.peers.get(this.senderID); 
		chokingPeer.peerChoking = true; 
		
	}

}
