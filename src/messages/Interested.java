package messages;

import java.util.Random;

import peers.NeighborPeer;
import peers.Peer;

public class Interested extends Message {

	
	public Interested(int senderID, int receiverID)
	{
		super(senderID, receiverID);
	}
	@Override
	public void handle() 
	{
		/*When to send: on receipt of a bitfield or a have that records a piece you don't have
		 * Actions on Receipt: update variables accordingly
		 */
		
		// If interested in peer that unchoked you, send request for a random piece they have that you don't.

				NeighborPeer unchokingPeer = Peer.peers.get(this.senderID); 
				unchokingPeer.peerChoking = true; 	
				
				if(unchokingPeer.amInterested){
					Request request = new Request(senderID, receiverID); 
					
					boolean [] unchokingBitfield = unchokingPeer.bitfield;
					boolean [] myBitfield = Peer.bitfield;
					
					Random random = new Random();
					
					int pieceDex = random.nextInt(myBitfield.length);
					while(!(unchokingBitfield[pieceDex] && !myBitfield[pieceDex])){
						pieceDex = random.nextInt(myBitfield.length);			
					}
					
					request.pieceIndex = pieceDex; 
					
					Peer.sendMessage(request);
					
				}
		// TODO Auto-generated method stub
		
	}

}
