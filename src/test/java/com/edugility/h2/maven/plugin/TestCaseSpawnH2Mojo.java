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

import java.sql.DriverManager;
import java.sql.Connection;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A <a href="http://www.junit.org/">JUnit</a> test suite that
 * exercises the {@link AbstractH2Mojo} class.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 1.0
 */
public class TestCaseSpawnH2Mojo {

  /**
   * The {@link AbstractH2Mojo} under test.  This field may be {@code
   * null}.  It is initialized by the {@link #setUp()} method.
   */
  private AbstractH2Mojo mojo;

  /**
   * Sets up the {@link AbstractH2Mojo} to be tested.
   */
  @Before
  public void setUp() {
    this.mojo = new SpawnH2Mojo();
    final String projectBuildDirectoryName = System.getProperty("maven.project.build.directory", System.getProperty("project.build.directory"));
    if (projectBuildDirectoryName != null) {
      this.mojo.setBaseDirectory(new File(projectBuildDirectoryName));
    }
    this.mojo.setLog(new SystemStreamLogWithDebugEnabled());
    this.mojo.setTrace(true);
    assertEquals(9092, this.mojo.getPort());
  }

  /**
   * Exercises the {@link AbstractH2Mojo#spawnServer()} and {@link
   * AbstractH2Mojo#shutdownServer()} methods directly.
   *
   * @exception Exception if an error occurs; make sure to check your
   * system to see if an H2 process spawned by this test is still
   * running
   */
  @Test
  public void testSpawnServer() throws Exception {
    final Process p = this.mojo.spawnServer();
    assertNotNull(p);

    // Make sure the process is alive and not exited.
    int exitValue = 0;
    try {
      exitValue = p.exitValue();
      fail();
    } catch (final IllegalThreadStateException expected) {
      // Process should be running, so an attempt to get the exit
      // value should fail.
    }

    // Check to make sure we can establish a connection to a
    // database at that location.
    final Connection connection = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/test", "sa", "");
    assertNotNull(connection);
    connection.close();

    // Shut down the spawned process.
    this.mojo.shutdownServer();
    p.waitFor();

    // Ensure it actually exited and exited cleanly.
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

  /**
   * Prints an {@link InputStream} to {@link System#out System.out}.
   *
   * @param stream the {@link InputStream} whose contents should be
   * entirely printed; may be {@code null} in which case nothing will
   * happen
   *
   * @exception IOException if an error occurs
   */
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
    public final boolean isDebugEnabled() {
      return true;
    }

  }


}