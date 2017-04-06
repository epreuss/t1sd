package main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    DatagramSocket socket;
    MyStack stack;
    int totalConsumed = 0;
    int id;
    
    public ProxyConsumer(DatagramPacket request, MyStack stack)
    {
        this.request = request;
        this.stack = stack;
        this.id = Integer.parseInt(new String(request.getData()).trim());
        createSocket();
    }
    
    private void createSocket()
    {
        try
        {
            socket = new DatagramSocket();
        } 
        catch (SocketException e) 
        {
            System.out.println("Proxy Socket: " + e.getMessage());
        }
    }
    
    private void sendShortToClient(short consumed)
    {
        try
        {
            byte[] bytes = String.valueOf(consumed).getBytes();
            
            // Cria resposta.
            DatagramPacket reply = new DatagramPacket(bytes,
                bytes.length, request.getAddress(), request.getPort());
        
            // Responde ao cliente.
            socket.send(reply);
            totalConsumed += consumed;
            //System.out.println("Proxy sent [" + consumed + "]");
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
                sendShortToClient(consumed);
                sends++;
                if (consumed == 0)
                    break;
            }
        }
        System.out.println("Proxy Consumer [id: " + id + "] ends; Total: " + totalConsumed + "; Sent " + sends + " shorts.");
        if (socket != null) 
        {
            socket.close();
            //System.out.println("Socket closed.");
        }
    }
}
