package ht.util;

/**
 *
 */
public class KMPMatch {
    private int[] failure = new int[20];
    private int matchPoint;

    public static void test() {
        KMPMatch kmp = new KMPMatch();

        int ind = kmp.match("ABC ABCDAB ABCDABDDABDE", "ABCDABD");
        assert ind == 11;

        ind = kmp.match("ABC ABCDAB ABCDABCDABDE", "ABCDABD");
        assert ind == -1;
    }

    /**
     * Finds the first occurrence of the pattern in the text.
     */
    final public int match(String text, String pattern) {
        if (failure.length < text.length()) {
            failure = new int[text.length() * 2];
        }
        computeFailure(pattern);
        int j = 0;
        int tl = text.length();
        int pl = pattern.length();
        if (tl == 0) return -1;

        for (int i = 0; i < tl; i++) {
            char tc = text.charAt(i);
            while (j > 0 && pattern.charAt(j) != tc) {
                j = failure[j - 1];
            }
            if (pattern.charAt(j) == tc) {
                j++;
            }
            if (j == pl) {
                matchPoint = i - pl + 1;
                return matchPoint;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private void computeFailure(String pattern) {
        int j = 0;
        for (int i = 1; i < pattern.length(); i++) {
            while (j > 0 && pattern.charAt(j) != pattern.charAt(i)) {
                j = failure[j - 1];
            }
            if (pattern.charAt(j) == pattern.charAt(i)) {
                j++;
            }
            failure[i] = j;
        }
    }

    public int getMatchPoint() {
        return matchPoint;
    }
}
