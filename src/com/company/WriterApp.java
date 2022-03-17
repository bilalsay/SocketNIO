package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class WriterApp {

    public static void main(String[] args) throws IOException {
        int limit = 5;
        boolean isContinueWrite = true;
        AtomicInteger ofsetLine = new AtomicInteger(1);
        String filePath = "./socketnio.txt";
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1111);
        SocketChannel socketChannel = SocketChannel.open(socketAddress);
        ByteBuffer buffer = ByteBuffer.allocate(256);
        while(true) {
            int ofset = ofsetLine.get();
            if (isContinueWrite) {
                System.out.println("Between Line " + ofset + " and Line " + (ofset + limit));
                IntStream.range(0, 1000)
                        .skip(ofsetLine.get() - 1)
                        .limit(limit)
                        .forEach(data -> {
                            String msgData = "Data Line " + ofsetLine.get() + "\n";
                            try {
                                Files.write(Paths.get(filePath), msgData.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ofsetLine.getAndIncrement();
                        });
                buffer = ByteBuffer.wrap(new String("ContinueRead").getBytes(StandardCharsets.UTF_8));
                socketChannel.write(buffer);
                isContinueWrite = false;
            }

            buffer.clear();
            socketChannel.read(buffer);
            String response = new String(buffer.array()).trim();
            if (response.equals("ContinueWrit")) {
                isContinueWrite = true;
                if (ofsetLine.get() == 1001) {
                    isContinueWrite = false;
                    buffer.clear();
                    buffer = ByteBuffer.wrap(new String("StopRead").getBytes(StandardCharsets.UTF_8));
                    socketChannel.write(buffer);
                    socketChannel.close();
                }

            }
        }
    }
}
