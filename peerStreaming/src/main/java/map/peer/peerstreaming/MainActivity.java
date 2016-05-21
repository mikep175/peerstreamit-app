package map.peer.peerstreaming;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.support.v7.app.ActionBar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import map.peer.core.DBHelper;
import map.peer.core.NetworkUtil;
import map.peer.core.StreamService;
import map.peer.peerstreaming.R.drawable;
import map.peer.peerstreaming.util.SharedAdapter;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	
	public static PeerStreamService mPeerStreamService = null;

	public ServiceConnection mPeerServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mPeerStreamService = (PeerStreamService) ((PeerStreamService.LocalBinder)service).getService();
			mPeerStreamService.addCallbackListener(mStreamCallbackListener);

//			try {
//				File file = File.createTempFile("black", "mp4");
//
//				FileOutputStream output = new FileOutputStream(file);
//				InputStream input = getResources().openRawResource(R.raw.black);
//
//				try {
//					byte[] buffer = new byte[4 * 1024]; // or other buffer size
//					int read;
//
//					while ((read = input.read(buffer)) != -1) {
//						output.write(buffer, 0, read);
//					}
//					output.flush();
//				} finally {
//					output.close();
//				}
//
//				mPeerStreamService.blackMp4 = file.getAbsolutePath();
//
//			}catch(Exception ex) {
//				Log.d("PeerStreamService", "Error persisting black.mp4");
//			}
			mPeerStreamService.start();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {}

	};
	
	public static StreamService.CallbackListener mStreamCallbackListener = new StreamService.CallbackListener() {

		@Override 
		public void onError(StreamService server, Exception e, int error) {
			// We alert the user that the port is already used by another app.
//			if (error == PeerStreamService.ERROR_BIND_FAILED) {
//				new AlertDialog.Builder(SpydroidActivity.this)
//				.setTitle(R.string.port_used)
//				.setMessage(getString(R.string.bind_failed, "RTSP"))
//				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					public void onClick(final DialogInterface dialog, final int id) {
//						startActivityForResult(new Intent(SpydroidActivity.this, OptionsActivity.class),0);
//					}
//				})
//				.show();
//			}
		}

		@Override
		public void onMessage(StreamService server, int message) {
			/*if (message==PeerStreamService.MESSAGE_STREAMING_STARTED) {
				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
					mAdapter.getHandsetFragment().update();
			} else if (message==PeerStreamService.MESSAGE_STREAMING_STOPPED) {
				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
					mAdapter.getHandsetFragment().update();
			}*/
		}

	};	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	boolean init = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		  
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

		dbHelper = new DBHelper(this);
		
		startService(new Intent(this,PeerStreamService.class));

		bindService(new Intent(this,PeerStreamService.class), mPeerServiceConnection, Context.BIND_AUTO_CREATE);
				
		
		Intent intent = getIntent();

		if(Intent.ACTION_SEND.equals(intent.getAction())) {
			try{
				
				Uri selectedImage = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
				
				ContentResolver resolver = getContentResolver();
				String type = resolver.getType(selectedImage); 
				
				if(type != null && type.toLowerCase(Locale.US).indexOf("image") == 0) {
					
					MainActivity.MediaType = MainActivity.MEDIA_IMAGE;
					
				}else if(type != null && type.toLowerCase(Locale.US).indexOf("audio") == 0) {

					MainActivity.MediaType = MainActivity.MEDIA_AUDIO;

				} else {
					
					MainActivity.MediaType = MainActivity.MEDIA_VIDEO;
				}
				
				
				String[] filePathColumn = { MediaStore.Video.Media.DATA };
				Cursor cursor = resolver.query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
		
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				picturePath = cursor.getString(columnIndex);
				cursor.close();
		
				if(picturePath != null) {
					
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
					        != PackageManager.PERMISSION_GRANTED) {

//					    // Should we show an explanation?
//					    if (ContextCompat.shouldShowRequestPermissionRationale(
//					            Manifest.permission.READ_EXTERNAL_STORAGE)) {
//					        // Explain to the user why we need to read the contacts
//					    }

					    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					            REQUEST_READ_EXTERNAL_STORAGE);

					    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
					    // app-defined int constant

					    return;
					}
					
					newFrag = NewShareFragment.newInstance(picturePath, this, dbHelper, mPeerStreamService);

					FragmentManager fragmentManager = getSupportFragmentManager();
					fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
					fragmentManager.beginTransaction().replace(R.id.container, newFrag).addToBackStack(null).commit();
					
				} else {
					
					newFrag = null;
					Toast.makeText(this, "Only content stored on the device can be shared", Toast.LENGTH_LONG).show();
					
					FragmentManager fragmentManager = getSupportFragmentManager();
					fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
					fragmentManager.beginTransaction().replace(R.id.container, SharedListFragment.newInstance(0, mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
					
				}
	
				
			} catch(Exception ex) {
				Toast.makeText(this, "Only content stored on the device can be shared", Toast.LENGTH_LONG).show();
				
				FragmentManager fragmentManager = getSupportFragmentManager();
				fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				fragmentManager.beginTransaction().replace(R.id.container, SharedListFragment.newInstance(0, mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
				
			}
		} else {
			
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			fragmentManager.beginTransaction().replace(R.id.container, SharedListFragment.newInstance(0, mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
			
		}
		init = false;
	}
	
	public void onStart() {
		super.onStart();
		
	}
	

	private DBHelper dbHelper; 
	
	private static int RESULT_LOAD_IMAGE = 1;
	
	public static int MediaType = -1;
	
	public static int MEDIA_VIDEO = 1;
	public static int MEDIA_IMAGE = 2;
	public static int MEDIA_AUDIO = 3;

	@Override
	 public void onRequestPermissionsResult(int requestCode, String[] permissions,
	         int[] grantResults) {
	     if (requestCode == REQUEST_READ_EXTERNAL_STORAGE
	             && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	    	 newFrag = NewShareFragment.newInstance(picturePath, this, dbHelper, mPeerStreamService);
	     } else if(requestCode == REQUEST_READ_EXTERNAL_STORAGE_BROWSER
				 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

			 Intent fileExploreIntent = new Intent(
					 FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
					 null,
					 this,
					 FileBrowserActivity.class
			 );

			 fileExploreIntent.putExtra(FileBrowserActivity.filterExtension, ".mp3");
			 //  fileExploreIntent.putExtra(
			 //      ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.startDirectoryParameter,
			 //      "/sdcard"
			 //  );//Here you can add optional start directory parameter, and file browser will start from that directory.
			 startActivityForResult(
					 fileExploreIntent,
					 REQUEST_CODE_PICK_FILE_TO_SAVE_INTERNAL
			 );
		 }
	 }
	 
	int REQUEST_READ_EXTERNAL_STORAGE = 1;
	
	String picturePath = null;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE_PICK_FILE_TO_SAVE_INTERNAL) {
			if(resultCode == this.RESULT_OK) {
				picturePath = data.getStringExtra(
						FileBrowserActivity.returnFileParameter);
				newFrag = NewShareFragment.newInstance(picturePath, this, dbHelper, mPeerStreamService);
			} else {//if(resultCode == this.RESULT_OK) {

			}//END } else {//if(resultCode == this.RESULT_OK) {
		}//if (requestCode == REQUEST_CODE_PICK_FILE_TO_SAVE_INTERNAL) {

		else if (requestCode == RESULT_LOAD_IMAGE && null != data) {
			try{
				
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Video.Media.DATA };
	
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
	
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				picturePath = cursor.getString(columnIndex);
				cursor.close();
				
				if(picturePath != null) {

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
					        != PackageManager.PERMISSION_GRANTED) {

//					    // Should we show an explanation?
//					    if (ContextCompat.shouldShowRequestPermissionRationale(
//					            Manifest.permission.READ_EXTERNAL_STORAGE)) {
//					        // Explain to the user why we need to read the contacts
//					    }

					    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					            REQUEST_READ_EXTERNAL_STORAGE);

					    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
					    // app-defined int constant

					    return;
					}
					
					newFrag = NewShareFragment.newInstance(picturePath, this, dbHelper, mPeerStreamService);
				
				} else {
					Toast.makeText(this, "Only content stored on the device can be shared", Toast.LENGTH_LONG).show();
					newFrag = null;
				}
		    	 
			} catch(Exception ex) {
				Toast.makeText(this, "Only content stored on the device can be shared", Toast.LENGTH_LONG).show();
				newFrag = null;
			}
        }
    }
	
	NewShareFragment newFrag = null;
	
	@Override
	public void onResume(){
	    super.onResume();

	    if(newFrag != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			fragmentManager.beginTransaction().replace(R.id.container, newFrag).addToBackStack(null).commit();
			newFrag = null;
	    } 
	    else {

	    	FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			fragmentManager.beginTransaction().replace(R.id.container, SharedListFragment.newInstance(0, mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
	    }
	}

	int REQUEST_READ_EXTERNAL_STORAGE_BROWSER = 5;
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		if(init == false) {
			if(position == 0) {
				MainActivity.MediaType = MainActivity.MEDIA_VIDEO;
				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
				intent.setType("video/*");
				startActivityForResult(Intent.createChooser(intent, "Select a video"), 1);
			}
//			else if(position == 1) {
//				MainActivity.MediaType = MainActivity.MEDIA_IMAGE;
//				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//				intent.setType("image/*");
//				startActivityForResult(Intent.createChooser(intent, "Select a picture"), 1);
//			}
			else if(position == 1) {
				MainActivity.MediaType = MainActivity.MEDIA_AUDIO;
//				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//				intent.setType("audio/*");
//				startActivityForResult(Intent.createChooser(intent, "Select a picture"), 1);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {

//					    // Should we show an explanation?
//					    if (ContextCompat.shouldShowRequestPermissionRationale(
//					            Manifest.permission.READ_EXTERNAL_STORAGE)) {
//					        // Explain to the user why we need to read the contacts
//					    }

					ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
							REQUEST_READ_EXTERNAL_STORAGE_BROWSER);

					// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
					// app-defined int constant

					return;
				}

				Intent fileExploreIntent = new Intent(
						FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
						null,
						this,
						FileBrowserActivity.class
				);

				fileExploreIntent.putExtra(FileBrowserActivity.filterExtension, ".mp3");
				//  fileExploreIntent.putExtra(
				//      ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.startDirectoryParameter,
				//      "/sdcard"
				//  );//Here you can add optional start directory parameter, and file browser will start from that directory.
				startActivityForResult(
						fileExploreIntent,
						REQUEST_CODE_PICK_FILE_TO_SAVE_INTERNAL
				);
			}
		} else {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			fragmentManager.beginTransaction().replace(R.id.container, SharedListFragment.newInstance(0, mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
		}
	}

	int REQUEST_CODE_PICK_FILE_TO_SAVE_INTERNAL = 6;

	public void onSectionAttached(int number) {
//		switch (number) {
//		case 1:
//			mTitle = getString(R.string.title_section1);
//			break;
//		case 2:
//			mTitle = getString(R.string.title_section2);
//			break;
//		case 3:
//			mTitle = getString(R.string.title_section3);
//			break;
//		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(Html.fromHtml("peer<b>stream</b>it"));
		
		actionBar.setLogo(drawable.bee);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			
			final View layout = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_pref,
					(ViewGroup)this.findViewById(R.id.popup_pref));
			
			final PopupWindow popup = new PopupWindow(this);
			popup.setContentView(layout);
			popup.setWidth(View.MeasureSpec.UNSPECIFIED);
		    popup.setHeight(View.MeasureSpec.UNSPECIFIED);

		    popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		    
			popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
			
			final Context ctx = this;
			
			SharedPreferences sharedPref = this.getSharedPreferences("peer", Context.MODE_PRIVATE);
			boolean defaultValue = false;
			boolean offWifi = sharedPref.getBoolean(("OFFWIFI"), defaultValue);
			
			((CheckBox)layout.findViewById(R.id.cbStreamWifi)).setChecked(offWifi == false);
			
			((CheckBox)layout.findViewById(R.id.cbStreamWifi)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					SharedPreferences sharedPref = ctx.getSharedPreferences("peer", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putBoolean("OFFWIFI", ((CheckBox)layout.findViewById(R.id.cbStreamWifi)).isChecked() == false);
					editor.commit();
					
				}
			});
			
			((Button)layout.findViewById(R.id.btn_close_popup)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					
					popup.dismiss();
				}
			});
			return true;
		} else if (id == R.id.action_exit) {

			mPeerStreamService.stop();
			unbindService(mPeerServiceConnection);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class SharedListFragment extends Fragment {
		
		 private DBHelper dbHelper;
			
		 public SharedAdapter adapter;
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "1";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static SharedListFragment newInstance(int sectionNumber, PeerStreamService mPeerStreamService, FragmentManager fragmentManager) {
			SharedListFragment fragment = new SharedListFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			fragment.fragmentManager = fragmentManager;
			return fragment;
		}
		
		PeerStreamService mPeerStreamService;
		FragmentManager fragmentManager;

		public SharedListFragment() {
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			dbHelper = new DBHelper(this.getActivity());
			
			ListView lv = (ListView)rootView.findViewById(R.id.listView1);

			adapter = new SharedAdapter(this.getActivity(), dbHelper.getAllPublishedCursor(), 0, dbHelper, fragmentManager);

			TextView emptyText = (TextView)rootView.findViewById(R.id.emptyView);
			lv.setEmptyView(emptyText);
			
			lv.setAdapter(adapter);
			
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}
	
	public static class NewShareFragment extends Fragment {
		
		 private DBHelper dbHelper;
		 PeerStreamService mPeerStreamService;
		 public SimpleCursorAdapter adapter;
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "999";

		public static NewShareFragment newInstance(String filePath, ActionBarActivity parent, DBHelper dbHelper, PeerStreamService mPeerStreamService) {
			
			NewShareFragment frag = new NewShareFragment();
			
			frag.filePath = filePath;
			frag.parent = parent;
			frag.dbHelper = dbHelper;
			frag.mPeerStreamService = mPeerStreamService;

			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, 999);
			frag.setArguments(args);
			
			return frag;
		}

		public NewShareFragment() {
			
		}

		String filePath = "";
		ActionBarActivity parent;
		
		View rootView;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
			rootView = inflater.inflate(R.layout.fragment_newshare, container, false);

			TextView tvFileName = (TextView)rootView.findViewById(R.id.textViewFileName);
			
			tvFileName.setText(filePath.substring(filePath.lastIndexOf("/") + 1));
			
			Button btnCancel = (Button)rootView.findViewById(R.id.btnCancelNew);
			
			btnCancel.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {
	            	 
	            	 FragmentManager fragmentManager = parent.getSupportFragmentManager();
		     			fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	     			 fragmentManager.beginTransaction().replace(R.id.container, SharedListFragment.newInstance(0, mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
	     			 
	             }
	             });
			
			Button btnCreate = (Button)rootView.findViewById(R.id.btnCreateNew);
			
			btnCreate.setOnClickListener(new View.OnClickListener() {
	             public void onClick(View v) {

	            	 String title = ((EditText)rootView.findViewById(R.id.txtTitle)).getText().toString();
	            	 
	            	 String desc = ((EditText)rootView.findViewById(R.id.txtDesc)).getText().toString();
	            	 
	            	 String password = ((EditText)rootView.findViewById(R.id.txtPassword)).getText().toString();
	            	 
	            	 boolean publishNow = ((Switch)rootView.findViewById(R.id.swtichPublished)).isChecked();
	            	 
	            	 SecureRandom random = new SecureRandom();
	         		
	         		 String psikey =  new BigInteger(260, random).toString(32);
	         		 
	         		 String concurrent = ((EditText)rootView.findViewById(R.id.txtConStreams)).getText().toString();
	         		 
	            	 dbHelper.insertPublished(filePath, title, desc, psikey, password, publishNow ? 1 : 0, MainActivity.MediaType, Integer.parseInt(concurrent));
	            	 
	            	 if(publishNow) {
	            		 
	            		 mPeerStreamService.addPsiKey(psikey);
	            		 
		            	 String shareBody = "";

						 if(MainActivity.MediaType == MainActivity.MEDIA_AUDIO) {

							 shareBody = "I would like to share audio with you.  ";

						 } else {
							 shareBody = "I would like to share a " + (MainActivity.MediaType == MainActivity.MEDIA_VIDEO ? "video" : "picture") + " with you.  ";
						 }
		            	 
		            	 if(password.length() > 0) {
		            		 
		            		 shareBody += "Please contact me for the password.  ";
		            	 }
		            	 
		            	 shareBody += "You can view it at this URL: https://app.peerstreamit.com/viewer.html?psi=" + psikey;
		            	  
		            	    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		            	        sharingIntent.setType("text/plain");
		            	        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "PeerStreamIt: " + title);
		            	        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		            	        startActivity(Intent.createChooser(sharingIntent, "Share using..."));
	            	        
	            	 }
	            	 
	            	 FragmentManager fragmentManager = parent.getSupportFragmentManager();
		     			fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	     			 fragmentManager.beginTransaction().replace(R.id.container, SharedListFragment.newInstance(0, mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
	             }
	         });
			
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}

}
