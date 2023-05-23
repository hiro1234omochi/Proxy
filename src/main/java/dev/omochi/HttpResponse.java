package dev.omochi;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.omochi.Proxy.CRLF;

public class HttpResponse{
    private LinkedHashMap<String, String> header = new LinkedHashMap<>();
    private String body;
    private BufferedWriter os2;
    private OutputStream os;
    public HttpResponse(OutputStream os) throws IOException {

        //try(
        this.os2 = new BufferedWriter(new OutputStreamWriter(os));

        //) {

        //}
    }
    public void createResponse(BufferedReader br) throws IOException {
        setHeader(readHeader(br));
        setBody(readBody(br));
    }
    public void CloseBufferedWriter() throws IOException {
        os2.close();
    }
    public LinkedHashMap<String, String> getHeader(){
        return header;
    }
    public String getBody(){
        return body;
    }
    public void setHeader(LinkedHashMap<String, String> header ){
        this.header=header;
    }
    public void setBody(String body){
        this.body=body;
    }
    public void sendResponse() throws IOException {
        String ResponseMessage="";

        StringBuilder HeaderText=new StringBuilder();

        this.header.put("Content-Length",Integer.toString(body.getBytes().length));
        this.header.remove("Transfer-Encoding");
        for(String key:this.header.keySet()){
            if(key.equals("request")) {
                HeaderText.append(this.header.get(key)).append(CRLF);
            }else{
                HeaderText.append(String.format("%s: %s", key, this.header.get(key))).append(CRLF);
            }
        }
        ResponseMessage=HeaderText.toString();
        ResponseMessage+=CRLF;
        ResponseMessage+=body;


        os2.write(ResponseMessage);
        os2.flush();
    }
    private LinkedHashMap<String, String> readHeader(BufferedReader in) throws IOException {
        LinkedHashMap<String, String> header=new LinkedHashMap<>();
        String line = in.readLine();
        int ContentLength=0;
        Pattern pattern = Pattern.compile("(.+): (.+)");
        header.put("request",line);
        line=in.readLine();
        while(!(line==null || line.isEmpty())){//空でないなら
            Matcher matcher = pattern.matcher(line);
            if(matcher.find()) {
                header.put(matcher.group(1), matcher.group(2));
            }
            line=in.readLine();
        }
        return header;
    }
    private String readBody(BufferedReader in) throws IOException{
        if(this.header.containsKey("Content-Length")){
            return readBodyContentLength(in);
        }else if(this.header.containsKey("Transfer-Encoding") && this.header.get("Transfer-Encoding").equals("chunked")){//短絡評価
            return readBodyChunkedTransfer(in);
        }else{
            return "";
        }
    }
    private String readBodyContentLength(BufferedReader in) throws IOException{
        StringBuilder body= new StringBuilder(new String());

        if(!this.header.containsKey("Content-Length")){
            return new String();
        }
        char c;
        while(body.toString().getBytes("UTF-8").length!=Integer.parseInt(this.header.get("Content-Length"))){
            if(!in.ready()){
                break;
            }
            c=(char)in.read();
            body.append(c);

        }
        return body.toString();
    }
    private String readBodyChunkedTransfer(BufferedReader in) throws IOException {
        StringBuilder body=new StringBuilder();
        long ChunkSize;
        String line;
        StringBuilder ReadLine;
        while(true) {
            ChunkSize = Long.parseLong(in.readLine(), 16);
            ReadLine = new StringBuilder();
            while (ReadLine.toString().getBytes("UTF-8").length != ChunkSize) {
                char c;
                if (!in.ready()) {
                    break;
                }
                c = (char) in.read();
                ReadLine.append(c);
            }
            ReadLine.append("\n");


            body.append(ReadLine);
            in.readLine();//改行は飛ばす
            if (!in.ready()) {
                break;
            }

        }
        return body.toString();
    }
}
