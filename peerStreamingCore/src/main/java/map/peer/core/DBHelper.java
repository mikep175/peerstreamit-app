package map.peer.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

   public static final String DATABASE_NAME = "peer.db";
   public static final String PUBLISHED_TABLE_NAME = "published";
   public static final String PUBLISHED_COLUMN_ID = "id";
   public static final String PUBLISHED_COLUMN_FILEPATH = "filepath";
   public static final String PUBLISHED_COLUMN_NAME = "name";
   public static final String PUBLISHED_COLUMN_DESC = "desc";
   public static final String PUBLISHED_COLUMN_PSIKEY = "psikey";
   public static final String PUBLISHED_COLUMN_PASSWORD = "password";
   public static final String PUBLISHED_COLUMN_STATE = "state";
   public static final String PUBLISHED_COLUMN_CONTENT = "content";
   private HashMap hp;

   public DBHelper(Context context)
   { 
      super(context, DATABASE_NAME , null, 2);
   }
   
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

       // If you need to add a column
       if (oldVersion <= 1) {
           db.execSQL("ALTER TABLE published ADD COLUMN concurrent INTEGER DEFAULT 1");
       }
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      // TODO Auto-generated method stub
      db.execSQL(
      "create table published " +
      "(id integer primary key, filepath text, name text,desc text,psikey text, password text, state integer, failedAttempts integer, created text, content integer, concurrent INTEGER)"
      );
      
      db.execSQL(
    	      "create table streamviewers " +
    	      "(id integer primary key, origin text, userAgent text, nsi text, psikey text, state integer, created text)"
    	      );
   }

   
   public boolean insertViewer(String origin, String userAgent, String nsi, String psikey, int state)
   {
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues contentValues = new ContentValues();
      contentValues.put("origin", origin);
      contentValues.put("userAgent", userAgent);
      contentValues.put("nsi", nsi);	
      contentValues.put("psikey", psikey);	
      contentValues.put("state", state);
      contentValues.put("created", (new Date()).toString());
      db.insert("streamviewers", null, contentValues);
      return true;
   }
   
   public Cursor getViewers(String psikey)
   {
	   SQLiteDatabase db = this.getReadableDatabase();
	   Cursor res =  db.rawQuery( "select id as _id, * from streamviewers where psikey = ? and state = 1", new String[] { psikey } );
	   
	   return res;
   }
   
   public Integer archiveViewer(String nsi)
   {
	   SQLiteDatabase db = this.getWritableDatabase();
	  ContentValues contentValues = new ContentValues();
	  contentValues.put("state", 0);
      return db.update("streamviewers", contentValues,
      "nsi = ? ", 
      new String[] { nsi });
   }
   
   public Integer archiveViewersAll()
   {
	   SQLiteDatabase db = this.getWritableDatabase();
	  ContentValues contentValues = new ContentValues();
	  contentValues.put("state", 0);
      return db.update("streamviewers", contentValues,
      null, 
      new String[] {  });
   }
   
   public Integer archiveViewers(String psiKey)
   {
	   SQLiteDatabase db = this.getWritableDatabase();
	  ContentValues contentValues = new ContentValues();
	  contentValues.put("state", 0);
      return db.update("streamviewers", contentValues,
      "psikey = ? ", 
      new String[] { psiKey });
   }

   public boolean insertPublished(String path, String name, String desc, String psikey, String password, int state, int content, int concurrent)
   {
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues contentValues = new ContentValues();
      contentValues.put("filepath", path);
      contentValues.put("name", name);
      contentValues.put("desc", desc);
      contentValues.put("psikey", psikey);	
      contentValues.put("password", password);
      contentValues.put("state", state);
      contentValues.put("failedAttempts", 0);
      contentValues.put("created", (new Date()).toString());
      contentValues.put("content", content);
      contentValues.put("concurrent", concurrent);
      db.insert("published", null, contentValues);
      return true;
   }
   
   public Cursor getData(int id){
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor res =  db.rawQuery( "select * from published where id="+id+"", null );
      return res;
   }
   
   public int numberOfRows(){
      SQLiteDatabase db = this.getReadableDatabase();
      int numRows = (int) DatabaseUtils.queryNumEntries(db, PUBLISHED_TABLE_NAME);
      return numRows;
   }
   
   public boolean updateState (Integer id, int state)
   {
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues contentValues = new ContentValues();
      contentValues.put("state", state);
      db.update("published", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
      return true;
   }
   
   public boolean updatePublished (Integer id, String name, String desc, String psikey, String password, int state)
   {
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues contentValues = new ContentValues();
      contentValues.put("name", name);
      contentValues.put("desc", desc);
      contentValues.put("psikey", psikey);	
      contentValues.put("password", password);
      contentValues.put("state", state);
      db.update("published", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
      return true;
   }

   public Integer deletePublished(Integer id, String psikey)
   {
      SQLiteDatabase db = this.getWritableDatabase();
      
      db.delete("streamviewers", 
    	      "psikey = ? ", 
    	      new String[] { psikey });
      
      return db.delete("published", 
      "id = ? ", 
      new String[] { Integer.toString(id) });
   }
   
   public Cursor getAllPublishedCursor() {
	   
	   SQLiteDatabase db = this.getReadableDatabase();
	   Cursor res =  db.rawQuery( "select id as _id, * from published", null );
	   
	   return res;
   }
   
   public String[] getPathAndPassword(String psiKey)
   {
      
      //hp = new HashMap();
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor res =  db.rawQuery( "select \"filepath\", \"password\", \"content\", \"concurrent\"   from published where psikey = ? and state = 1 and failedAttempts < 20", new String[] { psiKey } );
   
      res.moveToFirst();
      
      
      
      if(res.isAfterLast() == false){
    	 String[] ret = new String[4];
    	 ret[0] = res.getString(0);
    	 ret[1] = res.getString(1);
    	 ret[2] = Integer.toString(res.getInt(2));
    	 ret[3] = Integer.toString(res.getInt(3));
    	 return ret;
      }
      return null;
   }
   
   public int updateFailedAttempts(String psiKey)
   {
      
      //hp = new HashMap();
      SQLiteDatabase db = this.getWritableDatabase();
      Cursor res =  db.rawQuery( "select failedAttempts from published where psikey = ?", new String[] { psiKey } );
      res.moveToFirst();
      
      int ret = 21;
      
      if(res.isAfterLast() == false){
    	 ret = res.getInt(0);

    	 ret++;
    	 
    	 ContentValues contentValues = new ContentValues();
         contentValues.put("failedAttempts", ret);
         db.update("published", contentValues, "psikey = ? ", new String[] { psiKey } );
    	 
      }
      
      return ret;
   }
   
   public void resetFailedAttempts(String psiKey)
   {
	 //hp = new HashMap();
	   	 SQLiteDatabase db = this.getWritableDatabase();
    	 
    	 ContentValues contentValues = new ContentValues();
         contentValues.put("failedAttempts", 0);
         db.update("published", contentValues, "psikey = ? ", new String[] { psiKey } );
	    	 
	   
	   
   }
   
   public void updatePassword(int id, String password)
   {
	 //hp = new HashMap();
	   	 SQLiteDatabase db = this.getWritableDatabase();
    	 
    	 ContentValues contentValues = new ContentValues();
         contentValues.put("password", password);
         db.update("published", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
	    	 
	   
	   
   }
   
   public ArrayList<String> getAllPublished()
   {
      ArrayList<String> array_list = new ArrayList<String>();
      
      //hp = new HashMap();
      SQLiteDatabase db = this.getReadableDatabase();
      Cursor res =  db.rawQuery( "select id as _id, * from published", null );
      res.moveToFirst();
      
      while(res.isAfterLast() == false){
         array_list.add(res.getString(res.getColumnIndex(PUBLISHED_COLUMN_PSIKEY)));
         res.moveToNext();
      }
   return array_list;
   }
}