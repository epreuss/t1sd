package main;

/**
 *
 * @author Estevan
 */
public class Message implements java.io.Serializable
{
    enum MessageType { ID, CONNECTION, CONSUME };
    
    public MessageType type;
    public short number;
    
    public Message(MessageType type, short number) 
    {
        this.type = type;
        this.number = number;
    }
    
    public void setType(MessageType t) { type = t; }
    
    public MessageType getType() { return type; }
    
    public void getNumber(short n) { number = n; }
    
    public short getNumber() { return number; }

}
