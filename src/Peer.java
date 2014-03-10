import java.util.*;

import java.lang.*;
import java.io.*;


public class Peer
{
	
	
	int nPref; //number of preferred peers
	int updatePrefInterval; //update preferred peers every updatePrefInterval seconds
	int opUnchokeInterval;
	int peerID; 
	String fileName; 
	int fileSize;
	int pieceSize; 
	boolean [] bitfield; //tracks which pieces of the file have been downloaded
	int numUnfinishedPeers; //leave the torrent when this is 0
	HashMap<Integer, NeighborPeer> peers; //Contains 
	int [] preferredPeers; //contains the peer ids of preferred peers
	
	public Peer(int peerID)
	{
		this.peerID = peerID; //this should be supplied as a command-line parameter when PeerProcess is started
		fileName = "";
		peers = new HashMap<Integer, NeighborPeer>(); 

	}
	
	public void obtainConfFiles(String commonLocation, String peerInfoLocation)
	{
	// Read Common.cfg and PeerInfo.cfg; set variables appropriately
		try
		{
			Scanner scan;
			/*
			reading Common.cfg: 
				NumberOfPreferredNeighbors 2 
				UnchokingInterval 5 
				OptimisticUnchokingInterval 15 
				FileName TheFile.dat 
				FileSize 10000232 
				PieceSize 32768 
			*/
			scan = new Scanner(new File(commonLocation));
			for(int i = 0; i < 5; i++)
			{
				String variable = scan.next();
				if(!variable.equals("FileName"))
				{
					int value = scan.nextInt(); 
					switch(variable)
					{
					case "NumberOfPreferredNeighbors": nPref = value; break;
					case "UnchokingInterval": updatePrefInterval = value; break; 
					case "OptimisticUnchokingInterval": opUnchokeInterval = value; break; 
					case "FileSize": fileSize = value; break;
					case "PieceSize": pieceSize = value; break;
					default: break;					
					}
				}
				else{
					String name = scan.next();
					fileName = name; 
				}
			}		
			scan.close();
		
			//reading PeerInfo.cfg
			//scan = new Scanner(new File(peerInfoLocation)); 
			
		}
		catch(Exception e){
			System.err.println("Error! "+ e.toString());
			System.err.println("Probably a conf file error. ");
	
		}
	}	
	
	public void joinTorrent()
	{
		// open TCP connections and handshake with all previous
		// (already-started) peers in the peer_info conf file

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
		/*
		 * If the peer hasn't downloaded the whole file: Calculate the download
		 * rate for each interested peer during the previous download interval
		 * (p seconds, unchokeInterval seconds). Unchoke the top nPref senders;
		 * choke anyone else who was unchoked before (except the optimistically
		 * unchoked peer). If the peer has downloaded the whole file: choose
		 * preferred peers randomly from the interested peers. Unchoke them.
		 * choke everyone else except the optimistically unchoked peer.
		 */

	}

	public void optimisticUnchoke()
	{
		/*
		 * Select a random peer from chocked peers interested in your data;
		 * unchoke them. (send them an unchoke message, mark them as unchoked).
		 * Choke the peer that was previously optimistically unchoked.
		 */

	}

	public void choke(int peerID)
	{
		/*
		 * Chokes the specified PeerID
		 */

	}

	public void unchoke(int peerID)
	{
		/*
		 * Unchokes the specified PeerID
		 */

	}

	public void log(String s)
	{

	}

}
