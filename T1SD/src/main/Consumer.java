package main;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Message.MessageType;

public class Consumer extends Thread
{
    enum ConnectionResult { SUCCESS, TIMEOUT };
    
    DatagramSocket datagramSocket;
    Socket socketForMsg;
    boolean useMessage;
    final int timeout = 500;
    final int totalRetransmissions = 5;
    int retransCounter = 1;
    int totalConsumed = 0;    
    int id;
    
    public Consumer(int id, boolean useMessage)
    {
        this.id = id;
        this.useMessage = useMessage;
        //System.out.println("Consumer [id: " + id + "] is created.");
    }
    
    private boolean connectToServerByBytes()
    {
        datagramSocket = createDatagramSocket();
        ConnectionResult lastResult = tryConnectionWithBytes();
        while (lastResult == ConnectionResult.TIMEOUT && retransCounter < totalRetransmissions)
        {            
            retransCounter++;        
            lastResult = tryConnectionWithBytes();
        }
        boolean connected = lastResult == ConnectionResult.SUCCESS;
        return connected;
    }
    
    private boolean connectToServerByMessage()
    {
        socketForMsg = createSocketForMsg();
        ConnectionResult lastResult = tryConnectionWithMessage();
        while (lastResult == ConnectionResult.TIMEOUT && retransCounter < totalRetransmissions)
        {            
            retransCounter++;        
            lastResult = tryConnectionWithMessage();
        }
        boolean connected = lastResult == ConnectionResult.SUCCESS;
        return connected;
    }
    
    @Override
    public void run() 
    {
        System.out.println("Consumer [id: " + id + "] is connecting to server...");
        if (useMessage)
        {
            if (connectToServerByMessage())
                consumeDataWithMessage();
        }
        else 
        {
            if (connectToServerByBytes())
                consumeDataWithBytes();
        }
    }
    
    public void consumeDataWithBytes()
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
                datagramSocket.receive(reply);
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
        if (datagramSocket != null) 
        {
            datagramSocket.close();
            //System.out.println("Socket closed.");
        }
    }
    
    public void consumeDataWithMessage()
    {
        /*
        ServerSocket serverSocket = null;
        // Pega porta do socket que se comunicou com o servidor.
        int localPort = socketForMsg.getLocalPort();
        if (socketForMsg != null) 
        {
            try 
            {
                socketForMsg.close();
            } 
            catch (IOException e) 
            {
                System.out.println("IO: " + e.getMessage() + ".");
            }
        }
        // Cria um socket do tipo servidor para ouvir do proxy.
        System.out.println("Consumer local port: " + localPort);
        try 
        {
            //System.out.println("Consumer [id: " + id + "] starts consuming...");
            serverSocket = new ServerSocket(localPort);
        } 
        catch (IOException e) 
        {
            System.out.println("Server Socket: " + e.getMessage() + ".");
        }
        
        // Estabelece conexão com proxy.
        Socket proxySocket = null;
        while (true)
        {   
            try 
            {
                proxySocket = serverSocket.accept();
                proxySocket.setSoTimeout(timeout);
                break;
            }
            catch (IOException e) 
            {
                System.out.println("IO: " + e.getMessage() + ".");
            } 
        }
        
        System.out.println("Recebe shorts do proxy: " + proxySocket.getLocalPort() + ", " + proxySocket.getPort());
        */
        int receives = 0; 
        while (true)
        {   
            try 
            {
                // Cria objeto de stream.
                ObjectInputStream objectIn = new ObjectInputStream(socketForMsg.getInputStream());
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
                System.out.println("Got [" + consumed + "]");
            } 
            catch (IOException e) 
            {
                System.out.println("IO: " + e.getMessage() + ".");
            } 
            catch (ClassNotFoundException e) 
            {
                System.out.println("Class: " + e.getMessage() + ".");
            }
        }
        System.out.println("Consumer [id: " + id + "] ends; Total: " + totalConsumed + "; Received " + receives + " shorts.");
        
        if (socketForMsg != null) 
        {
            try 
            {
                socketForMsg.close();
                System.out.println("Socket closed.");
            } 
            catch (IOException e) 
            {
                System.out.println("IO: " + e.getMessage() + ".");
            }
        }
    }
    
    private DatagramSocket createDatagramSocket()
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
    
    private Socket createSocketForMsg()
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
    
    private ConnectionResult tryConnectionWithBytes()
    {
        ConnectionResult result = ConnectionResult.SUCCESS;
        try 
        {
            byte[] bytes = String.valueOf(id).getBytes();
            InetAddress host = InetAddress.getByName(Definitions.serverIp);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, host, Definitions.serverPort);
            System.out.println("Trying [id: " + id + "] connection... " + "[tries: " + retransCounter + "]");
            datagramSocket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            datagramSocket.receive(reply);
            System.out.println("Consumer [id: " + id + "] connected.");
        }
        catch (IOException e)
        {
            System.out.println("IO: " + e.getMessage() + ".");
            result = ConnectionResult.TIMEOUT;
        }        
        return result;
    }
    
    private ConnectionResult tryConnectionWithMessage()
    {
        ConnectionResult result = ConnectionResult.SUCCESS;
        Message msg = new Message(MessageType.ID, (short)id);
        try 
        {
            // Cria objetos de stream.
            ObjectOutputStream objectOut = new ObjectOutputStream(socketForMsg.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(socketForMsg.getInputStream());
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