import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

class NeighborPeer
{

	int peerID;
	boolean isDone;
	boolean[] bitfield;
	boolean amChoking; // am I choking this peer?
	boolean amInterested; // am I interested in something this peer has?
	boolean peerChoking; // is this peer choking me?
	boolean peerInterested; // is this peer interested in something I have?
	double datarate;
	final String hostname;
	final int portNumber;
	Socket socket; // socket for downloading from peers

	public NeighborPeer(int peerID, String hostname, int portNumber, boolean isDone, int numPieces)
	{
		this.peerID = peerID;
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
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
