package messages;

import java.nio.ByteBuffer;

import peers.*;

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
