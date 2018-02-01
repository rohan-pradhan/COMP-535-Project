package socs.network.node;

import socs.network.message.SOSPFPacket;

import java.io.*;
import java.net.Socket;

/**
 * Created by RohanPradhan on 1/31/18.
 */
public class ServerTaskThread implements Runnable {
    private Router r;
    private int portNumber;
    private Socket inputSocket;

    public static final short HELLO_MESSAGE = 0;
    public static final short LINK_STATE_UPDATE_MESSAGE = 1;


    public ServerTaskThread(Router aRouter, Socket aSocket){
        r = aRouter;
        portNumber = r.rd.processPortNumber;
        inputSocket = aSocket;
    }

    public void run(){

        try{
            OutputStream outputStream = inputSocket.getOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(outputStream);

            InputStream inputStream = inputSocket.getInputStream();
            ObjectInputStream objectInput = new ObjectInputStream(inputStream);

            Object newRequestObject = objectInput.readObject();
            SOSPFPacket newRequestMessage;

            if (newRequestObject instanceof SOSPFPacket){
                newRequestMessage = (SOSPFPacket) newRequestObject;

                if(newRequestMessage.sospfType == HELLO_MESSAGE){

                    if (checkNoDuplicateIP(newRequestMessage.srcIP)){
                        portNumber = r.findFreePort();
                        RouterDescription routerToConnect = new RouterDescription();
                        routerToConnect.simulatedIPAddress = newRequestMessage.srcIP;
                        routerToConnect.processPortNumber = newRequestMessage.srcProcessPort;
                        routerToConnect.processIPAddress = newRequestMessage.srcProcessIP;
                        Link toAdd = new Link(r.rd, routerToConnect);
                        r.ports[portNumber] = toAdd;
                    }
                    System.out.println("");
                    System.out.println("received HELLO from " + newRequestMessage.srcIP);
                    changeRouterStatus(RouterStatus.INIT);
                    System.out.println("set " + newRequestMessage.srcIP + " state to INIT");

                    SOSPFPacket message = new SOSPFPacket();
                    message.srcProcessIP = r.rd.processIPAddress;
                    message.srcProcessPort = r.rd.processPortNumber;
                    message.srcIP = r.rd.simulatedIPAddress;
                    message.dstIP = r.ports[portNumber].router2.simulatedIPAddress;


                    message.sospfType = HELLO_MESSAGE;
                    message.routerID = r.rd.simulatedIPAddress;
                    message.neighborID = r.ports[portNumber].router2.simulatedIPAddress;

                    objectOutput.writeObject(message);

                    Object confirmationObject = objectInput.readObject();
                    SOSPFPacket confirmationMessage = (SOSPFPacket) confirmationObject;

                    if (confirmationMessage.sospfType == HELLO_MESSAGE){
                        System.out.println("received HELLO from " + confirmationMessage.srcIP);
                        changeRouterStatus(RouterStatus.TWO_WAY);
                        System.out.println("set " + confirmationMessage.srcIP + " state to TWO_WAY");
                        inputStream.close();
                        outputStream.close();
                        objectOutput.close();
                        objectInput.close();
                        inputSocket.close();
                        System.out.println(">>");

                    }









                }

            }





        } catch (Exception e) {

        }

    }

    public boolean checkNoDuplicateIP(String ip){
        for (Link l : r.ports){
            if (l!=null){
                if (l.router2.simulatedIPAddress.equalsIgnoreCase(ip)){
                    return false;
                }
            }
        }
        return true;
    }

    public void changeRouterStatus(RouterStatus status){
        r.ports[portNumber].router1.status = status;
        r.ports[portNumber].router2.status = status;

    }
}
