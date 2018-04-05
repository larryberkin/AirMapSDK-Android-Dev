package com.airmap.airmapsdk.util;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class ThrottleablePublishSubject<T> {

    public static ThrottleablePublishSubject create() {
        return new ThrottleablePublishSubject();
    }

    private PublishSubject<ObserverItem> publishSubject;

    private ThrottleablePublishSubject() {
        publishSubject = PublishSubject.create();
    }

    public Observable<T> asObservable() {
        Observable<ObserverItem> unthrottledObservable = publishSubject
                .subscribeOn(Schedulers.io())
                .filter(new Func1<ObserverItem, Boolean>() {
                    @Override
                    public Boolean call(ObserverItem observerItem) {
                        return observerItem.type == Type.Unthrottled;
                    }
                });

        Observable<ObserverItem> throttledObservable = publishSubject
                .subscribeOn(Schedulers.io())
                .filter(new Func1<ObserverItem, Boolean>() {
                    @Override
                    public Boolean call(ObserverItem observerItem) {
                        return observerItem.type == Type.Throttled;
                    }
                })
                .throttleWithTimeout(750, TimeUnit.MILLISECONDS);

        return Observable.merge(throttledObservable, unthrottledObservable)
                .map(new Func1<ObserverItem, T>() {
                    @Override
                    public T call(ObserverItem observerItem) {
                        return observerItem.data;
                    }
                });
    }

    public void onNext(T item) {
        publishSubject.onNext(new ObserverItem(item, Type.Unthrottled));
    }

    public void onNextThrottled(T item) {
        publishSubject.onNext(new ObserverItem(item, Type.Throttled));
    }

    private class ObserverItem {
        final T data;
        final Type type;

        ObserverItem(T data, Type type) {
            this.data = data;
            this.type = type;
        }
    }

    private enum Type {
        Throttled,
        Unthrottled
    }
}
