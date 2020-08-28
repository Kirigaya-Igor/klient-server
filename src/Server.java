import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class Server
{
        private List<ClientThread> users = new ArrayList<>();
        private ServerSocket serverSocket;
        private Socket socket;
        //private int port;
        public static void main(String... args) throws IOException
        {
                Server server = new Server();
                server.serverSocket = new ServerSocket( 23 );

                while ( true )
                {
                        server.socket = server.serverSocket.accept();
                        System.out.println( "Client accepted" );
                        ClientThread client = new ClientThread( server.socket, server::getUsers );
                        Thread thread = new Thread( client );
                        server.users.add( client );
                        thread.start();
                }
        }

        private List<ClientThread> getUsers()
        {
                return users;
        }
}
@FunctionalInterface
interface GetList
{
        List<ClientThread> getUserList();
}
class ClientThread implements Runnable
{
        private Socket socket;
        private List<ClientThread> userList;
        private String nick;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        ClientThread(Socket socket, GetList list) throws IOException
        {
                this.socket = socket;
                this.userList = list.getUserList();

                this.oos = new ObjectOutputStream( socket.getOutputStream() );
                this.ois = new ObjectInputStream( socket.getInputStream() );
        }
        private void sendAll(Message message)
        {
                this.userList.stream()
                        .filter( x -> !x.socket.isClosed() )
                        .forEachOrdered( x -> x.send( message ) );
        }

        private void send(Message message)
        {
                try
                {
                        this.oos.writeObject( message );
                } catch ( IOException e )
                {
                        e.printStackTrace();
                }
        }

        private void sendToRecipient(Message message)
        {
                this.userList.stream()
                        .filter( x -> x.nick.equals( message.getRecipient() ) )
                        .filter( x -> !x.socket.isClosed() )
                        .forEachOrdered( x -> x.send( message ) );
        }

        private boolean validate(String userNick)
        {
                return this.userList.stream().noneMatch( x -> x.nick != null && x.nick.equals( userNick ) );
        }

        private String[] activeUsers()
        {
                return this.userList.stream().map( (e) -> e.nick ).toArray( String[]::new );
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
        
        public void downloadFile(String name,Socket socket) throws IOException
        {
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
        
        private void disconnect()
        {
                this.userList.remove( this );

                Message msg = new Message.MessageBuilder()
                        .sender( this.nick )
                        .message( " has been disconnected" )
                        .build();

                sendAll( msg );
        }

        @Override
        public void run()
        {
                try
                {
                        while ( true )
                        {
                                Message msgReceived = ( Message ) ois.readObject();

                                if ( msgReceived.getIsValidate() )
                                {
                                        Message.MessageBuilder newMsg = new Message.MessageBuilder();

                                        if ( validate( msgReceived.getSender() ) )
                                        {
                                                newMsg.acceptNick();
                                                this.nick = msgReceived.getSender();
                                                newMsg.message( Arrays.toString( this.activeUsers() ) );
                                                sendAll( new Message.MessageBuilder().message( this.nick + " dolaczyl do czatu" ).build());
                                        }
                                        send( newMsg.build() );
                                        continue;
                                }

                                if ( msgReceived.getRecipient() != null || msgReceived.getFile() != null )
                                {
                                        sendToRecipient( msgReceived );
                                } else
                                {
                                        sendAll( msgReceived );
                                }
                        }
                } catch ( IOException | ClassNotFoundException e )
                {
                        disconnect();
                }
        }
}