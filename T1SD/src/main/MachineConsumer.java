package main;

/**
 *
 * @author Estevan
 */
public class MachineConsumer 
{
    public static void main(String args[])
    {
        System.out.println("Consumers " + Definitions.connectionType + " start!");
        createConsumer(1);        
        createConsumer(2);   
    }
    
    public static void createConsumer(int id)
    {
        new Consumer(id).start();
    }
}
