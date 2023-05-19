package dev.omochi;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static dev.omochi.Proxy.CRLF;

public class HttpRequestSender {
    private LinkedHashMap<String, String> header = new LinkedHashMap<>();
    private String body;
    private BufferedWriter bw;
    private  HttpRequestCatcher httpRequestCatcher;
    public HttpRequestSender(BufferedWriter bw,HttpRequestCatcher httpRequestCatcher) throws IOException {

        //try(
        this.bw = bw;

        //) {
        this.header=httpRequestCatcher.getHeader();
        this.body=httpRequestCatcher.getBody();
        this.httpRequestCatcher=httpRequestCatcher;
        setToServerDirectory(httpRequestCatcher.getToServerDirectory());
        setHost(httpRequestCatcher.getToServerDomain());
        //}
    }
    public void CloseBufferedWriter() throws IOException {
        bw.close();
    }
    public LinkedHashMap<String, String> getHeader(){
        return header;
    }
    public String getBody(){
        return body;
    }
    public void setToServerDirectory(String directory) {
        Pattern pattern = Pattern.compile("(?<= )[^ ]+(?= )");
        Matcher matcher= pattern.matcher(header.get("request"));
        matcher.find();
        header.put("request",matcher.replaceAll(directory));
    }
    public void setHost(String host){
        header.put("Host",host);
        /*
        if(httpRequestCatcher.getToServerURl().contains("https")){
            header.put(":Authority", host);
            if(!httpRequestCatcher.getIsMyServerSSL()) {
                String[] request = header.get("request").split(" ");
                header.put(":Method", request[0]);
                header.put(":Path", request[1]);
                header.put(":Scheme", "https");
                List requestList= new ArrayList(List.of(request));
                requestList.remove(2);
                requestList.add("HTTP/1.1");
                header.put("request",String.join(" ",requestList));
            }
        }else{
            header.put("Host",host);
            if(httpRequestCatcher.getIsMyServerSSL()){
                ;
            }
        }*/
    }
    public void setHeader(LinkedHashMap<String, String> header){
        this.header=header;
    }
    public void sendRequest() throws IOException {
        String RequestSendMessage="";

        StringBuilder HeaderText=new StringBuilder();

        this.header.put("Content-Length",Integer.toString(body.getBytes().length));
        this.header.remove("Accept-Encoding");
        for(String key:this.header.keySet()){
            if(key.equals("request")) {
                HeaderText.append(this.header.get(key)).append(CRLF);
            }else{
                HeaderText.append(String.format("%s: %s", key, this.header.get(key))).append(CRLF);
            }
        }

        RequestSendMessage=HeaderText.toString();
        RequestSendMessage+=CRLF;
        RequestSendMessage+=body;

        System.out.println(RequestSendMessage);
        bw.write(RequestSendMessage);
        bw.flush();
    }
}
