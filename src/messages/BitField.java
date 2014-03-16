package messages;

public class BitField extends Message {
	public BitField(int senderID, int receiverID){
		super(senderID, receiverID);
	}
	@Override
	public void handle() {
		// If  (B ^ ~ A) != 0, send interested to host B. Else, send not interested.
		
	}
	

}
