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
        UDPServer server = new UDPServer();
        server.start();
    }
}
