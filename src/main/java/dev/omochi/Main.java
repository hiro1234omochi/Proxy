package dev.omochi;
import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;


public class Main {
    public static ServerSocket serverNotSSL;
    public static ServerSocket serverSSL;
    public static void main(String[] args) throws Exception {
        String key_password="omochi";
        String key_path="C:\\Users\\hirom\\key2.pfx";
        //String TrustPass="omochi";
        //String TrustPath="C:\\Users\\hirom\\key.pfx";
        char[] key_pass_char = key_password.toCharArray();
        KeyStore key_store = KeyStore.getInstance("JKS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

        key_store.load(new FileInputStream(key_path), key_pass_char );
        kmf.init(key_store, key_pass_char);

        //TrustStoreの読み込み
        /*char[] trust_pass_char =TrustPass.toCharArray();;
        KeyStore trust_store = KeyStore.getInstance("JKS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

        trust_store.load(new FileInputStream(TrustPath), trust_pass_char);
        tmf.init(trust_store);
        */
        KeyManager[] key_managers = kmf.getKeyManagers();
        //TrustManager[] trust_managers = tmf.getTrustManagers();

        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(key_managers, null, null);//trust_managers-->null
        serverSSL=sslcontext.getServerSocketFactory().createServerSocket();
        serverSSL.bind(new InetSocketAddress("localhost",18443));
        serverNotSSL=new ServerSocket(18080);
        ProxyCall.ProxyStart();

    }
}