package com.loic.common.sqliteTool;

import java.util.List;

public class SqliteWorker 
{
    protected SqliteHelper helper = null;
    
    public SqliteWorker(SqliteHelper helper) 
    {
    	this.helper = helper;
    }

    public List<Object> retrieveDatas(Class<?> modelClass, String where)
    {
    	return SqliteManager.retrieveDatas(helper, modelClass, where);
    }
    
    public Object retrieveAData(Class<?> modelClass, String where)
    {
    	return SqliteManager.retrieveAData(helper, modelClass, where);
    }
    
    public long insertData (Object model)
    {
    	return SqliteManager.insertData(helper, model);
    }
    
    public void updateData(Object model)
    {
    	SqliteManager.updateData(helper, model);
    }
    
    public void deleteData(Class modelClass, String where)
    {
    	SqliteManager.deleteData(helper, modelClass, where);
    }
    
    public void deleteData(Object model)
    {
    	SqliteManager.deleteData(helper, model);
    }
}