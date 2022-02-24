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

import android.widget.TextView;

import com.example.howzit.messages.Message;
import com.example.howzit.messages.TextMessage;

import org.w3c.dom.Text;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Class used to represent a contact. It keeps track of it information, as well as the conversation with it
 */
public class Contact implements Serializable {

    /**
     * The name given to the contact
     */
    private String name;

    /**
     * The contact's ID
     */
    private int ID;

    /**
     * The present contact's IP
     */
    private String IP;

    /**
     * The contact's public key
     */
    private Key publicKey;

    /**
     * The conversation had with the contact
     */
    private LinkedList<TextMessage> conversation;

    /**
     * wheter the contact is online
     */
    private boolean online;

    /**
     * whether the contact has unread messages
     */
    private boolean newMessage;

    /**
     * Constructor to be used when you have all the information (e.g., already decoded from the QR code
     *
     * @param name the contact's username
     * @param ID    the contact's ID
     * @param IP    the contact's present IP
     * @param online    true if the contact is online
     * @param publicKey contact's public key
     */
    public Contact(String name, int ID, String IP, boolean online, Key publicKey) {
        this.ID = ID;
        this.name = name;
        this.IP = IP;
        this.publicKey = publicKey;
        this.conversation = new LinkedList<>(); //new LinkedList<TextMessage>();
        this.online = false;
        this.newMessage = false;
    }


    /**
     * Getter
     *
     * @return the contact's public key
     */
    public Key getPublicKey() {
        return publicKey;
    }

    /**
     * Setter
     *
     * @param publicKey the public key to be set
     */
    public void setPublicKey(Key publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Getter
     *
     * @return the conversation
     */
    public LinkedList<TextMessage> getConversation() {
        return conversation;
    }

    /**
     * Setter
     *
     * @param conversation the conversation to be set
     */
    public void setConversation(LinkedList<TextMessage> conversation) {
        this.conversation = conversation;
    }

    /**
     * Getter
     *
     * @return the contact's username
     */
    public String getName() {
        return name;
    }

    /**
     * Setter
     *
     * @param n the username to be set
     */
    public void setName(String n) {this.name = n;}

    /**
     * Getter
     *
     * @return the contac's ID
     */
    public int getID() {return ID;}

    /**
     * Setter
     *
     * @param id the ID to be set for the contact
     */
    public void setID(int id){this.ID = id;}

    /**
     * Getter
     *
     * @return the present contact's IP
     */
    public String getIP() { return IP; }

    /**
     * Setter
     *
     * @param ip the IP to be set
     */
    public void setIP(String ip){this.IP = ip;}

    /**
     * Getter
     *
     * @return {@code true} if the contact is online, {@code false} otherwise
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Setter
     *
     * @param b {@code true} if the contact is found to be onlne, {@code false} otherwise
     */
    public void setOnline(boolean b) {this.online = b;}


    public boolean hasNewMessage() {
        return newMessage;
    }

    public void setNewMessage(boolean b) {this.newMessage = b;}



    /**
     * Retrieves a particular message in the conversation.
     * Can return the most recent message setting the parameter.
     *
     * @param index the index of the message to return. If equal to {@code -1}, return the last message
     * @return  the desired (or latest) message
     */
    public TextMessage getMessage(int index){
        if(index == -1)
            if(conversation.size()>0)
                return conversation.getLast();
            else return null;

        if(conversation.size() > index) return conversation.get(index);
        else return null;
    }

    /**
     * Add a message to the conversation.
     *
     * @param msg   the message to be added
     */
    public void addMessage(TextMessage msg){
        this.conversation.addLast(msg);
    }

   // private static int lastContactId = 0;


    /*public static ArrayList<Contact> createContactsList(int numContacts) {
        ArrayList<Contact> contactsList = new ArrayList<Contact>();

        for (int i = 1; i <= numContacts; i++) {
            contactsList.add(new Contact("Person " + ++lastContactId, "IPaddr", i <= numContacts / 2));
        }

        return contactsList;
    }*/
}