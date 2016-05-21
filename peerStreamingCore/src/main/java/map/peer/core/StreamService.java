package map.peer.core;

import java.util.LinkedList;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class StreamService extends Service {

	public static String blackMp4 = null;

	@Override
	public void onTaskRemoved(Intent rootIntent){

	    super.onTaskRemoved(rootIntent);
	    mListenerThread.stop();
	    
	}
	
	@Override
    public void onDestroy() {

		super.onDestroy();
		
		mListenerThread.stop();

        Log.i(TAG, "Service onDestroy");
    }
	
	public void stopStreaming(String psiKey) {
		
		mListenerThread.stopStreaming(psiKey);
	}
	
	public void addPsiKey(String psiKey) {
		
		mListenerThread.addPsiKey(psiKey);
	}
	
	private final LinkedList<CallbackListener> mListeners = new LinkedList<CallbackListener>();
	
	public interface CallbackListener {

		/** Called when an error occurs. */
		void onError(StreamService server, Exception e, int error);

		/** Called when streaming starts/stops. */
		void onMessage(StreamService server, int message);
		
	}
 
	/**
	 * See {@link RtspServer.CallbackListener} to check out what events will be fired once you set up a listener.
	 * @param listener The listener
	 */
	public void addCallbackListener(CallbackListener listener) {
		synchronized (mListeners) {
			if (mListeners.size() > 0) {
				for (CallbackListener cl : mListeners) {
					if (cl == listener) return;
				}
			}
			mListeners.add(listener);			
		}
	}

	/**
	 * Removes the listener.
	 * @param listener The listener
	 */
	public void removeCallbackListener(CallbackListener listener) {
		synchronized (mListeners) {
			mListeners.remove(listener);				
		}
	}

	
	
	DBHelper dbHelper;
	
	public static final String TAG = "map.peer.core.StreamService";
	
	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	
	public class LocalBinder extends Binder {
		public StreamService getService() {
			return StreamService.this;
		}
	}

	public boolean isEnabled;
	public boolean needsRestart;
	
	private RequestListener mListenerThread;
	 
	public void start() {
		if (!isEnabled || needsRestart) stop();
		if (isEnabled && mListenerThread == null) {
			try {

				SharedPreferences sharedPref = this.getSharedPreferences("peer", Context.MODE_PRIVATE);
				boolean defaultValue = false;
				boolean offWifi = sharedPref.getBoolean(("OFFWIFI"), defaultValue);

		        int status = NetworkUtil.getConnectivityStatus(this);
		        
				if(offWifi == true || status == NetworkUtil.TYPE_WIFI) {
					
					dbHelper = new DBHelper(this.getBaseContext());
					
					mListenerThread = new RequestListener(dbHelper);
					
					new Thread(mListenerThread).start();
				
				}
				
			} catch (Exception e) {
				mListenerThread = null;
			}
		}
		needsRestart = false;
	}
	
	public void stop() {
		if (mListenerThread != null) {
			try {
				
				mListenerThread.stop();
				
			} catch (Exception e) {
			} finally {
				mListenerThread = null;
			}
		}
	}
	
}
