package com.example.bndtools.xcore.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExampleTest {

	@Test
	public void testXcore() {
		One one = ModelFactory.eINSTANCE.createOne();
		Two two = ModelFactory.eINSTANCE.createTwo();
		one.setTwo(two);
		assertEquals(one, two.getOne());
	}

}
