import java.io.IOException;

import peers.Peer;

class PeerProcess
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			System.err.println("Usage: java PeerProcess <peer_ID>");
			System.exit(1);
		}

		int peerID = Integer.parseInt(args[0]);
		Peer peer = new Peer(peerID);
		peer.run();
	}

}
