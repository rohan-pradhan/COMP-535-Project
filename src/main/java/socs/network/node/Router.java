package socs.network.node;

import socs.network.message.SOSPFPacket;
import socs.network.util.Configuration;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Router {

  public static final short HELLO_MESSAGE = 0;
  public static final short LINK_STATE_UPDATE_MESSAGE = 1;

  protected LinkStateDatabase lsd;

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4];

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    rd.processPortNumber = config.getShort("socs.network.router.port");
    lsd = new LinkStateDatabase(rd);
    
    try {
		rd.processIPAddress = InetAddress.getLocalHost().getHostAddress();
		System.out.println("Physical IP Address: "+  rd.processIPAddress);
		System.out.println("Accepting Socket Connections on port: " + rd.processPortNumber);
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		System.out.println("IP Address error. Please try again. Currently setting IP address to -1");
		rd.processIPAddress="-1";
	}
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP, short weight) {
	  RouterDescription routerToConnect = new  RouterDescription();
	  routerToConnect.processIPAddress=processIP;
	  routerToConnect.processPortNumber=processPort;
	  routerToConnect.simulatedIPAddress=simulatedIP;

	  for (Link l : ports){
	      if (l !=null) {
              if (l.router2.simulatedIPAddress.equals(simulatedIP)) {
                  System.out.println("Router already attached!");
                  return;
              }
          }
      }
	  
	  int portNumber = findFreePort();
	  if (portNumber == -1){
		  System.out.println("Error! All ports in use");
		  return;
	  }
      try {
		Socket socket = new Socket(routerToConnect.processIPAddress, routerToConnect.processPortNumber);
		System.out.println("Validating Attachment");
		socket.close();



	} catch (Exception e) {

		System.err.println("Cannot connect to given port/address. Please try again.");
		System.out.println("");
//		System.out.println(">>");
		return;
	}
      System.out.print("Router to connect validated!\n");
	  ports[portNumber] = new Link(rd, routerToConnect);
	  
//
	  

  }
  
  protected int findFreePort(){
	  for(int i =0; i<ports.length; i++){
		  if (ports[i] ==null){
			  return i;
		  }
	  }
	  return -1;
  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {

      for (Link neighbor : ports) {
          if (neighbor != null) {
              if (neighbor.router2.status == null) {
                  SOSPFPacket message = new SOSPFPacket();


                  message.srcProcessIP = neighbor.router1.processIPAddress;
                  message.srcProcessPort = neighbor.router1.processPortNumber;
                  message.srcIP = neighbor.router1.simulatedIPAddress;
                  message.dstIP = neighbor.router2.simulatedIPAddress;

                  String destinationProcessIP = neighbor.router2.processIPAddress;
                  short destinationProcessPort = neighbor.router2.processPortNumber;
                  System.out.println("DestinationProcessIP: " + destinationProcessIP);
                  System.out.println("DestinationProcessPort: " + destinationProcessPort);

                  message.sospfType = HELLO_MESSAGE;
                  message.routerID = neighbor.router1.simulatedIPAddress;
                  message.neighborID = neighbor.router2.simulatedIPAddress;

                  try {
                      Socket newConnection = new Socket(destinationProcessIP, destinationProcessPort);
                      ObjectOutputStream outStream = new ObjectOutputStream(newConnection.getOutputStream());
                      ObjectInputStream inStream = new ObjectInputStream(newConnection.getInputStream());
                      outStream.writeObject(message);

                      Object acknowledgementMessageObject;

                      try {
                          acknowledgementMessageObject = inStream.readObject();
                          SOSPFPacket acknowledgementMessage;

                          if (acknowledgementMessageObject instanceof SOSPFPacket) {
                              acknowledgementMessage = (SOSPFPacket) acknowledgementMessageObject;
                              if (acknowledgementMessage.sospfType == HELLO_MESSAGE) {
                                  System.out.println("received HELLO from " + acknowledgementMessage.srcIP);
                                  neighbor.router1.status = RouterStatus.TWO_WAY;
                                  neighbor.router2.status = RouterStatus.TWO_WAY;
                                  System.out.println("Set " + acknowledgementMessage.srcIP + " to TWO WAY");
//                                  lsd._store.put()

                                  outStream.writeObject(message);
                                  closeSockets(newConnection, outStream, inStream);

                              }

                          }


                      } catch (Exception e) {
                          System.out.println("Danger");
                      }


                  } catch (IOException e) {
                      e.printStackTrace();

                      System.out.println("Connection Refused!");

                  }


              }

          }
      }
  }

  private void closeSockets(Socket tempClientSocket, ObjectOutputStream outStream, ObjectInputStream inputStream) throws IOException{
      if (inputStream!=null) inputStream.close();
      if (outStream!=null) outStream.close();
      tempClientSocket.close();

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP, short weight) {

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
      int i =1;
      for (Link l : ports ){
          if (l !=null){
              System.out.println("IP Address of neighbor " + i + " is: "+ l.router2.simulatedIPAddress+". The router status is: "+ l.router2.status);
              i++;
          }
      }

  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
