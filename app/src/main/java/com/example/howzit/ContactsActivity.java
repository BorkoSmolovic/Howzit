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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howzit.messages.EncryptedMessage;
import com.example.howzit.messages.IntroductionMessage;
import com.example.howzit.messages.Message;
import com.example.howzit.messages.PublishMessage;
import com.example.howzit.messages.QueryMessage;
import com.example.howzit.messages.TextMessage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.example.howzit.SignUp.convertToBytes;
import static com.example.howzit.SignUp.encodeFile;
import static com.example.howzit.SignUp.generateKey;
import static java.security.MessageDigest.*;


public class ContactsActivity extends AppCompatActivity {

    private static final int LISTENINGPORT = 6000;
    //time between 2 KeepAliveMessages in millisec
    private static final int KEEPALIVE_PERIOD = 10000;

    private static User u;
    public static User getUser(){ return u; }

    //ContactsAdapter needs an array list to be initialized. this will be accessed by position, referring to the contact that has been clicked.
    private static ArrayList<Contact> contacts;

    /**
     * Methods used in CommunicationActivity for accessing contacts
     * @param msg
     * @param contactPos
     */
    public static void _addMessage(TextMessage msg, int contactPos) {
        contacts.get(contactPos).addMessage(msg);
    }

    public static TextMessage _getConversationMessage(int contactPos, int index) {
        return contacts.get(contactPos).getMessage(index);
    }

    public static void _setNewMessage(int pos){
        contacts.get(pos).setNewMessage(false);
    }

    //Used in MessageListAdapter, to get the name from the ID. Will be easier when contacts hashmap will be indicized on ID.
    public static String _getName(int senderID){
        String contactName = null;
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getID() == senderID) {
                contactName = contacts.get(i).getName();
            }
        }
        return contactName;
    }

    //List of sockets, input, and output streams, one for each contact. To keep them in class Contact would broke the storage.
    private static ArrayList<Socket> sockets;
    private static ArrayList<ObjectOutputStream> outputs;
    private static ArrayList<ObjectInputStream> inputs;

    public static Socket getSocket(int pos){return sockets.get(pos);}
    public static ObjectOutputStream getOutput(int pos){return outputs.get(pos);}
    public static ObjectInputStream getInput(int pos){return inputs.get(pos);}

    //RecyclerView is the component that shows and scrolls the contactslist.
    private RecyclerView contactsView;
    //Used to adapt each single Contact item into contactsView
    private static ContactsAdapter adapter;
    public static ContactsAdapter getAdapter() { return adapter; }

    //Observer to be registered as receiver of the broadcast.
    private CommunicationActivity.Observer receiver;
    //New intent that specifies the type of notification
    private IntentFilter intentFilter;
    //Handler for posting operations
    private Handler ProcessMessageHandler;
    //Thread for listen to new connections
    Thread listeningThread;

    ServerSocket listeningSocket;


    Activity activity;
    Context context;
    Dialog dialogAddServer;
    Dialog dialogChangeName;
    Dialog dialogEditContact;
    String contactUsername;
    Integer contactIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);

        //get user from global storage so we can use his contacts, private and public keys etc.
        u = ((LoggedUser) this.getApplication()).getLoggedUser();

        //initialize Handler
        ProcessMessageHandler = new Handler();
        activity = this;
        context = this;

        //initialize ArrayLists
        contacts = new ArrayList<Contact>();
        sockets = new ArrayList<Socket>();
        outputs = new ArrayList<ObjectOutputStream>();
        inputs = new ArrayList<ObjectInputStream>();

        //get contacts from user we have to display
        for (Contact c : u.getContacts().values()) {
            c.setOnline(false);
            contacts.add(c);
            sockets.add(null);
            outputs.add(null);
            inputs.add(null);
        }

        // Lookup the recyclerview in activity layout
        contactsView = (RecyclerView) findViewById(R.id.rvContacts);

        // Attach the adapter to the recyclerview to populate items
        adapter = new ContactsAdapter(contacts);
        contactsView.setAdapter(adapter);

        // Set layout manager to position the items
        contactsView.setLayoutManager(new LinearLayoutManager(this));

        //Decorate with separator lines between contacts
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        contactsView.addItemDecoration(itemDecoration);


        //----Add a click listener to the ReciclerView of contacts.----
        //Class from their github repo: https://gist.github.com/nesquena/231e356f372f214c4fe6, and
        ItemClickSupport.addTo(contactsView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView rv, int position, View v) {
                int contactID = contacts.get(position).getID();

                //Start task to connect to contact
                new ConnectTask().execute(((Integer)position).toString(), ((Integer)contactID).toString());

                Intent chatIntent = new Intent(ContactsActivity.this, CommunicationActivity.class);
                chatIntent.putExtra("contactID", contactID);
                chatIntent.putExtra("contactName", contacts.get(position).getName());
                chatIntent.putExtra("position", position);
                startActivity(chatIntent);
                contacts.get(position).setNewMessage(false);
            }
        });


        //----Add a LONG click listener to the ReciclerView of contacts.----
        ItemClickSupport.addTo(contactsView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {


                for(int i=0; i<contacts.size(); i++)
                    adapter.notifyItemChanged(i);

                contactUsername = contacts.get(position).getName();
                contactIndex = position;
                editContactDialog(position);
                return true;
            }
        });

        //intentFilter specifies the kind of notification in the broadcast
        intentFilter = new IntentFilter("NEW_MESSAGE");
        //adding CommunicationACtivityObserver as a receiver of the broadcast.
        receiver = new CommunicationActivity.Observer();
        registerReceiver(receiver, intentFilter);

        //To get my IP  ----deprecated, will be deleted -----
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        //Create message for publishing IP on server
        PublishMessage msg = new PublishMessage(u.getID(), ipAddress);
        //Send it to server
        Thread publish = new Thread(new SendToServer(msg));
        publish.start();

        //Try to connect to every contact in the list. This will create multiple connections with the server
        for(int pos=0; pos<contacts.size(); pos++){
            new ConnectTask().execute(((Integer)pos).toString(), ((Integer)contacts.get(pos).getID()).toString());
        }

        try {
            listeningSocket = new ServerSocket(LISTENINGPORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Starting Thread listening on port 6000 for new communications
        listeningThread = new Thread(new ListeningThread());
        listeningThread.start();
    }


    private void AddServer() {
        // Get dialog layout and prevent user from cancel by pressing outside dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.dialog_add_server, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(myView);
        builder.setCancelable(false);

        // Get confirm button, cancel button, and contact username edittext
        EditText input = (EditText) myView.findViewById(R.id.editTextServerIP);
        Button buttonConfirm = (Button) myView.findViewById(R.id.buttonConfirmDialog);
        Button buttonCancel = (Button) myView.findViewById(R.id.buttonCancelDialog);

        // Set logic for buttons
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String IP = input.getText().toString();
                Toast.makeText(ContactsActivity.this, "Add server IP: " + IP, Toast.LENGTH_LONG).show();
                dialogAddServer.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialogAddServer.dismiss();
            }
        });
        // Create the dialog set the background to transparent
        dialogAddServer = builder.create();
        dialogAddServer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogAddServer.show();
    }


    private void RemoveServer() {
        // Get dialog layout and prevent user from cancel by pressing outside dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.dialog_remove_server, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(myView);
        builder.setCancelable(false);

        // Get confirm button, cancel button, and contact username edittext
        Button buttonConfirm = (Button) myView.findViewById(R.id.buttonConfirmDialog);
        Button buttonCancel = (Button) myView.findViewById(R.id.buttonCancelDialog);

        // Get spinner and populate it with the current server IPs
        // TODO: load the actual IP addresses (stored in user?)
        Spinner spinner = myView.findViewById(R.id.spinner);
        String[] content = {"19.117.63.127", "19.117.63.94", "19.117.63.111"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ContactsActivity.this,
                android.R.layout.simple_spinner_item,
                content);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set logic for buttons
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String IP = spinner.getSelectedItem().toString();
                Toast.makeText(ContactsActivity.this, "Remove server IP: " + IP, Toast.LENGTH_LONG).show();
                dialogAddServer.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialogAddServer.dismiss();
            }
        });
        // Create the dialog set the background to transparent
        dialogAddServer = builder.create();
        dialogAddServer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogAddServer.show();
    }

    /**
     * this is called everytime finish() is called from this activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregister CommunicationActivity from the observer list
        unregisterReceiver(receiver);

        //interrupt listeningThread
        if(listeningThread.isAlive()) {
            listeningThread.interrupt();
            try {
                listeningSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Close all socket and streams
        try {
            for (int i=0; i<sockets.size();i++) {
                if (sockets.get(i) != null && !sockets.get(i).isClosed())
                    sockets.get(i).close();

                if (outputs.get(i) != null)
                    outputs.get(i).close();

                if (inputs.get(i) != null)
                    inputs.get(i).close();
            }

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void changeNameDialog() {
        // Get dialog layout and prevent user from cancel by pressing outside dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.dialog_contactname, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(myView);
        builder.setCancelable(false);

        // Get confirm button, cancel button, and contact username edittext
        EditText input = (EditText) myView.findViewById(R.id.editTextContactUsername);
        input.setText(contactUsername);
        Button buttonConfirm = (Button) myView.findViewById(R.id.buttonConfirmDialog);
        Button buttonCancel = (Button) myView.findViewById(R.id.buttonCancelDialog);

        // Set logic for buttons
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String username = input.getText().toString();
                if (username.isEmpty()) {
                    Toast.makeText(ContactsActivity.this, "please enter a username for contact", Toast.LENGTH_LONG).show();
                } else if (username.equals(contactUsername)) {
                    Toast.makeText(ContactsActivity.this, "please enter a new username for contact", Toast.LENGTH_LONG).show();
                } else if (u.getContacts().containsKey(username)) {
                    Toast.makeText(ContactsActivity.this, "Another contact with this username already exists", Toast.LENGTH_LONG).show();
                } else {
                    // Final check if contact is successfully updated
                    if (u.updateContactName(contactUsername, username)) {

                        //encrypt the storage on a separate thread
                        new Thread(() -> encryptStorage()).start();

                        contacts.remove(contactIndex);
                        contactsView.getAdapter().notifyDataSetChanged();
                        Toast.makeText(ContactsActivity.this,
                                contactUsername + " renamed to " + username, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ContactsActivity.this,
                                contactUsername + " could not be renamed to " + username, Toast.LENGTH_LONG).show();
                    }
                    dialogChangeName.dismiss();
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialogChangeName.dismiss();
            }
        });
        // Create the dialog set the background to transparent
        dialogChangeName = builder.create();
        dialogChangeName.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogChangeName.show();
    }

    private void editContactDialog(int pos) {
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.dialog_edit_contact, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(myView);
        builder.setCancelable(false);

        // Get confirm button, cancel button, and radio group for edit action
        RadioButton radioRemove = myView.findViewById(R.id.radioRemove);
        RadioButton radioRename = myView.findViewById(R.id.radioRename);
        RadioGroup radioGroup = myView.findViewById(R.id.radioGroup);
        Button buttonConfirm = myView.findViewById(R.id.buttonConfirmDialog);
        Button buttonCancel = myView.findViewById(R.id.buttonCancelDialog);

        // Set logic for buttons
        radioRemove.setOnClickListener(null);
        radioRename.setOnClickListener(null);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioRename:
                        changeNameDialog();
                        dialogEditContact.dismiss();
                        break;
                    case R.id.radioRemove:

                        if (u.removeContact(contactUsername)) {

                            new Thread(() -> encryptStorage()).start();

                            if(sockets.get(pos) != null) {
                                try {
                                    sockets.get(pos).close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            contacts.remove(pos);
                            sockets.remove(pos);
                            outputs.remove(pos);
                            inputs.remove(pos);

                            contactsView.getAdapter().notifyDataSetChanged();
                            Toast.makeText(ContactsActivity.this,
                                    contactUsername + " removed from contact list", Toast.LENGTH_LONG);
                        } else {
                            Toast.makeText(ContactsActivity.this,
                                    contactUsername + " unable to removed from contact list", Toast.LENGTH_LONG);
                        }
                        dialogEditContact.dismiss();
                        break;
                    default:
                        Log.e("Unknown ID", "editContactDialog: neither remove or rename selected");
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialogEditContact.dismiss();
            }
        });
        // Create the dialog set the background to transparent
        dialogEditContact = builder.create();
        dialogEditContact.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogEditContact.show();
    }

    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case R.id.signOut:
                ((LoggedUser) this.getApplication()).setLoggedUser(null);
                Intent myIntent = new Intent(ContactsActivity.this, SignIn.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent);
                finish();
                return true;
            case R.id.addServer:
                AddServer();
                return true;
            case R.id.removeServer:
                RemoveServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // TODO: we could compare the size of "contacts" and "u.getContacts()" before checking each object
    //  to save some computation time, but would that introduce cases where we don't update?
    @Override
    protected void onResume () {
        // Check if any user is missing from the recycler view
        boolean userHasBeenAdded = false;
        for (Contact c : u.getContacts().values()) {
            if (!contacts.contains(c)) {
                contacts.add(c);
                sockets.add(null);
                outputs.add(null);
                inputs.add(null);
                userHasBeenAdded = true;
                Toast.makeText(getApplicationContext(), c.getName(), Toast.LENGTH_SHORT).show();
            }
        }

        // Notify adapter if a change has occurred
        if (userHasBeenAdded) {

            //Try to connect to every contact in the list. This will create multiple connections with the server
            for(int pos=0; pos<contacts.size(); pos++){
                new ConnectTask().execute(((Integer)pos).toString(), ((Integer)contacts.get(pos).getID()).toString());
            }
        }

        super.onResume();
    }

    public void AddContact (View view){
        Intent myIntent = new Intent(ContactsActivity.this, AddContact.class);
        startActivity(myIntent);
    }

    //--------Task to connect to the device with IP (passed by parameters) on Port (hardcoded) -------------
    public class ConnectTask extends AsyncTask<String, byte[], Boolean> {
        private int position;
        int contactID;

        @Override
        protected Boolean doInBackground(String... params) { //This runs on a different thread
            boolean result = false; //used to change UI in OnPostExec()
            position = Integer.parseInt(params[0]);//params[0] = pos
            contactID = Integer.parseInt(params[1]);

            //get the socket for this contact
            Socket socket = sockets.get(position);

            //If socket of indexed user is null or closed, create a connection.
            if (socket == null || socket.isClosed()){
                result = connect(position, contactID);


            if(result){
                try {
                    //create and send an introduction message for introduce yourself to that contact. Without this, user should wait for 1st msg to arrive
                    IntroductionMessage introMessage = new IntroductionMessage(contacts.get(position).getID(), u.getID());
                    outputs.get(position).writeObject(introMessage);
                    outputs.get(position).flush();

                    //start thread that handle message exchanging
                    new Thread(new CommunicationThread(sockets.get(position), outputs.get(position), inputs.get(position), contactID)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             }
            }
            else result = true;

            return result;
        }


        public boolean connect(int position, int ID) {
            boolean result = false;

            //Send request for IP address
            QueryMessage queryMessage = new QueryMessage(ID);
            SendToServer query = new SendToServer(queryMessage);
            Thread queryThread = new Thread(query);
            queryThread.start();
            System.out.println("QUERY THREAD STARTED");

            try{
                //wait for query thread to end and get reply
                queryThread.join();
            } catch(Exception e){ e.printStackTrace(); }

            try {
                //get the ip from the query reply
                String IP = query.getReply();
                System.out.println("IP RECEIVED FROM SERVER: " + IP);

                //if socket is null, contact is not present on the server database. Skip also other problematic IPs
                if(IP == null || IP.equals("0.0.0.0") || IP.equals("127.0.0.1"))
                    return result;

                //TODO: maybe useless to store IP, because we ask the server every time we need it
                System.out.println("TRY TO CONNECT");

                //Connect to the user.
                Socket newSocket = new Socket(); //(IP, LISTENINGPORT);
                SocketAddress socketAddress = new InetSocketAddress(IP, LISTENINGPORT);
                //connect to the server,with 1sec timeout for connecting
                newSocket.connect(socketAddress, 1000);

                System.out.println("CONNECTED");
                //Add new socket in the list of sockets

                result = ((newSocket).isConnected() && !newSocket.isClosed());
                if (result){
                    //add sockets and streams to list
                    sockets.add(position, newSocket);
                    //Create streams for communication
                    ObjectOutputStream output = new ObjectOutputStream(sockets.get(position).getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(sockets.get(position).getInputStream());
                    outputs.add(position, output);
                    inputs.add(position, input);
                }


            }catch (SocketTimeoutException e){
                Log.e("Peer connection error", "Peer not reachable");
                e.printStackTrace();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                Log.e("nullHostException", e1.toString());
            } catch (IOException e1) {
                Log.e("IOexception", e1.toString());
                System.out.println("NOT CONNECTED");
            } catch (Exception e2){
                Log.e("Exception", e2.toString());
            }
            return result;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            if (result == false) {
                System.out.println("NOT ABLE TO CONNECT");
                contacts.get(position).setOnline(false);
            }

            if (result == true) {
                System.out.println("CONNECTED AND NOT CLOSED");
                contacts.get(position).setOnline(true);

            }
            adapter.notifyItemChanged(position);
        }
    }

    //--------Thread that listens for new connections on port 6000, hardcoded. (Blocking)-------------------
    class ListeningThread implements Runnable {


        public void run() {


            try {
                while (!Thread.currentThread().isInterrupted()) {

                        //Listen for incoming connections. (Blocking operation)
                        Socket socket = listeningSocket.accept();

                        //Create streams for communication
                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                        //Receive the introduction message from the connecting peer.
                        IntroductionMessage introMessage = (IntroductionMessage) input.readObject();

                        //Get ip from socket
                        String contactIP = socket.getInetAddress().toString();
                         //excluding first character because its a "slash"
                        contactIP = contactIP.substring(1);

                        //find out who this contact is, searching its ID
                        String contactName;
                        int contactID = introMessage.getSenderID();
                        boolean found = false;
                        for (int i = 0; i < contacts.size(); i++) {
                            if (contacts.get(i).getID() == contactID) {
                                found = true;
                                contactName = contacts.get(i).getName();
                                int pos = i;
                                System.out.println("NAME OF THE CONTACT WHO IS CONNECTING: " + contactName);
                                //Add the new socket and streams in right position
                                sockets.add(pos, socket);
                                outputs.add(pos, output);
                                inputs.add(pos, input);
                                contacts.get(pos).setIP(contactIP);// maybe useless to store IP, because we ask the server every time we need it
                                contacts.get(pos).setOnline(true);

                                //Post to main thread the UI changes.
                                ProcessMessageHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyItemChanged(pos);
                                    }
                                });
                                //start thread that handles message exchanging
                                new Thread(new CommunicationThread(socket, output, input, contactID)).start();
                            }

                        }
                        if(found == false)socket.close();


                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //------  Thread in charge of receiving msgs once connection is established. (Blocking)-----------------
    class CommunicationThread implements Runnable {

        private ObjectOutputStream output;
        private ObjectInputStream input;
        private int contactID;


        public CommunicationThread(Socket s, ObjectOutputStream oos, ObjectInputStream ois, int ID) {
            //this.socket = s;
            this.output = oos;
            this.input = ois;
            this.contactID = ID;
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    //RECEIVE THE MESSAGE OBJECT (only EncryptedMessages here)
                    EncryptedMessage encryptedMessage = (EncryptedMessage) input.readObject();
                    System.out.println("GOT A MESSAGE");

                    //Decrypting the message
                    byte[] encodedBytes = encryptedMessage.getData();
                    byte[] decodedBytes = null;
                    byte[] encodedKeyAES = encryptedMessage.getAesKey();
                    byte[] decodedKeyAES = null;


                    //RSA cipher for decrypt key
                    Cipher rsa = Cipher.getInstance("RSA");
                    rsa.init(Cipher.DECRYPT_MODE, u.getPrivateKey());

                    //decode aeskey with rsa cipher
                    decodedKeyAES  = rsa.doFinal((byte[])encodedKeyAES);
                    SecretKey keyAES = new SecretKeySpec(decodedKeyAES, 0, decodedKeyAES.length, "AES");

                    //AES cipher for decrypt message
                    Cipher aes = Cipher.getInstance("AES");
                    aes.init(Cipher.DECRYPT_MODE, keyAES);


                    Message msg = null;


                    //convert deocoded bytes back to Message
                    decodedBytes = aes.doFinal((byte[])encodedBytes);
                    ByteArrayInputStream in = new ByteArrayInputStream(decodedBytes);
                    ObjectInputStream is = new ObjectInputStream(in);
                    msg = (Message) is.readObject();

                    ProcessMessageHandler.post(new ProcessMessage(msg));


                } catch (IOException e) {
                    e.printStackTrace();
                    //other user closed the communication
                    Thread.currentThread().interrupt();




                    try {
                        //compute the position of this contact (may change during time)
                        int position = 0;
                        for(int i=0; i<contacts.size(); i++) {
                            if (contacts.get(i).getID() == contactID) {
                                position = i;

                                //close the socket also on my side.
                                sockets.get(position).close();
                                outputs.get(position).close();
                                inputs.get(position).close();

                                contacts.get(position).setOnline(false);
                            }
                        }


                        //Post to main thread the UI changes.
                        ProcessMessageHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                        System.out.println("CLOSING SOCKET");
                    } catch (IOException e2) {
                        e.printStackTrace();
                    }

                } catch (ClassNotFoundException e) {   //here
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
    }



    public void encryptStorage(){
          File encryptedFile = new File(context.getFilesDir(), u.getUsername());

                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encryptedFile));
                    byte[] yourKey = generateKey(u.getPassword());
                    byte[] filesBytes = encodeFile(yourKey, convertToBytes(u));
                    bos.write(filesBytes);
                    bos.flush();
                    bos.close();
                }catch (Exception e1){
                    Log.e("Encrypt error", e1.getMessage());
                }
    }

    //----------- for updating UI: prints new messages----------
    class ProcessMessage implements Runnable {
        private int pos;
        private Message msg;


        public ProcessMessage(Message m) {
            this.msg = m;
            this.pos = 0;
        }

        @Override
        public void run() {

            if (msg.getType() == 0) {//TextMessage
                TextMessage textMessage = (TextMessage) msg;

                //Compute position of contact everytime i get a message, because it may change
                String contactName = null;
                for (int i = 0; i < contacts.size(); i++) {

                    if (contacts.get(i).getID() == textMessage.getSenderID()) {
                        //Retrieve contactName to then access the contact map in user u.
                        contactName = contacts.get(i).getName();
                        this.pos = i;
                    }
                }

                //RSA cipher for decrypt key
                Cipher rsaSignature = null;
                byte[] decodedSignature = null;
                try {
                    rsaSignature = Cipher.getInstance("RSA");
                    rsaSignature.init(Cipher.DECRYPT_MODE, u.getContacts().get(contactName).getPublicKey());
                    decodedSignature = rsaSignature.doFinal(textMessage.getSignature());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }

                String text = textMessage.getText();

                //HASH
                MessageDigest digest = null;
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                byte[] encodedhash = digest.digest(
                        text.getBytes(StandardCharsets.UTF_8));

                if(!Arrays.equals(decodedSignature,encodedhash)){
                    return;
                }


                //add the message to the correct user conversation
                contacts.get(this.pos).addMessage(textMessage);
                contacts.get(this.pos).setNewMessage(true);


                //encrypt the storage on a separate thread
                new Thread(() -> encryptStorage()).start();


                //notify the chat activity related to this contact coommunication (if its opened) about the new message
                Intent intent = new Intent("NEW_MESSAGE");
                intent.putExtra("position", this.pos);
                sendBroadcast(intent);
                // sendBroadcast(new Intent("NEW_MESSAGE").putExtra("position", this.pos));

                //notify the rcview that the last message in the conversation with contact
                //in the given pos is changed. This will update the last msg view
                adapter.notifyItemChanged(pos);
            }

        }
    }
}
