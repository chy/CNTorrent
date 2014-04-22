package messages;

import java.nio.ByteBuffer;

import peers.*;

public class Choke extends Message{
	
	public Choke(int senderID, int receiverID){
		super(senderID, receiverID);
		logPeer = new Peer(receiverID); //create peer object based on receiver ID so log method in Peer class will write to the receiver ID's log file

		
	}
	
	@Override
	public void handle() 
	{
		/*
		 * Update variables: 
		 * 	you are now choked by this peer
		 * 
		 */
		System.out.println("choke message " + senderID + " -> " + receiverID);
		NeighborPeer chokingPeer = Peer.peers.get(this.senderID); 
		chokingPeer.peerChoking = true;
		logPeer.log("choke", receiverID, senderID); //handles log file: Peer, receiverID is choked by Peer, senderID

		
	}
	
	public String encodeMessage()
	{
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(0); 
		byte [] length = b.array(); 
		
		byte [] message = new byte[5];
		message[4] = 0;// type choke
		
		System.arraycopy(length, 0, message, 0, 4); 
		
		return new String(message); 
	}
}
