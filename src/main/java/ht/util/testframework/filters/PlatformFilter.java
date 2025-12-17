/**
 * Copyright (c) 2003-2008 HiTorro.net
 * <p>
 * <p>
 * description:  junit testcase filter for filtering on EnhancedTestCase.getPlatforms(), typically
 * filtering _in_ against the current running platform and the platforms specified
 * in EnhancedTestCase.getPlatforms().
 * <p>
 * User: chris
 */

package ht.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.ArrayUtil;
import ht.util.core.HTAssert;
import ht.util.core.Platform;
import ht.util.core.opers.HTPredicate;
import ht.util.testframework.TestCaseWrapper;


public class PlatformFilter implements HTPredicate<TestCaseWrapper> {
    private static Platform[] _validPlatforms;

    public static PlatformFilter getPlatformTestCaseFilter(Platform[] platforms) {
        PlatformFilter filter = new PlatformFilter();

        if (ArrayUtil.nullOrEmpty(platforms)) {
            platforms = new Platform[1];
            platforms[0] = Platform.getPlatform();
        }

        setValidPlatforms(platforms);

        return filter;
    }

    public static Platform[] getValidPlatforms() {
        return _validPlatforms;
    }

    public static void setValidPlatforms(Platform[] currentPlatforms) {
        _validPlatforms = currentPlatforms;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "PlatformFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {
    }

    public boolean test(TestCaseWrapper testcase) {
        Platform[] testcasePlatforms = testcase.testDef.platform();
        boolean match = false;

        if (ArrayUtil.nullOrEmpty(testcasePlatforms)) {
            match = true;
        } else {
            for (Platform currentPlatform : _validPlatforms) {
                if (ArrayUtil.contains(testcasePlatforms, currentPlatform)) {
                    match = true;
                }
                break;
            }
        }

        return match;
    }
}