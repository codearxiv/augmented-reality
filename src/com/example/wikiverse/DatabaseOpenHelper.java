// DatabaseConnector.java

package com.example.wikiverse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseOpenHelper extends SQLiteOpenHelper
{
	String createTableQuery;

	//----------------------------------------------------------------------------
	public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version, String createTableQuery)
	{
		super(context, name, factory, version);
		this.createTableQuery =  createTableQuery;
	}

	//----------------------------------------------------------------------------
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		if( createTableQuery != null ){	db.execSQL(createTableQuery); }
	}


	//----------------------------------------------------------------------------
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
	//----------------------------------------------------------------------------

} //end class DatabaseOpenHelper






