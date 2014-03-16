package messages;

public class Request extends Message {
	
	public int pieceIndex; //index of the requested piece 
	
	public Request(int senderID, int receiverID){
		super(senderID, receiverID);
	}
	@Override
	public void handle() {
		// If sending host is unchoked, send the requested data
		
	}

}
