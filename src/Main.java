import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main
{
        private String nick;

        private final static String HOST = "localhost";
        private final static int PORT = 23;

        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private boolean validation = false;

        public static void main(String[] args) throws IOException, InterruptedException
        {
                Main main = new Main();
                Socket socket = new Socket( HOST, PORT );

                main.oos = new ObjectOutputStream( socket.getOutputStream() );
                main.oos.flush();
                main.ois = new ObjectInputStream( socket.getInputStream() );

                main.startChat();
        }

        private void startChat() throws IOException, InterruptedException
        {
                new Thread( this::startListening ).start();
                validate();
                start();
        }

        private void validate() throws IOException, InterruptedException
        {
                do
                {
                        System.out.println( "Enter your nick" );
                        this.nick = new Scanner( System.in ).nextLine();

                        Message validateMsg = new Message.MessageBuilder()
                                .validate()
                                .sender( this.nick )
                                .build();
                        this.oos.writeObject( validateMsg );

                        Thread.sleep( 500 );

                } while ( !validation );
        }

        private void startListening()
        {
                while ( true )
                {
                        try
                        {
                                Message msgReceived = ( Message ) this.ois.readObject();
                                if ( msgReceived.getNickAccepted() )
                                {
                                        System.out.println( msgReceived.getMessage() );
                                        this.validation = true;
                                } else if ( msgReceived.getRecipient() == null && msgReceived.getFile() == null )
                                        System.out.println( msgReceived.getSender() + ": " + msgReceived.getMessage() );

                                else if ( msgReceived.getRecipient() != null && !msgReceived.getNickAccepted() && msgReceived.getFile() == null )
                                        System.out.println( "Message from: " + msgReceived.getSender() + ": " + msgReceived.getMessage() );

                                else if ( msgReceived.getFile() != null )
                                        receiveFile( msgReceived.getMessage(), msgReceived.getFile() );
                        } catch ( IOException | ClassNotFoundException e )
                        {
                                e.printStackTrace();
                                break;
                        }
                }
        }
        
        public void sendFile(String name, Socket socket) throws IOException
        {
            File file = new File(name);
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            int k = 0;
            char[] data = new char[512];
            while((k=br.read(data))!=-1)
            bw.write(data,0,k);
        }
        
        public void downloadFile(String name,Socket socket) throws IOException{
            File file = new File(name);
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);    
            FileWriter fw = new FileWriter(file);   
            BufferedWriter bw = new BufferedWriter(fw);
            int k = 0;
            char[] data = new char[512];
            while((k=br.read(data))!=-1)
            bw.write(data,0,k);
}
        
        private byte[] getFile(String filepath) throws IOException
        {
                return Files.readAllBytes( Paths.get( filepath ) );
        }
        private void receiveFile(String name, byte... file) throws IOException
        {
                try ( FileOutputStream fos = new FileOutputStream( "_" + name ) )
                {
                        fos.write( file );
                }
                System.out.println( "File has been received" );
        }

        private void start() throws IOException
        {
                System.out.println( "Welcome " + this.nick );

                while ( true )
                {
                        String line = new Scanner( System.in ).nextLine();

                        if ( line.equals( "#exit" ) )
                        {
                                quit();
                                break;
                        }

                        Message.MessageBuilder msg = new Message.MessageBuilder()
                                .sender( this.nick );

                        if ( line.split( ": " ).length == 2 )
                        {
                                msg.message( line.split( ": " )[1] )
                                        .recipient( line.split( ": " )[0] );
                        } 
                        
                        else if ( line.split( " " )[0].equals( "#file" ) )
                        {
                                msg.recipient( line.split( " " )[1] )
                                        .message( line.split( " " )[2] )
                                        .file( this.getFile( line.split( " " )[3] ) );
                        }  
                        
                        else
                                msg.message( line );

                        Message msgToSend = msg.build();
                        this.oos.writeObject( msgToSend );
                }
        }

        private void quit()
        {
                System.exit( 0 );
        }
}