/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.threads.CleaningThread;
import net.openhft.chronicle.core.threads.ThreadDump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory extends ThreadGroup implements ThreadFactory {
    private final AtomicInteger id = new AtomicInteger();
    private final String name;
    @Nullable
    private final Boolean daemon;
    @Nullable
    private final Integer priority;
    @Nullable
    private final StackTrace createdHere;

    public NamedThreadFactory(String name) {
        this(name, null, null);
    }

    public NamedThreadFactory(String name, Boolean daemon) {
        this(name, daemon, null);
    }

    public NamedThreadFactory(String name, @Nullable Boolean daemon, @Nullable Integer priority) {
        super(name);
        this.name = name;
        this.daemon = daemon;
        this.priority = priority;
        createdHere = Jvm.isResourceTracing() ? new StackTrace("NamedThreadFactory created here") : null;
    }

    @Override
    @NotNull
    public Thread newThread(@NotNull Runnable r) {
        int id = this.id.getAndIncrement();
        String nameN = Threads.threadGroupPrefix() + (id == 0 ? this.name : (this.name + '-' + id));
        Thread t = new CleaningThread(r, nameN);
        if (createdHere != null)
            ThreadDump.add(t, createdHere);
        if (daemon != null)
            t.setDaemon(daemon);
        if (priority != null)
            t.setPriority(priority);
        return t;
    }

    public void interruptAll() {
        Thread[] list = new Thread[activeCount() + 1];
        super.enumerate(list);
        for (Thread thread : list) {
            if (thread != null)
                thread.interrupt();
        }
    }
}
