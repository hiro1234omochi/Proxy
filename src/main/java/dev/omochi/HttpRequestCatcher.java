package dev.omochi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequestCatcher {
    private LinkedHashMap<String, String> header = new LinkedHashMap<>();
    private String body;
    private BufferedReader in;
    //private String MyServerURL;
    private String ToServerURl;
    private String ToServerDomain;
    private String ToServerDirectory;
    private boolean isMyServerSSL;
    public HttpRequestCatcher(InputStreamReader is) throws IOException {

        //try(
            this.in = new BufferedReader(is);

        //) {

            this.header= readHeader(in);
            //this.MyServerURL="http://"+this.header.get("Host");


            Pattern pattern = Pattern.compile("\\?url=(.*?) ");
            Matcher matcher = pattern.matcher(this.header.get("request"));

            matcher.find();

            System.out.println(this.header.get("request"));

            this.ToServerURl= new String(Base64.getDecoder().decode(matcher.group(1)));


            Pattern pattern2 = Pattern.compile("https?://([^/]+?)/");
            Matcher matcher2 = pattern2.matcher(ToServerURl);
            matcher2.find();
            this.ToServerDomain=matcher2.group(1);

            Pattern pattern3 = Pattern.compile("https?://[^/]*?(/.*$)");
            Matcher matcher3 = pattern3.matcher(ToServerURl);
            matcher3.find();
            this.ToServerDirectory=matcher3.group(1);

            this.body= readBody(in);
        //}
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
        while(true){
            ChunkSize=Long.parseLong(in.readLine(), 16);
            ReadLine=new StringBuilder();
            while(ReadLine.toString().getBytes("UTF-8").length==ChunkSize){
                char c;
                if(!in.ready()){
                    break;
                }
                c=(char)in.read();
                ReadLine.append(c);
            }
            ReadLine.append("\n");
            if(!in.ready()){
                break;
            }
            in.readLine();//改行は飛ばす
            body.append(ReadLine);
        }
        return body.toString();
    }
    public void CloseBufferedReader() throws IOException {
        in.close();
    }
    public LinkedHashMap<String, String> getHeader(){
        return header;
    }
    public String getBody(){
        return body;
    }

    /*
    public String getMyServerURL() {
        return MyServerURL;
    }*/

    public String getToServerDomain() {
        return ToServerDomain;
    }

    public String getToServerDirectory() {
        return ToServerDirectory;
    }

    public String getToServerURl() {
        return ToServerURl;
    }

    public void setIsMyServerSSL(boolean myServerSSL) {
        isMyServerSSL = myServerSSL;
    }

    public boolean getIsMyServerSSL() {
        return isMyServerSSL;
    }
}
