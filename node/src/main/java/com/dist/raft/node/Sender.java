package com.dist.raft.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sender implements Runnable{

    private final Message message;
    private final InetAddress ip;
    private final Integer port;

    public Sender(Message message, InetAddress ip, Integer port) {
        this.message = message;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            DatagramSocket ds = new DatagramSocket();
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();

            String msgStr = gson.toJson(message);
            DatagramPacket dps = new DatagramPacket(msgStr.getBytes(), msgStr.length(), ip, port);
            ds.send(dps);
            ds.close();
        } catch (Exception e) {
            System.out.println("Sender Thread Exception");
        }
    }
}
