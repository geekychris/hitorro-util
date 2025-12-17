package ht.util.core.iterator.mappers;

/**
 *
 */
public class BaseMappers {
    public static final BaseMapper<String, Integer> S2I = new StringToIntMapper();
    public static final BaseMapper<String, Long> S2L = new StringToLongMapper();
    public static final BaseMapper<String, Double> S2D = new StringToDoubleMapper();
    public static final BaseMapper<String, Float> S2F = new StringToFloatMapper();
    public static final BaseMapper<String, Short> S2S = new StringToShortMapper();
    public static final BaseMapper<String, Byte> S2B = new StringToByteMapper();
    public static final BaseMapper<String, String> S2String = new StringToStringMapper();
}
