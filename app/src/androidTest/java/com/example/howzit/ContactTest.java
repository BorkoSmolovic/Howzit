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

In addition, as a special exception, the copyright holders of Howzit give you permission to combine
Howzit with free software programs or libraries that are released under the GNU LGPL and with code
included in the standard release of JUnit under the Eclipse Public License - v 1.0 (or modified versions
of such code, with unchanged license). You may copy and distribute such a system following the terms
of the GNU LGPL for Howzit and the licenses of the other code concerned.

Note that people who make modified versions of Howzit are not obligated to grant this special exception
for their modified versions; it is their choice whether to do so. The GNU General Public License
gives permission to release a modified version without this exception; this exception
also makes it possible to release a modified version which carries forward this exception.
 */
package com.example.howzit;

import com.example.howzit.messages.TextMessage;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ContactTest {
    Contact SUT; // System under test (SUT)

     @Before
     public void setup() throws Exception {
         //TODO: find out how public key is initialized and add to test
         SUT = new Contact("Bob", 0, "192.168.1.13",true, null);
         LinkedList<TextMessage> conversations = new LinkedList<TextMessage>();
         conversations.add(new TextMessage(1,0,"By Azura, By Azura, By Azura!", "", "", null));
         conversations.add(new TextMessage(0,1,"It's the Grand Champion!", "", "", null));

         SUT.setConversation(conversations);
     }

    @Test
    public void getName() throws Exception {
        assertThat(SUT.getName(), is("Bob"));
    }

     @Test
    public void setName() throws Exception {
        String name = "Alice";
        SUT.setName(name);
        assertThat(SUT.getName(), is(name));
    }

    @Test
    public void getID() throws Exception {
        assertThat(SUT.getID(), is(0));
    }

    @Test
    public void setID() throws Exception {
        int id = 1;
        SUT.setID(id);
        assertThat(SUT.getID(), is(id));
    }

    @Test
    public void getIP() throws Exception {
        assertThat(SUT.getIP(), is("192.168.1.13"));
    }
    @Test
    public void setIP() throws Exception {
        String ip = "192.168.1.99";
        SUT.setIP(ip);
        assertThat(SUT.getIP(), is(ip));
    }

    @Test
    public void getPublicKey() throws Exception {
        //TODO: add real key in this test
    }

    @Test
    public void setPublicKey() throws Exception {
        //TODO: add real key in this test
    }

    @Test
    public void getConversation() throws Exception {
        assertThat(SUT.getConversation().get(0).getReceiverID(),is(1));
        assertThat(SUT.getConversation().get(0).getSenderID(),is(0));
        assertThat(SUT.getConversation().get(0).getText(),is("By Azura, By Azura, By Azura!"));
        assertThat(SUT.getConversation().get(1).getReceiverID(),is(0));
        assertThat(SUT.getConversation().get(1).getSenderID(),is(1));
        assertThat(SUT.getConversation().get(1).getText(),is("It's the Grand Champion!"));
    }

    @Test
    public void setConversation() throws Exception {
        LinkedList<TextMessage> conversations = new LinkedList<TextMessage>();
        conversations.add(new TextMessage(1,0,"You say hello!", "", "", null));
        conversations.add(new TextMessage(0,1,"And I say goodbye!", "", "", null));
        SUT.setConversation(conversations);
        assertThat(SUT.getConversation().get(0), is(conversations.get(0)));
        assertThat(SUT.getConversation().get(1), is(conversations.get(1)));
    }

    @Test
    public void isOnline() throws Exception {
        assertThat(SUT.isOnline(),is(true));
    }

    @Test
    public void setOnline() throws Exception{
        boolean online = false;
        SUT.setOnline(online);
        assertThat(SUT.isOnline(),is(online));
    }

    @Test
    public void getMessage_toHighIndex_returnNull() throws Exception{
        assertNull(SUT.getMessage(Integer.MAX_VALUE));
    }

    @Test
    public void getMessage_toLowIndex_returnNull() throws Exception{
         //TODO: this fails, is this something that could happen?
        assertNull(SUT.getMessage(Integer.MIN_VALUE));
    }

    @Test
    public void getMessage_indexMinusOne_returnLastMessage() throws Exception{
        LinkedList<TextMessage> conversations = new LinkedList<TextMessage>();
        conversations.add(new TextMessage(1,0,"You say hello!", "", "", null));
        conversations.add(new TextMessage(0,1,"And I say goodbye!", "", "", null));
        conversations.add(new TextMessage(1,0,"Hello! hello!", "", "", null));
        SUT.setConversation(conversations);
        assertEquals("Hello! hello!", SUT.getMessage(-1).getText());
    }

    @Test
    public void getMessage_indexInValidInterval_returnIndexessage() throws Exception {
        LinkedList<TextMessage> conversations = new LinkedList<TextMessage>();
        conversations.add(new TextMessage(1,0,"Stop! You have violated the Law!!","", "", null));
        conversations.add(new TextMessage(0,1,"Pay the court a fine or serve your sentance.", "", "", null));
        conversations.add(new TextMessage(1,0,"Your stolen goods are now forfeit", "", "", null));
        SUT.setConversation(conversations);
        assertEquals("Pay the court a fine or serve your sentance.", SUT.getMessage(1).getText());
    }

    @Test
    public void addMessage_emptyList_addElement() throws Exception {
        LinkedList<TextMessage> conversations = new LinkedList<TextMessage>();
        SUT.setConversation(conversations);
        SUT.addMessage(new TextMessage(0,1,"She sells seashells by the seashore", "", "", null));
        assertEquals("She sells seashells by the seashore", SUT.getMessage(0).getText());
    }

    @Test
    public void addMessage_fullList_addElement() throws Exception {
        LinkedList<TextMessage> conversations = new LinkedList<TextMessage>();
        conversations.add(new TextMessage(1,0,"Stop! You have violated the Law!!", "", "", null));
        conversations.add(new TextMessage(0,1,"Pay the court a fine or serve your sentance.", "", "", null));
        SUT.setConversation(conversations);
        SUT.addMessage(new TextMessage(0,1,"Your stolen goods are now forfeit", "", "", null));
        assertEquals("Your stolen goods are now forfeit", SUT.getMessage(2).getText());
    }
}