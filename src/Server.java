import java.io.*;
import java.net.*;
//this is just a basic server - no relation to router info yet

public class Server {
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream input = null;

    public Server(int port){
        try{
            ss = new ServerSocket(port);
            System.out.println("Server Started");
            System.out.println("Waiting for a client ...");
            s = ss.accept();
            System.out.println("Client accepted");

            input = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            String m = "";

            while(!m.equals("Over")){
                try{
                    m = input.readUTF();
                    System.out.println(m);
                }
                catch(IOException i){
                    System.out.println(i);
                }
            }
            System.out.println("Closing Connection ... ");
            s.close();
            input.close();
        }
        catch(IOException i){
            System.out.println(i);
        }
    }

    public static void main (String[] args){
       Server s = new Server(6666);
    }
}
