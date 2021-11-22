package Server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *
 * @author huert
 */
public class HTTPServer extends Thread{
    protected Socket socket;
    protected PrintWriter pw;
    protected BufferedOutputStream bos;
    protected BufferedReader br;
    private DataOutputStream dos;
    private DataInputStream dis;
    protected String fileName;
                        
    public HTTPServer(Socket socket) {
        this.socket = socket;
    }
	
    public void run() {
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            byte[] b = new byte[1024];
            int t = dis.read(b);
            String request = new String(b,0,t);
            System.out.println("t: "+t);
            if(request == null) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("<html><head><title>Servidor WEB\n");
                    sb.append("</title><body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>\n");
                    sb.append("</body></html>\n");
                    dos.write(sb.toString().getBytes());
                    dos.flush();
                    socket.close();
                    return;
            }
            System.out.println("\nClient connected from: "+socket.getInetAddress());
            System.out.println("At port: "+socket.getPort());
            System.out.println("Request:\n"+request+"\r\n\r\n");
            StringTokenizer st1= new StringTokenizer(request,"\n");
            String line = st1.nextToken();
            if(line.indexOf("?") == -1) {
                if(line.toUpperCase().startsWith("POST")){
                    String lastToken = request.substring(request.lastIndexOf("\n"));
                    System.out.println(lastToken);
                    paramsResponse(lastToken);
                } else if (line.toUpperCase().startsWith("PUT")) {
                    //TODO: PUT HTTP method implementation
                    getFileName(line);
                    System.out.println("Filename: "+fileName); //File path to process
                    //Not Implemented response ---> DLETE AFTER IMPLEMENTATION
                    System.out.println("PUT");
                    dos.write("HTTP/1.0 501 Not Implemented\r\n".getBytes());
                    dos.flush();
                    dos.close();
                    socket.close();
                } else if (line.toUpperCase().startsWith("DELETE")) {
                    //TODO: DELTE HTTP method implementation
                    getFileName(line);
                    System.out.println("Filename: "+fileName); //File path to process
                    //Not Implemented response ---> DLETE AFTER IMPLEMENTATION
                    System.out.println("DELETE");
                    dos.write("HTTP/1.0 501 Not Implemented\r\n".getBytes());
                    dos.flush();
                    dos.close();
                    socket.close();
                } else { //HEAD OR GET WITHPUT PARAMS
                    getFileName(line);
                    boolean get = (line.toUpperCase().startsWith("GET"));
                    if(fileName.compareTo("") == 0)
                        sendFile("index.htm",dos, get);
                    else
                        sendFile(fileName,dos, get);
                }
            } else if(line.toUpperCase().startsWith("GET")) { //GET WITH PARAMS
                StringTokenizer tokens=new StringTokenizer(line,"?");
                String req_a = tokens.nextToken();
                System.out.println("Token1: "+req_a);
                String req = tokens.nextToken();
                System.out.println("Token2: "+req);
                String params = req.substring(0, req.indexOf(" "))+"\n";
                paramsResponse(params);
            } else {
                dos.write("HTTP/1.0 501 Not Implemented\r\n".getBytes());
                dos.flush();
                dos.close();
                socket.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public void paramsResponse(String params) throws IOException{
        System.out.println("Params: "+params);
        StringBuffer response= new StringBuffer();
        response.append("HTTP/1.0 200 Okay \n");
        response.append("Date: ").append(new Date()).append(" \n");
        String mimeType = "Content-Type: text/html \n\n";
        response.append(mimeType);
        response.append("<html><head><title>SERVIDOR WEB</title></head>\n");
        response.append("<body bgcolor=\"#AACCFF\"><center><h1><br>Parametros Obtenidos..</br></h1><h3><b>\n");
        response.append(params);
        response.append("</b></h3>\n</center></body></html>\n\n");
        System.out.println("Response: "+response);
        dos.write(response.toString().getBytes());
        dos.flush();
        dos.close();
        socket.close();
    }

    public void getFileName(String line) {
        int i, f;
        i=line.indexOf("/");
        f=line.indexOf(" ",i);
        fileName=line.substring(i+1,f);
    }

    public void sendFile(String filePath, DataOutputStream dos1, boolean get) {
        try {
            int x = 0;
            DataInputStream dis2 = new DataInputStream(new FileInputStream(filePath));
            byte[] buf=new byte[1024];
            File ff = new File(filePath);			
            long tam_archivo=ff.length(),cont=0;
            StringBuffer sb = new StringBuffer();
            sb.append("HTTP/1.0 200 ok\n").append("Server: HTTPServer/1.0 \n");
            sb.append("Date: ").append(new Date()).append(" \n");
            sb.append("Content-Type: text/html \n");
            sb.append("Content-Length: ").append(tam_archivo).append(" \n\n");
            System.out.println(sb);
            dos1.write(sb.toString().getBytes());
            dos1.flush();
            if(get){
                while(cont<tam_archivo) {
                    x = dis2.read(buf);
                    dos1.write(buf,0,x);
                    cont += x;
                    dos1.flush();
                }
            }
            dis2.close();
            dos1.close();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
