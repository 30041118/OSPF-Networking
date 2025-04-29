import java.io.*;
import java.net.*;

public class Client {
    private Socket s = null;
    private DataInputStream input = null;
    private DataOutputStream output = null;

    public Client(String addr, int port){
        try{
            s = new Socket(addr, port);
            System.out.println("Connected");

            input = new DataInputStream(System.in);
            output = new DataOutputStream(s.getOutputStream());
        } catch(IOException i){
            System.out.println(i);
        }
        String m = "";

        while(!m.equals("Over")){
            try{
                m = input.readLine();
                output.writeUTF(m);
            } catch(IOException i){
                System.out.println(i);
            }
        }

        try{
            input.close();
            output.close();
            s.close();

        } catch(IOException i){
            System.out.println(i);
        }
    }

    public static void main(String[] args){
        Client c = new Client("127.0.0.1", 6666);
    }
}
