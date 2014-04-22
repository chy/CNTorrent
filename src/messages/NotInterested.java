package messages;

import java.nio.ByteBuffer;

import peers.Peer;

public class NotInterested extends Message {

	public NotInterested (int senderID, int receiverID){
		super(senderID, receiverID);
		logPeer = new Peer(receiverID); //create peer object based on receiver ID so log method in Peer class will write to the receiver ID's log file
		
	}
	public void handle()
	{
		//update variables accordingly; sender is not interested in receiver's stuff
		System.out.println("not interested message " + senderID + " -> " + receiverID);
		Peer.peers.get(senderID).peerInterested = false; 
		logPeer.log("receive_noInterest", receiverID, senderID);//handles log file: Peer, receiverID received a not interested message from Peer, senderID

		
	}
	public String encodeMessage()
	{
		byte [] length = (ByteBuffer.allocate(4)).putInt(0).array(); 
		
		byte [] message = new byte[5];
		message[4] = 3;// type not interested
		
		System.arraycopy(length, 0, message, 0, 4); 
		
		return new String(message); 
	}	
}
