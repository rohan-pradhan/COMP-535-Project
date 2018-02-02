package socs.network.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by RohanPradhan on 1/31/18.
 */
public class ServerThread implements Runnable {

    private Router r;
    private short portNumber;


    public ServerThread(Router aRouter){
        r = aRouter;
        portNumber = r.rd.processPortNumber;

    }

    public void run(){
//        System.out.println("Port Number: " + portNumber);
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber); // create new socket to accept requests on

            ExecutorService threadPool  = Executors.newCachedThreadPool(); // dynamic thread pool to spin off requests into their own thread

            while(1==1){

                Socket incomingMessagesSocket = serverSocket.accept();
                ServerTaskThread handleRequest = new ServerTaskThread(r,incomingMessagesSocket); // create new serever task handler instance

                threadPool.submit(handleRequest); //pass server task thread object into dynamic thread pool

            }

        }

        catch(IOException e){
            e.printStackTrace();
        }


    }




}
