package main;

/**
 *
 * @author Estevan
 */
public class MachineConsumer 
{
    public static void main(String args[])
    {
        createConsumer(1, true);        
    }
    
    public static void createConsumer(int id, boolean useMessage)
    {
        Consumer c = new Consumer(id, useMessage);
        c.start();
    }
}
