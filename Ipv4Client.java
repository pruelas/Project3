
import java.io.*;
import java.io.InputStreamReader;
import java.net.Socket;

public final class Ipv4Client {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("codebank.xyz", 38003)) {
            System.out.println("Connected to server.");
            
            //Obtain IP address of server
            String address = socket.getInetAddress().getHostAddress();
            byte[] addr = socket.getInetAddress().getAddress();

            //Initialize an input stream and an output stream to communicate with server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");

            //holds the data that will hold the bytes to each packet
            byte[] b = new byte[4116];

            //holds the least amount of bytes a header can contain
            int nBytes = 20;
            
            //sets the fixed bytes of the datagram
            //byte for Version and Hlen
            b[0]=69;
            //byte value for TOS
            b[1] = 0;
            //byte values for Ident
            b[4] = 0;
            b[5] = 0;
            //byte values for flag and offset
            b[6] = 64; 
            b[7] = 0;

            //bytes hold the fixed values for TTL and Protocol
            b[8] = 50;
            b[9] = 6;
            
            //4 bytes hold fixed source addr
            b[12] = 0;
            b[13] = 0;
            b[14] = 0;
            b[15] = 0;

            //4 bytes hold fixed destination addr
            b[16] = addr[0];
            b[17] = addr[1];
            b[18] = addr[2];
            b[19] = addr[3];

            //Loop finds the checksum for each different sized packet and sends the complete
            //packet to the server
            for(int i = 2; i <=4096 ; i = i*2){
              //update data
              nBytes = 20 + i;

              //resets the value of the checksum to zero
              b[10] = (byte)0;
              b[11] = (byte)0;

              //update bytes designated for length of datagram            
              b[2] = (byte) ((nBytes >> 8) & 0xff);
              b[3] = (byte) (nBytes & 0xFF);

              //calculate checksum
              short checkSum = checkSum(b, nBytes);
              long value = checkSum; 
             
              String chSum = Long.toHexString((long)(value) & 0XFFFF);

              //Turns the calculated checksum into a sequence of two bytes
              if(chSum.length() == 4){ 
              b[10] = (byte) Integer.parseInt(chSum.substring(0,2), 16);
              b[11] = (byte) Integer.parseInt(chSum.substring(2,4), 16);

              }else{
              //Appends 0's to the checksum to be able to create a sequence of two bytes
                String y = chSum;
                while(y.length() !=4){
                  y = "0" + y;
                }
 
                b[10] = (byte) Integer.parseInt(y.substring(0,2), 16);
                b[11] = (byte) Integer.parseInt(y.substring(2,4), 16);
              }  
              
              //Send bytes of current packet to server
              for(int v = 0; v<nBytes; v++)
              out.write(b[v]);

              //receive servers reply
              String line = br.readLine();

              System.out.println("data length: " + i);
              System.out.println(line + "\n");
              
            }            

            System.out.println("Disconnected from server.");
      }
    }

    //Method calculates the checksum of the bytes in the packet
    public static short checkSum(byte[] b, int nBytes){
        String sect = "";
        String b1 = "";
        long sum = 0;
        int q = 0;
        String[] arr = new String[2];

        //Loop appends two bytes at a time and adds them to the total sum. If overflow occurs,
        //it is cleared and added back to the sum
        for(int i = 0; i < (((nBytes)/2)) ; i ++){ 
          b1 =  Integer.toHexString((int)(b[q]) & 0X0FF);
          if(b1.length() == 1)
          b1 = "0" +b1;

         if(q+1 != nBytes){
           sect = Integer.toHexString((int)(b[q+1]) & 0X0FF);
           if(sect.length() == 1)
             sect = "0" + sect;
           }else{
             sect = Integer.toHexString(0 & 0X0FF);
             if(sect.length() == 1)
             sect = "0" + sect;
           }

           String b2 = b1 +sect;

           sum += Integer.parseInt(b2, 16);

           if((sum & 0XFFFF0000) > 0){
             sum &= 0xFFFF;
             sum++;
           }
         
            q+=2;          
            b1 = "";
            sect = "";
         }
      return  (short) ~(sum & 0xFFFF);
    }
    
}















