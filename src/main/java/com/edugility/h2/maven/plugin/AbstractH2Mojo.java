/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * Copyright (c) 2011-2011 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.h2.maven.plugin;

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;

import java.security.CodeSource;
import java.security.ProtectionDomain;

import java.sql.SQLException;

import java.util.LinkedList;
import java.util.List;

import org.h2.server.TcpServer;

import org.h2.tools.Server;

import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugin.logging.Log;

/**
 * An abstract <a href="http://maven.apache.org/">Maven</a> plugin, or
 * <i>mojo</i>, that helps with interacting with an <a
 * href="http://www.h2database.com/">H2</a> <a
 * href="http://h2database.com/html/tutorial.html#using_server">TCP
 * server</a>.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since <tt>1.0-SNAPSHOT</tt>
 */
public abstract class AbstractH2Mojo extends AbstractMojo {

  /**
   * Whether SSL should be used.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   * 
   * @parameter expression="${h2.useSSL}" property="useSSL"
   */
  private boolean useSSL;
  
  /**
   * Whether other processes may connect to the spawned server.  See
   * <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.allowOthers}" property="allowOthers"
   */
  private boolean allowOthers;

  /**
   * The port to run the H2 TCP server on; {@code 9092} by default.
   * See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter default-value="9092" expression="${h2.port}" property="port"
   */
  private int port;

  /**
   * The base directory beneath which H2 databases will be created by
   * the spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.baseDirectory}" property="baseDirectory"
   */
  private File baseDirectory;

  /**
   * Whether databases must exist in order to be connected to, or
   * whether they will be created on demand.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.ifExists}" property="ifExists"
   */
  private boolean ifExists;

  /**
   * The password required to shut down a spawned H2 TCP server.  See
   * <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.shutdownPassword}" property="shutdownPassword" default-value="h2-maven-plugin"
   */
  private String shutdownPassword;

  /**
   * The hostname to which shutdown requests will be directed. See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.shutdownHost}" property="shutdownHost" default-value="localhost"
   */
  private String shutdownHost;

  /**
   * Whether shutdown should be forced or attempted normally.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.forceShutdown}" property="forceShutdown"
   */
  private boolean forceShutdown;

  /**
   * Whether shutdown should force <i>all</i> servers spawned on the
   * same host to shut down.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.shutdownAllServers}" property="shutdownAllServers"
   */
  private boolean shutdownAllServers;

  /**
   * Whether trace information should be output. See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @parameter expression="${h2.trace}" property="trace"
   */
  private boolean trace;

  /**
   * The {@link File} that identifies the Java executable to use to
   * spawn a new H2 TCP server.  The default value used, if this field
   * is {@code null}, will be the path formed by concatenating the
   * value of the {@code java.home} {@linkplain
   * System#getProperty(String) System property} with "{@code bin}"
   * and "{@code java}".
   *
   * @parameter expression="${h2.java}" property="java"
   */
  private File java;

  /**
   * Any options to pass to the Java executable on the command line.
   * Each element of this array will <i>not</i> be split on
   * whitespace or otherwise tokenized.
   *
   * @parameter property="javaOptions"
   */
  private String[] javaOptions;

  /**
   * Creates a new {@link AbstractH2Mojo}.
   */
  protected AbstractH2Mojo() {
    super();
    this.setPort(9092);
    this.setShutdownPassword("h2-maven-plugin");
    this.setJava(new File(new File(new File(System.getProperty("java.home")), "bin"), "java"));
  }

  /**
   * Returns {@code true} if a shutdown operation should shut down all
   * H2 TCP servers running on the host in question.
   *
   * @return {@code true} if a shutdown operation should shut down all
   * H2 TCP servers running on the host in question; {@code false} otherwise
   */
  public boolean getShutdownAllServers() {
    return this.shutdownAllServers;
  }

  public void setShutdownAllServers(final boolean shutdownAllServers) {
    this.shutdownAllServers = shutdownAllServers;
  }

  public String getShutdownPassword() {
    return this.shutdownPassword;
  }

  public void setShutdownPassword(final String pw) {
    this.shutdownPassword = pw;
  }

  public String getShutdownHost() {
    return this.shutdownHost;
  }
  
  public void setShutdownHost(final String shutdownHost) {
    this.shutdownHost = shutdownHost;
  }

  public boolean getForceShutdown() {
    return this.forceShutdown;
  }

  public void setForceShutdown(final boolean shutdown) {
    this.forceShutdown = shutdown;
  }

  /**
   * Returns the {@link File} representing the path to the H2 jar file
   * that is on the classpath.  This method never returns {@code
   * null}.
   *
   * @return the {@link File} representing the path to the H2 jar
   * file; never {@code null}
   */
  public final File getH2() {
    final ProtectionDomain pd = Server.class.getProtectionDomain();
    assert pd != null;
    final CodeSource cs = pd.getCodeSource();
    assert cs != null;
    final URL location = cs.getLocation();
    assert location != null;
    try {
      return new File(location.toURI());
    } catch (final URISyntaxException wontHappen) {
      throw (InternalError)new InternalError().initCause(wontHappen);
    }
  }

  /**
   * Returns the {@link File} representing the path to the Java
   * executable used to spawn H2 TCP servers.  This method may return
   * {@code null}.
   *
   * @return the {@link File} representing the path to the Java
   * executable, or {@code null}
   */
  public File getJava() {
    return this.java;
  }

  /**
   * Sets the {@link File} representing the path to the Java
   * executable used to spawn H2 TCP servers.
   *
   * @param java the {@link File} to use; may be {@code null}
   */
  public void setJava(final File java) {
    this.java = java;
  }

  public String[] getJavaOptions() {
    return this.javaOptions;
  }

  public void setJavaOptions(final String... javaOptions) {
    this.javaOptions = javaOptions;
  }

  public boolean getTrace() {
    return this.trace;
  }

  public void setTrace(final boolean trace) {
    this.trace = trace;
  }

  public boolean getIfExists() {
    return this.ifExists;
  }

  public void setIfExists(final boolean ifExists) {
    this.ifExists = ifExists;
  }

  public boolean getAllowOthers() {
    return this.allowOthers;
  }

  public void setAllowOthers(final boolean allowOthers) {
    this.allowOthers = allowOthers;
  }

  public boolean getUseSSL() {
    return this.useSSL;
  }

  public void setUseSSL(final boolean useSSL) {
    this.useSSL = useSSL;
  }

  public File getBaseDirectory() {
    return this.baseDirectory;
  }

  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  /**
   * Returns the port on which new H2 TCP servers spawned by this
   * class will listen.  H2's default port is 9092.
   *
   * @return the port on which new H2 TCP servers spawned by this
   * class will listen; this will be a number between {@code 0} and
   * {@code 65535}, inclusive
   */
  public int getPort() {
    return this.port;
  }

  /**
   * Sets the port on which new H2 TCP servers spawned by this class
   * will listen.  H2's default port is 9092.
   *
   * @param port the new port; will be constrained to be between
   * {@code 0} and {@code 65535}, inclusive
   */
  public void setPort(final int port) {
    this.port = Math.min(65535, Math.max(0, port));
  }

  /**
   * Creates a new {@link Server} using H2's {@link
   * Server#createTcpServer(String[])} method.  This method must never
   * return {@code null}.
   *
   * <p><strong>Note:</strong> This method is experimental.</p>
   *
   * @return a new {@link Server} as produced by the {@link
   * Server#createTcpServer(String[])} method; never {@code null}
   *
   * @exception SQLException if an error occurs
   */
  protected Server createServer() throws SQLException {
    final List<String> args = this.getServerArguments();
    final Server server = Server.createTcpServer(args.toArray(new String[args.size()]));
    return server;
  }

  /**
   * Returns a {@link ProcessBuilder} that can be used and reused to
   * spawn new fully configured H2 TCP servers.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <h2>Design Notes</h2>
   *
   * <p>At the moment, the implementation of this method returns a new
   * {@link ProcessBuilder} in all cases, but this behavior should not
   * be relied upon.</p>
   *
   * @return a {@link ProcessBuilder}; never {@code null}
   */
  protected ProcessBuilder getServerSpawner() {
    final List<String> args = this.getServerArguments();
    assert args != null;
    args.remove("-tcpDaemon"); // when you're spawning it, it should NEVER run as a daemon, no matter what.

    int argumentIndex = 0;

    File java = this.getJava();
    if (java == null) {
      java = new File(new File(new File(System.getProperty("java.home")), "bin"), "java");
    }
    args.add(argumentIndex++, java.getAbsolutePath());

    final String[] javaOptions = this.getJavaOptions();
    if (javaOptions != null && javaOptions.length > 0) {
      for (final String option : javaOptions) {
        if (option != null && !option.trim().isEmpty()) {
          args.add(argumentIndex++, option);
        }
      }
    }

    args.add(argumentIndex++, "-cp");
    final File fileLocation = this.getH2();
    assert fileLocation != null;
    args.add(argumentIndex++, fileLocation.getAbsolutePath());

    args.add(argumentIndex++, Server.class.getName());

    final Log log = this.getLog();
    if (log != null && log.isDebugEnabled()) {
      log.debug("Process arguments: " + args);
    }
    return new ProcessBuilder(args);
  }

  /**
   * Returns a {@link Process} representing an H2 TCP server that has
   * been started.  The returned {@link Process} is guaranteed not to
   * be {@code null} and will not have been {@linkplain
   * Process#destroy() destroyed}.
   *
   * @return a non-{@code null} {@link Process}
   *
   * @exception IOException if an error occurred during {@link
   * Process} creation, usually because of a {@link
   * ProcessBuilder#start()} failure
   */
  protected Process spawnServer() throws IOException {
    return this.getServerSpawner().start();
  }

  /**
   * Shuts down a server spawned earlier by the {@link #spawnServer()} method.
   *
   * @exception SQLException if the server could not be shut down
   */
  protected void shutdownServer() throws SQLException {
    String password = this.getShutdownPassword();
    if (password == null) {
      password = "";
    }
    final int port = this.getPort();
    String host = this.getShutdownHost();
    if (host == null) {
      host = "";
    } else {
      host = host.trim();
    }
    if (host.isEmpty()) {
      host = "localhost";
    }
    TcpServer.shutdown(String.format("tcp://%s:%d", host, port), password, this.getForceShutdown(), this.getShutdownAllServers());
  }

  /**
   * Returns a {@link List} of arguments suitable for feeding to a new
   * H2 TCP server process, or to the parameters accepted by the
   * {@link Server#createTcpServer(String[])} method.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a new, mutable, non-{@code null} {@link List} of
   * arguments for new H2 processes
   */
  protected List<String> getServerArguments() {

    final List<String> args = new LinkedList<String>();

    final File baseDirectory = this.getBaseDirectory();
    if (baseDirectory != null) {
      args.add("-baseDir");
      args.add(String.format("%s", baseDirectory.getAbsolutePath()));
    }

    if (this.getIfExists()) {
      args.add("-ifExists");
    }

    if (this.getTrace()) {
      args.add("-trace");
    }

    args.add("-tcp");

    if (this.getAllowOthers()) {
      args.add("-tcpAllowOthers");
    }

    final String password = this.getShutdownPassword();
    if (password != null && !password.isEmpty()) {
      args.add("-tcpPassword");
      args.add(password);
    }

    args.add("-tcpPort");
    args.add(String.format("%d", Math.min(65535, Math.max(0, this.getPort()))));

    if (this.getUseSSL()) {
      args.add("-tcpSSL");
    }
    return args;
  }

}