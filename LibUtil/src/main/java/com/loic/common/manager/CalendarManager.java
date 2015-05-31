package com.loic.common.manager;

import java.util.Date;

public abstract class CalendarManager extends BasicManager
{
    public abstract CalendarProjection[] getAllCalendarProviders();

    public abstract long addEvent2Calendar(long calId, CalendarEvent event);

    public abstract void deleteEvent(long eventId);

    /*
        Inner class
     */
    public static class CalendarProjection
    {
        public long calId;
        public String accountName;
        public String accountType;
        public String ownerAccount;
    }

    public static class CalendarEvent
    {
        public String organizer;
        public String title;
        public String location;
        public String description;
        public Date startTime;
        public Date endTime;
    }
}
