package map.peer.peerstreaming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import map.peer.core.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {
 
    @Override
    public void onReceive(final Context context, final Intent intent) {
 
    	
    	SharedPreferences sharedPref = context.getSharedPreferences("peer", Context.MODE_PRIVATE);
		boolean defaultValue = false;
		boolean offWifi = sharedPref.getBoolean(("OFFWIFI"), defaultValue);
		 
		if(offWifi == false) {
			
	        int status = NetworkUtil.getConnectivityStatus(context);
	 
	        if(status == NetworkUtil.TYPE_WIFI) {
	        	
	        	if(MainActivity.mPeerStreamService != null) {
	        		
	        		MainActivity.mPeerStreamService.start();
		        	
	        	}
	        	
	        } else if(MainActivity.mPeerStreamService != null) {
	        	
	        	MainActivity.mPeerStreamService.stop();
	        }
		}
    }
}
