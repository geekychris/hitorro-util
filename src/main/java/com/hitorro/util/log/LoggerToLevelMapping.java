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
package com.hitorro.util.log;

import org.apache.log4j.Level;

/**
 * Wrapper around a log name and its level. Handles giving the correct answer to questions like what log level and where
 * is the log level inherited from.
 *
 * @author chris
 */
class LoggerToLevelMapping {
    private String msuperName = null;

    private org.apache.log4j.Logger msuper = null;

    private Level m_level = null;

    /**
     * Constructor for the case where we dont know of the logger yet (was not called in the constructor of the logger)
     *
     * @param name
     * @param level
     */
    public LoggerToLevelMapping(String name, Level level) {
        msuperName = name;
        m_level = level;
        // this may set to null if we dont know of the entry at this point in
        // time
        msuper = Logger.getLoggerByName(this.msuperName);
    }

    /**
     * Assumes that we have a logger initialized already
     *
     * @param name of the log category.
     */
    public LoggerToLevelMapping(String name) {
        msuperName = name;

        // this may set to null if we dont know of the entry at this point in
        // time
        msuper = Logger.getLoggerByName(this.msuperName);
        if (msuper != null) {
            m_level = msuper.getLevel();
        }
    }

    public LoggerToLevelMapping(Logger logger) {
        msuperName = logger.getName();

        // this may set to null if we dont know of the entry at this point in
        // time
        msuper = logger;
        m_level = msuper.getLevel();
    }

    public String getName() {
        return msuperName;
    }

    public Level getLevel() {
        return msuper.getEffectiveLevel();
    }

    /**
     * Get the name of the logger that this logger inherits its log level from.
     *
     * @return
     */
    public String getInheritedFromName() {
        if (msuper.getLevel() != null) {
            return "";
        }
        org.apache.log4j.Logger l = getInheritedFrom();
        if (l == null) {
            return "Unknown";
        }
        return l.getName();
    }

    public org.apache.log4j.Logger getInheritedFrom() {
        org.apache.log4j.Logger l = msuper;
        while (l != null && l.getLevel() == null) {
            l = (org.apache.log4j.Logger) l.getParent();
        }
        return l;

    }

    public org.apache.log4j.Logger getLogger() {
        return msuper;
    }

    /**
     * Ensure that there is a logger associated with this category. This is called when a logger is newed up by this
     * name and we may of already had a
     */
    public void ensureLoggerAssociation() {
        if (msuper == null) {
            msuper = Logger.getLoggerByName(this.msuperName);
            if (msuper != null) {
                // it came after we set it up so lets set the level
                msuper.setLevel(this.m_level);
            }
        }
    }
}
