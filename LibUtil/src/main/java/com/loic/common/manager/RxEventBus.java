package com.loic.common.manager;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxEventBus
{
    private static RxEventBus instance;

    private final Subject<Object, Object> bus;

    private RxEventBus()
    {
        bus = new SerializedSubject<>(PublishSubject.create());
    }

    private static RxEventBus getInstance()
    {
        if (instance == null)
        {
            synchronized (RxEventBus.class)
            {
                if (instance == null)
                {
                    instance = new RxEventBus();
                }
            }
        }
        return instance;
    }

    public static void post(@NonNull Object event)
    {
        RxEventBus.getInstance().bus.onNext(event);
    }

    public static <T> Observable<T> toObserverable (@NonNull final Class<T> eventType)
    {
        return RxEventBus.getInstance().bus.filter(new Func1<Object, Boolean>()
        {
            @Override
            public Boolean call (Object event)
            {
                return eventType.isInstance (event);
            }
        }).cast(eventType);
    }
}
