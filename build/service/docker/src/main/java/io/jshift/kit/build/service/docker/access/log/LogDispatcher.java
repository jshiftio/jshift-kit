package io.jshift.kit.build.service.docker.access.log;/*
 *
 * Copyright 2014 Roland Huss
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


import io.jshift.kit.build.service.docker.access.DockerAccess;

import java.util.HashMap;
import java.util.Map;

/**
 * @author roland
 * @since 25/11/14
 */
public class LogDispatcher {

    private Map<String,LogGetHandle> logHandles;

    private DockerAccess dockerAccess;

    public LogDispatcher(DockerAccess dockerAccess) {
        this.dockerAccess = dockerAccess;
        logHandles = new HashMap<>();
    }

    public synchronized void trackContainerLog(String containerId, LogOutputSpec spec)  {
        LogGetHandle handle = dockerAccess.getLogAsync(containerId, new DefaultLogCallback(spec));
        logHandles.put(containerId, handle);
    }

    public synchronized void fetchContainerLog(String containerId, LogOutputSpec spec) {
        dockerAccess.getLogSync(containerId, new DefaultLogCallback(spec));
    }

    public synchronized void untrackAllContainerLogs() {
        for (String key : logHandles.keySet()) {
            LogGetHandle handle = logHandles.get(key);
            handle.finish();
        }
        logHandles.clear();
    }

    // =======================================================================================


}
