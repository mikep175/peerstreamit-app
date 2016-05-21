package map.peer.core;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket.StringCallback;

import android.util.Log;

class RequestListener implements Runnable {
	
	public static final String TAG = "map.peer.core.RequestListener";
	
	Map<String, HashMap<String, WorkerThread>> workers = new HashMap<String, HashMap<String, WorkerThread>>();
	Map<String, String[]> workerRequests = new HashMap<String, String[]>();
	Map<String, String[]> pendingChallenges = new HashMap<String, String[]>();
	WebSocket serverSocket;
	
	public RequestListener() {
		
	}
	
	DBHelper dbHelper;
	
	public RequestListener(DBHelper dbHelper) {
		super();
		this.dbHelper = dbHelper;
	}

	boolean isStopped = false;
	
	public void stop() { 
		
		dbHelper.archiveViewersAll();
		isStopped = true;
		
	}
	AsyncHttpClient client;
	AsyncServer server;
	
	public static String proxyServerUrl =  "wss://peersockets-myappsplatform.rhcloud.com:8443/sockets"; //"ws://peersockets-myappsplatform.rhcloud.com:8000/sockets";//
	
	@Override
	public void run() {
		Log.i(TAG,"StreamService starting");
		
		try { 
			setupSocket();
			
			while (!Thread.interrupted() && isStopped == false) {
				//Log.d(TAG,"listener tick");
				if(serverSocket != null && serverSocket.isOpen() == false)
				Log.d(TAG,"serverSocket.isOpen()==false");
				
				if(workerRequests.size() > 0) {
					
					String nsi = (String)workerRequests.keySet().toArray()[0];
					
					String[] path = workerRequests.remove(nsi);
					
					WorkerThread wt = new WorkerThread(path[0], nsi, dbHelper, path[2]);
					
					String psikey = path[1];
					
					if(workers.containsKey(psikey)) {
						
						workers.get(psikey).put(nsi, wt);
					}
					else {
						
						workers.put(psikey, new HashMap<String, WorkerThread>());
						workers.get(psikey).put(nsi, wt);
					}

                	
                	new Thread(wt).start();
				}
				
				
				for(HashMap<String, WorkerThread> wts : workers.values()) {
					for (Iterator<Entry<String, WorkerThread>> iterator = wts.entrySet().iterator(); iterator.hasNext();) {
						Entry<String, WorkerThread> entry = iterator.next();
					    if (entry.getValue().isStopped == true) {
					        // Remove the current element from the iterator and the list.
					        iterator.remove();
					    }
					}
				}
				
				if(settingUpSocket == false && (serverSocket == null || serverSocket.isOpen() == false)) {
					
					setupSocket();
					
				}
			}
			
			for(HashMap<String, WorkerThread> wts : workers.values()) {
				for(WorkerThread wt : wts.values()) {
					wt.stop();
				}
			}
			
			serverSocket.close();
	        
			Log.i(TAG,"StreamService stopped");
			
		} catch (Exception e) {
			Log.e(TAG,e.getMessage());
		}
		
	}
	
	boolean settingUpSocket = true;

	private void setupSocket() {
		
		settingUpSocket = true;
		
		SecureRandom random = new SecureRandom();
		
		String serverName =  "RequestListener" + new BigInteger(130, random).toString(32);
		
		server = new AsyncServer(serverName);
		client = new AsyncHttpClient(server);
		client.websocket(proxyServerUrl, "my-protocol", new WebSocketConnectCallback() {
		    @Override
		    public void onCompleted(Exception ex, WebSocket webSocket) {
		    	
		        if (ex != null) {
		            Log.d(TAG, ex.getMessage() == null ? "Could not connect to server" : ex.getMessage());
		            settingUpSocket = false;
		            return;
		        }
		        
		        webSocket.setStringCallback(new StringCallback() {
		            public void onStringAvailable(String s) {
		            	
		            	Log.d(TAG, "RequestListener String Callback: " + s);
		            	
		                if(s.indexOf("PSICLIKEY:") == 0) {
		                	
		                	String[] raw = s.split(":");
		                	
		                	String psiKey = raw[1];
		                	String nsi = raw[2];
		                	String origin = raw[3];
		                	String userAgent = raw[4];
		                	//check if pw protected
		                	String[] pathAndPass = dbHelper.getPathAndPassword(psiKey);
		                	
		                	int concurrentAllowed = Integer.parseInt(pathAndPass[3]);
		                	
		                	if(workers.containsKey(psiKey) && workers.get(psiKey).size() >= concurrentAllowed) {
		                		
		                		serverSocket.send("PSIMAX:" + nsi);
		                		
		                	}
		                	else if(pathAndPass != null && pathAndPass[0].length() > 0) {

		                		if(pathAndPass[1] == null || pathAndPass[1].length() == 0) {
		                			
		                			dbHelper.insertViewer(origin, userAgent, nsi, psiKey, 1);
		                			
		                			workerRequests.put(nsi, new String[] { pathAndPass[0], psiKey, pathAndPass[2] });
		                			
		                		} else {
		                			String[] temp = new String[6];
		                			temp[0] = pathAndPass[0];
		                			temp[1] = pathAndPass[1];
		                			temp[2] = psiKey;
		                			temp[3] = origin;
		                			temp[4] = userAgent;
		                			temp[5] = pathAndPass[2];
		                			
		                			pendingChallenges.put(nsi, temp);
		                			serverSocket.send("PSICHALLENGE:" + nsi);
		                		}
		                	}

		                }
		                
		                if(s.indexOf("PSIAUTH:") == 0) {
		                	
		                	String[] raw = s.split(":");
		                	
		                	String nsi = raw[1];
		                	String pw = raw[2];
		                	
		                	if(pw.equals(pendingChallenges.get(nsi)[1]) == true) {
		                		
		                		dbHelper.resetFailedAttempts(pendingChallenges.get(nsi)[2]);
		                		
		                		dbHelper.insertViewer(pendingChallenges.get(nsi)[3], pendingChallenges.get(nsi)[4], nsi, pendingChallenges.get(nsi)[2], 1);
		                		
		                		workerRequests.put(nsi, new String[] { pendingChallenges.get(nsi)[0], pendingChallenges.get(nsi)[2], pendingChallenges.get(nsi)[5] });
		                		pendingChallenges.remove(nsi);
		                	}
		                	else {
		                		dbHelper.updateFailedAttempts(pendingChallenges.get(nsi)[2]);
		                		serverSocket.send("PSIAUTHREJECTED:" + nsi);
		                	}
		                }
		                
		                	
		            }
		        });
		        webSocket.setDataCallback(new DataCallback() {
		            public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
		               
		            	//System.out.println("I got some bytes!");
		                // note that this data has been read
		                byteBufferList.recycle();
		            }
		        });
		        
		        serverSocket = webSocket;
		    
		        List<String> pubs = dbHelper.getAllPublished();
		        
		        for(String key : pubs) {
		        	
		        	serverSocket.send("PSISERVKEY:" + key);
		        }
		        
		        settingUpSocket = false;

		        Log.i(TAG,"StreamService init complete");
		        
		    }
		});
	}

	public void addPsiKey(String psiKey) {
		if((serverSocket == null || serverSocket.isOpen() == false)) {
			
		} else {

			serverSocket.send("PSISERVKEY:" + psiKey);
		}
		
	}

	public void stopStreaming(String psiKey) {
		
		if((serverSocket == null || serverSocket.isOpen() == false)) {
			
		} else {

			serverSocket.send("PSIREMOVESERVKEY:" + psiKey);
		}
		
		if(workers.containsKey(psiKey)) {
			
			HashMap<String, WorkerThread> wts = workers.get(psiKey);
			
			for(WorkerThread wt : wts.values()) {
				wt.stop();
			}
			
			workers.remove(psiKey);
		}
		
		dbHelper.archiveViewers(psiKey);
		
	}
	
	
		
		
		
	}
