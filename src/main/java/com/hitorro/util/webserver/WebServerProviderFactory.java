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
 * Factory for creating {@link WebServerProvider} instances.
 * <p>
 * This factory automatically detects whether Jetty is available on the classpath
 * and returns the appropriate implementation:
 * - If Jetty is available: returns JettyWebServerProvider
 * - If Jetty is not available: returns NoOpWebServerProvider
 * <p>
 * This allows the Hitorro service framework to work in both standalone
 * (with Jetty) and embedded (Spring Boot) environments.
 */
public class WebServerProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(WebServerProviderFactory.class);
    
    private static Boolean jettyAvailable = null;
    private static WebServerProvider cachedProvider = null;
    
    /**
     * Get a web server provider instance.
     * <p>
     * This method caches the provider instance and returns the same one on subsequent calls.
     * 
     * @return appropriate WebServerProvider implementation
     */
    public static synchronized WebServerProvider getProvider() {
        if (cachedProvider == null) {
            cachedProvider = createProvider();
        }
        return cachedProvider;
    }
    
    /**
     * Check if Jetty is available on the classpath.
     * 
     * @return true if Jetty classes are available
     */
    public static synchronized boolean isJettyAvailable() {
        if (jettyAvailable == null) {
            jettyAvailable = checkJettyAvailable();
        }
        return jettyAvailable;
    }
    
    /**
     * Create a new provider instance based on classpath availability.
     * 
     * @return WebServerProvider implementation
     */
    private static WebServerProvider createProvider() {
        if (isJettyAvailable()) {
            try {
                logger.info("Jetty detected on classpath - using JettyWebServerProvider");
                Class<?> jettyProviderClass = Class.forName("com.hitorro.network.servlet.JettyWebServerProvider");
                return (WebServerProvider) jettyProviderClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.warn("Jetty is available but JettyWebServerProvider failed to instantiate - falling back to NoOp", e);
                return new NoOpWebServerProvider();
            }
        } else {
            logger.info("Jetty not available on classpath - using NoOpWebServerProvider");
            return new NoOpWebServerProvider();
        }
    }
    
    /**
     * Check if Jetty classes are available by attempting to load a core Jetty class.
     * 
     * @return true if Jetty is available
     */
    private static boolean checkJettyAvailable() {
        try {
            Class.forName("org.eclipse.jetty.server.Server");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Reset the cached provider (mainly for testing).
     */
    public static synchronized void resetProvider() {
        cachedProvider = null;
        jettyAvailable = null;
    }
}
