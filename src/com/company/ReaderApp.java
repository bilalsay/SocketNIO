package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ReaderApp {

    public static void main(String[] args) throws IOException {
        AtomicInteger ofsetLine = new AtomicInteger(1);
        String filePath = "./socketnio.txt";
        Selector selector = Selector.open();
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", 1111);
        socketChannel.bind(socketAddress);
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(256);

        while(true) {
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
            while(selectionKeyIterator.hasNext()) {
                SelectionKey key = selectionKeyIterator.next();

                if (key.isAcceptable()) {
                    SocketChannel channel = socketChannel.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    channel.read(buffer);
                    String result = new String(buffer.array()).trim();
                    if (result.equals("ContinueRead")) {
                        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                            stream.skip(ofsetLine.get() - 1).forEach(line -> {
                                ofsetLine.getAndIncrement();
                                System.out.println(line);
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        buffer = ByteBuffer.wrap(new String("ContinueWrit").getBytes(StandardCharsets.UTF_8));
                        channel.write(buffer);
                        buffer.clear();
                    }

                    if (result.equals("StopRead")) {
                        channel.close();
                    }
                }
                selectionKeyIterator.remove();
            }
        }
    }
}
