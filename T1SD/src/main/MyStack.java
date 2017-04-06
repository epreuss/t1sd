package main;
import java.util.Stack;

/**
 *
 * @author Estevan
 */
public class MyStack 
{
    Stack stack;
    
    public MyStack()
    {
        stack = new Stack();
    }
    
    public synchronized void push(short valor)
    {
        stack.push(valor);
        //System.out.println("Stack size: " + stack.size());
    }
    
    public synchronized short pop()
    {
        if (canConsume())
            return (short) stack.pop();
        else 
            return -1;
    }
    
    public synchronized boolean canConsume()
    {
        return !stack.empty();
    }
}
