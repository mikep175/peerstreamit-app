package map.peer.peerstreaming;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import map.peer.core.StreamService;

public class PeerStreamService extends StreamService  {
	
	public PeerStreamService() {
		
		super();
		isEnabled = true;
	}
}
