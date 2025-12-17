package ht.util.commandandcontrol;

import net.wimpi.telnetd.io.BasicTerminalIO;

public enum InfoLevel {
    Debug(BasicTerminalIO.GREEN), Info(BasicTerminalIO.GREEN), Warn(BasicTerminalIO.RED), Error(BasicTerminalIO.RED);

    protected int color;

    InfoLevel(int col) {
        color = col;
    }

    public int getColor() {
        return color;
    }
}
