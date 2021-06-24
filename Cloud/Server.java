import java.io.*;
import java.net.*;
  
// Server class
public class Server
{
    public static void main(String[] args) throws IOException 
    {
        // server is listening on port 80
        InetAddress ip = InetAddress.getByName("10.156.0.2");
        ServerSocket ss = new ServerSocket(80, 50, ip);
        Storage storage = new Storage();
          
        // running infinite loop for getting
        // client request
        while (true) 
        {
            Socket s = null;
              
            try 
            {
                // socket object to receive incoming client requests
                System.out.println(ss.getInetAddress());
                System.out.println(ss.getLocalPort());
                s = ss.accept();
                  
                System.out.println("A new client is connected : " + s);
                  
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                  
                System.out.println("Assigning new thread for this client");
  
                // create a new thread object
                Thread t = new ClientHandler(s, dis, dos, storage);
                
                // Invoking the start() method
                t.start();
                  
            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}
