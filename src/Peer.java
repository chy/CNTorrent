import java.io.File;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import messages.*;

public class Peer
{

	int nPref; //number of preferred peers
	int updatePrefInterval; //update preferred peers every updatePrefInterval seconds
	int opUnchokeInterval;
	int peerID; 
	String fileName; 
	int fileSize;
	int pieceSize;
	int numPieces;
	boolean isDone;
	boolean [] bitfield; //tracks which pieces of the file have been downloaded
	int numUnfinishedPeers; //leave the torrent when this is 0
	HashMap<Integer, NeighborPeer> peers; //tracks pertinant information for neighbor peers of the current peer
	int [] preferredPeers; //contains the peer ids of preferred peers
	String hostname;
	int portNumber;
	ServerSocket serverSocket; // socket for uploading to peers
	
	public Peer(int peerID)
	{
		this.peerID = peerID; //this should be supplied as a command-line parameter when PeerProcess is started
		peers = new HashMap<Integer, NeighborPeer>();
	}
	
	/**
	 * This method is called by PeerProcess. It will contain the main loop of our
	 * program.
	 */
	public void run()
	{
		// get info from config files
		readConfigFiles();
		// establish connections with all peers above this peer in the peer info config files
		joinTorrent();
	}
	
	public void readConfigFiles()
	{
		readCommonConfig();

		// initialization after reading common config file
		int tempNumPieces = fileSize / pieceSize;
		numPieces = (tempNumPieces * pieceSize == fileSize) ? tempNumPieces : (tempNumPieces + 1);
		bitfield = new boolean[numPieces];

		readPeerInfoConfig();

		// initialization after reading peer info config file
		if (isDone)
		{
			Arrays.fill(bitfield, true);
		}
		else
		{
			Arrays.fill(bitfield, false);
		}
	}
	
	public void readCommonConfig()
	{
		/*
		 * Example Common.cfg file:
		 * NumberOfPreferredNeighbors 2
		 * UnchokingInterval 5
		 * OptimisticUnchokingInterval 15
		 * FileName TheFile.dat
		 * FileSize 10000232
		 * PieceSize 32768
		 */
		String fileLocation = "Common.cfg";
		Scanner scan;
		try
		{
			scan = new Scanner(new File(fileLocation));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException("Cannot find file: " + fileLocation, e);
		}

		for (int i = 0; i < 5; i++)
		{
			String key = scan.next();
			if (!key.equals("FileName"))
			{
				int value = scan.nextInt(); 
				switch (key)
				{
				case "NumberOfPreferredNeighbors": nPref = value; break;
				case "UnchokingInterval": updatePrefInterval = value; break; 
				case "OptimisticUnchokingInterval": opUnchokeInterval = value; break; 
				case "FileSize": fileSize = value; break;
				case "PieceSize": pieceSize = value; break;
				default: break;					
				}
			}
			else
			{
				String name = scan.next();
				fileName = name;
			}
		}

		scan.close();
	}

	public void readPeerInfoConfig()
	{
		/*
		 * Example PeerInfo.cfg:
		 * 1001 sun114-11.cise.ufl.edu 6008 1
		 * 1002 sun114-12.cise.ufl.edu 6008 0
		 * 1003 sun114-13.cise.ufl.edu 6008 0
		 * 1004 sun114-14.cise.ufl.edu 6008 0
		 * 1005 sun114-21.cise.ufl.edu 6008 0
		 * 1006 sun114-22.cise.ufl.edu 6008 0
		 */
		String fileLocation = "PeerInfo.cfg";
		Scanner scan;
		try
		{
			scan = new Scanner(new File(fileLocation));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException("Cannot find file: " + fileLocation, e);
		}

		// iterate through peers that have already been started
		int ID;
		while ((ID = scan.nextInt()) != peerID)
		{
			String neighborHostname = scan.next();
			int neighborPortNumber = scan.nextInt();
			boolean neighborIsDone = (scan.nextInt() == 1);
			NeighborPeer neighborPeer = new NeighborPeer(ID, neighborHostname, neighborPortNumber, neighborIsDone, numPieces);
			peers.put(ID, neighborPeer);
			neighborPeer.establishConnection(); // establish connections with peers before this one
		}

		// we have reached this peer in PeerInfo.cfg
		hostname = scan.next();
		portNumber = scan.nextInt();
		isDone = (scan.nextInt() == 1);

		// get rest of info for peers
		while (scan.hasNext())
		{
			ID = scan.nextInt();
			String neighborHostname = scan.next();
			int neighborPortNumber = scan.nextInt();
			boolean neighborIsDone = (scan.nextInt() == 1);
			NeighborPeer neighborPeer = new NeighborPeer(ID, neighborHostname, neighborPortNumber, neighborIsDone, numPieces);
			peers.put(ID, neighborPeer);
		}

		scan.close();
	}

	/**
	 * Open TCP connections and handshake with all previous peers in the peer
	 * info config file
	 */
	public void joinTorrent()
	{
		/*
		Iterator<NeighborPeer> iter = peers.values().iterator();
		while (iter.hasNext())
		{
			NeighborPeer neighborPeer = iter.next();
			
		}
		*/
	}

	public void leaveTorrent()
	{
		// Close all connections, exit
	}

	public void sendMessage(Message m, int peerID)
	{
		
	}
	
	public void receiveMessage(Message m){
		m.handle();
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
		 * Chokes the specified PeerID: 
		 * Updates choked status of the appropriate neighborPeer
		 * sends a choked message to the appropriate peer
		 */
		NeighborPeer toChoke = peers.get(peerID);
		toChoke.amChoking = true; 
		
		Message choke = new Choke(); 
		sendMessage(choke, peerID);
		
	}

	public void unchoke(int peerID)
	{
		/*
		 * Unchokes the specified PeerID
		 * updates the choked status of the appropriate neighborPeer
		 * sends an unchoke message to the appropriate peer
		 */
		NeighborPeer toUnchoke = peers.get(peerID);
		toUnchoke.amChoking = false; 
		
		Message unchoke = new Unchoke(); 
		sendMessage(unchoke, peerID);
	}

	public void log(String s)
	{

	}

}
