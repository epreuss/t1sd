package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Message.MessageType;

/**
 *
 * @author Estevan
 */
public class ProxyConsumer extends Thread
{
    /*
    private InetAddress ip; 
    private int port;
    */
    
    DatagramPacket request; 
    DatagramSocket datagramSocket;
    Socket clientSocket;
    Socket socketForMsg;
    MyStack stack;
    boolean useDatagram;
    final int connectionTries = 5; // For MSG connections.
    int totalConsumed = 0;
    int id;
    
    public ProxyConsumer(DatagramPacket request, MyStack stack)
    {
        this.request = request;
        this.stack = stack;
        this.id = Integer.parseInt(new String(request.getData()).trim());
        useDatagram = true;
        createDatagramSocket();
    }
    
     public ProxyConsumer(Socket clientSocket, int consumerId, MyStack stack)
    {
        this.clientSocket = clientSocket;
        this.stack = stack;
        this.id = consumerId;
        useDatagram = false;
        //createSocketForMsg();
    }
    
    private void createDatagramSocket()
    {
        try
        {
            datagramSocket = new DatagramSocket();
        } 
        catch (SocketException e) 
        {
            System.out.println("Proxy DatagramSocket: " + e.getMessage());
        }
    }
    
    private void createSocketForMsg()
    {
        int tries = 0;
        while (tries < connectionTries)
        {   
            try
            {
                socketForMsg = new Socket(clientSocket.getInetAddress(), clientSocket.getPort());
            } 
            catch (IOException e) 
            {
                System.out.println("Proxy SocketForMsg: " + e.getMessage());
            }
            tries++;
        }
        System.out.println("Proxy Port: " + socketForMsg.getLocalPort() + ", " + socketForMsg.getPort());
    }
    
    private void sendShortByDatagram(short consumed)
    {
        try
        {
            byte[] bytes = String.valueOf(consumed).getBytes();
            
            // Cria resposta.
            DatagramPacket reply = new DatagramPacket(bytes,
                bytes.length, request.getAddress(), request.getPort());
            // Responde ao cliente.
            datagramSocket.send(reply);
            // Short enviado.
            totalConsumed += consumed;
            //System.out.println("Proxy sent [" + consumed + "]");
        }
        catch (IOException e) 
        {
            System.out.println("Proxy IO: " + e.getMessage());
        }
    }
    
    private void sendShortByMessage(short consumed)
    {
        try
        {
            // Cria objeto de stream.
            ObjectOutputStream objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            Message msg = new Message(MessageType.CONSUME, consumed);
            // Envia short ao cliente.
            objectOut.writeObject(msg);
            // Short enviado.
            totalConsumed += consumed;
            System.out.println("Proxy sent [" + consumed + "]");
        }
        catch (IOException e) 
        {
            System.out.println("Proxy IO: " + e.getMessage());
        }
    }
    
    private short getShortFromStack()
    {
        return stack.pop();
    }
    
    /*
    public ProxyConsumer(InetAddress ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }
    */
    
    @Override
    public void run() 
    {
        int sends = 0;
        System.out.println("Proxy Consumer [id: " + id + "] starts.");
        while (true)
        {
            if (stack.canConsume())
            {
                short consumed = getShortFromStack();    
                /*
                Outra thread pode já ter consumido e a pilha ficar vazia.
                Se isso acontece, ela retorna -1.
                */
                if (consumed == -1) 
                    continue;
                if (useDatagram)
                    sendShortByDatagram(consumed);
                else
                    sendShortByMessage(consumed);
                sends++;
                if (consumed == 0)
                    break;
            }
        }
        System.out.println("Proxy Consumer [id: " + id + "] ends; Total: " + totalConsumed + "; Sent " + sends + " shorts.");
        if (datagramSocket != null) 
        {
            datagramSocket.close();
            //System.out.println("Socket closed.");
        }
    }
}
