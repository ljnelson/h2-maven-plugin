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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.sql.DriverManager;
import java.sql.Connection;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseStartH2Mojo {

  private AbstractH2Mojo startMojo;

  private AbstractH2Mojo stopMojo;

  @Before
  public void setUp() throws Exception {
    this.startMojo = new StartH2Mojo();
    final String projectBuildDirectoryName = System.getProperty("maven.project.build.directory", System.getProperty("project.build.directory"));
    if (projectBuildDirectoryName != null) {
      this.startMojo.setBaseDirectory(new File(projectBuildDirectoryName));
    }
    this.startMojo.setLog(new SystemStreamLogWithDebugEnabled());
    this.startMojo.setTrace(true);
    assertEquals(9092, this.startMojo.getPort());

    this.stopMojo = new StopH2Mojo();
    if (projectBuildDirectoryName != null) {
      this.stopMojo.setBaseDirectory(new File(projectBuildDirectoryName));
    }
    this.stopMojo.setLog(new SystemStreamLogWithDebugEnabled());
    this.stopMojo.setTrace(true);
    assertEquals(9092, this.stopMojo.getPort());

  }

  @Test
  public void testStartAndStop() throws Exception {
    this.startMojo.execute();
    this.stopMojo.execute();
  }

  @Test
  public void testSpawn() throws Exception {
    this.startMojo.setJavaOptions("-Xmx384m");
    Process p = this.startMojo.spawnServer();
    assertNotNull(p);
    int exitValue = 0;
    try {
      exitValue = p.exitValue();
      fail();
    } catch (final IllegalThreadStateException expected) {
      // Process should be running, so an attempt to get the exit
      // value should fail.
    }

    final Connection connection = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/crap", "sa", "");
    assertNotNull(connection);
    connection.close();

    this.startMojo.shutdownServer();
    Thread.currentThread().sleep(200L);

    try {
      assertEquals(0, p.exitValue());
    } catch (final AssertionError error) {
      printInputStream(p.getErrorStream());
      p.destroy();
      throw error;
    } catch (final IllegalThreadStateException bang) {
      printInputStream(p.getErrorStream());
      p.destroy();
      throw bang;
    }
  }

  private static final void printInputStream(final InputStream stream) throws IOException {
    if (stream != null) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      String line = null;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
      reader.close();
    }
  }

  /**
   * A {@link SystemStreamLog} that is enabled for debug logging.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @version 1.0-SNAPSHOT
   *
   * @since 1.0-SNAPSHOT
   */
  public static final class SystemStreamLogWithDebugEnabled extends SystemStreamLog {
    
    /**
     * Overrides the default behavior of this method to return {@code
     * true} in all cases.
     *
     * @return {@code true}
     */
    @Override
    public boolean isDebugEnabled() {
      return true;
    }

  }


}