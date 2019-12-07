package com.example.wikiverse;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AddEditActivity extends Activity {

	private Bundle extras;
	private String databaseName;
	private String tableName;
	private String[] columnNames;
	private Long rowId;


	TableLayout entryTableLayout;


	//----------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_edit);

		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		extras = getIntent().getExtras();
		databaseName = extras.getString("databaseName");
		tableName = extras.getString("tableName");
		rowId = extras.getLong("rowId");

		new FormatDataTask().execute(rowId);


		entryTableLayout = (TableLayout) findViewById(R.id.entryTableLayout);

		Button saveButton = (Button) findViewById(R.id.saveButton2);
		saveButton.setOnClickListener(saveButtonListener);

	}


	//----------------------------------------------------------------------------
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

	}
	//----------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		getMenuInflater().inflate(R.menu.add_edit, menu);
		return true;
	}
	//----------------------------------------------------------------------------
	@Override
	public boolean onNavigateUp()
	{
		//finish();
		return true;
	}
	//----------------------------------------------------------------------------


	//----------------------------------------------------------------------------
	private class FormatDataTask extends AsyncTask<Long, Object, Cursor>
	{
		private SQLiteDatabase database;
		private String[] columnTypes;

		@Override
		protected Cursor doInBackground(Long... params)
		{

			database = ( new DatabaseOpenHelper(AddEditActivity.this, databaseName, null, 1, null) ).getWritableDatabase();

			//rawQuery("SELECT typeof (" + columnName[i] + ") FROM " + tableName;
			//return database.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
			Cursor columnCursor = database.rawQuery("PRAGMA table_info(" + tableName + ")", null);

			columnNames = new String[columnCursor.getCount()];
			columnTypes = new String[columnNames.length];
			int nameIndex = columnCursor.getColumnIndex("name");
			int typeIndex = columnCursor.getColumnIndex("type");


			for(int i=0; i<columnNames.length; i++){
				columnCursor.moveToNext();
				columnNames[i] = columnCursor.getString(nameIndex);
				columnTypes[i] = columnCursor.getString(typeIndex);
			}


			if( params[0] != 0L ){ return database.query(tableName, null, "_id" + "=" + params[0], null, null, null, null); }
			else{ return null; }
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		protected void onPostExecute(Cursor result)
		{
			super.onPostExecute(result);

			boolean rowExists = (result!= null) && (result.moveToFirst());

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			//for i=0 is primary key "_id"
			for(int i=1; i<columnNames.length; i++){

				View newEntryView = inflater.inflate(R.layout.blank_entry, null);

				TextView newEntryTextView = (TextView) newEntryView.findViewById(R.id.entryTextView);
				newEntryTextView.setText( columnNames[i] );
				newEntryTextView.setGravity( Gravity.CENTER_VERTICAL );


				EditText newEntryEditText = (EditText) newEntryView.findViewById(R.id.entryEditText);

				if( columnTypes[i].equals("INTEGER") ){
					newEntryEditText.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER );
					if( rowExists ){ newEntryEditText.setText( String.format("%d", result.getInt(i)) ); }
				}
				else if( columnTypes[i].equals("REAL") ){
					newEntryEditText.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER );
					if( rowExists ){ newEntryEditText.setText( String.format("%.9f", result.getDouble(i)) ); }
				}
				else{
					newEntryEditText.setInputType( InputType.TYPE_CLASS_TEXT );
					if( rowExists ){ newEntryEditText.setText( result.getString(i) ); }
				}
				//newEntryEditText.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER );
				entryTableLayout.addView(newEntryView, i-1);
			}


			database.close();

		}

	} //end class GetDataTask


	//----------------------------------------------------------------------------
	private class SaveDataTask extends AsyncTask<Long, Object, Object>
	{
		private SQLiteDatabase database;

		@Override
		protected Cursor doInBackground(Long... params)
		{
			database = ( new DatabaseOpenHelper(AddEditActivity.this, databaseName, null, 1, null) ).getWritableDatabase();
			ContentValues newRowValues = new ContentValues();

			for(int i=1; i<columnNames.length; i++){

				TableRow entryTableRow = (TableRow) entryTableLayout.getChildAt(i-1);
				EditText entryEditText = (EditText) entryTableRow.getChildAt(1);

				newRowValues.put( columnNames[i], entryEditText.getText().toString() );
			}

			if( params[0]!=0L ){
				database.update(tableName, newRowValues, "_id=" + params[0], null);
			}
			else{
				database.insert(tableName, null, newRowValues);
			}


			database.close();
			//finish();

			return null;

		}
	}//end class SaveDataTask

	//----------------------------------------------------------------------------
	public OnClickListener saveButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			new SaveDataTask().execute(rowId);

			//((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
			//hideSoftInputFromWindow(roomNumEditText.getWindowToken(), 0);

		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------

}//end main class
