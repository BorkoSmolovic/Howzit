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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.nayuki.qrcodegen.QrCode;

/**
 * Activity to add new contacts. It allows showing and scanning QR codes
 */
public class AddContact extends AppCompatActivity {

    /**
     * Button to start {@link AddContactScan} activity
     */
    private Button buttonScanQR;

    /**
     * Button to toggle the visibility of the QR code
     */
    private Button buttonShowQR;

    /**
     * Personal QR code
     */
    private ImageView imageViewQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        final Activity activity = this;


        // Initialize image and generate the QR code
        imageViewQR = findViewById(R.id.imageViewQR);
        imageViewQR.setVisibility(View.INVISIBLE);
        try {
            //Retrieve QR code as byte[] from the User session storage, decode it into a bitmap
            User u = ((LoggedUser) this.getApplication()).getLoggedUser();
            byte[] QRBytes = u.getQR();
            Bitmap QR = BitmapFactory.decodeByteArray(QRBytes,0,QRBytes.length);
            //Substitute default image with the new one
            imageViewQR.setImageBitmap(QR);
        } catch(Exception e) {
            //TODO we should probably have some error handling here
            Toast.makeText(this,"Error, cant read contact info!",Toast.LENGTH_LONG).show();
        }


        // Start the QR scanner
        buttonScanQR = findViewById(R.id.buttonScanQR);
        buttonScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(AddContact.this, AddContactScan.class);
                startActivity(myIntent);
            }
        });

        // Switch between showing and hiding the QR code
        buttonShowQR= findViewById(R.id.buttonShowQR);
        buttonShowQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (imageViewQR.getVisibility()){
                    case View.GONE:
                    case View.INVISIBLE:
                        imageViewQR.setVisibility(View.VISIBLE);
                        buttonShowQR.setText("Hide QR");
                        break;
                    case View.VISIBLE:
                        imageViewQR.setVisibility(View.INVISIBLE);
                        buttonShowQR.setText("Show QR");
                        break;
                    default:
                        //TODO we should probably have some error handling here
                }
            }
        });
    }

}