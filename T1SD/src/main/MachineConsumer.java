package main;

/**
 *
 * @author Estevan
 */
public class MachineConsumer 
{
    public static void main(String args[])
    {
        UDPConsumer c1 = new UDPConsumer();
        c1.connectToServer();
        //UDPConsumer c2 = new UDPConsumer();
        //c2.connectToServer(ip);
    }
}
