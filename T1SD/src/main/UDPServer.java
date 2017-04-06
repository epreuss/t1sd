package main;

import java.net.*;
import java.io.*;
public class UDPServer
{
    MyStack stack;
    int proxyId = 1;
    
    public UDPServer(MyStack stack)
    {
        this.stack = stack;
    }
    
    public void start()
    {
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket(Definitions.serverPort);
            byte[] buffer = new byte[1000];
            System.out.println("Server start!");
            while (true)
            {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                // Cliente conectado.
                System.out.println("Consumer connected; Port: " + request.getPort());
                // Criar thread para consumo.
                ProxyConsumer proxy = new ProxyConsumer(request, stack);
                proxy.start();
                
                DatagramPacket reply = new DatagramPacket(request.getData(),
                    request.getData().length, request.getAddress(), request.getPort());
                // Responde ao cliente.
                socket.send(reply);
            }
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
            if (socket != null) 
                socket.close();
        }
    }
}