package com.loic.common.graphic;

import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.loic.common.utils.AndroidUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Scroller;

public class AgendaView extends View 
{
    private static final String TAG = AgendaView.class.getSimpleName();
    
    private int startHour = 7;
    private int endHour = 21;
    private int dayNumPerPage = 5;
    private boolean showWeekend = false;
    private Calendar today;
    private Calendar originFirstDay;
    
    private int hourNumShown;
    private int mTextSize = 12;
    private float mTimeColoumWidth;
    private float mTimeHourDistance;
    private float mTimeTextHeight;
    private float mDayLineHeight;
    private float mDayColoumWidth;
    private float mEventPadding;
    private float mEventTextHeight;
    
    private float minPositionX;
    private float maxPositionX;
    private float maxPositionY;
    
    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private DateFormatSymbols dfs;
    private Scroller flyingScroller;
    private Handler mHandler;
    private float drawPositionX;
    private float drawPositionY;
    private int eventDefaultbgColor;
    private int todayColumnColor;
    //draw element
    private Paint mTimeTextPaint;
    private Paint mDayHeaderPaint;
    private Paint backgroundPaint;
    private TextPaint mEventTextPaint;

    private SparseArray<List<EventRect>> eventsMap; /*key: dayOfMonth*/
    private List<EventRect> eventsToDrawn;
    
    private WeakReference<AgendaViewEventTouchListener> listener;
    
    public AgendaView(Context context) 
    {
        this(context, null, -1);
    }
    
    public AgendaView(Context context, AttributeSet attrs) 
    {
        this(context, attrs, -1);
    }

    public AgendaView(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics());
        init(context);
    }
    
    private void init(Context context)
    {
        mHandler = new Handler();
        dfs = new DateFormatSymbols(Locale.getDefault());
        eventsMap = new SparseArray<List<EventRect>>();
        eventsToDrawn = new ArrayList<AgendaView.EventRect>();
        
        mEventPadding = AndroidUtils.dip2px(1);
        eventDefaultbgColor = Color.rgb(174, 208, 238);
        todayColumnColor = Color.GREEN & 0x88 << 24;
        
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextAlign(Paint.Align.CENTER);
        mTimeTextPaint.setTextSize(mTextSize);
        
        Rect rect = new Rect();
        String mesureTimeText = "00";
        mTimeColoumWidth = mTimeTextPaint.measureText(mesureTimeText);
        mTimeColoumWidth += 2 * AndroidUtils.dip2px(5); //time text padding 5dp
        mTimeHourDistance = AndroidUtils.dip2px(50);
        mTimeTextPaint.getTextBounds(mesureTimeText, 0, mesureTimeText.length(), rect);
        mTimeTextHeight = rect.height();
        
        mDayHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDayHeaderPaint.setTextAlign(Paint.Align.CENTER);
        mDayHeaderPaint.setTextSize(mTextSize * 1.5f);
        
        mDayHeaderPaint.getTextBounds(mesureTimeText, 0, mesureTimeText.length(), rect);
        mDayLineHeight = rect.height();
        mDayLineHeight += 2 * AndroidUtils.dip2px(5); //day header text padding 5dp
        
        mEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setColor(Color.BLACK);
        mEventTextPaint.setTextSize(mTextSize);
        mEventTextPaint.getTextBounds(mesureTimeText, 0, mesureTimeText.length(), rect);
        mEventTextHeight = rect.height();
        
        // Prepare event background color.
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(174, 208, 238));
        
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mScaleDetector = new ScaleGestureDetector(context, mScaleListener);
        flyingScroller = new Scroller(context);
        
        initCalendar(new Date(), false);
    }
    
    public void initCalendar(int year, int month, boolean resetLimitDis)
    {
        today = Calendar.getInstance();
        
        originFirstDay = (Calendar) today.clone();
        originFirstDay.set(Calendar.YEAR, year);
        originFirstDay.set(Calendar.MONTH, month);
        
        computerToMondayDis(resetLimitDis);
    }
    
    public void initCalendar(Date baseDate, boolean resetLimitDis)
    {
        today = Calendar.getInstance();
        
        originFirstDay = (Calendar) today.clone();
        originFirstDay.setTime(baseDate);
        
        computerToMondayDis(resetLimitDis);
    }
    
    private void computerToMondayDis(boolean resetLimitDis)
    {
        int distanceToMonday = originFirstDay.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        distanceToMonday = (distanceToMonday < 0) ? distanceToMonday + 7 : distanceToMonday;
        
        if(originFirstDay.get(Calendar.DAY_OF_MONTH) - distanceToMonday >= 1)
        {
            originFirstDay.add(Calendar.DAY_OF_MONTH, - distanceToMonday);
        }
        else
        {
            originFirstDay.set(Calendar.DAY_OF_MONTH, 1);
            if(! showWeekend)
            {
                switch (originFirstDay.get(Calendar.DAY_OF_WEEK)) 
                {
                case Calendar.SATURDAY:
                    originFirstDay.set(Calendar.DAY_OF_MONTH, 3);
                    break;
                case Calendar.SUNDAY:
                    originFirstDay.set(Calendar.DAY_OF_MONTH, 2);
                    break;
                default:
                    break;
                }
            }
        }
        
        if(resetLimitDis)
        {
            minPositionX = totalStepToValidStep(1 - originFirstDay.get(Calendar.DAY_OF_MONTH)) * mDayColoumWidth;
            maxPositionX = (totalStepToValidStep(originFirstDay.getActualMaximum(Calendar.DAY_OF_MONTH) - originFirstDay.get(Calendar.DAY_OF_MONTH)) - dayNumPerPage + 1) * mDayColoumWidth;
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
    {
        if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY)
        {
            mDayColoumWidth = (MeasureSpec.getSize(widthMeasureSpec) - mTimeColoumWidth) / (float) dayNumPerPage;
            minPositionX = totalStepToValidStep(1 - originFirstDay.get(Calendar.DAY_OF_MONTH)) * mDayColoumWidth;
            maxPositionX = (totalStepToValidStep(originFirstDay.getActualMaximum(Calendar.DAY_OF_MONTH) - originFirstDay.get(Calendar.DAY_OF_MONTH)) - dayNumPerPage + 1) * mDayColoumWidth;
            maxPositionY = (endHour - startHour) * mTimeHourDistance + mTimeTextHeight / 2f - MeasureSpec.getSize(heightMeasureSpec) + mDayLineHeight;
            
            hourNumShown = (int) Math.ceil((MeasureSpec.getSize(heightMeasureSpec) - mDayLineHeight) / mTimeHourDistance);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) 
    {
        super.onDraw(canvas);
        eventsToDrawn.clear();
        earlestTime = (int) ((startHour + drawPositionY / mTimeHourDistance) * 3600);
        lastestTime = (int) ((startHour + (drawPositionY + getHeight() - mDayLineHeight) / mTimeHourDistance) * 3600);
        // Draw the header row and all the axes/separators.
        drawHeaderRow(canvas);
        // Draw events
        drawEvents(canvas);
        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas);
    }
    
    private void drawEvents(Canvas canvas)
    {
        if(eventsToDrawn != null && !eventsToDrawn.isEmpty())
        {
            for(EventRect eventRect : eventsToDrawn)
            {
                int eventBgColor = eventRect.event.mColor == 0 ? eventDefaultbgColor : eventRect.event.mColor;
                backgroundPaint.setColor(eventBgColor);
                canvas.drawRect(eventRect.rectF.left + mEventPadding, eventRect.rectF.top + mEventPadding, 
                        eventRect.rectF.right - mEventPadding, eventRect.rectF.bottom - mEventPadding, backgroundPaint);
                drawTextOnRect(eventRect.event.mName, eventRect.rectF, canvas);
            }
        }
    }
    
    private void drawTextOnRect(String text, RectF rect, Canvas canvas) 
    {
        if(mEventTextHeight > (rect.bottom - rect.top - 2 * mEventPadding) || mEventTextHeight > (rect.right - rect.left - 2 * mEventPadding))
            return;
            
        StaticLayout sl= new StaticLayout(text, mEventTextPaint, (int)(rect.right - rect.left - 2 * mEventPadding), Alignment.ALIGN_CENTER, 1f, 0f, false);
        if(sl.getHeight() > rect.bottom - rect.top)
        {
            int numText = (int) (text.length() * (rect.bottom - rect.top - 2 * mEventPadding) / sl.getHeight());
            sl= new StaticLayout(text.substring(0, numText), mEventTextPaint, (int)(rect.right - rect.left), Alignment.ALIGN_CENTER, 1f, 0f, false);
        }
        canvas.save();
        canvas.translate(rect.left + mEventPadding, rect.top + mEventPadding);
        sl.draw(canvas);
        canvas.restore();
    }
    
    private void drawTimeColumnAndAxes(Canvas canvas)
    {
        backgroundPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, mTimeColoumWidth, getHeight(), backgroundPaint);
        
        backgroundPaint.setColor(Color.GRAY);
        canvas.drawLine(mTimeColoumWidth, mDayLineHeight, getWidth(), mDayLineHeight, backgroundPaint);
        
        int delta = (int) Math.ceil(Math.abs(drawPositionY) / mTimeHourDistance);
        
        for(int i = 0; i < hourNumShown; i++)
        {
            float drawY = mDayLineHeight + (i + delta) * mTimeHourDistance - drawPositionY;
            canvas.drawText((startHour + i + delta)+"", mTimeColoumWidth / 2, drawY + mTimeTextHeight / 2f, mTimeTextPaint);
            canvas.drawLine(mTimeColoumWidth, drawY, getWidth(), drawY, backgroundPaint);
        }
    }
    
    private void drawHeaderRow(Canvas canvas)
    {
        int delta = (int) Math.ceil(Math.abs(drawPositionX) / mDayColoumWidth);    
        if(drawPositionX > 0) //left shift
        {
            delta = delta >= 1 ? delta - 1: delta;
        }
        else //right shift
        {
            delta = - delta;
        }
        for(int i = 0; i < dayNumPerPage + 1; i++)
        {
            drawDayColoum(canvas, delta + i);
        }
        
        canvas.drawLine(mTimeColoumWidth, 0, mTimeColoumWidth, getHeight(), mTimeTextPaint);
    }
    
    private void drawDayColoum(Canvas canvas, int dayDelta)
    {
        Calendar drawDay = (Calendar) originFirstDay.clone();
        drawDay.add(Calendar.DAY_OF_MONTH, validStepToTotalStep(dayDelta));
        
        float drawX = mTimeColoumWidth + dayDelta * mDayColoumWidth - drawPositionX;
        if(drawX < getWidth() && drawX + mDayColoumWidth > mTimeColoumWidth)
        {
            if(isToday(drawDay))
            {
                backgroundPaint.setColor(todayColumnColor);
                canvas.drawRect(drawX, mDayLineHeight, drawX + mDayColoumWidth, getHeight(), backgroundPaint);
            }
            
            canvas.drawText(getDayTitle(drawDay), drawX + 0.5f * mDayColoumWidth, mDayLineHeight / 2, mTimeTextPaint);
            if(drawX > mTimeColoumWidth && drawDay.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            {
                backgroundPaint.setColor(Color.GRAY);
                canvas.drawLine(drawX, 0, drawX, getHeight(), backgroundPaint);
            }
            
            int distanceToMonday = drawDay.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            distanceToMonday = (distanceToMonday < 0) ? distanceToMonday + 7 : distanceToMonday;
            float dp = 0f;
            if(drawX + mDayColoumWidth > mTimeColoumWidth && distanceToMonday + 1 == (showWeekend ? 7 : 5))
            {
                backgroundPaint.setColor(Color.GRAY);
                dp = AndroidUtils.dip2px(5);
                canvas.drawRect(drawX + mDayColoumWidth - dp, 0, drawX + mDayColoumWidth, getHeight(), backgroundPaint);
            }

            //prepare event rectF
            List<EventRect> eventRects = eventsMap.get(drawDay.get(Calendar.DAY_OF_MONTH));
            if(eventRects != null && !eventRects.isEmpty())
            {
                for(EventRect eventRect : eventRects)
                {
                    float[] mesure = checkEventTimeShown(eventRect.event);
                    if(mesure != null)
                    {
                        float top, left, bottom, right;
                        top = mesure[0];
                        bottom = mesure[1];
                        left = drawX;
                        right = drawX + mDayColoumWidth;
                        right = (dp != 0) ? right - dp : right;
                        if(eventRect.rectF == null)
                            eventRect.rectF = new RectF(left, top, right, bottom);
                        else
                            eventRect.rectF.set(left, top, right, bottom);
                        eventsToDrawn.add(eventRect);
                    }
                }
            }
        }
    }
    
    private int earlestTime = -1, lastestTime = -1;
    private float[] checkEventTimeShown(AgendaEvent event)
    {
        int startTime = event.mStartTime.getHours() * 3600 + event.mStartTime.getMinutes() * 60 + event.mStartTime.getSeconds();
        int endTime = event.mEndTime.getHours() * 3600 + event.mEndTime.getMinutes() * 60 + event.mEndTime.getSeconds();
        if(endTime > earlestTime && startTime < lastestTime)
        {
            float[] mesure = new float[2];
            mesure[0] = (startTime - earlestTime) / 3600f * mTimeHourDistance + mDayLineHeight;
            if(mesure[0] < mDayLineHeight)
                mesure[0] = mDayLineHeight;
            mesure[1] = (endTime - earlestTime) / 3600f * mTimeHourDistance + mDayLineHeight;
            return mesure;
        }
        return null;
    }
    
    private String getDayTitle(Calendar drawDay)
    {
        return dfs.getShortWeekdays()[drawDay.get(Calendar.DAY_OF_WEEK)].toUpperCase()+" "+drawDay.get(Calendar.DAY_OF_MONTH);
    }
    
    private boolean isToday(Calendar drawDay)
    {
        return today.get(Calendar.YEAR) == drawDay.get(Calendar.YEAR) 
                && today.get(Calendar.MONTH) == drawDay.get(Calendar.MONTH) 
                && today.get(Calendar.DAY_OF_MONTH) == drawDay.get(Calendar.DAY_OF_MONTH);
    }

    private int validStepToTotalStep(int validStep)
    {
        int retVal = validStep;
        if(! showWeekend && validStep != 0)
        {
            retVal = 0;
            boolean[] weekList = {true, true, true, true, true, false, false};
            int xq = originFirstDay.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            xq = xq < 0 ? xq + 7 : xq;
            
            int valideDelta = 0;
            while (valideDelta < Math.abs(validStep)) 
            {
                if(validStep < 0)
                    xq = (xq - 1 + 7) % 7;
                else
                    xq = (xq + 1) % 7;
                
                if(weekList[xq])
                    valideDelta ++;
                retVal ++;
            }
            if(validStep < 0)
                retVal = - retVal;
        }
        return retVal;
    }
    
    private int totalStepToValidStep(int totalStep)
    {
        int retVal = totalStep;
        if(! showWeekend && totalStep != 0)
        {
            retVal = 0;
            boolean[] weekList = {true, true, true, true, true, false, false};
            int xq = originFirstDay.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            xq = xq < 0 ? xq + 7 : xq;
            
            for(int i=0; i<Math.abs(totalStep); i++)
            {
                if(totalStep < 0)
                    xq = (xq - 1 + 7) % 7;
                else
                    xq = (xq + 1) % 7;
                if(weekList[xq])
                    retVal ++;
            }

            if(totalStep < 0)
                retVal = - retVal;
        }
        return retVal;
    }
    
    /******************************************************
     ****************** Touch event Gesture****************
     ******************************************************/
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
        if(event.getAction() == MotionEvent.ACTION_UP) 
        {
            float leftDays = Math.round(drawPositionX / mDayColoumWidth);
            int nearestOrigin = (int) (drawPositionX - leftDays * mDayColoumWidth);
            
            flyingScroller.startScroll((int) drawPositionX, 0, -nearestOrigin, 0);
            ViewCompat.postInvalidateOnAnimation(AgendaView.this);
        }
        
        //mScaleDetector.onTouchEvent(event);  
        return mGestureDetector.onTouchEvent(event);
    }
    
    private final OnScaleGestureListener mScaleListener = new OnScaleGestureListener() 
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector) 
        {
            float factor = detector.getScaleFactor();
            int delta = -1;
            if(factor > 1.5)
                delta = - 2;
            if (factor < 0.7)
                delta = 2;
            
            if(delta != -1)
            {
                int newDayNumPerPage = dayNumPerPage + delta;
                if(newDayNumPerPage < 1)
                    newDayNumPerPage = 1;
                if(newDayNumPerPage > 5)
                    newDayNumPerPage = 5;
                
                if(newDayNumPerPage != dayNumPerPage)
                {
                    mHandler.removeCallbacks(redrawRunnable);
                    dayNumPerPage = newDayNumPerPage;
                    requestLayout();
                }
            }
            
            return true;
        }
        
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) 
        {
            return true;
        }
        
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) 
        {
            
        }
    };
    
    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() 
    {
        @Override
        public boolean onDown(MotionEvent e) 
        {
            flyingScroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
        {
            boolean edgeChecked = false;
            if(Math.abs(distanceX) > Math.abs(distanceY))
            {
                drawPositionX += distanceX;
                edgeChecked = checkCurrentPositionX();
            }
            else
            {
                drawPositionY += distanceY;
                checkCurrentPositionY();
            }
            
            invalidate();
            return !edgeChecked;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
        {
            if (Math.abs(velocityX) > Math.abs(velocityY))
            {
                flyingScroller.fling((int) drawPositionX, 0, (int) -velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            }
            ViewCompat.postInvalidateOnAnimation(AgendaView.this);
            return true;
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) 
        {
            if(eventsToDrawn != null && !eventsToDrawn.isEmpty() && listener != null && listener.get() != null && e.getX() > mTimeColoumWidth && e.getY() > mDayLineHeight)
            {
                for(EventRect event : eventsToDrawn)
                {
                    if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) 
                    {
                        listener.get().onEventClicked(event.event, event.rectF);
                        playSoundEffect(SoundEffectConstants.CLICK);
                        break;
                    }
                }
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) 
        {
            if(eventsToDrawn != null && !eventsToDrawn.isEmpty() && listener != null && listener.get() != null)
            {
                for(EventRect event : eventsToDrawn)
                {
                    if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) 
                    {
                        listener.get().onEventLongPressed(event.event, event.rectF);
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        break;
                    }
                }
            }
        }
    };
    
    @Override  
    public void computeScroll() 
    {
        if (flyingScroller.computeScrollOffset()) 
        {
            drawPositionX = flyingScroller.getCurrX();
            checkCurrentPosition();
            invalidate();
        }
/*        else 
        {
            flyingScroller.forceFinished(true);
            float leftDays = Math.round(drawPositionX / mDayColoumWidth);
            int nearestOrigin = (int) (drawPositionX - leftDays * mDayColoumWidth);
            
            flyingScroller.startScroll((int) drawPositionX, 0, -nearestOrigin, 0);
            ViewCompat.postInvalidateOnAnimation(AgendaView.this);
        }*/
    }
    
    @Override
    public void invalidate() 
    {
        mHandler.removeCallbacks(redrawRunnable);
        mHandler.postAtFrontOfQueue(redrawRunnable);
    }
    
    private Runnable redrawRunnable = new Runnable() 
    {    
        @Override
        public void run() 
        {
            AgendaView.super.invalidate();
        }
    };
    
    private void checkCurrentPosition()
    {
        checkCurrentPositionX();
        checkCurrentPositionY();
    }
    
    private boolean checkCurrentPositionX()
    {
        boolean edgeChecked = false;
        if(drawPositionX < minPositionX)
        {
            drawPositionX = minPositionX;
            edgeChecked = true;
        }
        else if(drawPositionX > maxPositionX)
        {
            drawPositionX = maxPositionX;
            edgeChecked = true;
        }
        return edgeChecked;
    }
    
    private void checkCurrentPositionY()
    {
        if(drawPositionY < 0)
        {
            drawPositionY = 0;
        }
        else if(drawPositionY > maxPositionY)
        {
            drawPositionY = maxPositionY;
        }
    }
    
    private void resetEventMap(List<AgendaEvent> events)
    {
        eventsMap.clear();
        if(events != null && !events.isEmpty())
        {
            Calendar cal = Calendar.getInstance();
            for(AgendaEvent event : events)
            {
                cal.setTime(event.mStartTime);
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                List<EventRect> eventRects = eventsMap.get(dayOfMonth);
                if(eventRects == null)
                {
                    eventRects = new ArrayList<AgendaView.EventRect>();
                    eventRects.add(new EventRect(event, null));
                    eventsMap.put(dayOfMonth, eventRects);
                }
                else 
                {
                    eventRects.add(new EventRect(event, null));
                }
            }
        }
    }
    
    private void scrollToDayOfMonth(int dayOfMonth)
    {
        if(dayOfMonth >= 1 && dayOfMonth <= originFirstDay.getActualMaximum(Calendar.DAY_OF_MONTH))
        {
            flyingScroller.forceFinished(true);
            
            float delta = totalStepToValidStep(dayOfMonth - originFirstDay.get(Calendar.DAY_OF_MONTH)) * mDayColoumWidth;
            if(delta < minPositionX)
            {
                delta = minPositionX;
            }
            else if (delta > maxPositionX) 
            {
                delta = maxPositionX;
            }
            
            flyingScroller.startScroll((int) drawPositionX, 0, (int) (delta - drawPositionX), 0);
            ViewCompat.postInvalidateOnAnimation(AgendaView.this);
        }
    }
    /******************************************************
     ****************** Common Inner Class ****************
     ******************************************************/
    
    public static class AgendaEvent
    {
        public long mId;
        public Date mStartTime;
        public Date mEndTime;
        public String mName;
        public int mColor;
    }
    
    private static class EventRect
    {
        public AgendaEvent event;
        public RectF rectF;
        
        public EventRect(AgendaEvent event, RectF rectF) 
        {
            this.event = event;
            this.rectF = rectF;
        }
    }
    
    /******************************************************
     ****************** Common Public Func ****************
     ******************************************************/
    public String refreshAgendaWithNewDate(int year, int month, boolean forceLoad)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        return refreshAgendaWithNewDate(cal.getTime(), forceLoad);
    }
    
    public String refreshAgendaWithNewDate(Date newDate, boolean forceLoad)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(newDate);
        if(! forceLoad && cal.get(Calendar.YEAR) == originFirstDay.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == originFirstDay.get(Calendar.MONTH))
        {
            scrollToDayOfMonth(cal.get(Calendar.DAY_OF_MONTH));
        }
        else 
        {
            initCalendar(newDate, true);
            if(listener != null && listener.get() != null)
            {
                resetEventMap(listener.get().onNeedNewEventList(originFirstDay.get(Calendar.YEAR), originFirstDay.get(Calendar.MONTH)));
            }
            else
            {
                eventsMap.clear();
            }
            
            drawPositionX = 0;
            invalidate();
        }
        return cal.get(Calendar.YEAR)+" "+dfs.getMonths()[cal.get(Calendar.MONTH)];
    }
    
    public String refreshAgendaWithNewDate(Date newDate)
    {
        return refreshAgendaWithNewDate(newDate, false);
    }
    
    public void askForEvents()
    {
        if(listener != null && listener.get() != null)
        {
            resetEventMap(listener.get().onNeedNewEventList(originFirstDay.get(Calendar.YEAR), originFirstDay.get(Calendar.MONTH)));
            invalidate();
        }
    }
    
    public int[] getAgendaYearMonth()
    {
        return new int[] {originFirstDay.get(Calendar.YEAR), originFirstDay.get(Calendar.MONTH)};
    }
    
    public int[] getPreviousYearMonth()
    {
        if(originFirstDay.get(Calendar.MONTH) == Calendar.JANUARY)
        {
            return new int[] {originFirstDay.get(Calendar.YEAR) - 1, Calendar.DECEMBER};
        }
        else
        {
            return new int[] {originFirstDay.get(Calendar.YEAR), originFirstDay.get(Calendar.MONTH) - 1};
        }
    }
    
    public int[] getNextYearMonth()
    {
        if(originFirstDay.get(Calendar.MONTH) == Calendar.DECEMBER)
        {
            return new int[] {originFirstDay.get(Calendar.YEAR) + 1, Calendar.JANUARY};
        }
        else
        {
            return new int[] {originFirstDay.get(Calendar.YEAR), originFirstDay.get(Calendar.MONTH) + 1};
        }
    }
    
    public void setEventTouchListener(AgendaViewEventTouchListener listener)
    {
        Class<? extends AgendaViewEventTouchListener> listenerClass = listener.getClass();
        if(listenerClass.isAnonymousClass() || listenerClass.isMemberClass() || listenerClass.isLocalClass())
        {
            throw new IllegalArgumentException("The following AgendaViewEventTouchListener should be static or leaks might occur: " + listenerClass.getCanonicalName());
        }
        this.listener = new WeakReference<AgendaView.AgendaViewEventTouchListener>(listener);
    }
    
    public boolean canScrollHorizontal(int dx)
    {
        return drawPositionX + dx >= minPositionX && drawPositionX + dx <= maxPositionX;
    }

    public int getStartHour() 
    {
        return startHour;
    }

    public void setStartHour(int startHour) 
    {
        this.startHour = startHour;
    }

    public int getEndHour() 
    {
        return endHour;
    }

    public void setEndHour(int endHour) 
    {
        this.endHour = endHour;
    }
    /******************************************************
     ******************** Touch listener ******************
     ******************************************************/
    
    public static interface AgendaViewEventTouchListener
    {
        public void onEventClicked(AgendaEvent event, RectF rect);
        public void onEventLongPressed(AgendaEvent event, RectF rect);
        public List<AgendaEvent> onNeedNewEventList(int year, int month);
    }
}
