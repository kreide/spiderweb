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
package com.medallia.tiny.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Class that uses Jetty to start the webapp located in the same
 * source tree as this class.
 *
 */
public class JettyWebRunner {
	
	private final Server server;

	/** constructor that uses a random port */
	private JettyWebRunner() throws IOException {
		this(null);
	}
	/** If the input port is null, its value is fetched from the webapp config url. */
	public JettyWebRunner(Integer port) throws IOException {
		server = new Server();
		
		// override so we can allocate the socket right away - we need to
		// know the port we get before we start the webapp (unfortunately,
		// see WebappConfig.overrideGlobalServerURI() )
		SocketConnector connector = new SocketConnector() {
			private boolean init = false;
			@Override public void open() throws IOException {
				if (init) return;
				super.open();
				init = true;
			}
		};
		if (port != null) connector.setPort(port);
		connector.open();
		server.setConnectors(new Connector[] { connector });
		
		WebAppContext webappcontext = new WebAppContext();
		webappcontext.setContextPath("/");
		webappcontext.setMaxFormContentSize(0);
		
		String url = findWebRoot().getAbsolutePath();
		System.out.println("Webapp web root is: " + url);
		webappcontext.setWar(url);
		
		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[]{ webappcontext, new DefaultHandler() });		
		
		server.setHandler(handlers);
	}

	/** @return the root of the 'web' folder of the .war file; assumes that this directory
	 * is in the directory below the jar or the root of the package hierarchy.
	 * 
	 * This is the case when running code directly from a checked out copy of the repository.
	 */
	public static File findWebRoot() {
		Class<?> clazz = JettyWebRunner.class;
		
		File root;
		String url = decodeUtf8Url(clazz.getResource(clazz.getSimpleName() + ".class").getPath());
		int k = url.indexOf(".jar");
		if (k > 0) {
			// Assume we have a web folder in ../ from where the jar is
			root = new File(url.substring(5, k+4)).getParentFile().getParentFile();
		}
		else {
			// Assume we have a web folder in ../ from the root of the package hierarchy
			k = url.indexOf(clazz.getName().replace('.', '/'));
			root = new File(url.substring(0, k)).getParentFile();
		}
		return new File(root, "web");
	}

	private static String decodeUtf8Url(String string) {
		try {
			return URLDecoder.decode(string, "utf-8");
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}
	
	/** @return a {@link JettyWebRunner} that starts on a random (free) port */
	public static JettyWebRunner onRandomPort() throws IOException {
		return new JettyWebRunner();
	}
	
	/** @return the port jetty is listening on */
	public int getPort() {
		return server.getConnectors()[0].getLocalPort();
	}

	/** start jetty */
	public JettyWebRunner start() throws Exception {
		System.out.println("Starting webapp");
		server.start();
		System.out.println("Webapp is running on port " + getPort());
		return this;
		
	}
	
	/** wait for jetty to stop */
	public void join() throws InterruptedException {
		System.out.println("Waiting for webapp to stop");
		server.join();
	}
	
	/** stop jetty */
	public void stop() throws Exception {
		System.out.println("Stopping webapp");
		server.stop();
		System.out.println("Webapp is stopped");
	}

}
