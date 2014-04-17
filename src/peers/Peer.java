package peers;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import messages.BitfieldMessage;
import messages.Choke;
import messages.LogMe;
import messages.Message;
import messages.Unchoke;
import util.Bitfield;

public class Peer
{

	private int nPref; // number of preferred peers
	private int updatePrefInterval; // update preferred peers every updatePrefInterval seconds
	private int opUnchokeInterval; // update optimistically-unchoked peer every opUnchokeInterval seconds
	public final int PEER_ID;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	public static int numPieces;
	private boolean isDone; // when THIS peer is done downloading the file
	
	public static Bitfield bitfield; // tracks which pieces of the file have been downloaded
	public static int numUnfinishedPeers; // leave the torrent when this is 0
	public static HashMap<Integer, NeighborPeer> peers = new HashMap<Integer, NeighborPeer>(); // tracks pertinant information for neighbor peers of the current peer
	private List<NeighborPeer> peersBeforeThis = new ArrayList<NeighborPeer>(); // peers before this one, for joinTorrent()
	private List<NeighborPeer> peersAfterThis = new ArrayList<NeighborPeer>(); // peers after this one, for joinTorrent()
	private int numPeers; // number of peers, including this peer
	private int[] preferredPeers; // contains the peer ids of preferred peers
	private int optimisticallyUnchokedPeer; // the peer id of the current optimistically-unchoked peer
	private String hostname;
	private int portNumber;
	private final AtomicBoolean allPeersDone = new AtomicBoolean(false);
	private BlockingQueue<MessagePair> messageQueue = new LinkedBlockingQueue<MessagePair>();
	private LogMe logFile = new LogMe(); // this will be the object that handles Log
	

	private long lastPreferredUpdateTime; // in milliseconds
	private long lastOpUnchokeUpdateTime; // in milliseconds

	private class MessagePair
	{

		final String messageString;
		final int senderID;

		MessagePair(String messageString, int senderID)
		{
			this.messageString = messageString;
			this.senderID = senderID;
		}

	}

	public synchronized boolean allPeersDone()
	{
		return allPeersDone.get();
	}

	private synchronized void setAllPeersDone(boolean allPeersDone)
	{
		this.allPeersDone.getAndSet(allPeersDone);
	}

	public synchronized void addToMessageQueue(String messageString, int senderID)
	{
		messageQueue.add(new MessagePair(messageString, senderID));
	}

	public Peer(int peerID)
	{
		this.PEER_ID = peerID; // this should be supplied as a command-line parameter when PeerProcess is started
		logFile.createFile(PEER_ID); //this will create a logFile for a Peer when a Peer is created
	}
	
	/**
	 * This method is called by PeerProcess. It will contain the main loop of our
	 * program.
	 */
	public void run()
	{
		// get info from config files
		System.out.println("Reading config files");
		readConfigFiles();

		// establish connections with all peers above this peer in the peer info config files
		System.out.println("Establishing connections with peers already in the torrent");
		joinTorrent();

		lastPreferredUpdateTime = System.currentTimeMillis();
		lastOpUnchokeUpdateTime = System.currentTimeMillis();

		// main loop
		System.out.println("Start main loop");
		while (numUnfinishedPeers != 0)
		{
			MessagePair messagePair;
			try
			{
				// try to get a message from buffer for 5 seconds
				System.out.println("Trying to get a message from buffer");
				messagePair = messageQueue.poll(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}

			if ((System.currentTimeMillis() - lastPreferredUpdateTime) / 1000 >= updatePrefInterval)
			{
				System.out.println("Updating preferred neighbors");
				updatePreferred();
				lastPreferredUpdateTime = System.currentTimeMillis();
			}

			if ((System.currentTimeMillis() - lastOpUnchokeUpdateTime) / 1000 >= opUnchokeInterval)
			{
				System.out.println("Updating optimistically-unchoked neighbor");
				optimisticUnchoke();
				lastOpUnchokeUpdateTime = System.currentTimeMillis();
			}

			if (messagePair != null)
			{
				String messageString = messagePair.messageString;
				int senderID = messagePair.senderID;
				Message m = Message.decodeMessage(messageString, senderID, PEER_ID);
				System.out.println("Executing a message: " + messageString);
				executeMessage(m);
			}
		}

		// notify all sockets that we are done
		System.out.println("We are done");
		setAllPeersDone(true);

		// when all peers have downloaded the file, leave the torrent
		System.out.println("Leave torrent");
		leaveTorrent();
	}

	private void readConfigFiles()
	{
		// read common config file
		readCommonConfig();

		// initialization after reading common config file
		int tempNumPieces = fileSize / pieceSize;
		numPieces = (tempNumPieces * pieceSize == fileSize) ? tempNumPieces : (tempNumPieces + 1);

		// read peer info config file
		readPeerInfoConfig();

		// initialization after reading peer info config file
		bitfield = new Bitfield(numPieces, isDone);
		numUnfinishedPeers = numPeers;
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

		for (int i = 0; i < 6; i++)
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
			String neighborHostname = scan.next();
			int neighborPortNumber = scan.nextInt();
			boolean neighborIsDone = (scan.nextInt() == 1);
			NeighborPeer neighborPeer = new NeighborPeer(this, ID, neighborHostname, neighborPortNumber, neighborIsDone, numPieces);
			peers.put(ID, neighborPeer);
			peersBeforeThis.add(neighborPeer);
			numPeers++;
		}

		// we have reached this peer in PeerInfo.cfg
		hostname = scan.next();
		portNumber = scan.nextInt();
		isDone = (scan.nextInt() == 1);

		numPeers++;

		// get rest of info for peers
		while (scan.hasNext())
		{
			ID = scan.nextInt();
			String neighborHostname = scan.next();
			int neighborPortNumber = scan.nextInt();
			boolean neighborIsDone = (scan.nextInt() == 1);
			NeighborPeer neighborPeer = new NeighborPeer(this, ID, neighborHostname, neighborPortNumber, neighborIsDone, numPieces);
			peers.put(ID, neighborPeer);
			peersAfterThis.add(neighborPeer);
			numPeers++;
		}

		scan.close();
	}

	/**
	 * Open TCP connections and handshake with all previous peers in the peer
	 * info config file
	 */
	private void joinTorrent()
	{
		for (NeighborPeer neighborPeer : peersBeforeThis)
		{
			System.out.println("Establish connection with " + neighborPeer.PEER_ID);
			neighborPeer.establishConnection();
			sendMessage(new BitfieldMessage(PEER_ID, neighborPeer.PEER_ID, Peer.bitfield));
		}

		for (NeighborPeer neighborPeer : peersAfterThis)
		{
			System.out.println("Wait for connection with peer " + neighborPeer.PEER_ID);
			neighborPeer.waitForConnection();
		}
	}

	private void leaveTorrent()
	{
		// Close all connections, exit
	}

	public static void sendMessage(Message m)
	{
		NeighborPeer neighborPeer = peers.get(m.receiverID);
		String encodedMessage = m.encodeMessage();
		neighborPeer.sendMessageToPeer(encodedMessage);
	}
	
	private void executeMessage(Message m)
	{
		m.handle();
	}

	public void updatePreferred()
	{
		/*
		 * If this peer hasn't downloaded the whole file: 
		 * 		Calculate the download rate for each interested peer during the previous download interval (p seconds, unchokeInterval seconds). 
		 * 			add bytes downloaded to datarate on receipt of each piece (in piece message) 
		 * 			in prefpeers: datarate /= prefpeersinterval (for each interested peer)
		 * 			use the datarate, then reset to 0. 
		 * 			
		 * 		Unchoke the top nPref senders;
		 * 		choke anyone else who was unchoked before 
		 * 			(except the optimistically unchoked peer). 
		 * If this peer has downloaded the whole file:
		 * 		 choose preferred peers randomly from the interested peers.
		 * 		 Unchoke them.
		 * 		 choke everyone else except the optimistically unchoked peer.
		 */
		
		//Case 1
		if(!isDone){
			
			PriorityQueue<NeighborPeer> queue = new PriorityQueue<NeighborPeer>(); 
			
			//FOR EACH INTERESTED PEER
			for(NeighborPeer peer : peers.values()){
				if(peer.peerInterested){
					queue.add(peer); 
				}
				peer.datarate = 0; 
			}
			for(int i = 0; i < nPref; i++){
				NeighborPeer p = queue.poll(); 
				if(p!= null){
					preferredPeers[i] = p.PEER_ID; 
					unchoke(p.PEER_ID);
				}
				else{
					System.err.println("Error! peer.prefpeers trying to add an unconnected-to peer to preferred peers"); 
				}
			}
			while(queue.peek() != null){
				NeighborPeer p = queue.poll();
				if(p != null){
					choke(queue.poll().PEER_ID); 
				}else{
					System.err.println("Error! Peer.prefpeers trying to choke an unconnectedto peer"); 			
				}
			}
		}
		
		//Case 2
		else{
			Random rand = new Random(); 
			Integer [] peerIDs = new Integer[peers.size()];
			peerIDs = peers.keySet().toArray(peerIDs);
			knuthshuffle(peerIDs); 
			
			int nPrefDex = 0; 
			
			for(int i = 0; i < peerIDs.length; i++){
				NeighborPeer peer = peers.get(peerIDs[i]); 
				if(peer.peerInterested && nPrefDex < preferredPeers.length){
					preferredPeers[nPrefDex++] = peer.PEER_ID; 
					unchoke(peer.PEER_ID); 					
					break;
				}		
				else{
					choke(peer.PEER_ID); 
				}
				peer.datarate = 0; 
			}
			
		}

	}
	
	public void choke(int receiverID)
	{
		/*
		 * Chokes the specified PeerID: 
		 * Updates choked status of the appropriate neighborPeer
		 * sends a choked message to the appropriate peer
		 */
		NeighborPeer toChoke = peers.get(receiverID);

		if (!toChoke.amConnected)
		{
			// we are not yet connected to this peer
			return;
		}

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
		Random rand = new Random(); 
		Integer [] peerIDs = new Integer[peers.size()];
		peerIDs = peers.keySet().toArray(peerIDs);
		knuthshuffle(peerIDs); 
		
	
		for(int i = 0; i < peerIDs.length; i++){
			NeighborPeer peer = peers.get(peerIDs[i]); 
			if(peer.peerInterested && peer.amChoking){
				
				optimisticallyUnchokedPeer = peerIDs[i]; 
				unchoke(optimisticallyUnchokedPeer); 
				
				break;
			}		
		}
				
	}
	
	public void knuthshuffle(Integer [] arr){
		Random rand = new Random(); 
		
		for(int i = arr.length-1; i > 0; i--){
			int j = rand.nextInt(i); 
			int temp = arr[j];
			arr[j] = arr[i];
			arr[i] = temp; 
		}
	}
	
	public void log(String s)
	{ 
		String time = time() + ":";
				
		switch(s){
		case "TCP":
			logFile.logger.info(time + "Peer" + PEER_ID + " makes a connection to Peer ");
			break;
		case "neighbor_change":
			break;
		case "opt_unchoke_neighbor_change":
			break;
		case "unchoke":
			break;
		case "choke":
			break;
		case "receive_have":
			break;
		case "receive_interested":
			break;
		case "receive_noInterest":
			break;
		case "download_piece":
			break;
		case "download_complete":
			break;
		}

	}
	
	public String time()
	{
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		return sdf.format(cal.getTime());
	}

}
