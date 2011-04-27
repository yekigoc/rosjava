/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros;

import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.server.MasterServer;

import java.net.URI;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RosCore implements NodeMain {

  private final MasterServer masterServer;

  public RosCore() {
    masterServer =
        new MasterServer(BindAddress.createPrivate(11311), AdvertiseAddress.createPrivate());
  }

  @Override
  public void run(NodeConfiguration nodeConfiguration) throws Exception {
    masterServer.start();
  }
  
  public URI getUri() {
    return masterServer.getUri();
  }
  
  public void awaitStart() {
    try {
      masterServer.awaitStart();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
