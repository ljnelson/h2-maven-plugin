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

import java.sql.SQLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugin.logging.Log;

/**
 * An {@link AbstractH2Mojo} that stops a running H2 TCP server.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @goal stop
 *
 * @phase post-integration-test
 *
 * @requiresProject false
 *
 * @since 1.0-SNAPSHOT
 */
public class StopH2Mojo extends AbstractH2Mojo {

  /**
   * Stops a running H2 TCP server by invoking the {@link
   * AbstractH2Mojo#shutdownServer()} method.
   *
   * @exception MojoExecutionException if an error occurs
   */
  @Override
  public void execute() throws MojoExecutionException {
    try {
      this.shutdownServer();
    } catch (final SQLException kaboom) {
      throw new MojoExecutionException("Could not shutdown TCP server. Please check to see if the process is still running.", kaboom);
    }
    final Log log = this.getLog();
    if (log != null && log.isInfoEnabled()) {
      log.info("H2 server stopped");
    }
  }

}