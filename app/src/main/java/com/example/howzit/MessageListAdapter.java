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
import android.os.Build;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howzit.messages.TextMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.BaseViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<TextMessage> mMessageList;

    public MessageListAdapter(/*Context context, */List<TextMessage> messageList) {
        //mContext = context;
        mMessageList = messageList;
    }



    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        TextMessage message = (TextMessage) mMessageList.get(position);

        if(message.getSenderID() == ContactsActivity.getUser().getID()){
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public MessageListAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(MessageListAdapter.BaseViewHolder holder, int position) {
        TextMessage message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }



    //------------------- Base View Holder --------------

    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        ImageView profileImage;

        BaseViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);

        }

    }

    // -------------------- Holder Received Msg ---------------------------------
    public class ReceivedMessageHolder extends BaseViewHolder {
        TextView nameText; //,timeText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            //timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
           // profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }

        void bind(TextMessage message) {

            messageText.setText(message.getText());
            nameText.setText(ContactsActivity._getName(message.getSenderID()));

           /* LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("h:mm a");
            String timeStamp = myDateObj.format(myFormatObj);*/
            timeText.setText(message.getTimestamp());
            
           // timeText.setText(message.getTimestamp());
            //nameText.setText(message.getSenderName());
            //nameText.setText(message.getNickname());

            // Format the stored timestamp into a readable String using method.
            long millis = 1607790668;
           /* timeText.setText(DateUtils.formatDateTime(mContext,millis, (DateUtils.FORMAT_SHOW_DATE |
                    DateUtils.FORMAT_SHOW_TIME |
                    DateUtils.FORMAT_NUMERIC_DATE)));*/
            //timeText.setText(Utils.formatDateTime(message.getCreatedAt()));


            // Insert the profile image from the URL into the ImageView.
            //Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        }
    }




    // -------------------- Holder Sent Msg ---------------------------------
    public class SentMessageHolder extends BaseViewHolder {

        SentMessageHolder(View itemView) {
            super(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        void bind(TextMessage message) {
            messageText.setText(message.getText());

           /* LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("h:mm a");
            String timeStamp = myDateObj.format(myFormatObj);*/
            timeText.setText(message.getTimestamp());

            //nameText.setText(ContactslistActivity._getName(message.getSenderID()));

            // Insert the profile image from the URL into the ImageView.
            //Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        }
    }
}
