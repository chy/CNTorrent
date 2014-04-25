package sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import main.PeerProcess;
import messages.Message;

public class ServerSocketHandler
	implements Runnable
{

	private final PeerProcess peerProcess;
	private final int portNumber;
	private final List<ClientSocketHandler> clientSocketHandlers = new ArrayList<ClientSocketHandler>();

	public synchronized void addClientSocketHandler(ClientSocketHandler csh)
	{
		clientSocketHandlers.add(csh);
	}

	public synchronized void sendMessageToPeer(Message m)
	{
		for (ClientSocketHandler csh : clientSocketHandlers)
		{
			if (csh.getPeerId() == m.receiverID)
			{
				csh.sendMessageToPeer(m.encodeMessage());
				return;
			}
		}

		System.err.printf("Could not send message to peer %d: %s%n",
				m.receiverID, m);
	}

	public ServerSocketHandler(PeerProcess peerProcess, int portNumber)
	{
		this.peerProcess = peerProcess;
		this.portNumber = portNumber;
	}

	@Override
	public void run()
	{
		try (ServerSocket serverSocket = new ServerSocket(portNumber))
		{
			while (true)
			{
				Socket clientSocket;
				try
				{
					clientSocket = serverSocket.accept();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					continue;
				}

				ClientSocketHandler csh = new ClientSocketHandler(peerProcess,
						clientSocket);
				clientSocketHandlers.add(csh);
				Thread clientSocketThread = new Thread(csh);
				clientSocketThread.setDaemon(true);
				clientSocketThread.start();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not create server socket", e);
		}
	}

}
