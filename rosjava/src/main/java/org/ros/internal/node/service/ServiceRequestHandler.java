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

package org.ros.internal.node.service;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageSerializer;

import java.nio.ByteBuffer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class ServiceRequestHandler<T, S> extends SimpleChannelHandler {

  private final ServiceDeclaration serviceDeclaration;
  private final ServiceResponseBuilder<T, S> responseBuilder;
  private final MessageDeserializer<T> deserializer;
  private final MessageSerializer<S> serializer;
  private final MessageFactory messageFactory;

  public ServiceRequestHandler(ServiceDeclaration serviceDeclaration,
      ServiceResponseBuilder<T, S> responseBuilder, MessageDeserializer<T> deserializer,
      MessageSerializer<S> serializer, MessageFactory messageFactory) {
    this.serviceDeclaration = serviceDeclaration;
    this.deserializer = deserializer;
    this.serializer = serializer;
    this.responseBuilder = responseBuilder;
    this.messageFactory = messageFactory;
  }

  private ByteBuffer handleRequest(ByteBuffer buffer) throws ServiceException {
    T request = deserializer.deserialize(buffer);
    S response = messageFactory.newFromType(serviceDeclaration.getType());
    responseBuilder.build(request, response);
    return serializer.serialize(response);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    ChannelBuffer requestBuffer = (ChannelBuffer) e.getMessage();
    ServiceServerResponse response = new ServiceServerResponse();
    ChannelBuffer responseBuffer;
    try {
      responseBuffer = ChannelBuffers.wrappedBuffer(handleRequest(requestBuffer.toByteBuffer()));
    } catch (ServiceException ex) {
      response.setErrorCode(0);
      response.setMessageLength(ex.getMessage().length());
      response.setMessage(ex.getMessageAsChannelBuffer());
      ctx.getChannel().write(response);
      return;
    }
    response.setErrorCode(1);
    response.setMessageLength(responseBuffer.readableBytes());
    response.setMessage(responseBuffer);
    ctx.getChannel().write(response);
    super.messageReceived(ctx, e);
  }

}
