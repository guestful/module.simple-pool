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

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class PoolStats {
    public final int size;
    public final long hits;
    public final long miss;
    public final long success;
    public final long failed;
    public final long makeTime;
    public final long evics;

    PoolStats(int size, long hits, long miss, long success, long failed, long makeTime, long evics) {
        this.size = size;
        this.hits = hits;
        this.miss = miss;
        this.success = success;
        this.failed = failed;
        this.makeTime = makeTime;
        this.evics = evics;
    }
}
