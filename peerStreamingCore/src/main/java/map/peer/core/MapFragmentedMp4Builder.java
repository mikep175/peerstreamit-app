package map.peer.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.SchemeTypeBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.CencMp4TrackImplImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.MP3TrackImpl;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.koushikdutta.async.http.AsyncHttpClient; 
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.WebSocketImpl;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket.StringCallback;
import com.mpatric.mp3agic.Mp3File;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

public class MapFragmentedMp4Builder extends FragmentedMp4Builder implements IBuilder {

	public static final String TAG = "map.peer.core.MapFragmentedMp4Builder";
 
	AsyncHttpClient client;
	AsyncServer server;

	Movie movie;
	IsoFile isoFile;
	DataSource channel;
	
	boolean kill = false;
	
	boolean socketReady = false;
	boolean seeking = false;
	double seekTo = 0.0;
	
	public void seek() {
		
		
		//((WebSocketImpl)webSocket).flush();
		//movieIsStopped = true;
		
		try { 
			
			//executor.shutdownNow();

			executor.shutdown();

			//((WebSocketImpl)socket).empty();
			
			//webSocket.send("PSIWAIT:" + nsi);
			
			//WebSocket toClose = webSocket;

			//webSocket = null;
			//toClose.end();
			//toClose.close();
			//server.stop();
			
		} catch(Exception ex) {
			
			executor.shutdownNow();
			
			Log.e(TAG, ex.getClass().getName());
			
			if(ex.getMessage() != null)
			Log.e(TAG, ex.getMessage());
		}
		
		//movieThreadThread.interrupt();
		
		streamQueue.clear();
		inProgress = 0;
		nextStreamNum = 0;
		nextProcessNum = 1;
		started = null;
		
		//movieIsStopped = false;
		
		//createMoofMdat(movie, seekTo - 1 < 0 ? 0.0 : seekTo - 1); 
		reconn = true;
		//setupSocket(true);
	}
	
	public void stop() {
		
		try {

			if(tempFilePath != null) {

				tempFilePath.delete();
			}

			socket.close();
			
        	server.stop();


			try {
				
				executor.shutdownNow();
				
			} catch(Exception ex) {
				
				Log.e(TAG, ex.getClass().getName());
				
				if(ex.getMessage() != null)
				Log.e(TAG, ex.getMessage());
			}
			
			isoFile.close();
			channel.close();

			
			
		} catch(Exception ex) {
			
			Log.e(TAG, ex.getClass().getName());
			
			if(ex.getMessage() != null)
			Log.e(TAG, ex.getMessage());
		}
	}
	
	boolean settingUpSocket = false;
	String nsi;
	DBHelper dbHelper;
	public boolean isAudio = false;

	File tempFilePath = null;

	public void controlLoop(String fileUrl, String nsi, DBHelper dbHelper) {

		this.nsi = nsi;
		this.dbHelper = dbHelper;
		
		setupSocket(false);
		
	    while(Thread.interrupted() == false) {
	    	
	    	if(kill == true) {
	    		
	    		return;
	    	}
	    	
	    	
	    	if(socketReady == false && settingUpSocket == false) {

    			setupSocket(false);
    			
	    	}else if(reconn == true) {
	        	
	        	reconn = false;
	        	
	        	//webSocket.send("PSIRECONN:" + nsi);
	        	
	        	//socket = webSocket;
	
	    		while(movieIsStopped == true) { }
	        	
	        	createMoofMdat(movie, seekTo - 1 < 0 ? 0.0 : seekTo - 1);
	        	movieIsStopped = false;
				
	        } else  if(socket != null && socket.isOpen() == true && initconn == true){
	        	
	        	initconn = false;
	        	
		        socket.send("PSISTREAM:" + nsi);
		        
	        	try{

					if(isAudio == true) {

						socket.send("PSIAUDIO");

						Mp3File mp3file = new Mp3File(fileUrl);
						if (mp3file.hasId3v1Tag()) {
							mp3file.removeId3v1Tag();
							Log.d("MP3agic","removeId3v1Tag");
						}
						if (mp3file.hasId3v2Tag()) {
							mp3file.removeId3v2Tag();
							Log.d("MP3agic","removeId3v2Tag");
						}
						if (mp3file.hasCustomTag()) {
							mp3file.removeCustomTag();
							Log.d("MP3agic","removeCustomTag");
						}
						tempFilePath = File.createTempFile(randomString, "mp3");

						//tempFilePath = temp.getAbsolutePath();

						mp3file.save(tempFilePath.getAbsolutePath());

						mp3file = null;

						channel = new FileDataSourceImpl(tempFilePath);

						MP3TrackImpl mp3Track = new MP3TrackImpl(channel);

						//isoFile = new IsoFile(new FileDataSourceImpl(new File(StreamService.blackMp4)));

						movie = new Movie();


//						boolean first = true;
//
//						List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
//						for (TrackBox trackBox : trackBoxes) {
//
//							if(first) {
//								first = false;
//								continue;
//							}
//
//							SchemeTypeBox schm = Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schm[0]");
//							if (schm != null && (schm.getSchemeType().equals("cenc") || schm.getSchemeType().equals("cbc1"))) {
//								movie.addTrack(new CencMp4TrackImplImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
//							} else {
//								movie.addTrack(new Mp4TrackImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
//							}
//						}
//						movie.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());

						movie.addTrack(mp3Track);

						stream(movie);

						createMoofMdat(movie, 0.0);
						movieIsStopped = false;

					}else {


						channel = new FileDataSourceImpl(new File(fileUrl));
						isoFile = new IsoFile(channel);

						movie = new Movie();
						List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
						for (TrackBox trackBox : trackBoxes) {
							SchemeTypeBox schm = Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schm[0]");
							if (schm != null && (schm.getSchemeType().equals("cenc") || schm.getSchemeType().equals("cbc1"))) {
								movie.addTrack(new CencMp4TrackImplImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
							} else {
								movie.addTrack(new Mp4TrackImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
							}
						}
						movie.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());

						Bitmap bm = ThumbnailUtils.createVideoThumbnail(fileUrl,
								MediaStore.Images.Thumbnails.MINI_KIND);

						if (bm != null && bm.getHeight() > bm.getWidth()) {

							//movie.setMatrix(Matrix.ROTATE_90);
							socket.send("PSIROTATE");
						}
						stream(movie);

						createMoofMdat(movie, 0.0);
						movieIsStopped = false;
					}
						
				} catch(Exception ex) {
					
					Log.e(TAG, ex.getClass().getName());
					
					if(ex.getMessage() != null)
					Log.e(TAG, ex.getMessage());
				}
	        }else if(seeking == true) {

	        	seeking = false;
	        	seek();
	    	}
	    }
	}

boolean reconn = false;
boolean initconn = true;
	String randomString = null;

	public MapFragmentedMp4Builder() {
		
		super();
		
		SecureRandom random = new SecureRandom();

		randomString = new BigInteger(130, random).toString(32);
		
	    String serverName =  "WorkerThreadAsyncServer" + randomString;
	    
		server = new AsyncServer(serverName);
		client = new AsyncHttpClient(server);
		
		cont = new Continue();
		
    	//streamingExecutor = new PausableExecutor(maxThreads, cont);
	}

	boolean movieIsStopped = false;
	
	public WebSocket socket;
	
	final int maxThreads = 4;

	HashMap<Long, Object[]> streamQueue = new HashMap<Long, Object[]>();

    int inProgress = 0;
    
    int nextStreamNum = 0;
    long nextProcessNum = 1;
    
    double fragmentDuration = 1;
    
	public void stream(Movie movie) throws IOException {
		
	    
		Log.d(TAG, "Creating movie " + movie);
		
		this.setFragmenter(new MapFragmenter(fragmentDuration));
        
        streamBox(createFtyp(movie));
        //isoFile.addBox(createPdin(movie));
        streamBox(createMoov(movie));
        
//        for (Box box : createMoofMdat(movie)) {
//        	try{
//        	streamBox(box, socket);
//        	}finally {
//        		
//        	
//        		
//        	}
//        }
        //MovieFragmentRandomAccessBox mfra = new MovieFragmentRandomAccessBox();
        //for (Track track : movie.getTracks()) {
        //    mfra.addBox(createTfra(track, isoFile));
        //}

       // MovieFragmentRandomAccessOffsetBox mfro = new MovieFragmentRandomAccessOffsetBox();
       // mfra.addBox(mfro);
       // mfro.setMfraSize(mfra.getSize());

    }
    
	int chunkSize = 16378;
	
    private void streamBox(final Box box) throws IOException {

		//streamingExecutor.execute(new Runnable() { 
			
	//		    @Override
	//		    public void run() {
		    	try(ByteArrayOutputStream mOutput = new ByteArrayOutputStream(); WritableByteChannel chan = Channels.newChannel(mOutput)) 
		    	{
					
					
		        	box.getBox(chan);
		    		
		        	byte[] ba = mOutput.toByteArray();
		        	
		        	Log.d(TAG, "streamBox " + box.getType() + ", size: " + ba.length);
		        	
		        	sendBytes(ba);
		        	
		    		
		    	} catch (Exception e) {
					throw e;
				}finally {}

//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
	//    } });
    }

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
	            	
	            	 if(s.indexOf("PSITIME:") == 0) {
		                	
		                	String[] raw = s.split(":");
		                	String position = raw[1];
		                	
		                	playerTime = Double.parseDouble(position);
		            }

	                if(s.indexOf("PSISEEK:") == 0) {
	                	
	                	String[] raw = s.split(":");
	                	String position = raw[1];
	                	movieIsStopped = true;
                		seeking = true;
                		playerTime = Double.parseDouble(position);
                		seekTo = playerTime;
	                }
	                
	                
                	if(s.indexOf("PSIKILL") == 0) {
	                	
						try {
							dbHelper.archiveViewer(nsi);
							kill = true;
							movieIsStopped = true;
							executor.shutdownNow();
							stop();
							
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
	
    
    
    @Override
    protected int createFragment(List<Box> moofsMdats, Track track, long startSample, long endSample, int sequence) {


        // if startSample == endSample the cycle is empty!
        if (startSample != endSample) {
        	try {
        		
				streamBox(createMoof(startSample, endSample, track, sequence));

	        	streamBox(createMdat(startSample, endSample, track, sequence));
	        	
			} catch (IOException ex) {
				
				Log.e(TAG, ex.getClass().getName());
				
				if(ex.getMessage() != null)
				Log.e(TAG, ex.getMessage());
			}
        }
        return sequence;
    }
    
    PausableExecutor executor;

	private boolean streamingRequested;
    
    @Override
    protected List<Box> createMoofMdat(final Movie movie) { 
    	
    	return createMoofMdat(movie, 0.0);
    	
    }
    
    double moofMdatStart = 0.0;
    
    public static int l2i(long l) {
        if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
            throw new RuntimeException("A cast to int has gone wrong. Please contact the mp4parser discussion group (" + l + ")");
        }
        return (int) l;
    }
    
    //Semaphore sema;
    Continue cont = null;
    //PausableExecutor streamingExecutor = null;
    
    protected List<Box> createMoofMdat(final Movie movie, double position) {
    	
    //    sema = new Semaphore(maxThreads);
    //	synchronized (this) 
		{
	    	moofMdatStart = position;
	    	Log.e(TAG, Thread.currentThread().getName() + " - " + position);
	    	
	    	executor = new PausableExecutor(maxThreads, cont);
	    	
	        HashMap<Track, long[]> intersectionMap = new HashMap<Track, long[]>();
	        HashMap<Track, Double> track2currentTime = new HashMap<Track, Double>();

	        for (Track track : movie.getTracks()) {
	            long[] intersects = fragmenter.sampleNumbers(track);
	            intersectionMap.put(track, intersects);
	            track2currentTime.put(track, 0.0);

	        }

	        int sequence = 1;
	        
	        long[] allStartSamples = null;
	        double totalDuration = 0;
	        
            for (Map.Entry<Track, Double> trackEntry : track2currentTime.entrySet()) {
                if (trackEntry.getValue() > totalDuration) {
                	totalDuration = trackEntry.getValue();
                }
            }
	        //tracks may not span entire video
	        
	        while (!intersectionMap.isEmpty()) {
		        //	synchronized (this) 
					{
			            Track earliestTrack = null;
			            double earliestTime = Double.MAX_VALUE;
			            for (Map.Entry<Track, Double> trackEntry : track2currentTime.entrySet()) {
			                if (trackEntry.getValue() < earliestTime) {
			                    earliestTime = trackEntry.getValue();
			                    earliestTrack = trackEntry.getKey();
			                }
			            }
			            assert earliestTrack != null;

			            long[] startSamples = intersectionMap.get(earliestTrack);
			            
			            if(allStartSamples == null) {
			            	
			            	allStartSamples = startSamples;
			            	Log.d(TAG, "allStartSamples: " + allStartSamples.length);
			            }
			            
			            
			            long startSample = startSamples[0];
			            long endSample = startSamples.length > 1 ? startSamples[1] : earliestTrack.getSamples().size() + 1;


			            //while(inProgress == maxThreads) { }
			            
			            if(earliestTime >= position) {
			            	executor.execute(new FragmentTask(++nextStreamNum, this, earliestTrack, startSample, endSample, sequence, earliestTime));
			            	inProgress++;

			            }
			             //new FragmentTask(++nextStreamNum, this, earliestTrack, startSample, endSample, sequence).start();
				         //inProgress++;
			            
			            //createFragment(moofsMdats, earliestTrack, startSample, endSample, sequence);

			            if (startSamples.length == 1) {
			                intersectionMap.remove(earliestTrack);
			                track2currentTime.remove(earliestTrack);
			                // all sample written.
			            } else {
			                long[] nuStartSamples = new long[startSamples.length - 1];
			                System.arraycopy(startSamples, 1, nuStartSamples, 0, nuStartSamples.length);
			                intersectionMap.put(earliestTrack, nuStartSamples);
			                track2currentTime.put(earliestTrack, earliestTime);
			            }
		            
			            if(movieIsStopped == true || Thread.interrupted() == true) {
			            	

							try {
								
								executor.shutdown();//Now();
								
							} catch(Exception ex) {
								
								Log.e(TAG, ex.getClass().getName());
								
								if(ex.getMessage() != null)
								Log.e(TAG, ex.getMessage());
							}
							//movieIsStopped = false;
					        return null;
			            }
			            
			            sequence++;
					}
	        	}

	        while(movieIsStopped == false && Thread.interrupted() == false && (executor.isTerminated() == false || streamQueue.size() > 0)) {
			    
				taskComplete();
				
			} 

	        executor.shutdown();//Now();
		}

        //movieIsStopped = false;
        
        return null;
}
    
   
    Date started = null;
    
    int batchComplete = 0;
    
    double playerTime = 0;
    
	public  void taskComplete() {

		try{
			

			synchronized(this) 
			{
			
				if(streamQueue.containsKey(nextProcessNum) == true) {
					
					
					try {

							if(started == null) {
								
								started = new Date();
							}
						
							if(Thread.interrupted() == true || this.movieIsStopped == true) {
				            	

								try {
									
									executor.shutdown();//Now();
									
								} catch(Exception ex) {
									
									Log.e(TAG, ex.getClass().getName());
									
									if(ex.getMessage() != null)
									Log.e(TAG, ex.getMessage());
								}
								
								return;
				            }
							
							Log.d(TAG, "IsOpen: " + socket.isOpen());
						
							Object[] boxes = streamQueue.remove(nextProcessNum);
							
							Date now = new Date();
							
							//long seconds = (now.getTime()- started.getTime())/1000;
						
							double vidTime = (double)boxes[2];

							long waitFor = 1;//(long)(vidTime - seconds - 30);
							
							while(vidTime > playerTime + 30) {
								
								Log.d(TAG, "throttling: " + waitFor);
								
								executor.ExecutorContinue.pause();
								
								Thread.sleep(waitFor * 1000);
								
								executor.ExecutorContinue.resume();
							}
							
							//Log.d(TAG, "done throttling");
							
							MovieFragmentBox moof = (MovieFragmentBox) boxes[0];
//							
//							List<TrackRunBox> hbs = moof.getTrackFragmentHeaderBoxes();
//							
//							for(TrackRunBox hb : hbs) {
//								 List<TrackRunBox.Entry> ents = hb.getEntries();
//								
//								 for(TrackRunBox.Entry ent : ents) {
//									 
//									long soFar =  ent.getSampleCompositionTimeOffset() + ent.getSampleDuration();
//									
//									if(soFar > durationProcessed) { durationProcessed = soFar; }
//									
//								 }
//							}
							if(Thread.interrupted() == true || movieIsStopped == true) return;
							waitToBuffer();
							if(Thread.interrupted() == true || movieIsStopped == true) return;
							streamBox(moof);
							//if(Thread.interrupted() == true || movieIsStopped == true) return;
							//waitToBuffer();
							//if(Thread.interrupted() == true || movieIsStopped == true) return;
							streamBox((Box)boxes[1]);
							//if(Thread.interrupted() == true || movieIsStopped == true) return;
							waitToBuffer();
							if(Thread.interrupted() == true || movieIsStopped == true) return;
							socket.send("TC:" + vidTime);
							if(Thread.interrupted() == true || movieIsStopped == true) return;
							nextProcessNum++;
							
							if(batchComplete == maxThreads) {
								batchComplete = 0;
								inProgress = 0;
							}
							
					} catch (IOException ex) {
						Log.e(TAG, ex.getClass().getName());
						
						if(ex.getMessage() != null)
						Log.e(TAG, ex.getMessage());
						
						executor.shutdownNow();
						
					} catch (InterruptedException ex) {
						Log.e(TAG, ex.getClass().getName());
						
						if(ex.getMessage() != null)
						Log.e(TAG, ex.getMessage());
						
						executor.shutdownNow();
					}
				}
			}
		} catch (Exception ex) {
			
			Log.e(TAG, ex.getClass().getName());
			
			if(ex.getMessage() != null)
			Log.e(TAG, ex.getMessage());
			
			executor.shutdownNow();
		}
	}
	


	private void waitToBuffer() {
		//Log.d(TAG, "IsBuffering: " + socket.isBuffering());
		
		while(socket.isBuffering() == true) {
			//Log.d(TAG, "IsBuffering: " + socket.isBuffering());
//			try {
//				//Thread.sleep(100);
//			} catch (InterruptedException e) {
//				Log.e(TAG, e.getClass().getName());
//				
//				if(e.getMessage() != null)
//				Log.e(TAG, e.getMessage());
				//((WebSocketImpl)socket).flush();
				
				if((socket.isBuffering() == true)) { Log.e(TAG, "FLUSH DIDNT WORK"); }
				if(Thread.interrupted() == true || movieIsStopped == true) return;
//			}
			
		}
	}
	
	public class FragmentTask implements Runnable {

		int streamOrder; MapFragmentedMp4Builder builder; Track track; long startSample; long endSample; int sequence; double earliestTime;
		
		public FragmentTask(int streamOrder, MapFragmentedMp4Builder builder, Track track, long startSample, long endSample, int sequence, double earliestTime) {
			
			super();
			
			this.streamOrder = streamOrder;
			
			this.builder = builder;
			
			this.track = track;
			
			this.startSample = startSample;
			
			this.endSample = endSample;
			
			this.sequence = sequence;
			
			this.earliestTime = earliestTime;
		}
		
		@Override
		public void run() {
			
//        	try {
//				sema.acquire();
//			} catch (Exception ex) {
//				
//				Log.e(TAG, ex.getClass().getName());
//				
//				if(ex.getMessage() != null)
//				Log.e(TAG, ex.getMessage());
//			}
        	
			if(Thread.interrupted() == true || movieIsStopped == true) return;
			Box moof = builder.createMoof(startSample, endSample, track, sequence);
			if(Thread.interrupted() == true || movieIsStopped == true) return;
			Box mdat = builder.createMdat(startSample, endSample, track, sequence);
			if(Thread.interrupted() == true || movieIsStopped == true) return;
			streamQueue.put(Long.valueOf(streamOrder), new Object[] { moof, mdat, earliestTime });

			inProgress--;
		}
		
	}
		
		
	}
    
