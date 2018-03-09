package socs.network.node;

import socs.network.message.LSA;
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
            //create stream to read and write from
            OutputStream outputStream = inputSocket.getOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(outputStream);

            InputStream inputStream = inputSocket.getInputStream();
            ObjectInputStream objectInput = new ObjectInputStream(inputStream);

            Object newRequestObject = objectInput.readObject();
            SOSPFPacket newRequestMessage;

            if (newRequestObject instanceof SOSPFPacket){//validate that object being read in is not corrupted and is of type SOSPFPacket
                newRequestMessage = (SOSPFPacket) newRequestObject;

                if(newRequestMessage.sospfType == HELLO_MESSAGE){ //ensure message being received is of hello message

                    if (checkNoDuplicateIP(newRequestMessage.srcIP)){ //ensure that message is not already inside link array of router
                        portNumber = r.findFreePort();
                        RouterDescription routerToConnect = new RouterDescription();
                        routerToConnect.simulatedIPAddress = newRequestMessage.srcIP;
                        routerToConnect.processPortNumber = newRequestMessage.srcProcessPort;
                        routerToConnect.processIPAddress = newRequestMessage.srcProcessIP;
                        Link toAdd = new Link(r.rd, routerToConnect);
                        r.ports[portNumber] = toAdd;
                    }
                    System.out.println("");
                    //print to console
                    System.out.println("received HELLO from " + newRequestMessage.srcIP);
                    changeRouterStatus(RouterStatus.INIT);
                    System.out.println("set " + newRequestMessage.srcIP + " state to INIT");

                    SOSPFPacket message = new SOSPFPacket(); //create message to send acknowledgemtn of receipt of original message
                    message.srcProcessIP = r.rd.processIPAddress;
                    message.srcProcessPort = r.rd.processPortNumber;
                    message.srcIP = r.rd.simulatedIPAddress;
                    message.dstIP = r.ports[portNumber].router2.simulatedIPAddress;


                    message.sospfType = HELLO_MESSAGE;
                    message.routerID = r.rd.simulatedIPAddress;
                    message.neighborID = r.ports[portNumber].router2.simulatedIPAddress;

                    objectOutput.writeObject(message); //send message acknowledgemnt

                    Object confirmationObject = objectInput.readObject();
                    SOSPFPacket confirmationMessage = (SOSPFPacket) confirmationObject; //wait for acknowledgement that original router received your message

                    if (confirmationMessage.sospfType == HELLO_MESSAGE){ // if message is hello set router communication to two way and close sockets
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









                } else if (newRequestMessage.sospfType == LINK_STATE_UPDATE_MESSAGE){
                    LSA oldLSARequest = r.lsd._store.get(newRequestMessage.srcIP);
                    if (checkLSAUpdateSequenceNumber(newRequestMessage.lsaArray.lastElement(), oldLSARequest)){
                        r.lsd._store.put(newRequestMessage.srcIP, newRequestMessage.lsaArray.lastElement());

                        r.processUpdate(newRequestMessage, newRequestMessage.srcIP);
                    }
                    else {
                        System.err.println("Error!");
                    }




                }
                else {
                    System.err.println("Error!");
                }


            }





        } catch (Exception e) {
            System.err.println("Error!");


        }

    }

    public boolean checkLSAUpdateSequenceNumber(LSA newLSA, LSA oldLSA){
        if (oldLSA == null || newLSA.lsaSeqNumber > oldLSA.lsaSeqNumber){
            return true;
        } else{
            return false;
        }
    }

    public boolean checkNoDuplicateIP(String ip){ //checks if there is no router with dupliate simulated IP to prevent multiple links with same router
        for (Link l : r.ports){
            if (l!=null){
                if (l.router2.simulatedIPAddress.equalsIgnoreCase(ip)){
                    return false;
                }
            }
        }
        return true;
    }

    public void changeRouterStatus(RouterStatus status){ // changes both routhers in link array to status passed in
        r.ports[portNumber].router1.status = status;
        r.ports[portNumber].router2.status = status;

    }
}
