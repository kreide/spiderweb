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
package com.medallia.tiny;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.LogFactory;

/** 
 * Interface for objects that can be started and later stopped.
 * 
 * This was originally used for test fixtures (i.e. fixed state used as a baseline
 * for running tests), but is now also used generally as a life-cycle interface.
 * 
 */
public interface Fixture {
	
	/** called to start the service or set up the test state */
	void up() throws Exception;
	
	/** called to stop the service or tear down the test state */
	void down() throws Exception;

	/** Interface for fixtures that start a network service */
	public interface NetworkFixture extends Fixture {
		/** @return the port number on which the network service is running */
		int getPort();
	}
	
	/** Interface for fixtures that start a server which can be reached via a {@link URL} */
	public interface ServerFixture extends NetworkFixture {
		/** @return the {@link URL} where the server can be searched */
		URL baseUrl();
	}
	
	/** Do-nothing fixture */
	Fixture NULL = new Fixture() {
		@Implement public void up() { }
		@Implement public void down() { }
	};

	/** Implementation of {@link Fixture} that wraps any number of other
	 * {@link Fixture} objects. It handles calling {@link #up()} and
	 * {@link #down()} on all of them with proper recovery in case
	 * an invocation throws an exception.
	 */
	public static class Chained implements Fixture {
		private final List<? extends Fixture> fixtures;
		private int n = 0;
		
		/**
		 * @param fixtures the fixtures to wrap
		 */
		public Chained(List<? extends Fixture> fixtures) {
			this(fixtures, false);
		}
		/**
		 * @param fixtures the fixtures to wrap
		 */
		public Chained(Fixture... fixtures) {
			this(Arrays.asList(fixtures));
		}
		
		private Chained(List<? extends Fixture> fixtures, boolean alreadyUp) {
			this.fixtures = fixtures;
			if (alreadyUp)
				n = fixtures.size();
		}
		
		/** @return a Chained for fixtures that have already been up()'ed */
		public static Chained alreadyUp(List<? extends Fixture> fixtures) {
			return new Chained(fixtures, true);
		}

		/** call {@link #down()} on the wrapped {@link Fixture} objects only if needed */
		public void downIfNeeded() throws Exception {
			if (n != 0) down();
		}
		
		/** call {@link #down()} on the wrapped {@link Fixture} objects */
		@Implement public void down() throws Exception {
			if (n == 0 && !fixtures.isEmpty()) throw new AssertionError(this + " not up-ed");
			Throwable firstFailure = null;
			while (n-- > 0) {
				try {
					fixtures.get(n).down();
				} catch (Throwable t) {
					LogFactory.getLog(Fixture.class).error("ChainedFixture: down failed: " + fixtures.get(n), t);
					if (firstFailure == null) firstFailure = t;
				}
			}
			if (firstFailure != null) {
				throw new Exception("the first throwable encountered during downing of chained fixture", firstFailure);
			}
		}

		/** call {@link #up()} on the wrapped {@link Fixture} objects */
		@Implement public void up() throws Exception {
			try {
				for (Fixture f : fixtures) {
					f.up();
					n++;
				}
			} catch (Exception e) {
				try {
					downIfNeeded();
				} catch (Exception e2) {
					// ignore
				}
				throw e;
			}
		}
		
	}
	
	/**
	 * Represents a Fixture that has not actually been created yet.
	 * Sometimes one Fixture cannot be created until another one has
	 * been started. In a chain of Fixtures it is then necessary to
	 * have a place holder for the Fixture that will be instantiated
	 * during the up'ing of the whole chain.
	 * 
	 * @param <X> The type of Fixture that this holder represents
	 */
	public static class FixturePlaceHolder<X extends Fixture> implements Fixture {
		private X fixture;
		private Callable<X> createMethod;
		
		/** @param createMethod a Callable with the code to create the real fixture */
		public FixturePlaceHolder(Callable<X> createMethod) {
			this.createMethod = createMethod;
		}
		
		@Implement
		public void up() throws Exception {
			fixture = createMethod.call();
			fixture.up();
		}
		@Implement
		public void down() throws Exception {
			fixture.down();
			fixture = null;
		}
		public X getRealFixture() {
			return fixture;
		}
	}
	
	/** Utility methods for running code with one or more fixtures */ 
	public static class Run {
		/**
		 * Run the given {@link Runnable} with the {@link Fixture}.
		 * 
		 * @param f the fixture to start and stop
		 * @param r the code to run between the start and stop
		 * @throws Exception if the code throws anything
		 */
		public static void with(Fixture f, final Runnable r) throws Exception {
			// Executors.callable eats exceptions, so this instead
			with(f, new Callable<Void>() {
				public Void call() throws Exception {
					r.run();
					return null;
				}
			});
		}
		
		/**
		 * Run the given {@link Callable} with the {@link Fixture}.
		 * 
		 * @param f the fixture to start and stop
		 * @param r the code to run between the start and stop
		 * @throws Exception if the code throws anything
		 */
		public static <X> X with(Fixture f, Callable<X> r) throws Exception {
			f.up();
			try {
				return r.call();
			} finally {
				f.down();
			}
		}
		
		/**
		 * Run the given {@link Callable} with all the giben {@link Fixture} objects.
		 * 
		 * @param r the code to run between the start and stop
		 * @param fixtures the fixtures to start and stop
		 * @throws Exception if the code throws anything
		 */
		public static <X> X withAll(Callable<X> r, Fixture... fixtures) throws Exception {
			return with(new Chained(fixtures), r);
		}
		
		/** call {@link #down()} on the {@link Fixture}; any exception thrown will be
		 * logged and then dropped on the floor
		 */
		public static void safeDown(Fixture fixture) {
			if (fixture == null) return;
			try {
				fixture.down();
			} catch (Throwable t) {
				LogFactory.getLog(Fixture.class).error("Fixture safeDown " + fixture, t);
			}
		}

	}
}
