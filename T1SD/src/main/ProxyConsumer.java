package main;

import java.net.InetAddress;

/**
 *
 * @author Estevan
 */
public class ProxyConsumer extends Thread
{
    private InetAddress ip; 
    private int port;
    
    public ProxyConsumer(InetAddress ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }
    
    @Override
    public void run() 
    {
        
    }
}
