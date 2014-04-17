package messages;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogMe {
	
	public int peer_ID;
	Logger logger = Logger.getLogger("TorrentLog");
	FileHandler fh;
	
	
	public void createFile( int peer_ID)
	{
		String peerID = "log_peer_" + Integer.toString(peer_ID);
		String fileLocation = "C:/Desktop/project/" + peerID + ".log";
		
		
		try {
			//This will configure the torrent log with handler and formatting
			
			fh = new FileHandler(fileLocation);	//file will be created in this location
			logger.addHandler(fh);
			logger.setLevel(Level.INFO); //set level to only dislay SEVERE, Warning, and info type messages
			
			//provides very simple formatting for the logs
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		}
		catch (SecurityException e){
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		}
	}


