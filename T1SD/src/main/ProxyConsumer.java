package main;

import java.io.DataOutputStream;
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
    int totalConsumed = 0;
    int clientId;
    
    public ProxyConsumer(DatagramPacket request, MyStack stack)
    {
        this.request = request;
        this.stack = stack;
        this.clientId = Integer.parseInt(new String(request.getData()).trim());
        createSocketDatagram();
    }
    
    public ProxyConsumer(Socket clientSocket, int consumerId, MyStack stack)
    {
        this.clientSocket = clientSocket;
        this.stack = stack;
        this.clientId = consumerId; 
    }
    
    private void createSocketDatagram()
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
    
    private void sendShortBySocketDatagram(short consumed)
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
    
    private void sendShortBySocketConnection(short consumed)
    {
        try
        {
            // Cria objeto de stream.
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());                        
            // Envia short ao cliente.
            out.writeUTF(String.valueOf(consumed));
            // Short enviado.
            totalConsumed += consumed;
            //System.out.println("Proxy sent [" + consumed + "]");
        }
        catch (IOException e) 
        {
            System.out.println("Proxy IO: " + e.getMessage());
        }
    }
    
    private void sendShortBySocketConnectionAndMessage(short consumed)
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
    
    @Override
    public void run() 
    {
        int sends = 0;
        System.out.println("Proxy Consumer [id: " + clientId + "] starts. It is consuming...");
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
                
                switch (Definitions.connectionType) 
                {
                    case SocketDatagram:
                        sendShortBySocketDatagram(consumed);
                        break;
                    case SocketConnection:
                        sendShortBySocketConnection(consumed);
                        break;
                    case SocketConnectionAndMessage:
                        sendShortBySocketConnectionAndMessage(consumed);
                        break;
                }
                sends++;
                if (consumed == 0)
                    break;
            }
        }
        System.out.println("Proxy Consumer [id: " + clientId + "] ends; Total: " + totalConsumed + "; Sent " + sends + " shorts.");
        if (datagramSocket != null) 
        {
            datagramSocket.close();
            System.out.println("Proxy Consumer [id: " + clientId + "] Socket closed.");
        }
    }
}
