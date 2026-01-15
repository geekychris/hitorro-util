/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op implementation of {@link WebServerProvider}.
 * <p>
 * This is used in environments like Spring Boot where the web container
 * is managed externally and Hitorro doesn't need to start its own Jetty server.
 * All operations are safe no-ops that won't fail but also won't do anything.
 */
public class NoOpWebServerProvider implements WebServerProvider {
    private static final Logger logger = LoggerFactory.getLogger(NoOpWebServerProvider.class);
    
    private boolean initialized = false;
    
    @Override
    public boolean initialize(int port, int httpsPort) {
        logger.debug("NoOp web server initialize called (port={}, httpsPort={}) - no action taken", port, httpsPort);
        initialized = true;
        return true;
    }
    
    @Override
    public boolean start() {
        logger.debug("NoOp web server start called - no action taken");
        return true;
    }
    
    @Override
    public boolean stop() {
        logger.debug("NoOp web server stop called - no action taken");
        return true;
    }
    
    @Override
    public boolean isRunning() {
        return initialized;
    }
    
    @Override
    public Object addContext(String contextName) {
        logger.debug("NoOp web server addContext called (contextName={}) - returning placeholder", contextName);
        return new Object(); // Return placeholder object
    }
    
    @Override
    public boolean addServlet(Object context, Class<?> servletClass, String pathSpec) {
        logger.debug("NoOp web server addServlet called (servlet={}, pathSpec={}) - no action taken", 
                    servletClass.getSimpleName(), pathSpec);
        return true;
    }
    
    @Override
    public String getDescription() {
        return "No-Op Web Server (external container managed)";
    }
}
