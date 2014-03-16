package messages;

public class Piece extends Message{
	public int pieceIndex; //id of the piece being sent
	
	public Piece (int senderID, int receiverID){
		super(senderID, receiverID);
	}
	@Override
	public void handle() {
		// TODO Auto-generated method stub
		
	}
	

}
