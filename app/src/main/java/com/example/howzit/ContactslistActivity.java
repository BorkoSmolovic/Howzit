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

import com.example.howzit.messages.DropMessage;
import com.example.howzit.messages.EncryptedMessage;
import com.example.howzit.messages.Message;
import com.example.howzit.messages.PublishMessage;
import com.example.howzit.messages.QueryMessage;
import com.example.howzit.messages.QueryReplyMessage;
import com.example.howzit.messages.SignUpMessage;
import com.example.howzit.messages.TextMessage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.example.howzit.SignUp.convertToBytes;
import static com.example.howzit.SignUp.encodeFile;
import static com.example.howzit.SignUp.generateKey;



//This is the main activity of the application. When app starts, this act shows the list of contacts
//(initialized to random fake users, except for the first 2 hardcoded IPs.

//on Creation, a listenThread is started.
//Whenever msg button is pressed, a connection is established to ip specified in
//contact_name textfield, on port 6000

//ConnectTask connects to a socket with LISTENINGPORT and IP passed throug params.

//ListenThread listen and accept for connection on LISTENINGPORT from new devices. Once accepted

//Whenever one of both connectTask or ListenThread result in a new active connection, a new
//CommunicationActivity is launched, that will take care of the one-to-one communication.

@Deprecated
public class ContactslistActivity extends AppCompatActivity {

    private static boolean done = false;

    Activity activity;
    Context context;
    Dialog dialogAddServer;
    Dialog dialogChangeName;
    Dialog dialogEditContact;
    String contactUsername;
    Integer contactIndex;
    static User u;
    public static User getUser(){return u;} //used in MessageListAdapter

    private static final int LISTENINGPORT = 6000;
    private ServerSocket listeningSocket;
    final CountDownLatch latch = new CountDownLatch(2);

    //static so that can be passed to CommunicationActivity, and to have only 1 socket for now. to simplify things
    private static Socket outSocket;

    public static Socket getOutSocket() {
        return outSocket;
    } //DELETE

    private static ObjectOutputStream serverOutput;
    private static ObjectInputStream serverInput;

    private static ObjectOutputStream oldOutput;
    private static ObjectInputStream oldInput;
    public static ObjectOutputStream getObjectOutputStream(){return oldOutput;}

    //private static DataOutputStream output;
    //private static DataInputStream input;
    //private static BufferedOutputStream output;
    //private static BufferedInputStream input;
    private static OutputStream output;
    private static InputStream input;
    public static OutputStream getOutputStream(){return output;}

    //RecyclerView is the component that shows and scrolls the contactslist. It also needs an adapter.
    private RecyclerView rvContacts;
    private static ContactsAdapter adapter;
    public static ContactsAdapter getAdapter() {
        return adapter;
    }

    //Toast msg as a class attribute to prevent user from creating too much overlapping toast messages
    Toast toast;

    //New intent for containing chat activity
    Intent chatIntent;

    //handler for update the UI from threads
    Handler ProcessMessageHandler;

    //observer to be registered as receiver of the broadcast.
    // New intent that specifies the type of notification
    private CommunicationActivity.Observer receiver;
    private IntentFilter intentFilter;

    private static ArrayList<Contact> contacts;


    //To get the ip of my device
    WifiManager wifiManager;

    //methods used in CommunicationActivity to add and retrieve msgs from the conversation with user in position "contactPos".
    //index: index of the message inside the conversation list. index = -1 --> take last message
    public static void _addMessage(TextMessage msg, int contactPos) {
        contacts.get(contactPos).addMessage(msg);
    }

    public static TextMessage _getConversationMessage(int contactPos, int index) {
        return contacts.get(contactPos).getMessage(index);
    }

    public static String _getName(int senderID){
        String contactName = null;
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getID() == senderID) {
                contactName = contacts.get(i).getName();
            }
        }
        return contactName;
    }

    //Asynk task for create connections
    private ConnectTask connectTask;

    //Thread to listen on port 6000
    private Thread listeningThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);


        //SendToServerThread sts = new SendToServerThread();

        activity = this;
        context = this;


        // ----------------------- CONTACTS STUFF ----------------------------------
        contacts = new ArrayList<Contact>();

        toast = new Toast(getApplicationContext());

        //get user from global storage so we can use his contacts, private and public keys etc.
        u = ((LoggedUser) this.getApplication()).getLoggedUser();

        // Lookup the recyclerview in activity layout
        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);

        //Decorate with separator lines between contacts
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rvContacts.addItemDecoration(itemDecoration);

        //get contacts from user we have to display
        for (Contact c : u.getContacts().values()) {
            contacts.add(c);
        }

        // Attach the adapter to the recyclerview to populate items
        //adapter = new ContactsAdapter(contacts);
        rvContacts.setAdapter(adapter);

        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        //----Add a click listener to the ReciclerView of contacts.----
        //There is not a pre-build onClikListener method working for RecyclerView.
        //And i can't use simple onMessageClick() provided by the item_contact view bcs it would always
        //inflate the first item_contact element (despite of what element on list i click)
        //So i use this decorator (as suggested on tutorial i linked) downloading ItemClickSupport
        //Class from their github repo: https://gist.github.com/nesquena/231e356f372f214c4fe6, and
        //adding ids.xml file (as specified in the repo)
        //Here i attach the onItemClickListener to rvContacts
        ItemClickSupport.addTo(rvContacts).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView rv, int position, View v) {
                String name = contacts.get(position).getName();
                String destIP = contacts.get(position).getIP();
                String destID = ((Integer) contacts.get(position).getID()).toString(); //pass as a String for the AsynkTask
                Connect(name, destIP, position, destID);
            }
        });

        ItemClickSupport.addTo(rvContacts).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                contactUsername = contacts.get(position).getName();
                contactIndex = position;
                editContactDialog();
                return true;
            }
        });

        //------------------------- OTHER STUFF --------------------------------------------

        //adding CommunicationACtivityObserver as a receiver of the broadcast.
        //intentFilter specifies the kind of notification in the broadcast
        receiver = new CommunicationActivity.Observer();
        intentFilter = new IntentFilter("NEW_MESSAGE");
        registerReceiver(receiver, intentFilter);

        //Handlers are used to delegate UI changing actions, from network operations Thread to the
        //main UI Thread. (using post() method)
        ProcessMessageHandler = new Handler();
        //toast = new Toast(getApplicationContext());

        outSocket = null;

        //Starting Thread listening on port 6000 for new communications
        listeningThread = new Thread(new ListeningThread());

        //important to call start() and not run(), because that would not
        // start a new parallel thread, but just run the code in this thread instead
        listeningThread.start();

        //publish my IP

        //to get the ip. use where needed
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        PublishMessage msg = new PublishMessage(u.getID(), ipAddress);

        Thread publish = new Thread(new SendToServerThread(msg));
        publish.start();
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
                Toast.makeText(ContactslistActivity.this, "Add server IP: " + IP, Toast.LENGTH_LONG).show();
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
                ContactslistActivity.this,
                android.R.layout.simple_spinner_item,
                content);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set logic for buttons
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String IP = spinner.getSelectedItem().toString();
                Toast.makeText(ContactslistActivity.this, "Remove server IP: " + IP, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ContactslistActivity.this, "please enter a username for contact", Toast.LENGTH_LONG).show();
                } else if (username.equals(contactUsername)) {
                    Toast.makeText(ContactslistActivity.this, "please enter a new username for contact", Toast.LENGTH_LONG).show();
                } else if (u.getContacts().containsKey(username)) {
                    Toast.makeText(ContactslistActivity.this, "Another contact with this username already exists", Toast.LENGTH_LONG).show();
                } else {
                    // Final check if contact is successfully updated
                    if (u.updateContactName(contactUsername, username)) {
                        //encrypting storage and saving file
                        File encryptedFile = new File(context.getFilesDir(), u.getUsername());
                        try {
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encryptedFile));
                            byte[] yourKey = generateKey(u.getPassword());
                            byte[] filesBytes = encodeFile(yourKey, convertToBytes(u));
                            bos.write(filesBytes);
                            bos.flush();
                            bos.close();
                        } catch (Exception e1) {
                            Log.e("Encrypt error", e1.getMessage());
                        }
                        contacts.remove(contactIndex);
                        rvContacts.getAdapter().notifyDataSetChanged();
                        Toast.makeText(ContactslistActivity.this,
                                contactUsername + " renamed to " + username, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ContactslistActivity.this,
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

    private void editContactDialog() {
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
                            //encrypting storage and saving file
                            u.getContacts().remove(contactUsername);
                            File encryptedFile = new File(context.getFilesDir(), u.getUsername());
                            try {
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encryptedFile));
                                byte[] yourKey = generateKey(u.getPassword());
                                byte[] filesBytes = encodeFile(yourKey, convertToBytes(u));
                                bos.write(filesBytes);
                                bos.flush();
                                bos.close();
                            } catch (Exception e1) {
                                Log.e("Encrypt error", e1.getMessage());
                            }
                            ArrayList<Contact> newContacts = new ArrayList<>(contacts.size() - 1);
                            for (Contact c : contacts) {
                                if (!c.getName().equalsIgnoreCase(contactUsername)) {
                                    newContacts.add(c);
                                }
                            }
                            contacts.clear();
                            contacts.addAll(newContacts);
                            rvContacts.getAdapter().notifyDataSetChanged();
                            Toast.makeText(ContactslistActivity.this,
                                    contactUsername + " removed from contact list", Toast.LENGTH_LONG);
                        } else {
                            Toast.makeText(ContactslistActivity.this,
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
                    Intent myIntent = new Intent(ContactslistActivity.this, SignIn.class);
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

        public void Connect (String name, String destIP,int position, String contactID){

            //Meanwhile Create new MainActivity to handle the new communications
            //outSocket will be caught in the new activity with getOutSocket()
            chatIntent = new Intent(ContactslistActivity.this, CommunicationActivity.class);
            //passing the IP and Name as a string to the new activity--> to set the contact name on top of the new chat.
            //and to check wheter the chat i opened is the one with who i have the connection active (only 1 conn for now)

            chatIntent.putExtra("contactID", Integer.parseInt(contactID));
            chatIntent.putExtra("contactName", name);
            chatIntent.putExtra("myID", u.getID());
            chatIntent.putExtra("sender", true);
            chatIntent.putExtra("position", position);


            //asynktask for connection
            connectTask = new ConnectTask(chatIntent);

            //create connection and open communic activity
            connectTask.execute(contactID, ((Integer) position).toString());

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
                    userHasBeenAdded = true;
                    toast.makeText(getApplicationContext(), c.getName(), Toast.LENGTH_SHORT).show();
                }
            }

            // Notify adapter if a change has occurred
            if (userHasBeenAdded) {
                rvContacts.getAdapter().notifyDataSetChanged();
            }
            super.onResume();
        }

        public void AddContact (View view){
            Intent myIntent = new Intent(ContactslistActivity.this, AddContact.class);
            startActivity(myIntent);
        }


        //--------Task to connect to the device with IP (passed by parameters) on Port (hardcoded) -------------------
        public class ConnectTask extends AsyncTask<String, byte[], Boolean> {

            Intent chatIntent;

            public ConnectTask(Intent i) {
                this.chatIntent = i;
            }

            public boolean connettiti(String ID, String position) {
                boolean result = false;

                QueryMessage queryMessage = new QueryMessage(Integer.parseInt(ID));
                //Thread for querying the server
                //Thread query = new Thread(new SendToServerThread(queryMessage));
                SendToServerThread query = new SendToServerThread(queryMessage);
                query.run();


                try {

                    while (done == false) {
                       // Thread.sleep(50);
                    }
                    done = false;

                    //wait for the queryReplayMessage to be received and processed.
                    // latch.await();
                    //get the ip from contacts

                    String IP = contacts.get(Integer.parseInt(position)).getIP();
                    chatIntent.putExtra("destIP", IP);

                    InetAddress destAddr = InetAddress.getByName(IP);  //params is the list of parameters passed in execute()

                    System.out.println("PROVO A CONNETTERMI");
                    int timeout = 500;
                    // This limits the time allowed to establish a connection in the case
                    // that the connection is refused or server doesn't exist.
                    outSocket = new Socket(destAddr, LISTENINGPORT);
                   // outSocket.setSoTimeout(timeout);
                   // outSocket.connect(new InetSocketAddress(destAddr, LISTENINGPORT), timeout);

                    // This stops the request from dragging on after connection succeeds.

                    System.out.println("FINITO");


                    if (outSocket.isConnected()) {
                        result = true;
                        //handle the cases in onPostExecute()
                    }

                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                    Log.e("nullHostException", e1.toString());
                } catch (IOException e1) {
                    done = true;
                Log.e("IOexception", e1.toString());
                try {
                    int a = 1;
                  //  outSocket.close();
                    //outSocket = new Socket();
                }catch (Exception e){
                    Log.e("CloseException" , e.toString());
                }
                } catch (Exception e2){
                    Log.e("Exception", e2.toString());
                }
                return result;
            }

            @Override
            protected Boolean doInBackground(String... params) { //This runs on a different thread
                boolean result = false; //used to change UI in OnPostExec()

                if (outSocket == null) result = connettiti(params[0], params[1]);
                    //create connection also if socket is closed. but to check it, socket must be not null
                else if (outSocket.isClosed()) result = connettiti(params[0], params[1]);

                String IP = contacts.get(Integer.parseInt(params[1])).getIP();
                chatIntent.putExtra("destIP", IP);

                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {

                Intent i = this.chatIntent;
                new Handler().post(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(i);
                    }
                }));

                if (result == false) {
                    //Show a toast message
                    //CharSequence text = "Can't establish a connection";
                    // toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }

                if (result == true) {
                    System.out.println("CONNESSOOOOOOOO");
                    //start thread that handle message exchanging
                    // startActivity(chatIntent);
                    CommunicationThread commThread = new CommunicationThread(outSocket);
                    new Thread(commThread).start();
                }
            }
        }


        //--------Thread that listens for new connections on port 6000, hardcoded. (Blocking)-------------------
        class ListeningThread implements Runnable {

            public void run() {
                try {
                    listeningSocket = new ServerSocket(LISTENINGPORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            //Listen for incoming connections. (Blocking operation)
                            outSocket = listeningSocket.accept();
                            //start thread that handle message exchanging once connection is established
                            CommunicationThread commThread = new CommunicationThread(outSocket);
                            new Thread(commThread).start();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {

                }
            }
        }


        //-------------Thread in charge of receiving msgs once connection is established. (Blocking)-----------
        //All messages should be received and processed here (type check, ecc)
        class CommunicationThread implements Runnable {

            private Socket socket;
            private int pos;
            private String contactIP;

            public CommunicationThread(Socket s) {
                this.socket = s;

                //find pos of contact in contacts list BY IP
                contactIP = outSocket.getInetAddress().toString();
                this.pos = 0;
                // contactName = "";
                //excluding first character because its a "slash"
                contactIP = contactIP.substring(1);
            }

            public void run() {

                try {
                    //Create Output and Input stream
                    //ALWAYS output first, input then.
                     //output = new DataOutputStream(socket.getOutputStream());
                     //input = new DataInputStream(socket.getInputStream());
                    oldOutput = new ObjectOutputStream(this.socket.getOutputStream());
                    oldInput = new ObjectInputStream(this.socket.getInputStream());
                  //  output = this.socket.getOutputStream();
                    //input = this.socket.getInputStream();
                    /* output = new BufferedOutputStream(this.socket.getOutputStream());
                    input = new BufferedInputStream(this.socket.getInputStream());*/

            } catch (IOException e) {
                e.printStackTrace();
            }


            while (!Thread.currentThread().isInterrupted()) {

                try {
                    //RECEIVE THE MESSAGE OBJECT (only EncryptedMessages here)
                    EncryptedMessage encryptedMessage = (EncryptedMessage) oldInput.readObject();

                    byte[] decodedBytes = null;
                    byte[] encodedBytes = encryptedMessage.getData();



                    System.out.println("GOT A MESSAGE");


                     // Use this for decrypting the message
                        Cipher c = Cipher.getInstance("RSA");
                        c.init(Cipher.DECRYPT_MODE, u.getPrivateKey());
                        decodedBytes = c.doFinal((byte[])encodedBytes);

                        Message msg = null;

                        //convert deocded bytes back to Message
                        ByteArrayInputStream in = new ByteArrayInputStream(decodedBytes);
                        ObjectInputStream is = new ObjectInputStream(in);
                        msg = (Message) is.readObject();

                    //RECEIVE THE MESSAGE OBJECT (only TextMessages here)
                    //Message msg = decryptedMsg;
                    //System.out.println(msg);


                    new Handler().post(new ProcessMessage(pos, msg, contactIP));
                   // ProcessMessageHandler.post(new ProcessMessage(pos, msg, contactIP));


/*              //FIND POS IN RECVIEW BY ID. USELESS UNTIL WE STORE AND LOAD REAL ID OF THE CONTACTS AND USER

                    int ID = msg.getSenderID();
                    int pos = 0;
                    for(int i = 0; i<contacts.size(); i++){
                        if(contacts.get(i).getID() == ID){
                            pos = i;
                        }
                    }
*/
                    } catch (IOException e) {
                        e.printStackTrace();
                        //other user closed the communication
                        Thread.currentThread().interrupt();

                        try {
                            //close the socket also on my side.
                            outSocket.close();
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
            }
        }


        //-----------Thread for updating UI: prints new messages----------
        class ProcessMessage implements Runnable {
            private int pos;
            private Message msg;
            private String contactIP;

            public ProcessMessage(int p, Message m, String ip) {
                this.msg = m;
                this.pos = p;
                this.contactIP = ip;

            }

            @Override
            public void run() {


                if (msg.getType() == 0) {//TextMessage
                    TextMessage textMessage = (TextMessage) msg;

                    String contactName = null;
                    for (int i = 0; i < contacts.size(); i++) {
                        System.out.println("DENTRO AL FOR");
                        System.out.println("contacts ID:  " + contacts.get(i).getID());
                        System.out.println("SENDER ID:  " + textMessage.getSenderID());
                        if (contacts.get(i).getID() == textMessage.getSenderID()) {
                            contacts.get(i).setIP(contactIP);
                            contactName = contacts.get(i).getName();
                            this.pos = i;

                        }
                    }
                   // textMessage.setSenderName(contacts.get(this.pos).getName());
                    //add the message to the correct user conversation
                    contacts.get(this.pos).addMessage(textMessage);


                    for (TextMessage t: u.getContacts().get(contactName).getConversation()){
                        Log.e("before", t.getText());
                    }
                    //save message in contact
                   // u.getContacts().get(contactName).addMessage(textMessage);

                    for (TextMessage t: u.getContacts().get(contactName).getConversation()){
                        Log.e("after", t.getText());
                    }
                    //save message in storage


                    //TODO:encrypting file and storing
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


    public class SendToServerThread implements Runnable {

        private static final String serverIP = "192.168.1.19";
        private static final int serverPort = 2048;
        private Message message;

        public SendToServerThread(Message m) {
            this.message = m;
        }

        public void run() {
            try {

                outSocket = new Socket(serverIP, serverPort);
                if (outSocket.isConnected()) {

                    //ObjectStream created
                    serverOutput = new ObjectOutputStream(outSocket.getOutputStream());
                    serverInput = new ObjectInputStream(outSocket.getInputStream());
                }


                if (message.getType() == 1) {
                    PublishMessage publishMessage = (PublishMessage) message;
                    serverOutput.writeObject(publishMessage);
                    serverOutput.flush();
                    outSocket.close();
                } else if (message.getType() == 2) {
                    QueryMessage queryMessage = (QueryMessage) message;
                    serverOutput.writeObject(queryMessage);
                    serverOutput.flush();
                    //wait for reply
                    ReceiveFromServerThread recv = new ReceiveFromServerThread();
                    recv.run();
                } else if (message.getType() == 4) {
                    SignUpMessage signUpMessage = (SignUpMessage) message;
                    serverOutput.writeObject(signUpMessage);
                    serverOutput.flush();
                } else if (message.getType() == 6) {
                    DropMessage dropMessage = (DropMessage) message;
                    serverOutput.writeObject(dropMessage);
                    serverOutput.flush();
                    outSocket.close();

                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


        public class ReceiveFromServerThread implements Runnable {

            private int pos;


            public void run() {

              /*  try {
                    //Create Output and Input stream
                    //ALWAYS output first, input then.
                    output = new ObjectOutputStream(outSocket.getOutputStream());
                   input = new ObjectInputStream(outSocket.getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                while (!Thread.currentThread().isInterrupted() & !done) {

                    try {
                        //RECEIVE THE MESSAGE OBJECT.
                        //TODO: set timeout
                        System.out.println("ASPETTANDO LA RISPOSTA");
                        Message msg = (Message) serverInput.readObject();
                        System.out.println("RISPOSTA RICEVUTA");
                        System.out.println("TYPE: " + msg.getType());


                        if (msg.getType() == 3) {//QueryReply Message
                            QueryReplyMessage queryReplyMessage = (QueryReplyMessage) msg;
                            System.out.println("IDDDDDDDDDD :");
                            System.out.println(queryReplyMessage.getRequestedID());
                            //find contact corresponding to the ID. Assign the IP
                            for (int i = 0; i < contacts.size(); i++) {
                                if (contacts.get(i).getID() == queryReplyMessage.getRequestedID()) {
                                    contacts.get(i).setIP(queryReplyMessage.getRequestedIP());
                                    System.out.println("POSIZIONEEEE :");
                                    System.out.println(i);
                                    System.out.println("IP: " + queryReplyMessage.getRequestedIP());
                                }
                            }
                            outSocket.close();
                            done = true;
                            //Thread.currentThread().interrupt();
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        //Server closed the communication
                        Thread.currentThread().interrupt();

                        try {
                            //close the socket also on my side.
                            outSocket.close();
                        } catch (IOException e2) {
                            e.printStackTrace();
                        }

                    } catch (ClassNotFoundException e2) {
                        e2.printStackTrace();
                    }
                }
            }


        }

    }
}


