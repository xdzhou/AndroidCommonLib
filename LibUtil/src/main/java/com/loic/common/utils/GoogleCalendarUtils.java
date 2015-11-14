package com.loic.common.utils;

import com.loic.common.FailException;
import com.loic.common.LibApplication;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

public class GoogleCalendarUtils 
{
    private static GoogleCalendarUtils instance;
    
    private static String calanderURL = "";
    private static String calanderEventURL = "";
    private static String calanderRemiderURL = "";
    private String calId;
    private String userName;

    static 
    {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) 
        {
            calanderURL = "content://com.android.calendar/calendars";
            calanderEventURL = "content://com.android.calendar/events";
            calanderRemiderURL = "content://com.android.calendar/reminders";
        } 
        else 
        {
            calanderURL = "content://calendar/calendars";
            calanderEventURL = "content://calendar/events";
            calanderRemiderURL = "content://calendar/reminders";
        }
    }
    
    public static GoogleCalendarUtils getInstance()
    {
        if(instance == null)
        {
            try 
            {
                instance = new GoogleCalendarUtils();
            } 
            catch (FailException e) 
            {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private GoogleCalendarUtils() throws FailException
    {
        Cursor userCursor;
        userCursor = LibApplication.getContext().getContentResolver().query(Uri.parse(calanderURL), null, null, null, null);
        for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()) 
        {        
            userName = userCursor.getString(userCursor.getColumnIndex("name"));
            if (userName != null && userName.contains("@gmail.com")) 
            {
                calId = userCursor.getString(userCursor.getColumnIndex("_id"));
                break;
            }
        }
        userCursor.close();
        if(userName == null || calId == null)
            throw new FailException("init GoogleCalendarUtils failed...");
    }

    @SuppressLint("SimpleDateFormat")
    public long addEvent2Calendar(String title, String location, String description, long startDate, long endDate) 
    {
        if(calId.isEmpty() || userName.isEmpty()) 
            return -1;
        
        ContentValues eventCV = new ContentValues();
        eventCV.put("calendar_id", calId);
        if(!title.isEmpty())
            eventCV.put("title", title);
        if(!location.isEmpty())
            eventCV.put("eventLocation", location);
        eventCV.put("eventTimezone", "Europe/Paris");
        if(description.isEmpty())
            eventCV.put("description", description);
        if (Integer.parseInt(Build.VERSION.SDK) < 14)
            eventCV.put("visibility", 0);

        eventCV.put("dtstart", startDate);
        eventCV.put("dtend", endDate);
        eventCV.put("hasAlarm", 1);

        Uri newEvent = LibApplication.getContext().getContentResolver().insert(Uri.parse(calanderEventURL), eventCV);
        
        long id = Long.parseLong(newEvent.getLastPathSegment());
        
        /*add remider*/
        ContentValues remiderCV = new ContentValues();
        remiderCV.put("event_id", id);
        remiderCV.put("minutes", 15);
        remiderCV.put("method", 1); // Alert(1), Email(2), SMS(3)

        LibApplication.getContext().getContentResolver().insert(Uri.parse(calanderRemiderURL),remiderCV);

        remiderCV = new ContentValues();
        remiderCV.put("event_id", id);
        remiderCV.put("minutes", 10);
        remiderCV.put("method", 3); // Alert(1), Email(2), SMS(3)
        LibApplication.getContext().getContentResolver().insert(Uri.parse(calanderRemiderURL),remiderCV);

        return id;
    }

    public void delEvent(long eventid)
    {
        Uri eventUri = ContentUris.withAppendedId(Uri.parse(calanderEventURL),eventid);
        LibApplication.getContext().getContentResolver().delete(eventUri, null, null);
    }

}
