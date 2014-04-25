package peers;

import util.Bitfield;

public class PeerData
{

	private final int peerId;
	private final Bitfield bitfield;
	private boolean isDone;
	private boolean choked = true;
	private boolean chokingThisPeer = true;
	private boolean interested = false;
	private double dataRate;
	private String hostName;
	private int portNumber;

	public boolean isDone()
	{
		return isDone;
	}

	public void setDone(boolean isDone)
	{
		this.isDone = isDone;
	}

	public boolean isChoked()
	{
		return choked;
	}

	public void setChoked(boolean choked)
	{
		this.choked = choked;
	}

	public boolean isChokingThisPeer()
	{
		return chokingThisPeer;
	}

	public void setChokingThisPeer(boolean chokingThisPeer)
	{
		this.chokingThisPeer = chokingThisPeer;
	}

	public boolean isInterested()
	{
		return interested;
	}

	public void setInterested(boolean interested)
	{
		this.interested = interested;
	}

	public double getDataRate()
	{
		return dataRate;
	}

	public void setDataRate(double dataRate)
	{
		this.dataRate = dataRate;
	}

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public int getPortNumber()
	{
		return portNumber;
	}

	public void setPortNumber(int portNumber)
	{
		this.portNumber = portNumber;
	}

	public int getPeerId()
	{
		return peerId;
	}

	public Bitfield getBitfield()
	{
		return bitfield;
	}

	public PeerData(int peerId, String hostName, int portNumber, boolean isDone, int numPieces)
	{
		this.peerId = peerId;
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.isDone = isDone;

		bitfield = new Bitfield(numPieces, isDone);
	}

}
