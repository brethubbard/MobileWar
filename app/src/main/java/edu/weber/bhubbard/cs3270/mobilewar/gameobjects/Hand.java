package edu.weber.bhubbard.cs3270.mobilewar.gameobjects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Bret on 6/18/2015.
 */
public class Hand extends LinkedList<Card> implements Serializable {

    public Card getCard() {
        return this.poll();
    }

    public void putCards(ArrayList<Card> cards) {
        addAll(cards);
    }


    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        return byteArrayOutputStream.toByteArray();
    }

    public static Hand deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (Hand) objectInputStream.readObject();
    }

}
