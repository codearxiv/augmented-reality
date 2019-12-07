package com.example.wikiverse;



import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class BrowseActivity extends ListActivity {


	private ListView dataListView;
	private SimpleCursorAdapter dataListViewAdapter;
	private View highlightedItemView = null;
	private TextView highlightedHeaderTextView = null;


	int savedScrollPosition = 0;
	int savedCheckedPosition = -1;

	private String databaseName;
	private String tableName;
	private String[] columnNames;
	private String selection;
	private String[] selectionArgs;
	private String groupby;
	private String having;
	private String orderby;
	private String createTableQuery;





	//----------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);


		Bundle extras = getIntent().getExtras();

		if( extras!=null ){
			databaseName = extras.getString("databaseName");
			tableName = extras.getString("tableName");
			columnNames = extras.getStringArray("columnNames");
			selection = extras.getString("selection");
			selectionArgs = extras.getStringArray("selectionArgs");
			groupby = extras.getString("groupby");
			having = extras.getString("having");
			orderby = extras.getString("orderby");
			createTableQuery = extras.getString("createTableQuery");
		}

		dataListView = getListView();
		dataListView.getViewTreeObserver().addOnGlobalLayoutListener(listViewLayoutListener);
		//dataListView.getViewTreeObserver().addOnGlobalFocusChangeListener(listViewFocusListener);
		//dataListView.getViewTreeObserver().addOnWindowFocusChangeListener(listViewFocusListener);
		dataListView.setBackgroundColor(Color.WHITE);

		new GetQueriedDataTask().execute((Object[]) null);

		dataListView.setOnItemClickListener(dataListViewClickListener);
		dataListView.setOnItemLongClickListener(dataListViewLongClickListener);
		dataListView.setOnScrollListener(dataListViewScrollListener);




		Button deleteButton = (Button) findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(deleteButtonListener);

		Button editButton = (Button) findViewById(R.id.editButton);
		editButton.setOnClickListener(editButtonListener);

		Button addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(addButtonListener);


	}

	//----------------------------------------------------------------------------
	@Override
	protected void onRestart()
	{
		super.onRestart();

		new GetQueriedDataTask().execute((Object[]) null);

	}

	//----------------------------------------------------------------------------
	@Override
	protected void onResume()
	{
		super.onResume();

	}

	//----------------------------------------------------------------------------
	@Override
	protected void onPause()
	{
		super.onPause();

	}

	//----------------------------------------------------------------------------
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if( !hasFocus ){

			savedScrollPosition = dataListView.getFirstVisiblePosition();
			savedCheckedPosition = dataListView.getCheckedItemPosition();
			//savedScrollPosition = dataListView.getLastVisiblePosition()-1; //for smooth scrolling

		}
	}

	//----------------------------------------------------------------------------
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

	}


	//----------------------------------------------------------------------------
	@Override
	protected void onStop()
	{
		super.onStop();

		Cursor cursor = dataListViewAdapter.getCursor();
		if(cursor != null) {cursor.deactivate();}

		dataListViewAdapter.changeCursor(null);


	}
	//----------------------------------------------------------------------------
	@Override
	public boolean onNavigateUp()
	{
		//finish();
		return true;
	}
	//----------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.browse, menu);
		return true;
	}
	//----------------------------------------------------------------------------




	//----------------------------------------------------------------------------
	public OnGlobalLayoutListener listViewLayoutListener = new OnGlobalLayoutListener()
	{
		boolean previouslyFocused = false;


		@Override
		public void onGlobalLayout() {

			if( dataListView.isFocused() ){

				if( !previouslyFocused ){

					previouslyFocused = true;


					if( highlightedItemView != null ){
						highlightedItemView.setBackgroundColor(Color.WHITE);
						highlightedItemView = null;
					}

					if( savedCheckedPosition != -1 ){

						dataListView.setItemChecked(savedCheckedPosition,true);

						if( savedCheckedPosition>=dataListView.getFirstVisiblePosition() && savedCheckedPosition<=dataListView.getLastVisiblePosition() ){

							highlightedItemView = dataListView.getChildAt( savedCheckedPosition - dataListView.getFirstVisiblePosition() );
							highlightedItemView.setBackgroundColor(Color.LTGRAY);

						}
					}

				}


			}
			else if( previouslyFocused ){
				previouslyFocused = false;
			}


		}


	}; //end anonymous inner class



	//----------------------------------------------------------------------------
	public OnItemLongClickListener dataListViewLongClickListener = new OnItemLongClickListener()
	{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
		{
			return true;
		}

	}; //end OnItemLongClickListener anonymous inner class


	//----------------------------------------------------------------------------
	public OnItemClickListener dataListViewClickListener = new OnItemClickListener()
	{


		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{

			if( position != savedCheckedPosition ){

				//((ListView) parent).setItemChecked(position,true);  //unnecessary
				savedCheckedPosition = position;

				if( view != highlightedItemView ){
					if( highlightedItemView != null ){ highlightedItemView.setBackgroundColor(Color.WHITE); }
					view.setBackgroundColor(Color.LTGRAY);
					highlightedItemView = view;
				}

			}
			else{
				((ListView) parent).setItemChecked(position,false);
				savedCheckedPosition = -1;

				//this *might* always be true
				if( view == highlightedItemView || highlightedItemView != null ){
					highlightedItemView.setBackgroundColor(Color.WHITE);
					highlightedItemView = null;
				}

			}



		}

	}; //end OnItemClickListener inner class

	//----------------------------------------------------------------------------
	public OnScrollListener dataListViewScrollListener = new OnScrollListener()
	{
		private boolean checkedPreviouslyVisible = true;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {

			int currentCheckedPosition = view.getCheckedItemPosition();

			if(currentCheckedPosition != -1){

				if( checkedPreviouslyVisible ){

					if( currentCheckedPosition<firstVisibleItem || currentCheckedPosition>=(firstVisibleItem + visibleItemCount) ){

						if( highlightedItemView!= null) {
							highlightedItemView.setBackgroundColor(Color.WHITE);
							highlightedItemView = null;
						}

						checkedPreviouslyVisible = false;
					}

				}
				else{
					if( currentCheckedPosition>=firstVisibleItem && currentCheckedPosition<(firstVisibleItem + visibleItemCount) ){

						highlightedItemView = view.getChildAt( currentCheckedPosition - firstVisibleItem );
						highlightedItemView.setBackgroundColor(Color.LTGRAY);
						checkedPreviouslyVisible = true;
					}
				}


			}


		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
			int currentCheckedPosition = view.getCheckedItemPosition();
			//toast("restarted", Toast.LENGTH_SHORT);
			if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){

				if( currentCheckedPosition != -1 ){
					if( currentCheckedPosition>=((ListView) view).getFirstVisiblePosition() && currentCheckedPosition<=((ListView) view).getLastVisiblePosition() ){
						checkedPreviouslyVisible = true;
					}
					else{
						checkedPreviouslyVisible = false;
					}

				}
			}


		}

	};


	//----------------------------------------------------------------------------
	public OnClickListener deleteButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if( dataListView.getCheckedItemPosition() != -1 ){
				new DeleteDataTask().execute( (dataListView.getCheckedItemIds())[0] );
			}
		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------
	public OnClickListener editButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if( dataListView.getCheckedItemPosition() != -1 ){

				Intent edit = new Intent(BrowseActivity.this, AddEditActivity.class);
				edit.putExtra("databaseName",databaseName);
				edit.putExtra("tableName",tableName);
				edit.putExtra("rowId",(dataListView.getCheckedItemIds())[0]);

				startActivity(edit);
			}


		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------
	public OnClickListener addButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			Intent add = new Intent(BrowseActivity.this, AddEditActivity.class);
			add.putExtra("databaseName",databaseName);
			add.putExtra("tableName",tableName);
			//add.putExtra("rowId",(Long) null);

			startActivity(add);


		}

	}; //end OnClickListener anonymous inner class



	//----------------------------------------------------------------------------
	public class HeaderClickListener implements OnClickListener
	{
		String columnName;

		HeaderClickListener(String columnName)
		{
			this.columnName = columnName;
		}

		@Override
		public void onClick(View v)
		{
			if( highlightedHeaderTextView != null ){
				highlightedHeaderTextView.setBackgroundColor(Color.LTGRAY);
				highlightedHeaderTextView.setTextColor(Color.BLUE);
			}
			((TextView) v).setBackgroundColor(Color.GRAY);
			((TextView) v).setTextColor(Color.CYAN);
			orderby = columnName;
			new GetQueriedDataTask().execute((Object[]) null);
		}

	}

	//----------------------------------------------------------------------------














	//----------------------------------------------------------------------------
	private class GetQueriedDataTask extends AsyncTask<Object, Object, Cursor>
	{

		private SQLiteDatabase database;

		@Override
		protected Cursor doInBackground(Object... params)
		{

			database = ( new DatabaseOpenHelper(BrowseActivity.this, databaseName, null, 1, createTableQuery) ).getWritableDatabase();
			return database.query(tableName, columnNames, selection, selectionArgs, groupby, having, orderby);

		}
		//----------------------------------------------------------------------------
		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Cursor result)
		{

			if(columnNames == null){ columnNames = result.getColumnNames(); }

			final String[] displayedColumns = new String[ Math.min( columnNames.length - 1, 4 ) ];
			int[] to = new int[displayedColumns.length];

			Integer layout = null;
			Integer layoutHeader = null;

			switch( displayedColumns.length ){
			case 1:
				to[0] = R.id.columnEntry00TextView;
				layout = R.layout.blank_one_column;
				break;
			case 2:
				to[0] = R.id.columnEntry10TextView;
				to[1] = R.id.columnEntry11TextView;
				layout = R.layout.blank_two_column;
				break;
			case 3:
				to[0] = R.id.columnEntry20TextView;
				to[1] = R.id.columnEntry21TextView;
				to[2] = R.id.columnEntry22TextView;
				layout = R.layout.blank_three_column;
				break;
			case 4:
				to[0] = R.id.columnEntry30TextView;
				to[1] = R.id.columnEntry31TextView;
				to[2] = R.id.columnEntry32TextView;
				to[3] = R.id.columnEntry33TextView;
				layout = R.layout.blank_four_column;
				break;
			}


			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View headerView = inflater.inflate( layout, null );
			headerView.setBackgroundColor(Color.BLACK);


			for(int i=0; i<displayedColumns.length; i++){
				displayedColumns[i] = columnNames[i+1];			//assuming columnNames[0] equals "_id"

				TextView headerTextView = (TextView) headerView.findViewById( to[i] );
				headerTextView.setText( displayedColumns[i].replace('_', ' ') );

				if( displayedColumns[i].equals(orderby) ){
					headerTextView.setBackgroundColor(Color.GRAY);
					headerTextView.setTextColor(Color.CYAN);
					highlightedHeaderTextView  = headerTextView;
				}
				else{
					headerTextView.setBackgroundColor(Color.LTGRAY);
					headerTextView.setTextColor(Color.BLUE);
				}

				headerTextView.setOnClickListener( new HeaderClickListener(displayedColumns[i]) );

			}


			TableLayout headerTableLayout = (TableLayout) findViewById(R.id.headerTableLayout);
			headerTableLayout.removeAllViews();
			headerTableLayout.addView(headerView, 0);

			dataListViewAdapter = new SimpleCursorAdapter(BrowseActivity.this, layout, result, displayedColumns, to);
			setListAdapter(dataListViewAdapter);



			if( savedScrollPosition>0 ){
				dataListView.setSelection(savedScrollPosition);
				//dataListView.post( new Runnable() { @Override public void run() { dataListView.smoothScrollToPosition(savedScrollPosition);} });
			}


			database.close();

		}
	} //end class GetAllDatasTask


	//----------------------------------------------------------------------------
	private class GetSelectedRowDataTask extends AsyncTask<Long, Object, Cursor>
	{
		private SQLiteDatabase database;

		@Override
		protected Cursor doInBackground(Long... params)
		{

			database = ( new DatabaseOpenHelper(BrowseActivity.this, databaseName, null, 1, null) ).getWritableDatabase();

			if( params[0] != null ){
				return database.query(tableName, null, "_id" + "=" + params[0], null, null, null, null);
			}
			else{
				return database.query(tableName, null, "_id" + "=" + 0, null, null, null, null);
			}
		}

		@Override
		protected void onPostExecute(Cursor result)
		{
			super.onPostExecute(result);

			database.close();

		}

	} //end class GetDataTask


	//----------------------------------------------------------------------------
	private class DeleteDataTask extends AsyncTask<Long, Object, Object>
	{
		private SQLiteDatabase database;

		@Override
		protected Cursor doInBackground(Long... params)
		{

			database = ( new DatabaseOpenHelper(BrowseActivity.this, databaseName, null, 1, null) ).getWritableDatabase();
			database.delete(tableName, "_id" + "=" + params[0], null);
			database.close();

			return null;

		}

		@Override
		protected void onPostExecute(Object result)
		{
			super.onPostExecute(result);

			dataListView.setItemChecked( dataListView.getCheckedItemPosition(), false );
			savedCheckedPosition = -1;
			savedScrollPosition = dataListView.getFirstVisiblePosition();
			new GetQueriedDataTask().execute((Object[]) null);

		}

	}//end class DeleteDataTask

	//----------------------------------------------------------------------------






	//----------------------------------------------------------------------------
	void toast(String message, int duration)
	{
		Toast results = Toast.makeText(BrowseActivity.this, message, duration);

		//center the Toast in the screen
		results.setGravity(Gravity.CENTER, results.getXOffset()/2, results.getYOffset()/2);
		results.show();
	}
	//----------------------------------------------------------------------------


}//end inner class

