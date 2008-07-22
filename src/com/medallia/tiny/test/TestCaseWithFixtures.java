/*
 * This file is part of the Spider Web Framework.
 * 
 * The Spider Web Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Spider Web Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Spider Web Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.medallia.tiny.test;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.medallia.tiny.Empty;
import com.medallia.tiny.Fixture;


/**
 * Test case that has a list of @{link Fixture} objects that
 * will have their up() methods called before the test method
 * is invoked and down() after it is complete.
 * 
 * Note that tests using this class should override
 * {@link #safeUp()} and {@link #safeDown()} instead
 * of the methods provided by JUnit.
 * 
 * This class handles the logic necessary to correctly down()
 * all fixtures even if one fails, as well as down() those
 * fixtures that were up()'ed if one up() fails.
 * 
 * Call the {@link #addFixture(Fixture...)} method either
 * in the constructor or in an initializer block to add
 * the fixtures.
 * 
 */
public abstract class TestCaseWithFixtures extends TestCase {
	
	private List<Fixture> fixtures = Empty.list();
	private Fixture.Chained combined;
	
	/** add the given fixtures to the list of fixtures that the test depends on */
	protected void addFixture(Fixture... fixture) {
		fixtures.addAll(Arrays.asList(fixture));
	}
	
	/** code that should run before the test method is invoked */
	protected void safeUp() throws Exception { }
	/** code that should run after the test method completes */ 
	protected void safeDown() throws Exception { }
	
	/** Add a fixture instead (or override safeUp if you must) */
	@Override @Deprecated
	protected final void setUp() throws Exception {
		if (fixtures == null) throw new Error("Already up()ed");
		addFixture(new Fixture() {
			public void down() throws Exception {
				safeDown();
			}
			public void up() throws Exception {
				safeUp();
			}
		}); 
		combined = new Fixture.Chained(fixtures); fixtures = null;		
		super.setUp();
		// if up'ing one of the fixtures fails, we should try to take down the others to avoid causing trouble for later tests:  
		try {
			combined.up();
		} catch (Exception e) {
			combined.downIfNeeded();
			throw e;
		} catch (Error e) {
			combined.downIfNeeded();
			throw e;
		}
	}
	
	/** Add a fixture instead (or override safeDown if you must) */
	@Override @Deprecated
	protected final void tearDown() throws Exception {		
		combined.downIfNeeded();
		super.tearDown();		
	}

}