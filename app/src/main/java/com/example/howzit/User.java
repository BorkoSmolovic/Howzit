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

import java.io.Serializable;
import java.security.Key;
import java.util.HashMap;

/**
 * Class used to represent and store information about the current Howzit user.
 */
public class User implements Serializable {

    /**
     * The user's ID.
     */
    private int ID;

    /**
     * The user's username
     */
    private String username;

    /**
     * The user's password
     */
    private String password;

    /**
     * The user's private key
     */
    private Key privateKey;

    /**
     * The user's public key
     */
    private Key publicKey;

    /**
     * The contacs registered by the user
     */
    private HashMap<String,Contact> contacts;

    /**
     * The QR code containing the user information
     */
    private byte[] QR;


    /**
     * Constructor. The parameters are self-explanatory
     *
     * @param id
     * @param username
     * @param password
     * @param privateKey
     * @param publicKey
     * @param QR
     */
    public User(int id, String username, String password, Key privateKey, Key publicKey, byte[] QR) {
        this.ID = id;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.contacts = new HashMap<String,Contact>();
        this.QR = QR;
    }

    /**
     * Getter
     *
     * @return
     */
    public byte[] getQR() {
        return QR;
    }

    /**
     * Setter
     *
     * @param QR
     */
    public void setQR(byte[] QR) {
        this.QR = QR;
    }

    /**
     * Getter
     *
     * @return
     */
    public int getID(){return ID;}

    /**
     * Setter
     *
     * @param id
     */
    public void setID(int id){this.ID = id;}

    /**
     * Getter
     *
     * @return
     */
    public String getUsername() { return username; }

    /**
     * Setter
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter
     *
     * @return
     */
    public Key getPrivateKey() {
        return privateKey;
    }

    /**
     * Setter
     *
     * @param privateKey
     */
    public void setPrivateKey(Key privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Getter
     *
     * @return
     */
    public Key getPublicKey() {
        return publicKey;
    }

    /**
     * Setter
     *
     * @param publicKey
     */
    public void setPublicKey(Key publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Getter
     *
     * @return
     */
    public HashMap<String, Contact> getContacts() {
        return contacts;
    }

    /**
     * Setter
     *
     * @param contacts
     */
    public void setContacts(HashMap<String, Contact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Changes the name of a contact
     *
     * @param oldName Old name of the contact
     * @param newName New name of the contact
     * @return  {@code true} if update went well, {@code false} otherwise (i.e., {@code oldName] was not present,
     *          or {@code newName} is empty or already in the contacts)
     */
    public Boolean updateContactName(String oldName, String newName) {
        if(!contacts.containsKey(oldName)) {
            return false;
        } else if (newName.isEmpty() || contacts.containsKey(newName)) {
            return false;
        }
        Contact c = contacts.get(oldName);
        c.setName(newName);
        contacts.remove(oldName);
        contacts.put(c.getName(),c);
        return true;
    }

    /**
     * Removes a contact.
     *
     * @param username  the contact to be removed
     * @return  {@code true} if the contact was removed, {@code false} otherwise
     *          (i.e., {@code username} was not in the contacts)
     */
    public Boolean removeContact(String username) {
        if(!contacts.containsKey(username)) {
            return false;
        }
        contacts.remove(username);
        return true;
    }
}
