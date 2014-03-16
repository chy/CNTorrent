package messages;
import peers.NeighborPeer;
import peers.Peer;

public class Have extends Message{

	public int pieceIndex; //index of the requested piece
	
	public Have(int senderID, int receiverID, int pieceIndex){
		super(senderID, receiverID);
		this.pieceIndex = pieceIndex; 
	}
	@Override
	public void handle() 
	{
		// TODO Auto-generated method stub
		//when have received a complete piece, 
		
		
		
		//notify all neighbors that you have it (so they can keep your bitfield updated)
		/* have a piece index and set that index to true in the peer's bitfield that has sent that particular 
		 * piece. senderID is given. check if all of the indices in bitfiled is set to true, if so decrement numUnFinishedPeers(Peers class)
		 * 
		 */
		
		NeighborPeer peer = Peer.peers.get(this.senderID);
		
		boolean [] my_bitfield = peer.bitfield;
		
		my_bitfield[pieceIndex] = true;
		
		for(int i = 0; i < my_bitfield.length; i++)
		{
		//true false true	
			if(my_bitfield[i] == false)
			{
				break;
			}
			
			else
			{
				if(i == my_bitfield.length - 1)
				{
					Peer.numUnfinishedPeers--;
				}
				continue;
			}
			
			
		}
		
		
		
		
		
		
		
		
		
		
				
	}
}

