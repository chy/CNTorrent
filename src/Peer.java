import java.io.File;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import messages.Choke;
import messages.Handshake;
import messages.Message;
import messages.Unchoke;

public class Peer
{

	private int nPref; // number of preferred peers
	private int updatePrefInterval; // update preferred peers every updatePrefInterval seconds
	private int opUnchokeInterval;
	public final int PEER_ID;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	private int numPieces;
	private boolean isDone;
	public static boolean[] bitfield; // tracks which pieces of the file have been downloaded
	private int numUnfinishedPeers; // leave the torrent when this is 0
	public static HashMap<Integer, NeighborPeer> peers = new HashMap<Integer, NeighborPeer>(); // tracks pertinant information for neighbor peers of the current peer
	private HashSet<Integer> peersBeforeThis = new HashSet<Integer>(); // peers before this one, for joinTorrent()
	private int[] preferredPeers; // contains the peer ids of preferred peers
	private int optimisticallyUnchokedPeer; // the peer id of the current optimistically-unchoked peer
	private String hostname;
	private int portNumber;
	private ServerSocket serverSocket; // socket for uploading to peers
	
	public Peer(int peerID)
	{
		this.PEER_ID = peerID; // this should be supplied as a command-line parameter when PeerProcess is started
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
	
	private void readConfigFiles()
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
	
	private void readCommonConfig()
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

	private void readPeerInfoConfig()
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
		while ((ID = scan.nextInt()) != PEER_ID)
		{
			peersBeforeThis.add(ID);
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
	private void joinTorrent()
	{
		Iterator<NeighborPeer> iter = peers.values().iterator();
		while (iter.hasNext())
		{
			NeighborPeer neighborPeer = iter.next();
			if (peersBeforeThis.contains(neighborPeer.peerID))
			{
				neighborPeer.establishConnection();
				sendMessage(new Handshake(PEER_ID, neighborPeer.peerID));
			}
		}
	}

	private void leaveTorrent()
	{
		// Close all connections, exit
	}

	public void sendMessage(Message m)
	{
		
	}
	
	public void receiveMessage(Message m)
	{
		m.handle();
	}

	public void updatePreferred()
	{
		/*
		 * If the peer hasn't downloaded the whole file: 
		 * 		Calculate the download rate for each interested peer during the previous download interval (p seconds, unchokeInterval seconds). 
		 * 		Unchoke the top nPref senders;
		 * 		choke anyone else who was unchoked before 
		 * 			(except the optimistically unchoked peer). 
		 * If the peer has downloaded the whole file:
		 * 		 choose preferred peers randomly from the interested peers.
		 * 		 Unchoke them.
		 * 		 choke everyone else except the optimistically unchoked peer.
		 */
		
		//Case 1
		
		
		//Case 2

	}
	
	public void choke(int receiverID)
	{
		/*
		 * Chokes the specified PeerID: 
		 * Updates choked status of the appropriate neighborPeer
		 * sends a choked message to the appropriate peer
		 */
		NeighborPeer toChoke = peers.get(receiverID);
		toChoke.amChoking = true; 
		
		Message choke = new Choke(PEER_ID, receiverID); 
		sendMessage(choke);
	}

	public void unchoke(int receiverID)
	{
		/*
		 * Unchokes the specified PeerID
		 * updates the choked status of the appropriate neighborPeer
		 * sends an unchoke message to the appropriate peer
		 */
		NeighborPeer toUnchoke = peers.get(receiverID);
		toUnchoke.amChoking = false; 
		
		Message unchoke = new Unchoke(PEER_ID, receiverID); 
		sendMessage(unchoke);
	}
	
	public void optimisticUnchoke()
	{
		/*
		 * Select a random peer from chocked peers interested in your data;
		 * unchoke them. (send them an unchoke message, mark them as unchoked).
		 * Choke the peer that was previously optimistically unchoked.
		 */
		
	}

	public void log(String s)
	{ 

	}

}
