package main;

import java.net.*;
import java.io.*;

public class UDPConsumer
{
    enum ConnectionResult { SUCCESS, TIMEOUT };
    
    final int timeout = 500;
    int totalRetransmissions = 3;
    int retransCounter = 1;
    
    public void connectToServer()
    {
        System.out.println("UDPConsumer is connecting to server...");
        DatagramSocket socket = createSocket();
        while (tryConnection(socket) == ConnectionResult.TIMEOUT && retransCounter < totalRetransmissions)
            retransCounter++;
        if (socket != null) 
        {
            socket.close();
            System.out.println("Socket closed.");
        }
    }
    
    private DatagramSocket createSocket()
    {
        DatagramSocket socket = null;
        try 
        {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
        }
        catch (SocketException e)
        {
            System.out.println("Socket: " + e.getMessage());
        }
        return socket;
    }
    
    private ConnectionResult tryConnection(DatagramSocket socket)
    {
        ConnectionResult result = ConnectionResult.SUCCESS;
        String msg = "watefek is going on here";
        try 
        {
            byte[] m = msg.getBytes();
            InetAddress host = InetAddress.getByName(Definitions.serverIp);
            DatagramPacket request = new DatagramPacket(m, Definitions.serverIp.length(), host, Definitions.serverPort);
            System.out.println("Trying to send... " + "[" + retransCounter + "]");
            socket.send(request);
            byte[] buffer = new byte[3000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            System.out.println("Send done.");
            System.out.println("Reply: " + new String(reply.getData()));
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage() + ".");
            result = ConnectionResult.TIMEOUT;
        }        
        return result;
    }
}