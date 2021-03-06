package peers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientSocketHandler
	implements Runnable
{

	private Socket clientSocket;
	private PrintWriter outputStream;
	private Peer peer;
	private int peerID;

	public int getPeerID()
	{
		return peerID;
	}

	ClientSocketHandler(Socket clientSocket, Peer peer, int peerID)
	{
		this.clientSocket = clientSocket;
		this.peer = peer;
		this.peerID = peerID;
		try
		{
			outputStream =  new PrintWriter(clientSocket.getOutputStream(), true);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void sendMessageToPeer(String encodedMessage)
	{
		outputStream.println(encodedMessage);
	}

	@Override
	public void run()
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream())))
		{
			String messageString;
			while ((messageString = in.readLine()) != null)
			{
				peer.addToMessageQueue(messageString, peerID);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
