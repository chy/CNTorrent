import java.util.*;
import java.lang.*;

public class Peer
{

	int nPref; // number of preferred peers
	int updatePrefInterval; // update preferred peers every updatePrefInterval
							// seconds
	int opUnchokeInterval;
	String fileName;
	int fileSize;
	int pieceSize;
	boolean[] bitfield; // tracks which pieces of the file have been downloaded
	int numUnfinishedPeers; // leave the torrent when this is 0
	HashMap<Integer, NeighborPeer> peers; // Contains
	int[] preferredPeers; // contains the peer IDs of preferred peers

	public void obtainConfFiles()
	{
		// Read Common.cfg and PeerInfo.cfg; set variables appropriately
	}

	public void joinTorrent()
	{
		// pen TCP connections and handshake with all previous (already-started)
		// peers in the peer_info conf file

	}

	public void leaveTorrent()
	{
		// Close all connections, exit
	}

	public void sendMessage(Message m)
	{

	}

	public void updatePreferred()
	{

	}

	public void optimisticUnchoke()
	{

	}

	public void choke()
	{

	}

	public void unchoke()
	{

	}

	public void log(String s)
	{

	}

}
