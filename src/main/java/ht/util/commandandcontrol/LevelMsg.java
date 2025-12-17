package ht.util.commandandcontrol;

import ht.util.core.GenericKeyValue;


public class LevelMsg extends GenericKeyValue<InfoLevel, String> {
    public LevelMsg(InfoLevel infoLevel, String s) {
        super(infoLevel, s);
    }
}
