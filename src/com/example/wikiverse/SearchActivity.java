package com.example.wikiverse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends Activity
{

	private EditText buildingNumEditText;
	private EditText roomNumEditText;
	private EditText locationTypeEditText;
	private EditText locationNameEditText;

	String buildingNum;
	Integer roomNum;
	String locationType;
	String locationName;

	//----------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_main);

		Button browseBuildingsButton = (Button) findViewById(R.id.browseBuildingsButton);
		browseBuildingsButton.setOnClickListener(browseBuildingsButtonListener);

		Button browseCategoriesButton = (Button) findViewById(R.id.browseCategoriesButton);
		browseCategoriesButton.setOnClickListener(browseCategoriesButtonListener);

		Button searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(searchButtonListener);


		buildingNumEditText = (EditText) findViewById(R.id.searchBuildingNumEditText);
		roomNumEditText = (EditText) findViewById(R.id.searchRoomNumEditText);
		locationTypeEditText = (EditText) findViewById(R.id.searchLocationTypeEditText);
		locationNameEditText = (EditText) findViewById(R.id.searchLocationNameEditText);


	}

	//----------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.search, menu);
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
	public OnClickListener browseBuildingsButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			Intent browse = new Intent(SearchActivity.this, BrowseActivity.class);
			browse.putExtra("databaseName","buildingDB");
			browse.putExtra("tableName","buildings");
			browse.putExtra("columnNames", new String[]{"_id","Building_Number","Building_Name"});

			browse.putExtra("createTableQuery", "CREATE TABLE IF NOT EXISTS buildings" +
					"(_id integer primary key autoincrement," + "Building_Number STRING," + "Building_Name STRING);"
					);

			startActivity(browse);
		}

	}; //end OnClickListener anonymous inner class


	//----------------------------------------------------------------------------

	public OnClickListener browseCategoriesButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			Intent browse = new Intent(SearchActivity.this, BrowseActivity.class);
			browse.putExtra("databaseName","categoryDB");
			browse.putExtra("tableName","categories");
			browse.putExtra("columnNames", new String[]{"_id","Category"});

			browse.putExtra("createTableQuery", "CREATE TABLE IF NOT EXISTS categories" +
					"(_id integer primary key autoincrement," + "Category STRING);"
					);

			startActivity(browse);
		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------
	public OnClickListener searchButtonListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			Intent browse = new Intent(SearchActivity.this, BrowseActivity.class);
			browse.putExtra("databaseName","locationDB");
			browse.putExtra("tableName","locations");
			browse.putExtra("columnNames", new String[]{"_id","Building_Number","Room_Number","Location_Type","Location_Name"});

			browse.putExtra("createTableQuery", "CREATE TABLE IF NOT EXISTS locations" +
					"(_id integer primary key autoincrement," + "Building_Number STRING," +
					"Room_Number INTEGER," + "Location_Type STRING," + "Location_Name STRING," +
					"Longitude REAL," + "Latitude REAL," + "Altitude REAL);"
					);

			browse.putExtra( "selection", processSelectionQuery().toString() );
			browse.putExtra( "orderby", "Room_Number" );

			startActivity(browse);
		}

	}; //end OnClickListener anonymous inner class

	//----------------------------------------------------------------------------
	private StringBuilder processSelectionQuery()
	{

		String selection1 = buildingNumEditText.getText().toString();
		String selection2 = roomNumEditText.getText().toString();
		String selection3 = locationTypeEditText.getText().toString();
		String selection4 = locationNameEditText.getText().toString();

		StringBuilder selection = new StringBuilder();
		boolean existsPreviousArg = false;

		if( !selection1.isEmpty() ){
			selection.append("Building_Number IN (").append(processSelection(selection1)).append(")");
			existsPreviousArg= true;
		}
		if( !selection2.isEmpty() ){

			if(existsPreviousArg){ selection.append(" AND "); }
			else{ existsPreviousArg = true; }

			selection.append("Room_Number IN (").append(processSelection(selection2)).append(")");

		}
		if( !selection3.isEmpty() ){

			if(existsPreviousArg){ selection.append(" AND "); }
			else{ existsPreviousArg = true; }

			selection.append("Location_Type IN (").append(processSelection(selection3)).append(")");
		}
		if( !selection4.isEmpty() ){

			if(existsPreviousArg){ selection.append(" AND "); }
			else{ existsPreviousArg = true; }

			selection.append("Location_Name IN (").append(processSelection(selection4)).append(")");
		}

		EditText testEditText = (EditText) findViewById(R.id.testEditText);
		testEditText.setText(selection);

		return selection;

	}

	//----------------------------------------------------------------------------
	private StringBuilder processSelection(String selection)
	{
		StringBuilder processed = new StringBuilder();
		boolean endOfWord = true;
		boolean endOfPhrase = true;


		for(int i=0; i<selection.length(); i++){

			if( endOfPhrase ){
				if( selection.charAt(i) != ' ' ){
					processed.append("'").append(selection.charAt(i));
					endOfPhrase = false;
					endOfWord = false;
				}
			}
			else if( endOfWord ){
				if( selection.charAt(i) == ','){
					processed.append("',");
					endOfPhrase = true;
				}
				else if( selection.charAt(i) != ' ' ){
					processed.append(" ").append(selection.charAt(i));
					endOfWord = false;
				}
			}
			else{
				if( selection.charAt(i) == ','){
					processed.append("',");
					endOfPhrase = true;
					endOfWord = true;
				}
				else if( selection.charAt(i) != ' ' ){
					processed.append(selection.charAt(i));
				}
				else{
					endOfWord = true;
				}
			}


		}

		if( !selection.isEmpty() ){ processed.append("'"); }

		return processed;
	}

	//----------------------------------------------------------------------------

}

