package map.peer.core;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.SchemeTypeBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.*;
import com.googlecode.mp4parser.authoring.*;
import com.googlecode.mp4parser.util.Path;
import com.koushikdutta.async.*;
import com.koushikdutta.async.http.*;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket.StringCallback;

import android.util.Log;

public class WorkerThread implements Runnable {

	public static final String TAG = "map.peer.core.WorkerThread";
	
	IBuilder mapFrag; 
	
	
	public boolean isStopped = false;
	
	public void stop() {
		
		mapFrag.stop();
		isStopped = true;
		Thread.currentThread().interrupt();
	}

	WebSocket webSocket; String fileUrl; String nsi; DBHelper dbHelper; String contentType;
	
	public WorkerThread(String fileUrl, String nsi, DBHelper dbHelper, String contentType) {
		
		super();
		
		this.fileUrl = fileUrl;
		this.nsi = nsi;
		this.dbHelper = dbHelper;
		this.contentType = contentType;
	}
	
	@Override
	public void run()  {
		
		Log.d("Opening file: " + fileUrl, TAG);


		if(contentType.equals("3")) {

			MapFragmentedMp4Builder mp4 = new MapFragmentedMp4Builder();
			mp4.isAudio = true;

			mapFrag = mp4;
		}
		else if(contentType.equals("1")) {
		
			mapFrag = new MapFragmentedMp4Builder();
			
		} else {
			
			mapFrag = new ImageTransfer();
			
		}
		
		mapFrag.controlLoop(fileUrl, nsi, dbHelper);
		
	    stop();
	}

	}

