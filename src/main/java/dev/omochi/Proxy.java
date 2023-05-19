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

                            Socket socket1 = httpRequestCatcher.getToServerURl().contains("https://") ? new Socket(httpRequestCatcher.getToServerDomain(), 80) : SSLSocketFactory.getDefault().createSocket(httpRequestCatcher.getToServerDomain(), 443);

                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket1.getInputStream(), "UTF-8"));
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
                    ) {

                        writer.write("GET / HTTP/1.1\r\n" +
                                "Host: knowledge.sakura.ad.jp/\r\n" +
                                "Connection: keep-alive\r\n" +
                                "Pragma: no-cache\r\n" +
                                "Cache-Control: no-cache\r\n" +
                                "sec-ch-ua: \"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"\r\n" +
                                "sec-ch-ua-mobile: ?0\r\n" +
                                "sec-ch-ua-platform: \"Windows\"\r\n" +
                                "Upgrade-Insecure-Requests: 1\r\n" +
                                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36\r\n" +
                                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,;q=0.8,application/signed-exchange;v=b3;q=0.7\r\n" +
                                "Sec-Fetch-Site: none\r\n" +
                                "Sec-Fetch-Mode: navigate\r\n" +
                                "Sec-Fetch-User: ?1\r\n" +
                                "Sec-Fetch-Dest: document\r\n" +
                                "Accept-Encoding: gzip, deflate, br\r\n" +
                                "Accept-Language: ja,en-US;q=0.9,en;q=0.8\r\n" +
                                "\r\n" +
                                "\r\n");
                        writer.flush();

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
