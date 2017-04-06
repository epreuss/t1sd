package main;

/**
 *
 * @author Estevan
 */
public class MachineConsumer 
{
    public static void main(String args[])
    {
        createTwoConsumers(1, 2);
        createTwoConsumers(3, 4);
    }
    
    public static void createTwoConsumers(int id1, int id2)
    {
        UDPConsumer c1 = new UDPConsumer(id1);
        c1.start();
        UDPConsumer c2 = new UDPConsumer(id2);
        c2.start();
    }
}
