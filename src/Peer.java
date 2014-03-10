
import java.util.*;
import java.lang.*;


public class Peer{
	int nPref; //number of preferred peers
	int updatePrefInterval; //update preferred peers every updatePrefInterval seconds
	int opUnchokeInterval;
	String fileName; 
	int fileSize;
	int pieceSize; 
	boolean [] bitfield; //tracks which pieces of the file have been downloaded
	int numUnfinishedPeers; //leave the torrent when this is 0
	HashMap<Integer, NeighborPeer> peers; //Contains 
	int [] preferredPeers; //contains the peer ids of preferred peers
	
	public void obtainConfFiles(){
	// Read Common.cfg and PeerInfo.cfg; set variables appropriately	
	}	
	
	public void joinTorrent(){
	//	open TCP connections and handshake with all previous (already-started) peers in the peer_info conf file
	
	}
	public void leaveTorrent(){
	//Close all connections, exit
	}
	
/*	public void sendMessage(Message m){
		
	}
*/
	public void updatePreferred(){
		/* If the peer hasn't downloaded the whole file: 
		 * 		Calculate the download rate for each interested peer during the previous download interval 
		 * 		(p seconds, unchokeInterval seconds). 
		 * 		Unchoke the top nPref senders; choke anyone else who was unchoked before (except the optimistically unchoked peer).
		 * If the peer has downloaded the whole file: 
		 * 		choose preferred peers randomly from the interested peers. 
		 * 		Unchoke them.
		 * 		choke everyone else except the optimistically unchoked peer.
		 */
		
	}
	
	public void optimisticUnchoke(){
	/* Select a random peer from chocked peers interested in your data; 
	 * unchoke them. (send them an unchoke message, mark them as unchoked). 
	 * Choke the peer that was previously optimistically unchoked.
	 */
		
	}
	
	public void choke(int peerID){
	/*
	 * Chokes the specified PeerID
	 */
		
	}
	
	public void unchoke(int peerID){
	/*
	 * Unchokes the specified PeerID
	 */
		
	}
	public void log(String s){
		
		
	}
	
	
}