import java.util.*;
import java.lang.*;

class NeighborPeer {
	int peerID;
	boolean [] bitfield;
	boolean am_choking; //am I choking this peer?
	boolean am_interested; //am I  interested in something this peer has?
	boolean peer_choking; //is this peer choking me?
	boolean peer_interested; //is this peer interested in something I have?
	double datarate; 
	//ip address if we need it; not sure

}