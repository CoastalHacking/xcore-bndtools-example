package com.example.bndtools.xcore.test;

import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.ecore.EPackage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExampleIntegrationTest {

	/**
	 * The test below asserts the Xcore model is registered in the Equinox
	 * registry via the plugin.xml mechanism and MANIFEST.MF settings
	 */
	@Test
	public void shouldBeRegistered() {
		String nsURI = "com.example.bndtools.xcore.model";
		EPackage.Registry registry = EPackage.Registry.INSTANCE;
		EPackage ePackage = registry.getEPackage(nsURI);
		assertNotNull(ePackage);
	}

}