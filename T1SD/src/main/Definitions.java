package main;

/**
 *
 * @author Estevan
 */
public class Definitions 
{
    enum ConnectionType { SocketDatagram, SocketConnection, SocketConnectionAndMessage };
    
    public static ConnectionType connectionType = ConnectionType.SocketDatagram;
    public static int serverPort = 38001;
    public static String serverIp = "127.0.0.1";
}
