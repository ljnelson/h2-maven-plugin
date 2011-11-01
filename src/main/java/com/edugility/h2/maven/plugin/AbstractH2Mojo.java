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
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since <tt>1.0-SNAPSHOT</tt>
 */
public abstract class AbstractH2Mojo extends AbstractMojo {

  /**
   * @parameter expression="${h2.useDaemonThread}" property="useDaemonThread"
   */
  private boolean useDaemonThread;

  /**
   * @parameter expression="${h2.useSSL}" property="useSSL"
   */
  private boolean useSSL;
  
  /**
   * @parameter expression="${h2.allowOthers}" property="allowOthers"
   */
  private boolean allowOthers;

  /**
   * @parameter default-value="9092" expression="${h2.port}" property="port"
   */
  private int port;

  /**
   * @parameter expression="${h2.baseDirectory}" property="baseDirectory"
   */
  private File baseDirectory;

  /**
   * @parameter expression="${h2.ifExists}" property="ifExists"
   */
  private boolean ifExists;

  /**
   * @parameter expression="${h2.shutdownPassword}" property="shutdownPassword" default-value="h2-maven-plugin"
   */
  private String shutdownPassword;

  /**
   * @parameter expression="${h2.shutdownURL}" property="shutdownURL"
   */
  private String shutdownURL;

  /**
   * @parameter expression="${h2.forceShutdown}" property="forceShutdown"
   */
  private boolean forceShutdown;

  /**
   * @parameter expression="${h2.shutdownAllServers}" property="shutdownAllServers"
   */
  private boolean shutdownAllServers;

  /**
   * @parameter expression="${h2.trace}" property="trace"
   */
  private boolean trace;

  /**
   * @parameter property="server"
   */
  private Server server;

  /**
   * @parameter expression="${h2.java}" property="java"
   */
  private File java;

  /**
   * @parameter property="javaOptions"
   */
  private String[] javaOptions;

  protected AbstractH2Mojo() {
    super();
    this.setPort(9092);
    this.setShutdownPassword("h2-maven-plugin");
    this.setJava(new File(new File(new File(System.getProperty("java.home")), "bin"), "java"));
  }

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

  public String getShutdownURL() {
    return this.shutdownURL;
  }
  
  public void setShutdownURL(final String shutdownURL) {
    this.shutdownURL = shutdownURL;
  }

  public boolean getForceShutdown() {
    return this.forceShutdown;
  }

  public void setForceShutdown(final boolean shutdown) {
    this.forceShutdown = shutdown;
  }

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

  public File getJava() {
    return this.java;
  }

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

  public boolean getUseDaemonThread() {
    return this.useDaemonThread;
  }

  public void setUseDaemonThread(final boolean useDaemonThread) {
    this.useDaemonThread = useDaemonThread;
  }

  public File getBaseDirectory() {
    return this.baseDirectory;
  }

  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(final int port) {
    this.port = Math.min(65535, Math.max(0, port));
  }

  protected Server createServer() throws SQLException {
    final List<String> args = this.getServerArguments();
    final Server server = Server.createTcpServer(args.toArray(new String[args.size()]));
    return server;
  }

  public Server getServer() {
    return this.server;
  }

  public void setServer(final Server server) {
    this.server = server;
  }

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

  protected Process spawnServer() throws IOException {
    return this.getServerSpawner().start();
  }

  protected void shutdownServer() throws SQLException {
    String password = this.getShutdownPassword();
    if (password == null) {
      password = "";
    }
    String url = this.getShutdownURL();
    if (url == null) {
      url = String.format("tcp://localhost:%d", this.getPort());
    }
    TcpServer.shutdown(url, password, this.getForceShutdown(), this.getShutdownAllServers());
  }

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

    if (this.getUseDaemonThread()) {
      args.add("-tcpDaemon");
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