package ht.util.log;

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
