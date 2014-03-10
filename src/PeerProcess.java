public class PeerProcess
{

	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java peerProcess <peer_ID>");
		}

		int peerID = Integer.parseInt(args[0]);
		Peer peer = new Peer(peerID);
		peer.run();
	}

}