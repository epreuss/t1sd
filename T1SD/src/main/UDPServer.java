package main;

import java.net.*;
import java.io.*;
public class UDPServer
{
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
                DatagramPacket reply = new DatagramPacket(request.getData(),
                    request.getLength(), request.getAddress(), request.getPort());
                System.out.println("Received: " + reply.getAddress());
                // Criar thread para consumo.
                ProxyConsumer proxy = new ProxyConsumer(request.getAddress(), request.getPort());
                proxy.start();
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