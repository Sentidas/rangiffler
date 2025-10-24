package ru.sentidas.rangiffler.utils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class MapWithWait<K, V> {

    private final Map<K, SyncObject> store = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        store.computeIfAbsent(key, SyncObject::new).put(value);
    }

    /** Старое поведение: дождаться ПЕРВОГО события по ключу и вернуть его. */
    @Nullable
    public V get(K key, long maxWaitTime) throws InterruptedException {
        SyncObject so = store.computeIfAbsent(key, SyncObject::new);
        return so.awaitFirst(maxWaitTime);
    }

    /** Новое: дождаться обновления, удовлетворяющего предикату (не ломает старое API). */
    @Nullable
    public V waitFor(K key, long maxWaitTime, Predicate<V> predicate) throws InterruptedException {
        SyncObject so = store.computeIfAbsent(key, SyncObject::new);
        return so.awaitPredicate(maxWaitTime, predicate);
    }

    /** Опционально для тестов: сбросить ключ (сохранит one-shot семантику get). */
    public void reset(K key) {
        store.remove(key);
    }

    private final class SyncObject {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final Object monitor = new Object();
        private final K key;

        private V firstValue; // фиксируется один раз
        private V lastValue;  // обновляется всегда

        SyncObject(K key) {
            this.key = key;
        }

        void put(V value) {
            synchronized (monitor) {
                lastValue = value;                    // всегда обновляем «последнее»
                if (latch.getCount() != 0L) {         // первый приход — фиксируем one-shot
                    firstValue = value;
                    latch.countDown();
                }
                monitor.notifyAll();                  // будим ожидающих по предикату
            }
        }

        @Nullable
        V awaitFirst(long maxWaitTime) throws InterruptedException {
            return latch.await(maxWaitTime, TimeUnit.MILLISECONDS) ? firstValue : null;
        }

        @Nullable
        V awaitPredicate(long maxWaitTime, Predicate<V> p) throws InterruptedException {
            long end = System.currentTimeMillis() + maxWaitTime;
            synchronized (monitor) {
                while (true) {
                    if (lastValue != null && p.test(lastValue)) {
                        return lastValue;
                    }
                    long remain = end - System.currentTimeMillis();
                    if (remain <= 0) {
                        return lastValue; // для диагностики в ассертах
                    }
                    monitor.wait(remain);
                }
            }
        }
    }
}
