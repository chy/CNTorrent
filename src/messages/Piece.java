package messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import peers.Peer;

public class Piece extends Message{
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
		
		
	}
	
	public void writePieceToFile(){
		String filename = "";
	//	FileOutputStream out = new FileOutputStream(); 
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
				
	}
	
	public int getPieceIndex(){
		return this.pieceIndex; 
	}
	
}
