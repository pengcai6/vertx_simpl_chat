package com.cai;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class SimpleWebSocketChatServer extends AbstractVerticle {

    private Set<ServerWebSocket> connections = new HashSet<>();

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        // 创建路由器
        Router router = Router.router(vertx);

        // 设置静态文件处理（用于提供 index.html）
        router.route().handler(StaticHandler.create());

        // WebSocket 处理逻辑
        server.webSocketHandler(webSocket -> {
            // 将新连接添加到集合中
            connections.add(webSocket);
            System.out.println("Client connected: " + webSocket.textHandlerID());

            // 处理接收到的消息
            webSocket.textMessageHandler(message -> {
                message+="   发送时间为:"+ LocalDateTime.now();
                System.out.println("Received message: " + message);
                // 广播消息给所有连接的客户端
                broadcastMessage(message);
            });

            // 处理连接关闭
            webSocket.closeHandler(v -> {
                connections.remove(webSocket);
                System.out.println("Client disconnected: " + webSocket.textHandlerID());
            });
        });

        // 将路由器绑定到 HTTP 服务器
        server.requestHandler(router).listen(8080, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port 8080");
            } else {
                System.out.println("Failed to start server: " + result.cause());
            }
        });
    }

    private void broadcastMessage(String message) {
        // 将消息发送给所有连接的客户端
        for (ServerWebSocket socket : connections) {
            socket.writeTextMessage(message);
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new SimpleWebSocketChatServer());
    }
}