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
