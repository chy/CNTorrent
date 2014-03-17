package messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import peers.NeighborPeer;
import peers.Peer;
import util.Bitfield;

// UNFINISHED, but compiles. 
public class Piece extends Message
{

	private int pieceIndex; //id of the piece being sent
	public byte [] piece; //byte array of the contents of the piece to be sent

	public Piece (int senderID, int receiverID, int pieceIndex){
		super(senderID, receiverID);
		this.pieceIndex = pieceIndex;
		loadPiece(pieceIndex);
	}
	
	//Recieve methods: Called when a piece message is received
	@Override
	public void handle() {
		/*Two things: 
		 * 	Update variables: 
		 * 			store the piece
		 * 			update bitfield
		 * 			update interested
		 * 			send have to all neighbors
		 * 	If you are still unchoked and are still interested in something the sender has:
		 * 			 request another random piece that you are interested in from it	 * 
		 * 
		 */
		//PART 1
		
		writePieceToFile(piece);
		
		NeighborPeer sendingPeer = Peer.peers.get(this.senderID); 
		Bitfield myBitfield = Peer.bitfield;
		myBitfield.setPiece(pieceIndex, true);
		
		for(NeighborPeer peer : Peer.peers.values()){ //for each peer
			//check if you are interested in them
			for(int i = 0; i < Peer.numPieces; i++){
				if(peer.bitfield.hasPiece(i) && !myBitfield.hasPiece(i)){
					peer.amInterested = true;
					break; 
				}
			}
			//send them a have message
			Peer.sendMessage(new Have(receiverID, senderID, pieceIndex)); 
		}
		
		//PART 2
		if(!sendingPeer.peerChoking && sendingPeer.amInterested){
			
			Random random = new Random();
			int pieceIndex = random.nextInt(Peer.numPieces);
			while(!(sendingPeer.bitfield.hasPiece(pieceIndex) && !myBitfield.hasPiece(pieceIndex))){
				pieceIndex = random.nextInt(Peer.numPieces);			
			}
			Peer.sendMessage(new Request(receiverID, senderID, pieceIndex)); 
		}
	}
	
	public void writePieceToFile(byte[] piece){
		String fileLocation = "/src/peer_"+senderID+"/"+pieceIndex;
		FileOutputStream out; 
		try{
			out = new FileOutputStream(new File(fileLocation)); 
			out.write(piece);
			out.close();
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException("Cannot find file: " + fileLocation, e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write to file: " + fileLocation, e);
		}
		
	}
	
	
	//Send methods: Called before a piece message needs to be sent
	public void loadPiece(int pieceIndex){
	//parses the specified piece into the byte array to be sent. 
		this.pieceIndex = pieceIndex; 
		
		String fileLocation = "/src/peer_"+senderID+"/"+pieceIndex;
		Scanner scan;
		try
		{
			scan = new Scanner(new File(fileLocation));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException("Cannot find file: " + fileLocation, e);
		}
		StringBuilder pieceBuffer= new StringBuilder();
		while(scan.hasNext()){
			pieceBuffer.append(scan.next()); 
		}
		piece = pieceBuffer.toString().getBytes();
		
		scan.close(); 		
	}
	
	public int getPieceIndex(){
		return this.pieceIndex; 
	}
	
}
