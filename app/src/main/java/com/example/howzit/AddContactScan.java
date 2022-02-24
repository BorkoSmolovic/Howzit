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

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static com.example.howzit.SignUp.convertToBytes;
import static com.example.howzit.SignUp.encodeFile;
import static com.example.howzit.SignUp.generateKey;

/**
 * Activity to scan a new user's QR code and get its information
 */
public class AddContactScan extends AppCompatActivity {

    /**
     * CodeScanner used to scan and decode the new user's QR code.
     * It is suggested to view the source code to get an idea on how to use it,
     * or to go to <a href="https://github.com/yuriy-budiyev/code-scanner">CodeScanner's repo</a>
     */
    CodeScanner codeScanner;

    /**
     * A view to display code scanner preview.
     * It is suggested to view the source code to get an idea on how to use it,
     * or to go to <a href="https://github.com/yuriy-budiyev/code-scanner">CodeScanner's repo</a>
     */
    CodeScannerView codeScannerView;

    /**
     * Used within {@link #onResume()}.
     * It defaults to {@code false} but it is never assigned. Probably it should be removed
     *
     * @deprecated
     */
    Boolean contactFound = false;   //TODO do we need it?

    /**
     * Dialog to input the new user's {@code username}
     * It is instanciated from {@code androidx.appcompat.app.AlertDialog.Builder.create()}
     */
    Dialog dialog;

    // --------------------- Variables for storage ------------------------

    /**
     * Stores the current activity
     */
    Activity activity;

    /**
     * Stores the current context
     */
    Context context;

    /**
     * Stores the current user
     */
    User u;

    // Variable will contain the public key from the QR code scan
    /**
     * It stores the new user's public key.
     * It defaults to {@code null}, and it is assigned after the scan
     */
    Key publickey = null;

    /**
     * It stores the new user's current IP
     * It is assigned after the scan
     */
    String IP;

    /**
     * It stores the new user's ID.
     * It is assigned after the scan
     */
    int ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        activity = this;
        context = this;
        //read user from session storage
        u = ((LoggedUser) activity.getApplication()).getLoggedUser();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_scan);
        final Activity activity = this;

        // Get dialog layout and prevent user from cancel by pressing outside dialog
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.dialog_contactname,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(myView);
        builder.setCancelable(false);

        // Get confirm button, cancel button, and contact username edittext
        EditText contactUsername = (EditText) myView.findViewById(R.id.editTextContactUsername);
        Button buttonConfirm = (Button) myView.findViewById(R.id.buttonConfirmDialog);
        Button buttonCancel = (Button) myView.findViewById(R.id.buttonCancelDialog);

        // Functionality for AddUsername()
        // Set logic for buttons
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // Get the contact's name from user input
                String username = contactUsername.getText().toString();
                if(username.isEmpty()){
                    Toast.makeText(AddContactScan.this, "please enter a username for contact",Toast.LENGTH_LONG).show();
                } else if (u.getContacts().containsKey(username)) {
                    Toast.makeText(AddContactScan.this, "A contact with this username already exists",Toast.LENGTH_LONG).show();
                } else {
                    //make a new contact class
                    Contact contact = new Contact(username, ID,IP,true,publickey);

                    // read user from session storage and add the new contact
                    u.getContacts().put(contact.getName(),contact);

                    //TODO: update contact list in ContactsListActivity

                    //encrypting storage and saving file
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

                    finish();
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();   //TODO Are we sure this is already been assigned?
            }
        });

        // Create the dialog set the background to transparent
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // Creation of the QR scanner
        // Read the QR code and store in contact list
        codeScannerView = findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this,codeScannerView);

        // This is called when a QR code is found in the camera
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {

                        // The qr variable contain the string that is read from the QR code
                        // We check if the qr contain has the correct fields: "ID-IP-PublicKey"
                        String qr = result.getText().toString();
                        String[] parameters = qr.split("-");
                        if(parameters.length != 3) {
                            Toast.makeText(AddContactScan.this, "Invalid user information",Toast.LENGTH_LONG).show();
                            codeScanner.startPreview();
                            return;
                        } else if(!parameters[0].matches("[0-9]+")) {
                            Toast.makeText(AddContactScan.this, "Invalid user ID ",Toast.LENGTH_LONG).show();
                            codeScanner.startPreview();
                            return;
                        }

                        // Transform the qr parameters into the user variables
                        try {
                            // Base64 decode the data
                            byte [] encoded = Base64.getDecoder().decode(parameters[2].replace(" ","+"));
                            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encoded);
                            KeyFactory kf = KeyFactory.getInstance("RSA");
                            publickey = kf.generatePublic(publicKeySpec);
                        } catch (Exception e2) {
                            Toast.makeText(AddContactScan.this, "Invalid user key",Toast.LENGTH_LONG).show();
                            Log.e("error", e2.toString());
                            return;
                        }
                        ID = Integer.parseInt(parameters[0]);
                        IP = parameters[1];

                        // Check if the user try to add themselves as a contact
                        if(u.getID() == ID && u.getPublicKey().equals(publickey)){
                            Toast.makeText(AddContactScan.this, "You cant add yourself as a contact",Toast.LENGTH_LONG).show();
                            codeScanner.startPreview();
                            return;
                        }

                        // Check if the user try to add a contact twice
                        for (Contact c : u.getContacts().values()) {
                            if(c.getID() == ID && c.getPublicKey().equals(publickey)) {
                                Toast.makeText(AddContactScan.this, "You have already added this contact (" + c.getName() + ")",Toast.LENGTH_LONG).show();
                                codeScanner.startPreview();
                                return;
                            }
                        }

                        // Everything is in order, let the user assign a username for contact
                        AddUsername();
                    }
                });
            }
        });

        // Defines what happens after the QR code is scanned
        codeScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeScanner.startPreview();
            }
        });
    }

    /**
     * Displays the username {@link #dialog}
     */
    public void AddUsername() {
        dialog.show();
    }

    /**
     * Ask for camera permission when activity starts
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(contactFound == false){
            requestForCamera();
        }
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }


    /**
     * Handle the camera permission request
     */
    public void requestForCamera() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android build version require user permission to use camera
            if(getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                // Permission is already granted by user
                codeScanner.startPreview();
            } else {
                // Permission is not granted by user
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        } else {
            // Android build version do not require user permission to use camera
            codeScanner.startPreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check camera request from requestForCamera() function
        if(requestCode == 1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // User granted the request
                codeScanner.startPreview();
            } else {
                // User denied the request, end activity
                finish();
                Toast.makeText(AddContactScan.this, "Permission denied",Toast.LENGTH_LONG).show();
            }
        }
    }
}