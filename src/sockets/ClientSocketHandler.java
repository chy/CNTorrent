package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import main.PeerProcess;
import messages.Handshake;
import messages.Message;

public class ClientSocketHandler
	implements Runnable
{

	private final PeerProcess peerProcess;
	private final Socket clientSocket;
	private final PrintWriter outputStream;
	private int peerId = -1;

	public int getPeerId()
	{
		return peerId;
	}

	public ClientSocketHandler(PeerProcess peerProcess, Socket clientSocket)
	{
		this.peerProcess = peerProcess;
		this.clientSocket = clientSocket;
		try
		{
			outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void sendMessageToPeer(String encodedMessage)
	{
		System.out.println("Sending message: " + encodedMessage);
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
				if (peerId == -1)
				{
					Message m = Message.decodeMessage(messageString, -1,
							peerProcess.getThisPeerId());
					if (m instanceof Handshake)
					{
						// received handshake
						System.out.println("Connected to peer " + m.senderID);
						peerId = m.senderID;

						// send handshake back
						Handshake handshake = new Handshake(
								peerProcess.getThisPeerId(), m.senderID);
						String encodedMessage = handshake.encodeMessage();
						System.out.println("Sending message " + encodedMessage);
						sendMessageToPeer(encodedMessage);
					}
				}
				else
				{
					peerProcess.addMessageToQueue(messageString, peerId);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
