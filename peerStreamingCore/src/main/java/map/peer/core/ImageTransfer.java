package map.peer.core;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.WebSocketImpl;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket.StringCallback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageTransfer implements IBuilder {
	
	AsyncHttpClient client;
	AsyncServer server;
	
	public ImageTransfer() {
		
		super();
		
		SecureRandom random = new SecureRandom();
		
	    String serverName =  "WorkerThreadAsyncServer" + new BigInteger(130, random).toString(32);
	    
		server = new AsyncServer(serverName);
		client = new AsyncHttpClient(server);
		
    	//streamingExecutor = new PausableExecutor(maxThreads, cont);
	}

	
	public WebSocket socket;
	private String nsi;
	private DBHelper dbHelper;
	private boolean socketReady = false;
	private boolean settingUpSocket = false;
	private boolean reconn = false;
	private boolean initconn = true;
	private String TAG = "map.peer.core.ImageTransfer";
	
	public void controlLoop(String fileUrl, String nsi, DBHelper dbHelper) {

		this.nsi = nsi;
		this.dbHelper = dbHelper;
		
		setupSocket(false);
		
	    while(Thread.interrupted() == false) {
	    	
	    	if(socketReady == false && settingUpSocket == false) {

    			setupSocket(false);
    			
	    	}else if(reconn == true) {
	        	
	        	reconn = false;
	        	
	        	//webSocket.send("PSIRECONN:" + nsi);
	        	
	        	//socket = webSocket;
	
				
	        } else  if(socket != null && socket.isOpen() == true && initconn == true){
	        	
	        	initconn = false;
	        	
		        socket.send("PSISTREAM:" + nsi);
		        
	        	try{
	        		
	        		socket.send("PSIPIC");

	        		Bitmap bm = BitmapFactory.decodeFile(fileUrl);
	        		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	        		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); 
	        		byte[] b = baos.toByteArray();
	        		
	        		sendBytes(b);
	        		
	        		socket.send("PSIPICEND");
	        		
	        		return;
						
				} catch(Exception ex) {
					
					Log.e(TAG, ex.getClass().getName());
					
					if(ex.getMessage() != null)
					Log.e(TAG, ex.getMessage());
				}
	        }
	    }
	}

	private void ensureSocket() {
		if(socket.isOpen() == false) {
			
			remaining = ((WebSocketImpl)socket).mSink.mPendingWrites;
			
			socketReady = false;
			
			while(socketReady == false) {
				
				if(settingUpSocket == false) {

					setupSocket(false);
					Log.d(TAG, "Socket isn't ready... Trying to get a new one...");
				}
				
			}
			
			
		}
		
		if(canWriteSocket == false) {
			Log.d(TAG, "Socket isn't ready... Waiting to write again...");
		}
		
		while(canWriteSocket == false) {
			
			if(socket.isBuffering() == false) {
				canWriteSocket = true;
				Log.d(TAG, "Socket is ready");
			}
		}
		
	}
    
	boolean canWriteSocket = false;

	private void setupSocket(final boolean reconnect) {
		
		settingUpSocket = true;
		
		client.websocket(RequestListener.proxyServerUrl, "my-protocol", new WebSocketConnectCallback() {
		
		@Override
		public void onCompleted(Exception exs, WebSocket nsocket) {
	    	
	        if (exs != null) {
	            Log.d(TAG, exs.getMessage());
		        settingUpSocket = false;
	            return;
	        }
	        
	        socket = nsocket;
	        
	        if(reconnect) {
	        	
	        	reconn = true;
	        	
	        } else if(initconn == false) {
	        	
	        	socket.send("PSISTREAM:" + nsi);
	        	
	        	sendBytes(remaining.getAllByteArray());
	        	
	        	remaining = null;
	        }
	        
	        socket.setWriteableCallback(new WritableCallback() {

				@Override
				public void onWriteable() {
					canWriteSocket = true;
					
				}
	        });
	        
	        socket.setEndCallback(new CompletedCallback() {

				@Override
				public void onCompleted(Exception ex) {
					if (ex != null) {
			            Log.e(TAG, ex.getMessage());
			            socketReady = false;
			        }
					
				}
	        	
	        });
	        
	        socket.setClosedCallback(new CompletedCallback() {

				@Override
				public void onCompleted(Exception ex) {
					if (ex != null) {
			            Log.e(TAG, ex.getMessage());
			            socketReady = false;
			        }
					
				}
	        	
	        });
	        
	        socket.setStringCallback(new StringCallback() {
	            public void onStringAvailable(String s) {
	            	
	            
                	if(s.indexOf("PSIKILL") == 0) {
	                	
						try {
							dbHelper.archiveViewer(nsi);
							
						} catch(Exception ex) {
							
							Log.e(TAG, ex.getClass().getName());
							
							if(ex.getMessage() != null)
							Log.e(TAG, ex.getMessage());
						}

						socket.close();
						
						Thread.currentThread().interrupt();
	                }
	            }
	        });
	        settingUpSocket = false;
	        socketReady = true;
	        canWriteSocket = true;
		}
		});
	}
	
	int chunkSize = 16378;
	
	private void sendBytes(byte[] ba) {
		int total = 0;
		
		while(total < ba.length) 
		{
			
			ensureSocket();
			
			//if(Thread.interrupted() == true || movieIsStopped == true) return;
			
			int fin = total + chunkSize > ba.length ? ba.length : total + chunkSize;
			
			final byte[] toSend = new byte[ fin-total ];
			
			System.arraycopy(ba, total, toSend, 0, fin - total);
			
			if(toSend.length > 0) {
				
				try{
					if(isStopped == true) {
						return;
					}
							
			        		socket.send(toSend);
			        		
			        		if(socket.isBuffering() == true) {
			        			canWriteSocket = false;
			        		}
			        		
				}catch(Exception ex) {
					
					Log.e(TAG, ex.getClass().getName());
					
					if(ex.getMessage() != null)
					Log.e(TAG, ex.getMessage());
					
					ensureSocket();
					
					socket.send(toSend);
				}
			}
			
			total = fin;
		}
	}
    
    ByteBufferList remaining = null;

    boolean isStopped = false;
    
    public void stop() {
    	
    	isStopped = true;
    	
    }
	
}
