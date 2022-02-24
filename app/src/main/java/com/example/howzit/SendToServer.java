/*
This file is part of Howzit.
Copyright (C) 2020-2021 Mauka-Makai team: Nickolas Naydenov, Borko Smolovic, Iacopo Marri,
Viking Forsman, Matteo Cecini, Erblin Isaku, Alessandro Bertulli

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; version 2.1.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, see <https://www.gnu.org/licenses>.

Linking Howzit statically or dynamically with other modules is making a combined work
based on Howzit. Thus, the terms and conditions of the GNU Lesser General Public License
cover the whole combination.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of AndroidX under the Apache v.2.0 License
(or modified versions of such code, with unchanged license). You may copy and distribute such a system
following the terms of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of Material Components for Android under the Apache v.2.0 License
(or modified versions of such code, with unchanged license). You may copy and distribute such a system
following the terms of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of Lottie under the Apache v.2.0 License (or modified versions
of such code, with unchanged license). You may copy and distribute such a system following the terms
of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.
 */
package com.example.howzit;

import android.app.PendingIntent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.example.howzit.messages.DropMessage;
import com.example.howzit.messages.Message;
import com.example.howzit.messages.PublishMessage;
import com.example.howzit.messages.QueryMessage;
import com.example.howzit.messages.QueryReplyMessage;
import com.example.howzit.messages.SignUpMessage;
import com.example.howzit.messages.SignUpReplyMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static android.content.Context.WIFI_SERVICE;

//This class implements Singleton design pattern. Use getInstance() to get the object, instead of constructor.
//public class SendToServer extends AsyncTask<String, byte[], Boolean> {
public class SendToServer implements Runnable {

   // private static SendToServer instance = null;

    private static final String serverIP = "10.8.0.1";//"213.191.194.44"; // "192.168.1.19";
    private static final int serverPort = 4444;
    private Socket serverSocket;
    private ObjectOutputStream serverOutput;
    private ObjectInputStream serverInput;
    private Message message;
    private String reply;

    //Make private if want to make singletone
    public SendToServer(Message m) {
        this.message = m;
        this.reply = null;
    }

    //Use this if want to make sigletone
    /*public static SendToServer getInstance(Message m) {
        if(instance == null) instance = new SendToServer();
        instance.message = m;
        instance.reply = null;
        return instance;
    }*/

    public String getReply(){ return reply;}


   public void run() {
        try {


            System.out.println("CONNECTING TO THE SERVER..");
            serverSocket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(serverIP, serverPort);

            //connect to the server,with 1sec timeout for connecting
            serverSocket.connect(socketAddress, 1000);
            //set timeout for read operation
            serverSocket.setSoTimeout(2000);

            System.out.println("CONNECTED TO THE SERVER!");

            InetAddress senderInetAddress = serverSocket.getLocalAddress();
            String senderIP = (senderInetAddress.toString()).substring(1);


            if (serverSocket.isConnected()) {
                //ObjectStream created
                serverOutput = new ObjectOutputStream(serverSocket.getOutputStream());
                serverInput = new ObjectInputStream(serverSocket.getInputStream());
            }
            else {
                return;
            }

            //---------------------------- Sending the message ---------------------------------------
            //TODO: implement encryption before sending
            if (message.getType() == 1) {
                PublishMessage publishMessage = (PublishMessage) message;
                publishMessage.setSenderIP(senderIP);
                serverOutput.writeObject(publishMessage);
                serverOutput.flush();
                serverSocket.close();
                return;
            } else if (message.getType() == 2) {
                QueryMessage queryMessage = (QueryMessage) message;
                serverOutput.writeObject(queryMessage);
                serverOutput.flush();
                //wait for reply
                //ContactslistActivity.SendToServerThread.ReceiveFromServerThread recv = new ContactslistActivity.SendToServerThread.ReceiveFromServerThread();
                //recv.run();
            } else if (message.getType() == 4) {
                SignUpMessage signUpMessage = (SignUpMessage) message;
                serverOutput.writeObject(signUpMessage);
                serverOutput.flush();
            } else if (message.getType() == 6) {
                DropMessage dropMessage = (DropMessage) message;
                serverOutput.writeObject(dropMessage);
                serverOutput.flush();
                serverSocket.close();
                return;
            }



            //---------------------------- Waiting for replay (if requested) ------------------------
            Message msg = (Message) serverInput.readObject();

            if (msg.getType() == 3) {//QueryReply Message
                QueryReplyMessage queryReplyMessage = (QueryReplyMessage) msg;
                reply = queryReplyMessage.getRequestedIP();
            }
            else if(msg.getType() == 5) {//SignUpReply message

                SignUpReplyMessage signUpReplyMessage = (SignUpReplyMessage) msg;
                reply = signUpReplyMessage.getNewID();
            }
                System.out.println("REPLY: " + reply);
                serverOutput.close();
                serverInput.close();
                serverSocket.close();
                System.out.println("CLOSED SOCKET AND STREAMS WITH SERVER");
                return;

        }catch (SocketTimeoutException e){
            Log.e("Server error", "Server not responding");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    }
