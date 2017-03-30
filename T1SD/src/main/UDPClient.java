package main;

import java.net.*;
import java.io.*;

public class UDPClient
{
    public static void main(String args[])
    {
        // args give message contents and server hostname
        System.out.println("Client start!");
        args = new String[2];
        args[0] = "msg";
        args[1] = "127.0.0.1";
        DatagramSocket aSocket = null;
        try 
        {
            aSocket = new DatagramSocket();
            byte [] m = args[0].getBytes();
            InetAddress aHost = InetAddress.getByName(args[1]);
            int serverPort = 38000;
            DatagramPacket request = new DatagramPacket(m, args[0].length(), aHost, serverPort);
            System.out.println("Start send");
            aSocket.setSoTimeout(2000);
            aSocket.send(request);
            System.out.println("Send done");
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            System.out.println("Reply: " + new String(reply.getData()));
        }
        catch (SocketException e)
        {
            System.out.println("Socket: " + e.getMessage());
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage());
        }        
        finally 
        {
            if (aSocket != null) 
            {
                aSocket.close();
                System.out.println("Socket closed");
            }
        }
    }
}