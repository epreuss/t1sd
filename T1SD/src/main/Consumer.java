package main;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Message.MessageType;

public class Consumer extends Thread
{
    enum ConnectionResult { SUCCESS, TIMEOUT };
    
    DatagramSocket socketDatagram;
    Socket socketConnection;    
    final int timeout = 500;
    final int totalRetransmissions = 5;
    int retransCounter = 1;
    int totalConsumed = 0;    
    int id;
    
    public Consumer(int id)
    {
        this.id = id;
        //System.out.println("Consumer [id: " + id + "] is created.");
    }
    
    private boolean connectBySocketDatagram()
    {
        socketDatagram = createSocketDatagram();
        ConnectionResult lastResult = tryConnectBySocketDatagram();
        while (lastResult == ConnectionResult.TIMEOUT && retransCounter < totalRetransmissions)
        {            
            retransCounter++;        
            lastResult = tryConnectBySocketDatagram();
        }
        boolean connected = lastResult == ConnectionResult.SUCCESS;
        return connected;
    }
    
    private boolean connectBySocketConnection()
    {
        socketConnection = createSocketConnetion();
        ConnectionResult lastResult = tryConnectBySocketConnection();
        while (lastResult == ConnectionResult.TIMEOUT && retransCounter < totalRetransmissions)
        {            
            retransCounter++;        
            lastResult = tryConnectBySocketConnection();
        }
        boolean connected = lastResult == ConnectionResult.SUCCESS;
        return connected;
    }
    
    private boolean connectBySocketConnectionAndMessage()
    {
        socketConnection = createSocketConnetion();
        ConnectionResult lastResult = tryConnectBySocketConnectionAndMessage();
        while (lastResult == ConnectionResult.TIMEOUT && retransCounter < totalRetransmissions)
        {            
            retransCounter++;        
            lastResult = tryConnectBySocketConnectionAndMessage();
        }
        boolean connected = lastResult == ConnectionResult.SUCCESS;
        return connected;
    }
    
    @Override
    public void run() 
    {
        System.out.println("Consumer [id: " + id + "] is connecting to server...");
        switch (Definitions.connectionType)
        {
            case SocketDatagram:
                if (connectBySocketDatagram())
                    consumeDataBySocketDatagram();
                break;
            case SocketConnection:
                if (connectBySocketConnection())
                    consumeDataBySocketConnection();
                break;
            case SocketConnectionAndMessage:
                if (connectBySocketConnectionAndMessage())
                    consumeDataBySocketConnectionAndMessage();
                break;
        }
    }
    
    public void consumeDataBySocketDatagram()
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
                socketDatagram.receive(reply);
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
                System.out.println(e.getMessage() + " for [id: " + id + "]");
            }
        }
        System.out.println("Consumer [id: " + id + "] ends; Total: " + totalConsumed + "; Received " + receives + " shorts.");
        if (socketDatagram != null) 
        {
            socketDatagram.close();
            System.out.println("Consumer [id: " + id + "] Socket closed.");
        }
    }
    
    public void consumeDataBySocketConnection()
    {
        int receives = 0; 
        while (true)
        {   
            try 
            {
                // Cria objeto de stream.
                DataInputStream in = new DataInputStream(socketConnection.getInputStream());
                // Espera por resposta do proxy.
                String reply = in.readUTF();
                // Recebido do proxy.
                short consumed = (short) Integer.parseInt(reply.trim());
                receives++;
                if (consumed == 0)
                    break;
                else
                    totalConsumed += consumed;
                //System.out.println("Got [" + consumed + "]");
            } 
            catch (IOException e) 
            {
                System.out.println(e.getMessage() + " for [id: " + id + "]");
            } 
        }
        System.out.println("Consumer [id: " + id + "] ends; Total: " + totalConsumed + "; Received " + receives + " shorts.");
        
        if (socketConnection != null) 
        {
            try 
            {
                socketConnection.close();
                System.out.println("Consumer [id: " + id + "] Socket closed.");
            } 
            catch (IOException e) 
            {
                System.out.println("IO: " + e.getMessage() + ".");
            }
        }
    }
    
    public void consumeDataBySocketConnectionAndMessage()
    {
        int receives = 0; 
        while (true)
        {   
            try 
            {
                // Cria objeto de stream.
                ObjectInputStream objectIn = new ObjectInputStream(socketConnection.getInputStream());
                // Espera por resposta do proxy.
                Message msg = (Message)objectIn.readObject();
                // Recebido do proxy.
                short consumed = -1;
                if ((msg.getType()).equals(MessageType.CONSUME))
                {
                    consumed = (short) msg.getNumber();
                    receives++;
                    if (consumed == 0)
                        break;
                    else
                        totalConsumed += consumed;
                }
                //System.out.println("Got [" + consumed + "]");
            } 
            catch (IOException e) 
            {
                System.out.println(e.getMessage() + " for [id: " + id + "]");
            } 
            catch (ClassNotFoundException e) 
            {
                System.out.println("Class: " + e.getMessage() + ".");
            }
        }
        System.out.println("Consumer [id: " + id + "] ends; Total: " + totalConsumed + "; Received " + receives + " shorts.");
        
        if (socketConnection != null) 
        {
            try 
            {
                socketConnection.close();
                System.out.println("Consumer [id: " + id + "] Socket closed.");
            } 
            catch (IOException e) 
            {
                System.out.println("IO: " + e.getMessage() + ".");
            }
        }
    }
    
    private DatagramSocket createSocketDatagram()
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
    
    private Socket createSocketConnetion()
    {
        Socket socket = null;
        try 
        {
            socket = new Socket(Definitions.serverIp, Definitions.serverPort);
            socket.setSoTimeout(timeout);
        } 
        catch (IOException e) 
        {
            System.out.println("IO: " + e.getMessage() + ".");
        }
        return socket;
    }
    
    private ConnectionResult tryConnectBySocketDatagram()
    {
        ConnectionResult result = ConnectionResult.SUCCESS;
        try 
        {
            byte[] bytes = String.valueOf(id).getBytes();
            InetAddress host = InetAddress.getByName(Definitions.serverIp);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, host, Definitions.serverPort);
            System.out.println("Trying [id: " + id + "] connection... " + "[tries: " + retransCounter + "]");
            socketDatagram.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            socketDatagram.receive(reply);
            System.out.println("Consumer [id: " + id + "] connected.");
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage() + ".");
            result = ConnectionResult.TIMEOUT;
        }        
        return result;
    }
    
    private ConnectionResult tryConnectBySocketConnection()
    {
        ConnectionResult result = ConnectionResult.SUCCESS;
        try 
        {         
            // Cria objetos de stream.
            DataInputStream in = new DataInputStream(socketConnection.getInputStream());
            DataOutputStream out = new DataOutputStream(socketConnection.getOutputStream());
            // Solicita servidor.
            System.out.println("Trying [id: " + id + "] connection... " + "[tries: " + retransCounter + "]");
            out.writeUTF(String.valueOf(id));
            // Espera por resposta.
            in.readUTF();
            // Conectado.
            System.out.println("Consumer [id: " + id + "] connected.");
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage() + ".");
            result = ConnectionResult.TIMEOUT;
        } 
        return result;
    }
    
    private ConnectionResult tryConnectBySocketConnectionAndMessage()
    {
        ConnectionResult result = ConnectionResult.SUCCESS;
        Message msg = new Message(MessageType.ID, (short)id);
        try 
        {
            // Cria objetos de stream.
            ObjectOutputStream objectOut = new ObjectOutputStream(socketConnection.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(socketConnection.getInputStream());
            // Solicita servidor.
            System.out.println("Trying [id: " + id + "] connection... " + "[tries: " + retransCounter + "]");
            objectOut.writeObject(msg);
            // Espera por resposta.
            msg = (Message)objectIn.readObject();
            // Conectado.
            if ((msg.getType()).equals(MessageType.CONNECTION)) 
               System.out.println("Consumer [id: " + id + "] connected.");
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage() + ".");
            result = ConnectionResult.TIMEOUT;
        } 
        catch (ClassNotFoundException e) 
        {        
            System.out.println("Class: " + e.getMessage() + ".");
        }
        return result;
    }
}