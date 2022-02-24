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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.howzit.messages.EncryptedMessage;
import com.example.howzit.messages.Message;
import com.example.howzit.messages.TextMessage;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static com.example.howzit.SignUp.convertToBytes;
import static com.example.howzit.SignUp.encodeFile;
import static com.example.howzit.SignUp.generateKey;


//DESCRIPTION

//Asynk Communication through sockets. The problem i had it was not due to sockets, but to
//the method used to launch threads. I used run() instead of start() lol

//Application uses standard Threads to perform blocking operations (accept() and read())

//For non-blocking network operations, like send msg or connecting to new sockets, i normally
//would have implemented them in main UI Thread. But apparently, in the lasts updates of android, is no
//longer possible to run network stuff on the main UI thread, in order to avoi UI freezing.
//So i found out that best solution is to use AsynkTask, that runs them on background on separate threads.

//Also normal Threads could have been used for those, but it seems that AsynkTasks  are more native
// and suitable for performing light tasks (like connecting and so on).

//AT also provides nice OnPreExec and OnPostExec() that can change UI. Instead when using thread, i
//changed UI using a Handler.

//In conclusion this piece of software is kind of an hybrid solution, between Threads (with Handlers)
//and AsynkTask. Let me know if you think its better to use only one of those solutions.

//-----------------------------------------------------------------------------------------

//STRUCTURE (edited to v2.0) Listening for connections and establishing new connections
//move to ConctactslistActivity

//This activity is started whenever a connection incoming or outcoming is established
//This is in charge of asyncronously receive and send messages from/to one outSocket

//CommunicationThread waits for messages incoming from the connected socket. It then uses
//UpdateConversationHandler to update UI.

//UpdateUIThread is used along with updateConversationHandler to update the UI when needed.

//SendTaks create a PrintWriter and sends msg (passed in params) through it.

//------------------------------------------------------------------------------------------


/**
 * Chat activity between the user and a contact
 */
public class CommunicationActivity extends AppCompatActivity  {

    Activity activity;
    Context context;
    User u;

    private int myID;
    private int contactID;

    /**
     * name of the contact with we are chatting with
     */
    private static String contactName;
    //position of the contact in the contactList
    private static int contactPosition;
    private String destIP;

    /**
     * View containing the messages exchanged
     */
    private static RecyclerView messageRecycler;

    /**
     * Adapter used to get the data
     */
    private static MessageListAdapter messageAdapter;

    /**
     * Data of the conversation.
     * It is a list containing the messages exchanged, and it's used by the {@link #messageAdapter}
     */
    private static ArrayList<TextMessage> conversation;

    /**
     * Message to be sent text view
     */
    private TextView sendText;

    /**
     * Socket of the contact
     */
    private Socket socket;

    /**
     * Output stream of the contact
     */
    private ObjectOutputStream output;


    /**
     * adapter of the {@code RecylerView} to update lastMSG view when I send a msg from here,
     * so to update it directly from here
     */
    private ContactsAdapter adapter;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_new);

        u = ((LoggedUser) this.getApplication()).getLoggedUser();
        activity = this;
        context = this;

        //text = (TextView) findViewById(R.id.RCVtextView);
        sendText = (EditText) findViewById(R.id.MSGeditText);

        //catch values passed by Concats activity.
        contactID = getIntent().getIntExtra("contactID", -1);
        contactName = getIntent().getStringExtra("contactName");
        contactPosition = getIntent().getIntExtra("position", -1);
        myID = u.getID();

        adapter = ContactsActivity.getAdapter();

        messageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list );

        conversation = new ArrayList<TextMessage>();
        for (TextMessage t: u.getContacts().get(contactName).getConversation()){
            conversation.add(t);
        }

        messageAdapter = new MessageListAdapter(conversation);
        messageRecycler.setAdapter(messageAdapter);
        messageRecycler.setLayoutManager( new LinearLayoutManager(this));


        //Set the standard toolbar for the activity
        getSupportActionBar().setTitle(contactName); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar
        messageRecycler.scrollToPosition(conversation.size() -1);
    }


    /**
     * to handle the press of "back" button in the action bar.
     * For further implementations, we can define the things we want to have in the toolbar
     * inside the res/menu/menu_chat.xml file.
     * Tutorial: <a ref="https://stackoverflow.com/questions/39052127/how-to-add-an-actionbar-in-android-studio-for-beginners">here</a>
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the send button is clicked
     * @param view
     */
    public void onSendClick(View view) {
        //Check whether there is a message written
        if(sendText.getText().toString().trim().length()>0){

            String msg = ((EditText) findViewById(R.id.MSGeditText)).getText().toString();
            //start sending task
            new SendTask().execute(msg);
            sendText.setText("");

            //get the socket from other activity
            socket = ContactsActivity.getSocket(contactPosition);
            //check whether socket is correctly connected to contact
            if(socket != null && socket.isConnected() && !socket.isClosed()) {

                //compute sending time
                LocalDateTime myDateObj = LocalDateTime.now();
                DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("h:mm a");
                String timeStamp = myDateObj.format(myFormatObj);

                //Create message to be stored
                TextMessage msgToStore = new TextMessage(contactID, myID, msg, u.getUsername(), timeStamp, null);
                ContactsActivity._addMessage(msgToStore, contactPosition);
                conversation.add(msgToStore);
                messageRecycler.smoothScrollToPosition(conversation.size() -1);
                adapter.notifyItemChanged(contactPosition);
                messageAdapter.notifyDataSetChanged();

            }
        }
    }


    /**
     * AsyncTask to send message (passed in parameters) through ObjectOutputStream object
     */
    public class SendTask extends AsyncTask<String, byte[], Boolean> {

        /**
         * The message to be sent
         */
        private String msgToSend;


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(String... params){ //This runs on a different thread
            boolean result = false;
            msgToSend = params[0];

            //get socket and output stream for this contact, from Contactsactivity
            socket = ContactsActivity.getSocket(contactPosition);
            output = ContactsActivity.getOutput(contactPosition);

                try {
                        //check if the socket has been connected, and not yet closed
                        if(socket != null && socket.isConnected() && !socket.isClosed()){
                            result = true; //handle cases in OnPostExecute()



                            //compute sending time
                            LocalDateTime myDateObj = LocalDateTime.now();
                            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("h:mm a");
                            String timeStamp = myDateObj.format(myFormatObj);


                            //Use this for encrypting the message (just put message into stringForEncryption)
                            byte[] decodedBytes = null;
                            byte[] encodedBytes = null;
                            byte[] decodedKeyRSA = null;
                            byte[] encodedKeyRSA = null;
                            byte[] signature = null;

                            try {
                            //AES key
                            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                            keyGen.init(128);

                            //AES cipher
                            Cipher aes = Cipher.getInstance("AES");
                            SecretKey secretKey = keyGen.generateKey();
                            aes.init(Cipher.ENCRYPT_MODE, secretKey);

                            //RSA cipher
                            Cipher rsa = Cipher.getInstance("RSA");
                            rsa.init(Cipher.ENCRYPT_MODE, u.getContacts().get(contactName).getPublicKey());

                            Cipher rsaSignature = Cipher.getInstance("RSA");
                            rsaSignature.init(Cipher.ENCRYPT_MODE, u.getPrivateKey());

                            String sgn = null;

                            //HASH
                            MessageDigest digest;
                            digest = MessageDigest.getInstance("SHA-256");
                            byte[] encodedhash = digest.digest(msgToSend.getBytes(StandardCharsets.UTF_8));
                            signature = rsaSignature.doFinal(encodedhash);

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ObjectOutputStream out = null;
                            ObjectOutputStream outKey = null;


                            //Create message object
                            TextMessage msg = new TextMessage(contactID, myID, msgToSend, u.getUsername(), timeStamp, signature);


                            try {
                                //stream TextMessage from object to byte array
                                out = new ObjectOutputStream(bos);
                                out.writeObject(msg);
                                out.flush();
                                decodedBytes = bos.toByteArray();

                                decodedKeyRSA = secretKey.getEncoded();
                            } finally {
                                try {
                                    bos.close();
                                } catch (IOException ex) {
                                    // ignore close exception
                                }
                            }

                            encodedBytes = aes.doFinal(decodedBytes);
                            encodedKeyRSA = rsa.doFinal(decodedKeyRSA);


                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("EncryptionError", "Error while doing encryption");
                        }

                         //Create encrypted object to be sent
                        EncryptedMessage encryptedMessage = new EncryptedMessage(encodedBytes, encodedKeyRSA);


                        //send the EncryptedMessage object
                        if(output!= null && encryptedMessage.getData() != null) {
                            output.writeObject(encryptedMessage);
                            output.flush();

                        }

                        //Encrypt the storage
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

                } catch (Exception e) {
                    e.printStackTrace();
                }

            return result;
        }


        @Override
        protected void onPostExecute(Boolean result) { //This runs on a different thread


                if(result == true) {
                    //Do something..
                }

                if(result == false){
                    CharSequence text = "Connection not established.";
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
        }
    }


    //------------  ------------------

    /**
     * Class that is notified when new received messages are added to the conversation
     */
    public static class Observer extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //Get the parameter from the intent
            int pos = intent.getIntExtra("position", -1);

            //if there is a new msg, and if it is from the contact that this activity chat is communicating with, update the view.
            if (intent.getAction().equals("NEW_MESSAGE") & pos == contactPosition) {

                //Add new message to conversation list
                ContactsActivity._setNewMessage(contactPosition);
                conversation.add(ContactsActivity._getConversationMessage(pos, -1));
                messageRecycler.smoothScrollToPosition(conversation.size() -1);
                //notify the changes to the RecyclerView
                messageAdapter.notifyDataSetChanged();
            }
        }
    }
}