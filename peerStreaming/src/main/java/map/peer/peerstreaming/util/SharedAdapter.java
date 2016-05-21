package map.peer.peerstreaming.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import map.peer.core.DBHelper;
import map.peer.peerstreaming.MainActivity;
import map.peer.peerstreaming.PeerStreamService;
import map.peer.peerstreaming.R;

public class SharedAdapter extends CursorAdapter {

	LayoutInflater cursorInflater;
	DBHelper dbHelper;
	Context vcontext;
	FragmentManager fragmentManager;
	
	public SharedAdapter(Context context, Cursor cursor, int flags, DBHelper dbHelper, FragmentManager fragmentManager) {
		super(context, cursor, R.layout.published_list_item);
		this.dbHelper = dbHelper;
		this.cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
		this.vcontext = context;
		this.fragmentManager = fragmentManager;
	}
	
	static class ViewHolder {
	  TextView text;
	  TextView timestamp;
	  ImageView imgPreview;
	  ProgressBar prgImgPreview;
	  int position;
	  ImageButton btnStop;
	  ImageButton btnPlay;
	  ImageButton btnCaution;
	  ImageButton btnLock;
	  ImageButton btnUnlocked;
	  ImageButton btnDelete;
	  ImageButton btnShare;
	  ImageButton btnWatching;
	  ImageButton btnDetails;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

	       final View mView = view;
	       
	       ViewHolder holder = null;
	       
	       if(mView.getTag() != null) {
	    	   
	    	   holder = (ViewHolder)mView.getTag();
	       }
	       else {
		       holder = new ViewHolder();
		       holder.imgPreview = (ImageView) view.findViewById(R.id.imgPreview);
		       holder.prgImgPreview = (ProgressBar)view.findViewById(R.id.prgImgPreview);
		       holder.btnPlay = ((ImageButton)mView.findViewById(R.id.btnPlay));
		       holder.btnStop = ((ImageButton)mView.findViewById(R.id.btnStop));
		       holder.btnCaution = ((ImageButton)mView.findViewById(R.id.btnCaution));
		       holder.btnLock = ((ImageButton)mView.findViewById(R.id.btnLock));
		       holder.btnUnlocked = ((ImageButton)mView.findViewById(R.id.btnUnlocked));
		       holder.btnDelete = ((ImageButton)mView.findViewById(R.id.btnDelete));
		       holder.btnShare = ((ImageButton)mView.findViewById(R.id.btnShare));
		       holder.btnWatching = ((ImageButton)mView.findViewById(R.id.btnWatching));
		       holder.btnDetails = ((ImageButton)mView.findViewById(R.id.btnDetails));
		       
		       mView.setTag(holder);
	       }
	       
		   final int id = cursor.getInt( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_ID ) );
		   
		   final int concurrent = cursor.getInt(cursor.getColumnIndex("concurrent"));
		   
		   final int contentType = cursor.getInt( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_CONTENT ) );
		   
		   final String psikey = cursor.getString( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_PSIKEY ) );
		   
		   final String filepath = cursor.getString( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_FILEPATH ) );
		
		   TextView textViewTitle = (TextView) view.findViewById(R.id.txtCol1);
		   
		   final String title = cursor.getString( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_NAME ) );
		   
		   textViewTitle.setText(title);
		   
		   final String desc = cursor.getString( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_DESC ) );
		   
//		   TextView textViewDesc = (TextView) view.findViewById(R.id.txtCol2);
//		   
//		   textViewDesc.setText(cursor.getString( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_DESC ) ));
	       final int state = cursor.getInt(cursor.getColumnIndex(DBHelper.PUBLISHED_COLUMN_STATE));
	       
	       if(contentType == MainActivity.MEDIA_VIDEO) {
	    	   
	    	   //Bitmap thumbnail = null;
	    	   
	    	   new AsyncTask<ViewHolder, Void, Bitmap>() {
	    		    private ViewHolder v;

	    		    @Override
	    		    protected Bitmap doInBackground(ViewHolder... params) {
	    		    	v = params[0];
	    		    	Bitmap bm = ThumbnailUtils.createVideoThumbnail(filepath,
	    			               MediaStore.Images.Thumbnails.MINI_KIND);	
	    		    	return getScaledImage(bm, 512);	
	    		    }

	    		    @Override
	    		    protected void onPostExecute(Bitmap result) {
	    		        super.onPostExecute(result);
	    		        v.imgPreview.setImageBitmap(result);
	    		        v.prgImgPreview.setVisibility(View.GONE);
	    		        v.imgPreview.setVisibility(View.VISIBLE);
	    		    }
	    		}.execute(holder); 
		       
		       //imageView.setImageBitmap(thumbnail);
	       
	       } else if(contentType == MainActivity.MEDIA_IMAGE) {
	    	   
	    	   new AsyncTask<ViewHolder, Void, Bitmap>() {
	    		    private ViewHolder v;

	    		    @Override
	    		    protected Bitmap doInBackground(ViewHolder... params) {
	    		    	v = params[0];
	    		    	Bitmap bm = BitmapFactory.decodeFile(filepath);
	    		    	return getScaledImage(bm, 512);
	    		    }

	    		    @Override
	    		    protected void onPostExecute(Bitmap result) {
	    		        super.onPostExecute(result);
	    		        v.imgPreview.setImageBitmap(result);
	    		        v.prgImgPreview.setVisibility(View.GONE);
	    		        v.imgPreview.setVisibility(View.VISIBLE);

	    		    }
	    		}.execute(holder); 
	    		
	    	   //Bitmap bm = BitmapFactory.decodeFile(filepath);
	    	   //imageView.setImageBitmap(bm);
	       } else if(contentType == MainActivity.MEDIA_AUDIO) {


			   new AsyncTask<ViewHolder, Void, Bitmap>() {
				   private ViewHolder v;

				   @Override
				   protected Bitmap doInBackground(ViewHolder... params) {
					   v = params[0];
					   Bitmap bm = getAlumbArt(filepath);

					   if(bm != null) {
						   bm = getScaledImage(bm, 512);
					   }

					   return bm;
				   }

				   @Override
				   protected void onPostExecute(Bitmap result) {
					   super.onPostExecute(result);

					   if(result == null) {

						   result = BitmapFactory.decodeResource(mView.getContext().getResources(),
								   R.drawable.bee);

					   }

					   v.imgPreview.setImageBitmap(result);
					   v.prgImgPreview.setVisibility(View.GONE);
					   v.imgPreview.setVisibility(View.VISIBLE);

				   }
			   }.execute(holder);
			   //Bitmap bm = BitmapFactory.decodeFile(filepath);
			   //imageView.setImageBitmap(bm);
		   }
	       
	       if(state == 0) {
	    	   
	    	   holder.btnStop.setVisibility(View.GONE);
	    	   holder.btnPlay.setVisibility(View.VISIBLE);
	       }
	       else {
	    	   
	    	   holder.btnPlay.setVisibility(View.GONE);
	    	   holder.btnStop.setVisibility(View.VISIBLE);
	       }
	       
	       if(cursor.getInt(cursor.getColumnIndex("failedAttempts")) < 20) {
	    	   
	    	   holder.btnCaution.setVisibility(View.GONE);
	       }else {
	    	   holder.btnCaution.setVisibility(View.VISIBLE);
	       }
	       
	       
	       final String password = cursor.getString( cursor.getColumnIndex( DBHelper.PUBLISHED_COLUMN_PASSWORD ) );
	       
	       if(password == null || password.length() == 0) {
	    	   
	    	   holder.btnLock.setVisibility(View.GONE);
	    	   holder.btnUnlocked.setVisibility(View.VISIBLE);
	       } else {
	    	   
	    	   holder.btnUnlocked.setVisibility(View.GONE);
	    	   holder.btnLock.setVisibility(View.VISIBLE);
	       }
	       
	       holder.btnDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					dbHelper.deletePublished(id, psikey);
					if(MainActivity.mPeerStreamService != null)
						MainActivity.mPeerStreamService.stopStreaming(psikey);

					fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
					fragmentManager.beginTransaction().replace(R.id.container, MainActivity.SharedListFragment.newInstance(0, MainActivity.mPeerStreamService, fragmentManager)).addToBackStack(null).commit();
				}
				
	       });
	       
	       holder.btnStop.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					dbHelper.updateState(id, 0);
					if(MainActivity.mPeerStreamService != null)
						MainActivity.mPeerStreamService.stopStreaming(psikey);
					
					mView.findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
					mView.findViewById(R.id.btnStop).setVisibility(View.GONE);
				}
				
	       });
	       
	       holder.btnPlay.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					dbHelper.updateState(id, 1);
					if(MainActivity.mPeerStreamService != null)
						MainActivity.mPeerStreamService.addPsiKey(psikey);
					
					mView.findViewById(R.id.btnStop).setVisibility(View.VISIBLE);
					mView.findViewById(R.id.btnPlay).setVisibility(View.GONE);
				}
				
	       });
	       
	       holder.btnShare.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {

					String shareBody = "";

					if(contentType == MainActivity.MEDIA_AUDIO) {

						shareBody = "I would like to share audio with you.  ";

					} else {
						shareBody = "I would like to share a " + (contentType == MainActivity.MEDIA_VIDEO ? "video" : "picture") + " with you.  ";
					}

	            	 if(password.length() > 0) {
	            		 
	            		 shareBody += "Please contact me for the password.  ";
	            	 }
	            	 
	            	 shareBody += "You can view it at this URL: https://app.peerstreamit.com/viewer.html?psi=" + psikey;
	            	  
	            	    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
	            	        sharingIntent.setType("text/plain");
	            	        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "PeerStreamIt: " + title);
	            	        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
	            	        vcontext.startActivity(Intent.createChooser(sharingIntent, "Share using..."));
				}
				
	       });
					
	       
	       holder.btnWatching.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View view) {
									
									View layout = cursorInflater.inflate(R.layout.popup_viewers,
											(ViewGroup)view.findViewById(R.id.popup_viewers));	
									
							        ListView lv = (ListView)layout.findViewById(R.id.lvViewers);
									
							        ViewerAdapter adapter = new ViewerAdapter(vcontext, dbHelper.getViewers(psikey), 0);
						
									TextView emptyText = (TextView)layout.findViewById(R.id.emptyView);
									lv.setEmptyView(emptyText);
									
									lv.setAdapter(adapter);
									
									final PopupWindow popup = new PopupWindow(layout);
									
									popup.setWidth(View.MeasureSpec.UNSPECIFIED);
								    popup.setHeight(View.MeasureSpec.UNSPECIFIED);
				
								    popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
								    
									popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
									
									((Button)layout.findViewById(R.id.btn_close_popup)).setOnClickListener(new OnClickListener() {
										
										@Override
										public void onClick(View view) {
											
											popup.dismiss();
										}
									});
							
								}
						});
	       
	       holder.btnUnlocked.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					
							final View layout = cursorInflater.inflate(R.layout.popup_pw,
							(ViewGroup)view.findViewById(R.id.popup_pw));
					
							final PopupWindow popup = new PopupWindow(layout);
							
							popup.setWidth(View.MeasureSpec.UNSPECIFIED);
						    popup.setHeight(View.MeasureSpec.UNSPECIFIED);
						    popup.setOutsideTouchable(true);
						    popup.setFocusable(true);
						    popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
						    
							popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
							
							((Button)layout.findViewById(R.id.btn_set_pw)).setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View view) {
									
									String newPass = ((EditText)layout.findViewById(R.id.txtPwNewPw)).getText().toString();
									
									dbHelper.updatePassword(id, newPass);
									
									if(newPass == null || newPass.length() == 0) {
								    	   
								    	   mView.findViewById(R.id.btnLock).setVisibility(View.GONE);
								    	   mView.findViewById(R.id.btnUnlocked).setVisibility(View.VISIBLE);
								       } else {
								    	   
								    	   mView.findViewById(R.id.btnUnlocked).setVisibility(View.GONE);
								    	   mView.findViewById(R.id.btnLock).setVisibility(View.VISIBLE);
								       }
									
									popup.dismiss();
								}
							});
							
							((Button)layout.findViewById(R.id.btn_close_popup)).setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View view) {
									
									popup.dismiss();
								}
							});
				}
				
	       });
	       
	       holder.btnLock.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					
							final View layout = cursorInflater.inflate(R.layout.popup_pw,
							(ViewGroup)view.findViewById(R.id.popup_pw));
					
							final PopupWindow popup = new PopupWindow(layout);
							
							popup.setWidth(View.MeasureSpec.UNSPECIFIED);
						    popup.setHeight(View.MeasureSpec.UNSPECIFIED);
						    popup.setOutsideTouchable(true);
						    popup.setFocusable(true);
						    popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
						    
							popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
							
							((Button)layout.findViewById(R.id.btn_set_pw)).setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View view) {
									
										String newPass = ((EditText)layout.findViewById(R.id.txtPwNewPw)).getText().toString();
										
										dbHelper.updatePassword(id, newPass);
										
										if(newPass == null || newPass.length() == 0) {
									    	   
									    	   mView.findViewById(R.id.btnLock).setVisibility(View.GONE);
									    	   mView.findViewById(R.id.btnUnlocked).setVisibility(View.VISIBLE);
									       } else {
									    	   
									    	   mView.findViewById(R.id.btnUnlocked).setVisibility(View.GONE);
									    	   mView.findViewById(R.id.btnLock).setVisibility(View.VISIBLE);
									       }
										
										popup.dismiss();	
									
									}
							});
							
							((Button)layout.findViewById(R.id.btn_close_popup)).setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View view) {
									
									popup.dismiss();
								}
							});
				}
				
	       });
	       
	       holder.btnDetails.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
								View layout = cursorInflater.inflate(R.layout.popup_details,
								(ViewGroup)view.findViewById(R.id.popup_element));
								
								((TextView) layout.findViewById(R.id.txtDetailTitle)).setText(title);
								((TextView) layout.findViewById(R.id.txtDetailFilePath)).setText(filepath);
								((TextView) layout.findViewById(R.id.txtDetailDesc)).setText(desc);
								((CheckBox) layout.findViewById(R.id.cbDetailProtected)).setChecked(password != null && password.length() > 0);
								((CheckBox) layout.findViewById(R.id.cbDetailPublished)).setChecked(state != 0);
								TextView csd = (TextView) layout.findViewById(R.id.txtConStreamsDisplay);
								csd.setText(Integer.toString(concurrent) + csd.getText());
								final PopupWindow popup = new PopupWindow(layout);
								
								popup.setWidth(View.MeasureSpec.UNSPECIFIED);
							    popup.setHeight(View.MeasureSpec.UNSPECIFIED);

							    popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
							    
								popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
								
								((Button)layout.findViewById(R.id.btn_close_popup)).setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View view) {
										
										popup.dismiss();
									}
								});
								
					}
	       });
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return cursorInflater.inflate(R.layout.published_list_item, arg2, false);
	}
	
	private static Bitmap getScaledImage(Bitmap bitmap, int size) 
	  {
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	
	    if ((size == width) && (size == height))
	      return bitmap;
	    
	    float coefWidth = ((float) size) / width;
	    float coefHeight = ((float) size) / height;
	
	    Matrix matrix = new Matrix();
	    matrix.postScale(coefWidth, coefHeight);
	
	    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	  }

	Bitmap getAlumbArt(String filepath)              //filepath is path of music file
	{
		Bitmap image;

		MediaMetadataRetriever mData=new MediaMetadataRetriever();
		mData.setDataSource(filepath);
		try{
			byte art[]=mData.getEmbeddedPicture();
			image=BitmapFactory.decodeByteArray(art, 0, art.length);
		}
		catch(Exception e)
		{
			image=null;
		}
		return image;
	}
}
