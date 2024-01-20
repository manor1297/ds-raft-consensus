package com.dist.raft.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.w3c.dom.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Receiver implements Runnable {

    private final NodeApplication nodeObj = new NodeApplication();
    private final Integer port;
    private final String nodeName;

    public Receiver(String nodeName, Integer port) {
        this.port = port;
        this.nodeName = nodeName;
    }

    @Override
    public void run() {
        try {
            //Setting variables for receiver thread
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();
            InetAddress ip = InetAddress.getByName(nodeName);

            //Receiver Thread
            while (true) {
                //Receiving Datagram at assigned port
                System.out.println("Listening at port "+port+"....");
                DatagramSocket dr = new DatagramSocket(port);
                byte[] buf = new byte[1024];
                DatagramPacket dpr = new DatagramPacket(buf, 1024);
                dr.receive(dpr);
                dr.close();

                //Preprocess Datagram and convert to Message
                String inboundMessage = new String(dpr.getData(), 0, dpr.getLength());
                System.out.println("Message on receiver : "+inboundMessage);
                Message msgObj = gson.fromJson(inboundMessage, Message.class);

                //Message received and sent for processing
                nodeObj.processMessage(msgObj);
            }
        }catch (Exception e){
            System.out.println("Exception in Receiver Thread " +e.getMessage() );
        }
    }
}
