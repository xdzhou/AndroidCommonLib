package com.loic.common.sqliteTool;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import com.loic.common.LibApplication;
import com.loic.common.utils.ClassFinderUtils;

import dalvik.system.DexFile;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class SqliteHelper extends SQLiteOpenHelper 
{
    public SqliteHelper(Context context, String name, CursorFactory factory,int version) 
    {
		super(context, name, factory, version);
	}

	@Override
    public void onCreate(SQLiteDatabase db) 
    {
		
    }
	
	public void close() 
    {
    	if (getWritableDatabase() != null)
    		getWritableDatabase().close();
    }
    
    protected abstract String getModelPackage();
}