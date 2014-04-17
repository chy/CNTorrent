package peers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
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
	public boolean amInterested; // am I interested in a piece this peer has?
	public boolean peerChoking; // is this peer choking me?
	public boolean peerInterested; // is this peer interested in a piece I have?
	public double datarate;
	private final String hostname;
	private final int portNumber;
	private Socket socket; // socket for downloading from peers
	private PrintWriter socketOutputStream;

	private class SocketReader
		implements Runnable
	{

		private BufferedReader inputStream;

		private SocketReader(BufferedReader inputStream)
		{
			this.inputStream = inputStream;
		}

		@Override
		public void run()
		{
			while (!peer.allPeersDone())
			{
				String inputLine;
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
				socketOutputStream.close();
				socket.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

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

	public void establishConnection()
	{
		BufferedReader socketInputStream;
		try
		{
			socket = new Socket(hostname, portNumber);
			socketOutputStream = new PrintWriter(socket.getOutputStream(), true);
			socketInputStream = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
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

		startSocketReader(socketInputStream);
	}

	public void waitForConnection()
	{
		BufferedReader socketInputStream;
		try (ServerSocket serverSocket = new ServerSocket(portNumber))
		{
			socket = serverSocket.accept();
			socketOutputStream = new PrintWriter(socket.getOutputStream(), true);
			socketInputStream = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		amConnected = true;

		startSocketReader(socketInputStream);
	}

	private void startSocketReader(BufferedReader inputStream)
	{
		SocketReader socketReader = new SocketReader(inputStream);
		Thread socketReaderThread = new Thread(socketReader);
		socketReaderThread.start();
	}

	public void sendMessageToPeer(String encodedMessage)
	{
		socketOutputStream.println(encodedMessage);
	}

}
