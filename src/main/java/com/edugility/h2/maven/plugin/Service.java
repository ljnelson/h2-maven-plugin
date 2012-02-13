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

import java.io.Serializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.h2.tools.Server; // for javadoc only

/**
 * A simple object representing a service that the H2 {@link Server}
 * class can spawn.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 1.1-SNAPSHOT
 */
public final class Service implements Serializable {

  /**
   * The version of this class for serialization purposes.
   */
  private static final long serialVersionUID = 1L;

  /**
   * A {@link Map} used to store default ports for each of the
   * different kinds of services supported by H2.  The {@linkplain
   * Map#keySet() key set} doubles as the canonical set of identifiers
   * of supported services.
   *
   * <p>This field is never {@code null} and is initialized by a
   * static initializer.</p>
   */
  private static final Map<String, Integer> defaultPorts;

  /**
   * Static initializer; sets up the {@link #defaultPorts} field.
   */
  static {
    final Map<String, Integer> idsToDefaultPorts = new HashMap<String, Integer>(7);
    idsToDefaultPorts.put("tcp", Integer.valueOf(9092));
    idsToDefaultPorts.put("pg", Integer.valueOf(5435));
    idsToDefaultPorts.put("web", Integer.valueOf(8082));
    defaultPorts = Collections.unmodifiableMap(idsToDefaultPorts);
  }

  /**
   * The identifier of this {@link Service}; as of this writing, must
   * be one of {@code tcp}, {@code pg}, or {@code web}.
   *
   * <p>This field must never be {@code null}.</p>
   *
   * @see #setId(String)
   */
  private String id;

  /**
   * The TCP port that this {@link Service} is exposed on; must be
   * between {@code 0} and {@code 65535}, inclusive.
   *
   * @see #setPort(int)
   */
  private int port;

  /**
   * Whether or not this {@link Service} allows remote access; see <a
   * href="http://www.h2database.com/html/advanced.html?highlight=advanced&search=advanced#remote_access">the
   * relevant H2 documentation</a> for details.
   */
  private boolean allowOthers;

  /**
   * If the {@link Service} supports it, sets whether SSL is used.
   * Currently supported only by the {@code tcp} and {@code web}
   * services.
   */
  private boolean ssl;

  /**
   * Creates a new {@link Service}.
   */
  public Service() {
    super();
  }

  /**
   * Creates a new {@link Service}.
   *
   * @param id the identifier; must not be {@code null}
   *
   * @param port a TCP port number; must be between {@code 0} and
   * {@code 65535}, inclusive
   *
   * @param allowOthers if {@code true} then remote access will be
   * permitted
   *
   * @see #setId(String)
   *
   * @see #setPort(int)
   *
   * @see #setAllowOthers(boolean)
   */
  public Service(final String id, final int port, final boolean allowOthers) {
    this(id, port, allowOthers, false);
  }

  /**
   * Creates a new {@link Service}.
   *
   * @param id the identifier; must not be {@code null}
   *
   * @param port a TCP port number; must be between {@code 0} and
   * {@code 65535}, inclusive
   *
   * @param allowOthers if {@code true} then remote access will be
   * permitted
   *
   * @param ssl whether SSL access is to be used if the underlying
   * service supports it
   *
   * @see #setId(String)
   *
   * @see #setPort(int)
   *
   * @see #setAllowOthers(boolean)
   *
   * @see #setSSL(boolean)
   */
  public Service(final String id, final int port, final boolean allowOthers, final boolean ssl) {
    super();
    this.setId(id);
    if (port < 0 || port > 65535) {
      this.setPort(getDefaultPort(id));
    } else {
      this.setPort(port);
    }
    this.setAllowOthers(allowOthers);
  }

  /**
   * Given a service identifier (currently only {@code tcp}, {@code
   * web} and {@code pg} are supported), returns the default port that
   * a newly constructed {@link Service} will listen on, unless
   * otherwise specified.
   *
   * @return a valid TCP port for the supplied identifier, or {@code
   * -1} if a port is not supported for that service identifier
   */
  public static final int getDefaultPort(final String id) {
    final Integer port;
    if (id == null) {
      port = null;
    } else {
      port = defaultPorts.get(id);
    }
    if (port == null) {
      return -1;
    }
    return port.intValue();
  }
  
  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id", new NullPointerException("id"));
    }
    id = id.trim();
    if (!defaultPorts.containsKey(id)) {
      throw new IllegalArgumentException("id: " + id);
    }
    this.id = id;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(final int port) {
    this.port = Math.min(65535, Math.max(0, port));
  }

  public boolean getAllowOthers() {
    return this.allowOthers;
  }

  public void setAllowOthers(final boolean allowOthers) {
    this.allowOthers = allowOthers;
  }

  public boolean getSSL() {
    return this.ssl;
  }

  public void setSSL(final boolean ssl) {
    this.ssl = ssl;
  }

  @Override
  public int hashCode() {
    int hashCode = this.getPort();
    final String id = this.getId();
    if (id != null) {
      hashCode += id.hashCode();
    }
    if (this.getAllowOthers()) {
      hashCode++;
    }
    if (this.getSSL()) {
      hashCode++;
    }
    return hashCode;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass().equals(this.getClass())) {
      final Service him = (Service)other;

      final boolean mySSL = this.getSSL();
      if (mySSL) {
        if (!him.getSSL()) {
          return false;
        }
      }

      final boolean allowOthers = this.getAllowOthers();
      if (allowOthers) {
        if (!him.getAllowOthers()) {
          return false;
        }
      }

      final int port = this.getPort();
      if (him.getPort() != port) {
        return false;
      }

      final String id = this.getId();
      if (id == null) {
        if (him.getId() != null) {
          return false;
        }
      } else if (!id.equals(him.getId())) {
        return false;
      }

      return true;
      
    } else {
      return false;
    }
  }

}