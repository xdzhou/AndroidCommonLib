package com.loic.common.sqliteTool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteManager 
{
	private static final String TAG = SqliteManager.class.getSimpleName();
	
	/******************************************************
	 **************** DateBase Table Function**************
	 ******************************************************/
	public static String generateTableSql(Class<?> modelClass)
	{
		String tableName = getTableName(modelClass);
		if(tableName != null)
		{
			StringBuilder sb = new StringBuilder("create table if not exists "+tableName+" (");
			Field[] allFields = modelClass.getDeclaredFields();
			
			for(Field field : allFields)
			{
				ID id = field.getAnnotation(ID.class);
				Column column = field.getAnnotation(Column.class);
				switch (getFieldType(field)) 
    			{
				case StringField:
					sb.append(field.getName()).append(" ");
					sb.append("varchar(").append(column.length()).append(")");
					break;
				case DateField:
				case LongField:
					sb.append(field.getName()).append(" ");
					sb.append("INTEGER");
					break;
				case BooleanField:
				case IntField:
				case EnumField:
					sb.append(field.getName()).append(" ");
					sb.append("int");
					break;
				default:
					Log.e(TAG, "Unsupport Class Type : "+field.getType().getName());
					continue;
				}
				if(id != null)
					sb.append(" primary key");
				sb.append(", ");
			}
			String sql = sb.toString();
			return sql.substring(0, sql.length()-2)+");";
		}
		else 
		{
			return null;
		}
	}

	/******************************************************
	 ***************** Sql common function ****************
	 ******************************************************/ 	
	public static List<Object> retrieveDatas(SQLiteOpenHelper helper, Class<?> modelClass, String where)
    {
		if(! isModel(modelClass))
    		return null;
		
    	SQLiteDatabase db = helper.getReadableDatabase();
    	String sql = "select * from "+modelClass.getSimpleName();
    	if(where != null)
    		sql = sql.concat(" where ").concat(where).concat(";");
        Cursor cursor = db.rawQuery(sql, null);
        List<Object> list = new ArrayList<Object>();

    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) 
        {
    		try 
    		{
				Object model = modelClass.newInstance();
				Field[] allFields = modelClass.getDeclaredFields();
	    		for(Field field : allFields)
	        	{
	    			int columnIndex = cursor.getColumnIndex(field.getName());
	    			switch (getFieldType(field)) 
	    			{
					case StringField:
						field.set(model, cursor.getString(columnIndex));
						break;
					case DateField:
						long time = cursor.getLong(columnIndex);
						field.set(model, new Date(time));
						break;
					case LongField:
						field.set(model, cursor.getLong(columnIndex));
						break;
					case BooleanField:
						int value = cursor.getInt(columnIndex);
						field.set(model, value == 1);
						break;
					case IntField:
						field.set(model, cursor.getInt(columnIndex));
						break;
					case EnumField:
						int enumIndex = cursor.getInt(columnIndex);
						Object[] objects = field.getType().getEnumConstants();
						if(enumIndex < objects.length)
							field.set(model, objects[enumIndex]);
						break;	
					default:
						Log.e(TAG, "Unsupport Class Type : "+field.getType().getName());
						break;
					}
	        	}
	    		list.add(model);
			} 
    		catch (InstantiationException e) 
    		{
				e.printStackTrace();
			} 
    		catch (IllegalAccessException e) 
    		{
				e.printStackTrace();
			}
        }
    	cursor.close();
        db.close();
    	return list;
    }
	
	public static Object retrieveAData(SQLiteOpenHelper helper, Class<?> modelClass, String where)
    {
		Object retVal = null;
		List<Object> list = retrieveDatas(helper, modelClass, where);
		if(list != null && list.size() == 1)
			retVal = list.get(0);
		return retVal;
    }
	 
	public static long insertData (SQLiteOpenHelper helper, Object model)
    {
		if(! isModel(model.getClass()))
    		return -1;
		
    	SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(model.getClass().getSimpleName(), null, autoCreateContentValues(model));
        db.close();
        return id;
    }
    
	public static void updateData(SQLiteOpenHelper helper, Object model)
    {
		if(! isModel(model.getClass()))
    		return;

    	Field idField = getModelIDField(model.getClass());
    	try 
    	{
			Object value = idField.get(model);
			String stringValue = null;
			switch (getFieldType(idField)) 
			{
			case StringField:
				stringValue = value.toString();
				break;
			case DateField:
				stringValue = String.valueOf(((Date) value).getTime());
				break;
			case LongField:
				stringValue = String.valueOf((Long) value);
				break;
			case BooleanField:
				stringValue = String.valueOf((Boolean) value ? 1 : 0);
				break;
			case IntField:
				stringValue = String.valueOf((Integer) value);
				break;
			case EnumField:
				stringValue = String.valueOf(Arrays.asList(idField.getType().getEnumConstants()).indexOf(value));
				break;
			default:
				Log.e(TAG, "Unsupport Class Type : "+idField.getType().getName());
				break;
			}
			SQLiteDatabase db = helper.getWritableDatabase();
	    	db.update(model.getClass().getSimpleName(), autoCreateContentValues(model), idField.getName() + " = ?", new String[]{stringValue});
	    	db.close();
		} 
    	catch (IllegalArgumentException e) 
    	{
			e.printStackTrace();
		} 
    	catch (IllegalAccessException e) 
    	{
			e.printStackTrace();
		}
	}
	
	public static void deleteData(SQLiteOpenHelper helper, Class modelClass, String where)
    {
		if(! isModel(modelClass))
    		return;
		
		SQLiteDatabase db = helper.getWritableDatabase();
    	db.delete(modelClass.getSimpleName(), where, null);
    	db.close();
    }
	
	public static void deleteData(SQLiteOpenHelper helper, Object model)
    {
		if(! isModel(model.getClass()))
    		return;

    	Field idField = getModelIDField(model.getClass());
    	try 
    	{
			Object value = idField.get(model);
			String stringValue = null;
			switch (getFieldType(idField)) 
			{
			case StringField:
				stringValue = value.toString();
				break;
			case DateField:
				stringValue = String.valueOf(((Date) value).getTime());
				break;
			case LongField:
				stringValue = String.valueOf((Long) value);
				break;
			case BooleanField:
				stringValue = String.valueOf((Boolean) value ? 1 : 0);
				break;
			case IntField:
				stringValue = String.valueOf((Integer) value);
				break;
			case EnumField:
				stringValue = String.valueOf(Arrays.asList(idField.getType().getEnumConstants()).indexOf(value));
				break;
			default:
				Log.e(TAG, "Unsupport Class Type : "+idField.getType().getName());
				break;
			}
			SQLiteDatabase db = helper.getWritableDatabase();
	    	db.delete(model.getClass().getSimpleName(), idField.getName() + " = ?", new String[]{stringValue});
	    	db.close();
		} 
    	catch (IllegalArgumentException e) 
    	{
			e.printStackTrace();
		} 
    	catch (IllegalAccessException e) 
    	{
			e.printStackTrace();
		}
    }

	/******************************************************
	 ********************* Common Funct *******************
	 ******************************************************/
	private static String getTableName(Class modelClass)
	{
		String tabelName = null;
		Model model = (Model) modelClass.getAnnotation(Model.class);
		if(model != null)
		{
			//tabelName = model.tableName();
			//if(tabelName == null)
				tabelName = modelClass.getSimpleName();
		}
		return tabelName;
	}
	
	private static String getTableName(Object modelObject)
	{
		return getTableName(modelObject.getClass());
	}
	
	private static boolean isModel(Class modelClass)
	{
		return modelClass.getAnnotation(Model.class) != null;
	}
	
	private static Field getModelIDField(Class modelClass)
	{
		Field idField = null;
		if(isModel(modelClass))
		{
			Field[] allFields = modelClass.getDeclaredFields();
			for(Field field : allFields)
			{
				if(field.getAnnotation(ID.class) != null)
				{
					idField = field;
					break;
				}
			}
		}
		return null;
	}
	
    public static ContentValues autoCreateContentValues(Object model)
    {
    	if(! isModel(model.getClass()))
    		return null;
    	
    	ContentValues cv = new ContentValues();
    	Field[] allFields = model.getClass().getDeclaredFields();

    	for(Field field : allFields)
    	{
    		try 
    		{
				Object value = field.get(model);
				if(value == null) 
					continue;
				
				switch (getFieldType(field)) 
    			{
				case StringField:
					cv.put(field.getName(), value.toString());
					break;
				case DateField:
					cv.put(field.getName(), ((Date) value).getTime());
					break;
				case LongField:
					cv.put(field.getName(), (Long) value);
					break;
				case BooleanField:
					cv.put(field.getName(), (Boolean) value);
					break;
				case IntField:
					cv.put(field.getName(), (Integer) value);
					break;
				case EnumField:
					cv.put(field.getName(), Arrays.asList(field.getType().getEnumConstants()).indexOf(value));
					break;	
				default:
					Log.e(TAG, "Unsupport Class Type : "+field.getType().getName());
					break;
				}
			} 
    		catch (IllegalArgumentException e) 
    		{
				e.printStackTrace();
			} 
    		catch (IllegalAccessException e) 
    		{
				e.printStackTrace();
			}
    	}
    	return cv;
    }
    
    public static String sqliteEscape(String keyWord)
    {  
        keyWord = keyWord.replace("/", "//");  
        keyWord = keyWord.replace("'", "''");  
        keyWord = keyWord.replace("[", "/[");  
        keyWord = keyWord.replace("]", "/]");  
        keyWord = keyWord.replace("%", "/%");  
        keyWord = keyWord.replace("&","/&");  
        keyWord = keyWord.replace("_", "/_");  
        keyWord = keyWord.replace("(", "/(");  
        keyWord = keyWord.replace(")", "/)");  
        return keyWord;  
    }
	/******************************************************
	 ******************* Field Type Enum ******************
	 ******************************************************/    
	private static enum FieldTypeEnum
	{
		UnknownField, StringField, DateField, LongField, BooleanField, IntField, EnumField;
	}
	
	public static FieldTypeEnum getFieldType(Field field)
	{
		FieldTypeEnum fieldType = FieldTypeEnum.UnknownField;
		Class<?> fieldClass = field.getType();
		
		if(fieldClass.isAssignableFrom(String.class))
			fieldType = FieldTypeEnum.StringField;
		else if (fieldClass.isAssignableFrom(Date.class))
			fieldType = FieldTypeEnum.DateField;
		else if (fieldClass.isAssignableFrom(Long.class) || fieldClass.getName().equals("long"))
			fieldType = FieldTypeEnum.LongField;
		else if (fieldClass.isAssignableFrom(Boolean.class) || fieldClass.getName().equals("boolean"))
			fieldType = FieldTypeEnum.BooleanField;
		else if (fieldClass.isAssignableFrom(Integer.class) || fieldClass.getName().equals("int"))
			fieldType = FieldTypeEnum.IntField;
		else if (fieldClass.isEnum())
			fieldType = FieldTypeEnum.EnumField;
		
		return fieldType;
	}
}
