package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import messages.Choke;
import messages.Message;
import messages.Unchoke;
import peers.PeerData;
import sockets.ClientSocketHandler;
import sockets.ServerSocketHandler;

public class PeerProcess
{

	private PeerData thisPeer;
	private final int thisPeerId;
	private final List<PeerData> neighborPeers = new ArrayList<PeerData>();
	private final HashMap<Integer, Integer> peerIdMap = new HashMap<Integer, Integer>();

	private int numPreferred;
	private int updatePreferredInterval;
	private int optimisticUnchokeInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	private int numPieces;

	private int numUnfinishedPeers;
	private final List<PeerData> peersBeforeThisPeer = new ArrayList<PeerData>();
	private final List<PeerData> preferredPeers = new ArrayList<PeerData>();
	private PeerData optimisticallyUnchokedPeer;
	private final BlockingQueue<MessagePair> messageQueue = new LinkedBlockingQueue<MessagePair>();

	private static ServerSocketHandler serverSocketHandler;

	private long lastPreferredUpdateTime; // in milliseconds
	private long lastOpUnchokeUpdateTime; // in milliseconds

	public int getThisPeerId()
	{
		return thisPeerId;
	}

	private PeerData getPeer(int peerId)
	{
		return neighborPeers.get(peerIdMap.get(peerId));
	}

	private PeerProcess(int thisPeerId)
	{
		this.thisPeerId = thisPeerId;
	}

	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.err.println("Usage: java PeerProcess <peer_ID>");
			System.exit(1);
		}

		int peerId = Integer.parseInt(args[0]);
		PeerProcess peerProcess = new PeerProcess(peerId);
		peerProcess.run();
	}

	private void run()
	{
		readCommonConfig();

		int tempNumPieces = fileSize / pieceSize;
		numPieces = (tempNumPieces * pieceSize == fileSize) ? tempNumPieces
				: (tempNumPieces + 1);

		readPeerInfoConfig();

		// create server socket handler
		serverSocketHandler = new ServerSocketHandler(this,
				thisPeer.getPortNumber());

		joinTorrent();

		Thread serverSocketHandlerThread = new Thread(serverSocketHandler,
				"Server Socket Handler Thread");
		serverSocketHandlerThread.setDaemon(true);
		serverSocketHandlerThread.start();

		long lastPreferredUpdateTime = System.currentTimeMillis(); // in milliseconds
		long lastOpUnchokeUpdateTime = System.currentTimeMillis(); // in milliseconds

		// main loop
		System.out.println("Starting main loop");
		while (numUnfinishedPeers != 0)
		{
			MessagePair messagePair;
			try
			{
				// try to get a message from queue for 5 seconds
				System.out.println("Trying to get a message from queue");
				messagePair = messageQueue.poll(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}

			if ((System.currentTimeMillis() - lastPreferredUpdateTime) / 1000 >= updatePreferredInterval)
			{
				updatePreferred();
				lastPreferredUpdateTime = System.currentTimeMillis();
			}

			if ((System.currentTimeMillis() - lastOpUnchokeUpdateTime) / 1000 >= optimisticUnchokeInterval)
			{
				optimisticUnchoke();
				lastOpUnchokeUpdateTime = System.currentTimeMillis();
			}

			if (messagePair != null)
			{
				String messageString = messagePair.messageString;
				int senderID = messagePair.senderId;
				Message m = Message.decodeMessage(messageString, senderID, thisPeerId);
				executeMessage(m);
			}
		}
		
		System.out.println("We are done");
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
		Scanner in;
		try
		{
			in = new Scanner(new File(fileLocation));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException("Cannot find file: " + fileLocation, e);
		}

		while (in.hasNext())
		{
			String key = in.next();
			if (!key.equals("FileName"))
			{
				int value = in.nextInt(); 
				switch (key)
				{
				case "NumberOfPreferredNeighbors": numPreferred = value; break;
				case "UnchokingInterval": updatePreferredInterval = value; break;
				case "OptimisticUnchokingInterval": optimisticUnchokeInterval = value; break;
				case "FileSize": fileSize = value; break;
				case "PieceSize": pieceSize = value; break;
				default: break;
				}
			}
			else
			{
				String value = in.next();
				fileName = value;
			}
		}

		in.close();
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
		Scanner in;
		try
		{
			in = new Scanner(new File(fileLocation));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException("Cannot find file: " + fileLocation, e);
		}

		int peerId;
		String hostName;
		int portNumber;
		boolean isDone;

		// iterate through peers that have already been started
		while ((peerId = in.nextInt()) != thisPeerId)
		{
			hostName = in.next();
			portNumber = in.nextInt();
			isDone = (in.nextInt() == 1);
			if (!isDone)
			{
				numUnfinishedPeers++;
			}
			PeerData neighborPeer = new PeerData(peerId, hostName, portNumber,
					isDone, numPieces);
			peerIdMap.put(peerId, neighborPeers.size());
			neighborPeers.add(neighborPeer);
			peersBeforeThisPeer.add(neighborPeer);
		}

		// we have reached this peer in PeerInfo.cfg
		hostName = in.next();
		portNumber = in.nextInt();
		isDone = (in.nextInt() == 1);
		if (!isDone)
		{
			numUnfinishedPeers++;
		}
		thisPeer = new PeerData(peerId, hostName, portNumber, isDone,
				numPieces);
		peerIdMap.put(peerId, neighborPeers.size());

		// get rest of info for peers
		while (in.hasNext())
		{
			peerId = in.nextInt();
			hostName = in.next();
			portNumber = in.nextInt();
			isDone = (in.nextInt() == 1);
			if (!isDone)
			{
				numUnfinishedPeers++;
			}
			PeerData neighborPeer = new PeerData(peerId, hostName, portNumber, isDone, numPieces);
			peerIdMap.put(peerId, neighborPeers.size());
			neighborPeers.add(neighborPeer);
		}

		in.close();
	}

	/**
	 * Open TCP connections and handshake with all previous peers in the peer
	 * info config file
	 */
	private void joinTorrent()
	{
		int count = 0;
		for (PeerData neighborPeer : peersBeforeThisPeer)
		{
			System.out.println("Establish connection with peer " + neighborPeer.getPeerId());

			Socket clientSocket;
			try
			{
				clientSocket = new Socket(neighborPeer.getHostName(),
						neighborPeer.getPortNumber());
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				continue;
			}

			ClientSocketHandler csh = new ClientSocketHandler(this, clientSocket);
			serverSocketHandler.addClientSocketHandler(csh);
			Thread clientSocketThread = new Thread(csh, "Client Socket Thread " + count++);
			clientSocketThread.setDaemon(true);
			clientSocketThread.start();
		}
	}

	private void updatePreferred()
	{
		List<PeerData> oldPreferredPeers = new ArrayList<PeerData>(preferredPeers);
		preferredPeers.clear();

		if (!thisPeer.isDone())
		{
			PriorityQueue<PeerData> queue = new PriorityQueue<PeerData>();

			for (PeerData peer : neighborPeers)
			{
				if (peer != null)
				{
					if (peer.isInterested())
					{
						// TODO calculate data rate
						queue.add(peer);
					}
				}
			}

			for (int i = 0; i < queue.size(); i++)
			{
				PeerData peer = queue.poll();
				if (peer == null)
				{
					break;
				}

				preferredPeers.add(peer);
				oldPreferredPeers.remove(peer);
				unchoke(peer);
			}
		}
		else
		{
			int[] peerIds = new int[neighborPeers.size()];
			for (int i = 0; i < peerIds.length; i++)
			{
				peerIds[i] = neighborPeers.get(i).getPeerId();
			}

			knuthShuffle(peerIds);

			for (int i = 0; i < peerIds.length && preferredPeers.size() < numPreferred; i++)
			{
				PeerData peer = getPeer(peerIds[i]);
				if (peer.isInterested())
				{
					preferredPeers.add(peer);
					oldPreferredPeers.remove(peer);
					unchoke(peer);
				}
			}
		}

		for (PeerData peer : oldPreferredPeers)
		{
			choke(peer);
		}
	}

	private void optimisticUnchoke()
	{
		int[] peerIds = new int[neighborPeers.size()];
		for (int i = 0; i < peerIds.length; i++)
		{
			peerIds[i] = neighborPeers.get(i).getPeerId();
		}

		knuthShuffle(peerIds);

		for (int i = 0; i < peerIds.length; i++)
		{
			PeerData peer = getPeer(peerIds[i]);
			if (peer.isInterested() && peer.isChoked())
			{

				optimisticallyUnchokedPeer = peer;
				unchoke(optimisticallyUnchokedPeer);
				break;
			}
		}

	}

	private void choke(PeerData peer)
	{
		peer.setChoked(true); 

		Choke choke = new Choke(thisPeerId, peer.getPeerId()); 
		sendMessage(choke);
	}

	private void unchoke(PeerData peer)
	{
		peer.setChoked(false);

		Unchoke unchoke = new Unchoke(thisPeerId, peer.getPeerId()); 
		sendMessage(unchoke);
	}

	private void knuthShuffle(final int[] arr)
	{
		Random rand = new Random();

		for (int i = arr.length - 1; i > 0; i--)
		{
			int j = rand.nextInt(i);
			int temp = arr[j];
			arr[j] = arr[i];
			arr[i] = temp;
		}
	}

	public synchronized void addMessageToQueue(String messageString, int senderId)
	{
		messageQueue.add(new MessagePair(messageString, senderId));
	}

	public static void sendMessage(Message m)
	{
		serverSocketHandler.sendMessageToPeer(m);
	}

	private void executeMessage(Message m)
	{
		m.handle();
	}

	private class MessagePair
	{

		final String messageString;
		final int senderId;

		MessagePair(String messageString, int senderId)
		{
			this.messageString = messageString;
			this.senderId = senderId;
		}

	}

}
