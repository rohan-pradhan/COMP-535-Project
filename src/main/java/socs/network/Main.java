package socs.network;

import socs.network.node.Router;
import socs.network.node.ServerTaskThread;
import socs.network.node.ServerThread;
import socs.network.util.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("usage: program conf_path");
      System.out.println("Go chen");
      System.exit(1);
    }

    Router r = new Router(new Configuration(args[0]));

//    ServerThread server = new ServerThread(r);
    ExecutorService serverPool = Executors.newFixedThreadPool(2);
    serverPool.submit(new ServerThread(r));
//    Thread server = new Thread(new ServerThread(r));
//    server.start();
//    System.out.println("Hi");
    r.terminal();

  }
}
