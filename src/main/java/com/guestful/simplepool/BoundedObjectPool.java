/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.simplepool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class BoundedObjectPool<T> implements ObjectPool<T> {

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong miss = new AtomicLong();
    private final AtomicLong success = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private final AtomicLong makeTime = new AtomicLong();
    private final AtomicLong evics = new AtomicLong();

    private final AtomicInteger size = new AtomicInteger();
    private final BlockingQueue<T> queue;
    private final ObjectPoolFactory<T> factory;
    private final int max;
    private final int maxWait;

    public BoundedObjectPool(int min, int max, int maxWait, ObjectPoolFactory<T> factory) {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("[$min, $max]");
        }
        this.factory = factory;
        this.queue = new ArrayBlockingQueue<>(max);
        this.max = max;
        this.maxWait = maxWait;
        for (int i = 0; i < min; i++) {
            queue.offer(factory.make());
        }
        size.set(queue.size());
    }

    @Override
    public T borrow() throws TimeoutException, InterruptedException {
        hits.incrementAndGet();
        while (queue.isEmpty() && size.get() < max) {
            miss.incrementAndGet();
            long time = System.nanoTime();
            try {
                T el = factory.make();
                success.incrementAndGet();
                if (queue.offer(el)) {
                    size.incrementAndGet();
                } else {
                    evics.incrementAndGet();
                }
            } catch (RuntimeException e) {
                failed.incrementAndGet();
                throw e;
            } finally {
                makeTime.addAndGet(System.nanoTime() - time);
            }
        }
        T obj = queue.poll(maxWait, TimeUnit.MILLISECONDS);
        if (obj == null) {
            throw new TimeoutException("Waited to get resource from pool more than " + maxWait + " ms.");
        }
        return obj;
    }

    @Override
    public void yield(T object) {
        if (object != null && !queue.offer(object)) {
            evics.incrementAndGet();
        }
    }

    @Override
    public <V> V withObject(Function<T, V> f) throws TimeoutException, InterruptedException {
        T obj = borrow();
        try {
            return f.apply(obj);
        } finally {
            yield(obj);
        }
    }

    @Override
    public PoolStats getStatistics() throws TimeoutException, InterruptedException {
        return new PoolStats(
            size.get(),
            hits.get(),
            miss.get(),
            success.get(),
            failed.get(),
            makeTime.get(),
            evics.get()
        );
    }

}
