package dev.omochi;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

import static dev.omochi.Main.serverNotSSL;
import static dev.omochi.Main.serverSSL;

public class Proxy implements Runnable{
    public boolean isSSL;
    public static String CRLF;
    @Override
    public void run() {
        try {
            System.out.println("start >>>" + isSSL);
            HttpRequestCatcher httpRequestCatcher = null;
            HttpResponse httpResponse = null;
            CRLF = "\r\n";


            try (
                 Socket socket = isSSL ? serverSSL.accept() : serverNotSSL.accept();
                 InputStreamReader is = new InputStreamReader(socket.getInputStream(), "UTF-8");
                 OutputStream os = socket.getOutputStream();
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            ) {
                try {
                    ProxyCall.createProxy(isSSL);
                    httpRequestCatcher = new HttpRequestCatcher(is);

                    for (String key : httpRequestCatcher.getHeader().keySet()) {
                        System.out.println(String.format("%s: %s", key, httpRequestCatcher.getHeader().get(key)));
                    }


                    System.out.println();
                    System.out.println(httpRequestCatcher.getBody());

                    httpResponse = new HttpResponse(os);
                    ;
                    HttpRequestSender httpRequestSender = null;

                    try (

                            Socket socket1 = httpRequestCatcher.getToServerURl().startsWith("https://") ? SSLSocketFactory.getDefault().createSocket(httpRequestCatcher.getToServerDomain(), 443) : new Socket(httpRequestCatcher.getToServerDomain(), 80);

                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket1.getInputStream(), "UTF-8"));
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
                    ) {

                        httpRequestSender = new HttpRequestSender(writer, httpRequestCatcher);
                        //httpRequestSender.getHeader().remove("Accept-Encoding");
                        httpRequestSender.sendRequest();
                        httpResponse.createResponse(reader);
                        httpResponse.sendResponse();

                        System.out.println(httpResponse.getHeader());
                        System.out.println(httpResponse.getBody());
                        //StringBuilder data=new StringBuilder()
                /*
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println(line);
                    bw.write(line + CRLF);
                    bw.flush();
                    //data.append(line+CRLF);
                }*/
                        //bw.flush();
                        //bw.write(String.join(CRLF, reader.lines().toList()));
                        //bw.write(data.toString());
                        //System.out.println(data.toString());
                    } catch (Exception e) {
                        throw e;


                    } finally {
                        if (httpRequestSender != null) {
                            httpRequestSender.CloseBufferedWriter();
                        }
                    }

                } catch (Exception e) {
                    //bw.write("400 Bad Request");
                    //bw.flush();
                    throw e;

                }
            } finally {
                if (httpRequestCatcher != null) {
                    httpRequestCatcher.CloseBufferedReader();
                }
                if (httpResponse != null) {
                    httpResponse.CloseBufferedWriter();
                }
            }

            System.out.println("<<< end");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean getIsSSL() {
        return isSSL;
    }

    public void setIsSSL(boolean SSL) {
        this.isSSL = SSL;
    }
}
