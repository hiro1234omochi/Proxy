package dev.omochi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyCall {
    public static void ProxyStart() {
        createProxy(true);
        createProxy(false);
    }
    public static void createProxy(boolean isSSL){
        if(isSSL){
            ExecutorService exec1 = Executors.newSingleThreadExecutor();
            Proxy SSL = new Proxy();
            SSL.setIsSSL(true);
            exec1.submit(SSL);
        }else{
            ExecutorService exec2 = Executors.newSingleThreadExecutor();
            Proxy NotSSL = new Proxy();
            NotSSL.setIsSSL(false);
            exec2.submit(NotSSL);
        }
    }
}
