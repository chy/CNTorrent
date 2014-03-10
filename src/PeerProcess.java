public class PeerProcess
{

	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java peerProcess <peer_ID>");
		}

		int peerID = Integer.parseInt(args[0]);

		PeerProcess peerProcess = new PeerProcess();
		peerProcess.run(peerID); // so every method doesn't have to be static
	}

	public void run(int peerID)
	{
		
	}

}