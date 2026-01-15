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

/**
 * Abstraction for web server functionality.
 * <p>
 * This allows the Hitorro service framework to work without hard dependencies on Jetty.
 * Implementations can provide actual web server functionality (Jetty) or no-op placeholders
 * for environments like Spring Boot where the container is managed externally.
 * 
 * @see JettyWebServerProvider for Jetty-based implementation
 * @see NoOpWebServerProvider for placeholder implementation
 */
public interface WebServerProvider {
    
    /**
     * Initialize the web server with the given configuration.
     * 
     * @param port HTTP port to bind to
     * @param httpsPort HTTPS port to bind to (0 if not using HTTPS)
     * @return true if initialization succeeded
     */
    boolean initialize(int port, int httpsPort);
    
    /**
     * Start the web server.
     * 
     * @return true if server started successfully
     */
    boolean start();
    
    /**
     * Stop the web server.
     * 
     * @return true if server stopped successfully
     */
    boolean stop();
    
    /**
     * Check if the web server is running.
     * 
     * @return true if server is running
     */
    boolean isRunning();
    
    /**
     * Add a servlet context with the given name.
     * 
     * @param contextName name of the context (e.g., "api", "admin")
     * @return context handle or null if not supported
     */
    Object addContext(String contextName);
    
    /**
     * Add a servlet to a context.
     * 
     * @param context context returned from addContext()
     * @param servletClass servlet class to instantiate
     * @param pathSpec path specification (e.g., "/api/*")
     * @return true if servlet was added
     */
    boolean addServlet(Object context, Class<?> servletClass, String pathSpec);
    
    /**
     * Get a description of this provider implementation.
     * 
     * @return description string
     */
    String getDescription();
}
