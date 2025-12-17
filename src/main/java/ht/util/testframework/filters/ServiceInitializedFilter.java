package ht.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.ArrayUtil;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.io.StoreException;
import ht.util.startupframework.ServiceContext;
import ht.util.testframework.TestCaseWrapper;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;

import java.io.IOException;

/**
 * CEnsure that a test has all the services it requires.
 */
public class ServiceInitializedFilter implements HTPredicate<TestCaseWrapper> {

    public ServiceInitializedFilter() {

    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "ServiceInitialiedFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(TestCaseWrapper testcase) {
        Class s[] = testcase.testDef.dependentServices();
        if (ArrayUtil.nullOrEmpty(s)) {
            return true;
        }
        for (Class c : s) {
            if (ServiceContext.getSC().getInitializedModule(c) == null) {
                return false;
            }
        }

        return true;
    }


    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {

    }

    public int getSerializationVersion() {
        return 0;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }
}