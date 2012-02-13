/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * Copyright (c) 2011-2012 Edugility LLC.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugin.logging.Log;

import org.h2.server.TcpServer;

import org.h2.tools.Server;

/**
 * An abstract <a href="http://maven.apache.org/">Maven</a> plugin, or
 * <i>mojo</i>, that helps with interacting with an <a
 * href="http://www.h2database.com/">H2</a> <a
 * href="http://h2database.com/html/tutorial.html#using_server">TCP
 * server</a>.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 1.0
 */
public abstract class AbstractH2Mojo extends AbstractMojo {

  /**
   * The {@link Service}s to spawn.
   *
   * @parameter property="services"
   */
  private List<Service> services;

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
    final Service tcpService = new Service("tcp", Service.getDefaultPort("tcp"), false, false);
    this.setServices(Collections.singletonList(tcpService));
    this.setPort(Service.getDefaultPort("tcp"));
    this.setShutdownPassword("h2-maven-plugin");
    this.setJava(new File(new File(new File(System.getProperty("java.home")), "bin"), "java"));
  }

  /**
   * Returns a {@link List} of {@link Service}s that will be
   * {@linkplain #spawnServer() spawned}.  This method never returns
   * {@code null}.
   *
   * @return a non-{@code null} {@link List} of {@link Service}s
   */
  public List<Service> getServices() {
    return this.services;
  }

  /**
   * Returns the {@link Service} whose {@link Service#getId() id} is
   * equal to the supplied {@link String}, or {@code null} if there is
   * no such {@link Service}.
   *
   * @param the {@link Service#getId() id} of the {@link Service} to
   * return; may be {@code null}
   *
   * @return a {@link Service} that is a member of this {@link
   * AbstractH2Mojo}'s {@linkplain #getServices() list of
   * <tt>Service</tt>s}, or {@code null} if there is no such {@link
   * Service}
   */
  public Service getService(final String id) {
    Service service = null;
    final List<Service> services = this.getServices();
    if (services != null && !services.isEmpty()) {
      for (final Service s : services) {
        if (s != null) {
          if (id == null) {
            if (s.getId() == null) {
              service = s;
              break;
            }
          } else if (id.equals(s.getId())) {
            service = s;
            break;
          }
        }
      }
    }
    return service;
  }

  /**
   * Installs a {@link List} of {@link Service}s that this {@link
   * AbstractH2Mojo} is capable of {@linkplain #spawnServer()
   * spawning}.
   *
   * @param services a {@link List} of {@link Service}s; may be {@code
   * null}
   */
  public void setServices(final List<Service> services) {
    if (services == null || services.isEmpty()) {
      this.services = Collections.emptyList();
    } else {
      this.services = services;
    }
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

  /**
   * Sets whether a shutdown operation should shut down all H2 TCP
   * servers running on the host in question.
   *
   * @param shutdownAllServers if a shutdown operation should shut
   * down all H2 TCP servers running on the host in question
   */
  public void setShutdownAllServers(final boolean shutdownAllServers) {
    this.shutdownAllServers = shutdownAllServers;
  }

  /**
   * Returns the password necessary to shut down spawned H2 TCP
   * servers.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the shutdown password, or {@code null}
   */
  public String getShutdownPassword() {
    return this.shutdownPassword;
  }

  /**
   * Sets the password necessary to shut down spawned H2 TCP servers.
   *
   * @param pw the new password; may be {@code null}
   */
  public void setShutdownPassword(final String pw) {
    this.shutdownPassword = pw;
  }

  /**
   * Returns the hostname to which shutdown requests will be routed.
   *
   * <p>This method may return {@code null}.  Consumers of this method
   * should interpret such return values as being equal to {@code
   * localhost}.</p>
   *
   * @return the hostname to which shutdown requests will be routed,
   * or {@code null}
   */
  public String getShutdownHost() {
    return this.shutdownHost;
  }
  
  /**
   * Sets the hostname to which shutdown requests will be routed.
   * Passing a {@code null} parameter to this method will result in
   * "{@code localhost}" being used.
   *
   * @param shutdownHost the hostname to which shutdown requests will
   * be routed; may be {@code null}
   */
  public void setShutdownHost(final String shutdownHost) {
    this.shutdownHost = shutdownHost;
  }

  /**
   * Returns whether shutdown should be forced (if shutdown has been
   * requested).  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @return {@code true} if shutdown should be forced
   */
  public boolean getForceShutdown() {
    return this.forceShutdown;
  }

  /**
   * Sets whether shutdown should be forced (if shutdown has been
   * requested).  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @param shutdown if {@code true}, then shutdown operations will be
   * forced
   */
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

  /**
   * Returns any Java options passed to the spawned H2 process.  This
   * method may return {@code null}.
   *
   * @return any Java options passed to the spawned H2 process, or
   * {@code null}
   */
  public String[] getJavaOptions() {
    return this.javaOptions;
  }

  /**
   * Sets any command-line options to be passed to the Java runtime
   * when spawning a new H2 TCP server.
   *
   * @param javaOptions the options; may be {@code null}
   */
  public void setJavaOptions(final String... javaOptions) {
    this.javaOptions = javaOptions;
  }

  /**
   * Returns {@code true} if the {@code -trace} option will be
   * supplied to the spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @return {@code true} if the {@code -trace} option will be
   * supplied to the spawned H2 TCP server
   */
  public boolean getTrace() {
    return this.trace;
  }

  /**
   * Sets whether the {@code -trace} option will be supplied to new H2
   * TCP servers.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @param trace whether the {@code -trace} option will be supplied
   * to new H2 TCP servers
   */
  public void setTrace(final boolean trace) {
    this.trace = trace;
  }

  /**
   * Returns {@code true} if the {@code -ifExists} option will be
   * supplied to the spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @return {@code true} if the {@code -ifExists} option will be
   * supplied to the spawned H2 TCP server
   */
  public boolean getIfExists() {
    return this.ifExists;
  }

  /**
   * Sets whether the {@code -ifExists} option will be supplied to the
   * spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @param ifExists whether the {@code -ifExists} option will be
   * supplied to new H2 TCP servers
   */
  public void setIfExists(final boolean ifExists) {
    this.ifExists = ifExists;
  }

  /**
   * Returns {@code true} if the {@code -tcpAllowOthers} option will
   * be supplied to the spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @return {@code true} if the {@code -tcpAllowOthers} option will
   * be supplied to the spawned H2 TCP server
   *
   * @deprecated Use the correct {@link Service} instead.
   */
  @Deprecated
  public boolean getAllowOthers() {
    return this.allowOthers;
  }

  /**
   * Sets whether the {@code -tcpAllowOthers} option will be supplied
   * to the spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @param allowOthers whether the {@code -tcpAllowOthers} option
   * will be supplied to new H2 TCP servers
   *
   * @deprecated Use the correct {@link Service} instead.
   */
  @Deprecated
  public void setAllowOthers(final boolean allowOthers) {
    this.allowOthers = allowOthers;
    final Service tcpService = this.getService("tcp");
    if (tcpService != null) {
      tcpService.setAllowOthers(this.getAllowOthers());
    }
  }

  /**
   * Returns {@code true} if the {@code -tcpSSL} option will
   * be supplied to the spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @return {@code true} if the {@code -tcpSSL} option will
   * be supplied to the spawned H2 TCP server
   *
   * @deprecated Use the correct {@link Service} instead.
   */
  @Deprecated
  public boolean getUseSSL() {
    return this.useSSL;
  }

  /**
   * Sets whether the {@code -tcpSSL} option will be supplied
   * to the spawned H2 TCP server.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more details.
   *
   * @param useSSL whether the {@code -tcpSSL} option
   * will be supplied to new H2 TCP servers
   *
   * @deprecated Use the correct {@link Service} instead.
   */
  @Deprecated
  public void setUseSSL(final boolean useSSL) {
    this.useSSL = useSSL;
    final Service tcpService = this.getService("tcp");
    if (tcpService != null) {
      tcpService.setSSL(this.getUseSSL());
    }
  }

  /**
   * Returns the {@link File} representing the base directory from
   * which H2 TCP servers will be spawned.  
   *
   * <p>This property corresponds to the {@code -baseDir} option
   * supplied to spawned H2 TCP servers.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more
   * details.</p>
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link File} representing the base directory, or
   * {@code null}
   */
  public File getBaseDirectory() {
    return this.baseDirectory;
  }

  /**
   * Sets the base directory from which H2 TCP servers will be
   * spawned.
   *
   * <p>This property corresponds to the {@code -baseDir} option
   * supplied to spawned H2 TCP servers.  See <a
   * href="http://www.h2database.com/javadoc/org/h2/tools/Server.html#main_String...">the
   * documentation for the {@code Server} class</a> for more
   * details.</p>
   *
   * @param baseDirectory a {@link File} representing the new base
   * directory; may be {@code null}
   */
  public void setBaseDirectory(final File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  /**
   * Returns the port on which new H2 TCP servers spawned by this
   * class will listen.  H2's default port is 9092.
   *
   * @return the port on which new H2 TCP servers spawned by this
   * class will listen; this will be a number between {@code 0} and
   * {@code 65535}, inclusive
   *
   * @deprecated Use the correct {@link Service} instead.
   */
  @Deprecated
  public int getPort() {
    return this.port;
  }

  /**
   * Sets the port on which new H2 TCP servers spawned by this class
   * will listen.  H2's default port is 9092.
   *
   * @param port the new port; will be constrained to be between
   * {@code 0} and {@code 65535}, inclusive
   *
   * @deprecated Use the correct {@link Service} instead.
   */
  @Deprecated
  public void setPort(final int port) {
    this.port = Math.min(65535, Math.max(0, port));
    final Service tcpService = this.getService("tcp");
    if (tcpService != null) {
      tcpService.setPort(this.getPort());
    }
  }

  /**
   * Creates a new {@link Server} using H2's {@link
   * Server#createTcpServer(String[])}, {@link
   * Server#createPgServer(String[])} or {@link
   * Server#createWebServer(String[])} method.  This method must never
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
    Server server = null;
    final List<String> args = this.getServerArguments();
    if (args == null || args.isEmpty()) {
      throw new SQLException("Cannot create server; no arguments");
    } else if (args.contains("-tcp")) {
      server = Server.createTcpServer(args.toArray(new String[args.size()]));
    } else if (args.contains("-pg")) {
      server = Server.createPgServer(args.toArray(new String[args.size()]));
    } else if (args.contains("-web")) {
      server = Server.createWebServer(args.toArray(new String[args.size()]));
    } else {
      throw new SQLException("Unknown service");
    }
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

    // A spawned server should never run as a daemon.
    args.remove("-tcpDaemon");
    args.remove("-pgDaemon");
    args.remove("-webDaemon");

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

    List<String> args = null;

    final List<Service> services = this.getServices();
    int serviceCount = 0;
    if (services != null && !services.isEmpty()) {
      for (final Service service : services) {
        if (service != null) {

          if (args == null) {
            args = new LinkedList<String>();
          }
          
          final String id = service.getId();
          if (id != null) {
            args.add(new StringBuilder("-").append(id).toString());
          }

          final int port = service.getPort();
          if (port >= 0) {
            args.add(new StringBuilder("-").append(id).append("Port").toString());
            args.add(String.format("%d", Math.min(65535, Math.max(0, this.getPort()))));
          }

          final boolean allowOthers = service.getAllowOthers();
          if (allowOthers) {
            args.add(new StringBuilder("-").append(id).append("AllowOthers").toString());
          }

          final boolean ssl = service.getSSL();
          if (ssl) {
            args.add(new StringBuilder("-").append(id).append("SSL").toString());
          }

          serviceCount++;
        }
      }
    }
    if (args != null && serviceCount > 0) {

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
      
      final String password = this.getShutdownPassword();
      if (password != null && !password.isEmpty()) {
        args.add("-tcpPassword");
        args.add(password);
      }
      
    }
    if (args == null) {
      args = Collections.emptyList();
    }
    return args;
  }

}