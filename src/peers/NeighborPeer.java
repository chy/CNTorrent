package peers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import util.Bitfield;

public class NeighborPeer
{

	private Peer peer; // a reference to this process's peer
	final int PEER_ID;
	public boolean isDone;
	public Bitfield bitfield;
	public boolean amConnected = false; // am I connected to this peer?
	public boolean amChoking; // am I choking this peer?
	public boolean amInterested; // am I interested in something this peer has?
	public boolean peerChoking; // is this peer choking me?
	public boolean peerInterested; // is this peer interested in something I have?
	public double datarate;
	final String hostname;
	final int portNumber;
	public Socket socket; // socket for downloading from peers
	public PrintWriter socketOutputStream;
	private BufferedReader socketInputStream;

	private class SocketReader implements Runnable
	{

		private BufferedReader inputStream;

		SocketReader(BufferedReader inputStream)
		{
			this.inputStream = inputStream;
		}

		@Override
		public void run()
		{
			String inputLine;
			while (!peer.allPeersDone())
			{
				try
				{
					inputLine = inputStream.readLine();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
				peer.addToMessageQueue(inputLine, PEER_ID);
			}

			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public NeighborPeer(Peer peer, int peerID, String hostname, int portNumber, boolean isDone, int numPieces)
	{
		this.peer = peer;
		this.PEER_ID = peerID;
		this.hostname = hostname;
		this.portNumber = portNumber;
		this.isDone = isDone;

		bitfield = new Bitfield(numPieces, isDone);
	}

	public void establishConnection()
	{
		try
		{
			socket = new Socket(hostname, portNumber);
			socketOutputStream = new PrintWriter(socket.getOutputStream(), true);
			socketInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch (UnknownHostException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		amConnected = true;

		SocketReader socketReader = new SocketReader(socketInputStream);
		Thread socketReaderThread = new Thread(socketReader);
		socketReaderThread.start();
	}

}
