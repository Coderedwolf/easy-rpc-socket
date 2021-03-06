package ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation;

import org.springframework.web.socket.WebSocketSession;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * With this annotation, we mark a method that
 * processes requests that came via the websocket
 * Warning the method must have two parameters, the first is the argument for the method and
 * the second is necessarily the object class WebSocketSession
 *
 * @author CodeRedWolf
 * @see WebSocketSession
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMethod {

    /**
     * The name of the method for processing messages
     *
     * @return the suggested component name, if any
     */
    String value();

}
