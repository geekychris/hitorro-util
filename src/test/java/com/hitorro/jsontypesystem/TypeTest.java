package com.hitorro.jsontypesystem;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.testframework.TestPlus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

@DisplayName("Type System Tests")
public class TypeTest implements TestPlus {

	@Test
	void testTypeLoad () {
		JVSProperties.setDefaultProperties(new JVS(), false);
		BaseFile bf = getOutputFileRelativeBaseFile("foo", true);
		System.out.println(bf.getAbsolutePath());
	}
}
