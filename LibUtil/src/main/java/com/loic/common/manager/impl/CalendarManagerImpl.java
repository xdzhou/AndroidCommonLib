package com.loic.common.manager.impl;

import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.SparseArray;

import com.loic.common.LibApplication;
import com.loic.common.manager.CalendarManager;

public class CalendarManagerImpl extends CalendarManager
{
    private static final String TAG = CalendarManagerImpl.class.getSimpleName();

    private SparseArray<CalendarProjection> availableCalendarProviderMap;

    private void initAvailableCalendarMap()
    {
        if(availableCalendarProviderMap == null)
        {
            Cursor cur = LibApplication.getContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null);
            availableCalendarProviderMap = new SparseArray<>(cur.getCount());
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
            {
                long calId = cur.getLong(cur.getColumnIndex(CalendarContract.Calendars._ID));
                if(calId != 0)
                {
                    CalendarProjection cp = new CalendarProjection();
                    cp.calId = calId;
                    cp.accountType = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE));
                    cp.accountName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME));
                    cp.ownerAccount = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT));
                    availableCalendarProviderMap.put((int) calId, cp);
                }
            }
            cur.close();
        }
    }

    @Override
    public CalendarProjection[] getAllCalendarProviders()
    {
        initAvailableCalendarMap();

        if(availableCalendarProviderMap.size() > 0)
        {
            CalendarProjection[] retVals = new CalendarProjection[availableCalendarProviderMap.size()];
            for(int i = 0; i < availableCalendarProviderMap.size(); i++)
            {
                retVals[i] = availableCalendarProviderMap.valueAt(i);
            }
            return retVals;
        }
        return null;
    }

    @Override
    public long addEvent2Calendar(long calId, CalendarEvent event)
    {
        initAvailableCalendarMap();

        if(availableCalendarProviderMap.indexOfKey((int) calId) >= 0)
        {

        }
        return 0;
    }

    @Override
    public void deleteEvent(long eventId)
    {

    }
}
