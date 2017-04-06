package main;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPConsumer extends Thread
{
    enum ConnectionResult { SUCCESS, TIMEOUT };
    
    DatagramSocket socket;
    final int timeout = 500;
    final int totalRetransmissions = 5;
    int retransCounter = 1;
    int totalConsumed = 0;
    int id;
    
    public UDPConsumer(int id)
    {
        this.id = id;
        //System.out.println("Consumer [id: " + id + "] is created.");
    }
    
    public boolean connectToServer()
    {
        socket = createSocket();
        ConnectionResult lastResult = tryConnection();
        while (lastResult == ConnectionResult.TIMEOUT && retransCounter < totalRetransmissions)
        {            
            retransCounter++;        
            lastResult = tryConnection();
        }
        boolean connected = lastResult == ConnectionResult.SUCCESS;
        return connected;
    }
    
    @Override
    public void run() 
    {
        System.out.println("Consumer [id: " + id + "] is connecting to server...");
        if (connectToServer())
            consumeData();
    }
    
    public void consumeData()
    {
        //System.out.println("Consumer [id: " + id + "] starts consuming...");
        int receives = 0;
        while (true)
        {   
            try 
            {
                // Cria buffer.
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                socket.receive(reply);
                // Recebido do proxy.
                String data = new String(reply.getData());
                short consumed = (short) Integer.parseInt(data.trim());
                //System.out.println("Got [" + consumed + "]");
                receives++;
                if (consumed == 0)
                    break;
                else
                    totalConsumed += consumed;
            } 
            catch (IOException e) 
            {
                //System.out.println("IO: " + e.getMessage() + ".");
            }
        }
        System.out.println("Consumer [id: " + id + "] ends; Total: " + totalConsumed + "; Received " + receives + " shorts.");
        if (socket != null) 
        {
            socket.close();
            //System.out.println("Socket closed.");
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
    
    private ConnectionResult tryConnection()
    {
        ConnectionResult result = ConnectionResult.SUCCESS;
        try 
        {
            byte[] bytes = String.valueOf(id).getBytes();
            InetAddress host = InetAddress.getByName(Definitions.serverIp);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, host, Definitions.serverPort);
            System.out.println("Trying [id: " + id + "] connection... " + "[tries: " + retransCounter + "]");
            socket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socket.receive(reply);
            System.out.println("Consumer [id: " + id + "] connected.");
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage() + ".");
            result = ConnectionResult.TIMEOUT;
        }        
        return result;
    }
}