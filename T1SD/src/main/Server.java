package main;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Message.MessageType;
public class Server
{
    MyStack stack;
    int proxyId = 1;
    
    public Server(MyStack stack)
    {
        this.stack = stack;
    }
    
    public void startBySocketDatagram()
    {
        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket(Definitions.serverPort);
            byte[] buffer = new byte[1000];
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
    
    public void startBySocketConnection()
    {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try
        {
            serverSocket = new ServerSocket(Definitions.serverPort);
            while (true)
            {
                clientSocket = serverSocket.accept();  
                // Cliente conectado.
                System.out.println("Consumer connected; Ip: " + clientSocket.getInetAddress()+ "; Port: " + clientSocket.getPort());
                // Cria objetos de stream.
                DataInputStream in = new DataInputStream( clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream( clientSocket.getOutputStream());
                // Ouve cliente.
                String clientId = in.readUTF();
                // Responde ao cliente.
                out.writeUTF(clientId);
                // Criar thread para consumo.
                ProxyConsumer proxy = new ProxyConsumer(clientSocket, Integer.parseInt(clientId), stack);
                proxy.start();
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
            try 
            {
                if (clientSocket != null)
                    clientSocket.close();
                if (serverSocket != null)
                    serverSocket.close();
            } 
            catch (IOException e) 
            {
                System.out.println("Closing socket: " + e.getMessage());
            }
        }
    }
    
    public void startBySocketConnectionAndMessage()
    {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try
        {
            serverSocket = new ServerSocket(Definitions.serverPort);
            while (true)
            {
                clientSocket = serverSocket.accept();  
                // Cliente conectado.
                System.out.println("Consumer connected; Ip: " + clientSocket.getInetAddress()+ "; Port: " + clientSocket.getPort());
                
                // Cria objetos de stream.
                ObjectOutputStream objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream objectIn = new ObjectInputStream(clientSocket.getInputStream());
                // Ouve cliente.
                Message msg = (Message)objectIn.readObject();
                if ((msg.getType()).equals(MessageType.ID)) 
                    msg.setType(MessageType.CONNECTION);
                // Responde ao cliente.
                objectOut.writeObject(msg);
                
                // Criar thread para consumo.
                ProxyConsumer proxy = new ProxyConsumer(clientSocket, msg.getNumber(), stack);
                proxy.start();
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
        catch (ClassNotFoundException e) 
        {
            System.out.println("Class: " + e.getMessage());
        }
        finally 
        {
            try 
            {
                if (clientSocket != null)
                    clientSocket.close();
                if (serverSocket != null)
                    serverSocket.close();
            } 
            catch (IOException e) 
            {
                System.out.println("Closing socket: " + e.getMessage());
            }
        }
    }
}