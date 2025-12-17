package ht.util.commandandcontrol.telnet.shell;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TelnetIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.io.terminal.Terminal;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;

import java.io.IOException;
import java.lang.reflect.Field;

public final class ShellIo implements BasicTerminalIO {


    /**
     * Connection with telnet daemon.
     */
    private final Connection connection;

    private final BasicTerminalIO terminalIo;

    /**
     * Handles telnet protocol communication.
     */
    private final TelnetIO telnetIo;

    /**
     * Buffer for already read keys.
     */
    private final int[] buffer = new int[1024];

    /**
     * Marks the buffer position for {@link #buffer}.
     */
    private int bufferPosition = 0;

    /**
     * Constructor.
     *
     * @param connection Connection to work on.
     */
    public ShellIo(final Connection connection) {
        this.connection = connection;
        terminalIo = connection.getTerminalIO();

        // Dirty hack to get the TelnetIO from the Connection
        try {
            Field field = terminalIo.getClass().getDeclaredField("m_TelnetIO");
            field.setAccessible(true);
            telnetIo = (TelnetIO) field.get(terminalIo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Already read ints are put into this read buffer.
     *
     * @param c Read int.
     */
    private void putBuffer(final int c) {
        buffer[bufferPosition++] = c;
    }

    /**
     * Put already read int into read buffer for several times.
     *
     * @param c      Read int.
     * @param number Repeat this often.
     */
    private void putBuffer(final int c, final int number) {
        for (int i = 0; i < number; i++) {
            putBuffer(c);
        }
    }

    /**
     * Read from buffer or connection if nothing is left in the buffer.
     *
     * @return Read int.
     * @throws IOException Read error.
     */
    private int bufferedRead() throws IOException {
        int result;
        if (bufferPosition > 0) {
            bufferPosition--;
            result = buffer[bufferPosition];
        } else {
            result = telnetIo.read();
        }

        return result;
    }

    public int read() throws IOException {
        int i = bufferedRead();

        //translateClassFilenameToCanonical possible control sequences
        i = ((TerminalIO) terminalIo).getTerminal().translateControlCharacter(i);


        //catch & fire a logoutrequest event
        if (i == LOGOUTREQUEST) {
            connection.processConnectionEvent(new ConnectionEvent(connection,
                    ConnectionEvent.CONNECTION_LOGOUTREQUEST));
            i = TerminalIO.HANDLED;
        } else if (i > 256 && i == TerminalIO.ESCAPE) {
            //translateClassFilenameToCanonical an incoming escape sequence
            i = handleEscapeSequence();
        }

        //return i holding a char or a defined special key
        return i;
    }

    /**
     * After getting an ESC this method handles the ESC sequence. If the ESC sequence leads to several ints to read (for
     * example multiple backspace characters) these characters are put into the buffer with {@link #putBuffer(int)} of
     * {@link #putBuffer(int, int)}.
     * <p/>
     * This method extends the functionality of {@link net.wimpi.telnetd.io.terminal.BasicTerminal#translateEscapeSequence(int[])}.
     *
     * @return Read int.
     * @throws IOException Read error.
     */
    private int handleEscapeSequence() throws IOException {
        int c;
        if ((c = bufferedRead()) == Terminal.LSB) {
            int number = 0;
            do {
                c = bufferedRead();
                if (c >= 0 && c < 256 && Character.isDigit((char) c)) {
                    number = number * 10 + c - '0';
                } else {
                    break;
                }
            }
            while (true);
            if (number == 0) {
                number = 1;
            }
            switch (c) {
                case Terminal.A:
                    number--;
                    putBuffer(TerminalIO.UP, number);
                    return TerminalIO.UP;
                case Terminal.B:
                    number--;
                    putBuffer(TerminalIO.DOWN, number);
                    return TerminalIO.DOWN;
                case Terminal.C:
                    number--;
                    putBuffer(TerminalIO.RIGHT, number);
                    return TerminalIO.RIGHT;
                case Terminal.D:
                    number--;
                    putBuffer(TerminalIO.LEFT, number);
                    return TerminalIO.LEFT;
                case 80:    // new in comparison to the original
                    // P (DELETE). The byte code of P, as used in escape sequences
                    // for delete character.
                    number--;
                    putBuffer(TerminalIO.DELETE, number);
                    return TerminalIO.DELETE;
                case 126:   // ~ new in comparison to the original
                    if (number == 3) {
                        putBuffer(TerminalIO.DELETE);
                    }
                default:
                    break;
            }
        }
        return TerminalIO.UNRECOGNIZED;
    }

    public void bell() throws IOException {
        terminalIo.bell();
    }

    public void close() throws IOException {
        terminalIo.close();
    }

    public boolean defineScrollRegion(int topmargin, int bottommargin) throws IOException {
        return terminalIo.defineScrollRegion(topmargin, bottommargin);
    }

    public void eraseLine() throws IOException {
        terminalIo.eraseLine();
    }

    public void eraseScreen() throws IOException {
        terminalIo.eraseScreen();
    }

    public void eraseToBeginOfLine() throws IOException {
        terminalIo.eraseToBeginOfLine();
    }

    public void eraseToBeginOfScreen() throws IOException {
        terminalIo.eraseToBeginOfScreen();
    }

    public void eraseToEndOfLine() throws IOException {
        terminalIo.eraseToEndOfLine();
    }

    public void eraseToEndOfScreen() throws IOException {
        terminalIo.eraseToEndOfScreen();
    }

    public void flush() throws IOException {
        terminalIo.flush();
    }

    public void forceBold(boolean b) {
        terminalIo.forceBold(b);
    }

    public int getColumns() {
        return terminalIo.getColumns();
    }

    public int getRows() {
        return terminalIo.getRows();
    }

    public void homeCursor() throws IOException {
        terminalIo.homeCursor();
    }

    public boolean isAutoflushing() {
        return terminalIo.isAutoflushing();
    }

    public void setAutoflushing(boolean b) {
        terminalIo.setAutoflushing(b);
    }

    public boolean isLineWrapping() throws IOException {
        return terminalIo.isLineWrapping();
    }

    public boolean isSignalling() {
        return terminalIo.isSignalling();
    }

    public void setSignalling(boolean b) {
        terminalIo.setSignalling(b);
    }

    public void moveCursor(int direction, int times) throws IOException {
        terminalIo.moveCursor(direction, times);
    }

    public void moveDown(int times) throws IOException {
        terminalIo.moveDown(times);
    }

    public void moveLeft(int times) throws IOException {
        terminalIo.moveLeft(times);
    }

    public void moveRight(int times) throws IOException {
        terminalIo.moveRight(times);
    }

    public void moveUp(int times) throws IOException {
        terminalIo.moveUp(times);
    }

    public void resetAttributes() throws IOException {
        terminalIo.resetAttributes();
    }

    public void resetTerminal() throws IOException {
        terminalIo.resetTerminal();
    }

    public void restoreCursor() throws IOException {
        terminalIo.restoreCursor();
    }

    public void setBackgroundColor(int color) throws IOException {
        terminalIo.setBackgroundColor(color);
    }

    public void setBlink(boolean b) throws IOException {
        terminalIo.setBlink(b);
    }

    public void setBold(boolean b) throws IOException {
        terminalIo.setBold(b);
    }

    public void setCursor(int row, int col) throws IOException {
        terminalIo.setCursor(row, col);
    }

    public void setDefaultTerminal() throws IOException {
        terminalIo.setDefaultTerminal();
    }

    public void setForegroundColor(int color) throws IOException {
        terminalIo.setForegroundColor(color);
    }

    public void setItalic(boolean b) throws IOException {
        terminalIo.setItalic(b);
    }

    public void setLinewrapping(boolean b) throws IOException {
        terminalIo.setLinewrapping(b);
    }

    public void setTerminal(String terminalname) throws IOException {
        terminalIo.setTerminal(terminalname);
    }

    public void setUnderlined(boolean b) throws IOException {
        terminalIo.setUnderlined(b);
    }

    public void storeCursor() throws IOException {
        terminalIo.storeCursor();
    }

    public void write(byte b) throws IOException {
        terminalIo.write(b);
    }

    public void write(char ch) throws IOException {
        terminalIo.write(ch);
    }

    public void write(String str) throws IOException {
        terminalIo.write(str);
    }

    /**
     * Write byte array to connection.
     *
     * @param bytes Bytes to write.
     * @throws IOException Writing failed.
     */
    public void write(final byte[] bytes) throws IOException {
        telnetIo.write(bytes);
        if (terminalIo.isAutoflushing()) {
            terminalIo.flush();
        }
    }

}
