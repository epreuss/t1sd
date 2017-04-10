package main;

/**
 *
 * @author Estevan
 */
public class MachineProducer 
{
    public static void main(String args[])
    {
        MyStack stack = new MyStack();
        Producer producer = new Producer(stack);
        producer.start();
        Server server = new Server(stack);
        System.out.println("Server " + Definitions.connectionType + " start!");
        switch (Definitions.connectionType) 
        {
            case SocketDatagram:
                server.startBySocketDatagram();
                break;
            case SocketConnection:
                server.startBySocketConnection();
                break;
            case SocketConnectionAndMessage:
                server.startBySocketConnectionAndMessage();
                break;
        }
    }
}
