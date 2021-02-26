package io.github.rosemoe.util;

/**
 * Pack two int into a long
 * Also unpack it
 * This is convenient while passing data
 *
 * @author Rose
 */
public class IntPair {

    /**
     * Convert an integer to a long whose binary bits are equal to the given integer
     */
    private static long toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }

    /**
     * Pack two int into a long
     *
     * @param first  First of pair
     * @param second Second of pair
     * @return Packed value
     */
    public static long pack(int first, int second) {
        return (toUnsignedLong(first) << 32L) | toUnsignedLong(second);
    }

    /**
     * Get second of pair
     *
     * @param packedValue Packed value
     * @return Second of pair
     */
    public static int getSecond(long packedValue) {
        return (int) (packedValue & 0xFFFFFFFFL);
    }

    /**
     * Get first of pair
     *
     * @param packedValue Packed value
     * @return First of pair
     */
    public static int getFirst(long packedValue) {
        return (int) (packedValue >> 32L);
    }

}
