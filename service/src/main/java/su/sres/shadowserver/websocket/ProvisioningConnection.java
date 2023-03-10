/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.websocket;

import com.google.protobuf.InvalidProtocolBufferException;

import su.sres.dispatch.DispatchChannel;
import su.sres.shadowserver.entities.MessageProtos.ProvisioningUuid;
import su.sres.shadowserver.storage.PubSubProtos.PubSubMessage;
import su.sres.shadowserver.util.TimestampHeaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.sres.websocket.WebSocketClient;

import java.util.Collections;
import java.util.Optional;

public class ProvisioningConnection implements DispatchChannel {

    private final Logger logger = LoggerFactory.getLogger(ProvisioningConnection.class);

    private final WebSocketClient client;

    public ProvisioningConnection(WebSocketClient client) {
	this.client = client;
    }

    @Override
    public void onDispatchMessage(String channel, byte[] message) {
	try {
	    PubSubMessage outgoingMessage = PubSubMessage.parseFrom(message);

	    if (outgoingMessage.getType() == PubSubMessage.Type.DELIVER) {
		Optional<byte[]> body = Optional.of(outgoingMessage.getContent().toByteArray());

		client.sendRequest("PUT", "/v1/message", Collections.singletonList(TimestampHeaderUtil.getTimestampHeader()), body)
			.thenAccept(response -> client.close(1001, "All you get."))
			.exceptionally(throwable -> {
			    client.close(1001, "That's all!");
			    return null;
			});
	    }
	} catch (InvalidProtocolBufferException e) {
	    logger.warn("Protobuf Error: ", e);
	}
    }

    @Override
    public void onDispatchSubscribed(String channel) {
	try {
	    ProvisioningAddress address = new ProvisioningAddress(channel);
	    this.client.sendRequest("PUT", "/v1/address", Collections.singletonList(TimestampHeaderUtil.getTimestampHeader()),
		    Optional.of(ProvisioningUuid.newBuilder()
			    .setUuid(address.getAddress())
			    .build()
			    .toByteArray()));
	} catch (InvalidWebsocketAddressException e) {
	    logger.warn("Badly formatted address", e);
	    this.client.close(1001, "Server Error");
	}
    }

    @Override
    public void onDispatchUnsubscribed(String channel) {
	this.client.close(1001, "Closed");
    }
}
