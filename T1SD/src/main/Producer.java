package main;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Estevan
 */
public class Producer extends Thread
{
    MyStack stack;
    final int maxProduction = 10000;
    final int productionDelay = 2;
    
    public Producer(MyStack stack)
    {
        this.stack = stack;
    }
    
    @Override
    public void run() 
    {
        int counter = 0;
        while (counter < maxProduction)
        {
            randomSleep();
            produce();
            counter++;
        }
        endProduction();
    }
    
    public void endProduction()
    {
        stack.push((short) 0);
        stack.push((short) 0);
        stack.push((short) 0);
        stack.push((short) 0);
        System.out.println("PRODUCTION IS DONE!");
    }
    
    public void produce()
    {
        short production = (short) (1 + new Random().nextInt(32767));
        stack.push(production);
    }
    
    public void randomSleep()
    {
        try {
            sleep(productionDelay);// + new Random().nextInt(500));
        } catch (InterruptedException ex) {
            Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
