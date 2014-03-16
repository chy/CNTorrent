package peers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class NeighborPeer
{

	final int PEER_ID;
	public boolean isDone;
	public boolean[] bitfield;
	public boolean amConnected = false; // am I connected to this peer?
	public boolean amChoking; // am I choking this peer?
	public boolean amInterested; // am I interested in something this peer has?
	public boolean peerChoking; // is this peer choking me?
	public boolean peerInterested; // is this peer interested in something I have?
	public double datarate;
	final String hostname;
	final int portNumber;
	public Socket socket; // socket for downloading from peers
	public PrintWriter out;

	public NeighborPeer(int peerID, String hostname, int portNumber, boolean isDone, int numPieces)
	{
		this.PEER_ID = peerID;
		this.hostname = hostname;
		this.portNumber = portNumber;
		this.isDone = isDone;

		bitfield = new boolean[numPieces];
		if (isDone)
		{
			Arrays.fill(bitfield, true);
		}
		else
		{
			Arrays.fill(bitfield, false);
		}
	}

	public void establishConnection()
	{
		try
		{
			socket = new Socket(hostname, portNumber);
			out = new PrintWriter(socket.getOutputStream(), true);
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
	}

}
