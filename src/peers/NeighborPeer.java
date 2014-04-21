package peers;

import util.Bitfield;

public class NeighborPeer implements Comparable
{

	private Peer peer; // a reference to this process's peer
	final int PEER_ID;
	public boolean isDone;
	public Bitfield bitfield;
	public boolean amConnected = false; // am I connected to this peer?
	public boolean amChoking; // am I choking this peer?
	public boolean amInterested; // am I interested in a piece this peer has?
	public boolean peerChoking; // is this peer choking me?
	public boolean peerInterested; // is this peer interested in a piece I have?
	public double datarate; //just bytes (updated in piece.handle) until datarate is actually calculated in Peer.updatePreferred
	public final String hostname;
	public final int portNumber;

	public NeighborPeer(Peer peer, int peerID, String hostname, int portNumber,
			boolean isDone, int numPieces)
	{
		this.peer = peer;
		this.PEER_ID = peerID;
		this.hostname = hostname;
		this.portNumber = portNumber;
		this.isDone = isDone;

		bitfield = new Bitfield(numPieces, isDone);
	}
	
	public int compareTo(Object o) //reversed so prefpeers can use a max priority queue instead of the default min
	{	NeighborPeer peer = (NeighborPeer) o; 
		if(this.datarate < peer.datarate)
		{
			return 1; 
		}
		if(this.datarate > peer.datarate)
		{
			return -1;
		}
		return 0; 
	}

}
