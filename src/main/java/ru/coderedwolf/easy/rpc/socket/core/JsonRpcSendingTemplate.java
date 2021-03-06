package ru.coderedwolf.easy.rpc.socket.core;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageChannel;
import ru.coderedwolf.easy.rpc.socket.MessageHeaders;
import ru.coderedwolf.easy.rpc.socket.MessageType;
import ru.coderedwolf.easy.rpc.socket.exceptions.MessageSendException;
import ru.coderedwolf.easy.rpc.socket.exceptions.MessagingException;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;
import ru.coderedwolf.easy.rpc.socket.support.MessageHeaderAccessor;

import java.util.Map;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class JsonRpcSendingTemplate implements MessageSendingOperations {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final MessageChannel messageChannel;

    private final SmartMessageConverter messageConverter = new JsonRpcMessageConverter();

    public JsonRpcSendingTemplate(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    @Override
    public void send(Message<?> message) throws MessagingException {
        doSend(message);
    }

    @Override
    public void send(String destination, Message<?> message) throws MessagingException {
        MessageHeaderAccessor messageHeaderAccessor = MessageHeaderAccessor.ofMessage(message);
        messageHeaderAccessor.setMessageMethod(destination);
        messageHeaderAccessor.setSendMessageMethod(destination);
        MessageHeaders messageHeaders = messageHeaderAccessor.getMessageHeaders();
        messageHeaderAccessor.setImmutable();
        Message<?> sendMessage = MessageBuilder.fromMessage(message)
                .withHeaders(messageHeaders)
                .build();
        doSend(sendMessage);
    }

    @Override
    public void convertAndSend(String destination, Object payload) throws MessagingException {
        convertAndSend(destination, payload, null, null);
    }

    @Override
    public void convertAndSend(String destination, Object payload, MessagingPostProcessor postProcessor) throws MessagingException {
        convertAndSend(destination, payload, null, postProcessor);
    }

    @Override
    public void convertAndSend(String destination, Object payload, Map<String, Object> headers) throws MessagingException {
        convertAndSend(destination, payload, headers, null);
    }

    private void doSend(Message<?> message) {
        String messageMethod = MessageHeaderAccessor.getMessageMethod(message.getMessageHeader());
        if (StringUtils.isEmpty(messageMethod)) {
            throw new IllegalArgumentException("Message method is required");
        }

        boolean sent = this.messageChannel.send(message);
        logger.debug("Send message, messageMethod = {}, message = {}", messageMethod, message);
        if (!sent) {
            throw new MessageSendException(message, "Failed to send message to destination " + messageMethod);
        }
    }

    protected void convertAndSend(String destination, Object payload, @Nullable Map<String, Object> headers,
                                  @Nullable MessagingPostProcessor postProcessor) {
        MessageHeaders messageHeaders = new MessageHeaders(headers, MessageType.NOTIFICATION, -1L);
        Message<?> message = doConvert(destination, payload, messageHeaders, postProcessor);
        send(destination, message);
    }

    protected Message<?> doConvert(String destination, Object payload, MessageHeaders messageHeaders,
                                   @Nullable MessagingPostProcessor postProcessor) {
        Message<?> message = messageConverter.toMessage(payload, messageHeaders, destination);
        if (postProcessor != null) {
            postProcessor.postProcessMessage(message);
        }
        return message;
    }
}
