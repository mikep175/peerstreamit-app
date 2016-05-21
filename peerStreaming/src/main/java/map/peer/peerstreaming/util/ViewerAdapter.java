package map.peer.peerstreaming.util;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import map.peer.core.DBHelper;
import map.peer.peerstreaming.R;

public class ViewerAdapter extends CursorAdapter {

	LayoutInflater cursorInflater;
	Context vcontext;
	public ViewerAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, R.layout.viewer_list_item);
		this.cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
		this.vcontext = context;
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		
		final String origin = cursor.getString( cursor.getColumnIndex( "origin" ) );
		final String userAgent = cursor.getString( cursor.getColumnIndex( "userAgent" ) );
		
		TextView textViewTitle = (TextView) view.findViewById(R.id.txtCol1);
		
		textViewTitle.setText(
		        Html.fromHtml(
		            "<a href=\"http://whatismyipaddress.com/ip/" + origin + "\">" + origin + "</a> "));
		
		textViewTitle.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView textViewDesc = (TextView) view.findViewById(R.id.txtCol2);
		
		textViewDesc.setText(userAgent);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return cursorInflater.inflate(R.layout.viewer_list_item, arg2, false);
	}

}
