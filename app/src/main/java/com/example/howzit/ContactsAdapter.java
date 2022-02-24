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

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.howzit.messages.Message;
import com.example.howzit.messages.TextMessage;

import java.util.List;


/**
 * Create the basic adapter extending from {@code RecyclerView.Adapter}.
 * It is needed to prepare the data, extracted from the data source (in this case, the {@link #mContacts} list)
 * to be fed into the ViewHolder
 * Note that we specify the custom {@link com.example.howzit.ContactsAdapter.ViewHolder}
 * which gives us access to our views
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

 //   ContactslistActivity.ConnectTask connectTask;
    /**
     * Contacts saved
     */
    private List<Contact> mContacts;

    /**
     * Constructor.
     * @param contacts the contact list to be binded
     */
    public ContactsAdapter(List<Contact> contacts) {
        mContacts = contacts;
    }

    /**
     * Usually involves inflating a layout from XML and returning the holder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_contact, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    /**
     * Involves populating data into the item through holder
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ContactsAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Contact contact = mContacts.get(position);

        // Set item views based on your views and data model
        TextView nameTextView = holder.nameTextView;
        nameTextView.setText(contact.getName());

        TextView lastMessageTextView = holder.lastMessageTextView;



        //get last message from the contact conversation, if there is one
        TextMessage msg = contact.getMessage(-1);
        if(msg != null) {
            String msgText = msg.getText();
            if (msgText.length() > 12) {
                msgText = msgText.substring(0, 12) + "...";
            }
            lastMessageTextView.setText(msgText);
        }
                else {
            lastMessageTextView.setText(" ");
        }

        //set red or green circle whether contact is offline or online
        ImageView isOnline = holder.isOnline;
        if(!contact.isOnline()) isOnline.setImageResource(R.drawable.ic_baseline_person_red_24);
        else isOnline.setImageResource(R.drawable.ic_baseline_person_green_24);

        ImageView newMsg = holder.newMessage;
        if(contact.hasNewMessage())
            newMsg.setVisibility(View.VISIBLE);
        else  newMsg.setVisibility(View.INVISIBLE);
    }

    /**
     * Getter.
     * @return the total count of items in the list
     */
    @Override
    public int getItemCount() {
        return mContacts.size();
    }



    //---------------------nested class ViewHolder---------------------------

    /**
     * Nested class {@code ViewHolder}
     * Provide a direct reference to each of the views within a data item.
     * Used to cache the views within the item layout for fast access.
     * Your holder should contain a member variable for any view that will be set as you render a row
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        // Remind: Your holder should contain a member variable
        // for any view that will be set as you render a row

        /**
         * view containing the name of the contact
         */
        public TextView nameTextView;

        /**
         * view containing the last message exchanged with the contact
         */
        public TextView lastMessageTextView;

        /**
         *view showing if the contact is online or offline
         */
        public ImageView isOnline;

        public ImageView newMessage;


        // We also create
        //

        /**
         * a constructor that accepts the entire item row and does the view lookups to find each subview
         * @param itemView
         */
        public ViewHolder(View itemView) {

            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            lastMessageTextView = (TextView) itemView.findViewById(R.id.last_message);
            isOnline = (ImageView) itemView.findViewById(R.id.isonline);
            newMessage = (ImageView) itemView.findViewById(R.id.newmsg);
        }
    }
}