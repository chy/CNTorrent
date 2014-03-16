package messages;

public class Choke extends Message{
	
	public Choke(int senderID){
		super(senderID);
	}
	
	@Override
	public void handle() 
	{
		/*
		 * Update variables: 
		 * 	you are now choked by this peer
		 * 
		 */
		
	}

}
