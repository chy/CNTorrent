package messages;

public class Request extends Message {
	
	public int pieceIndex; //index of the requested piece 
	
	public Request(int senderID, int receiverID){
		super(senderID, receiverID);
	}
	@Override
	public void handle() {
		// TODO Auto-generated method stub
		
	}

}
