package edu.weber.bhubbard.cs3270.mobilewar.gameobjects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Bret on 6/18/2015.
 */


public class Card implements Serializable {

    Suit s;
    Rank r;

    public Card(Suit suit, Rank rank) {
        s = suit;
        r = rank;
    }

    public int CompareTo(Card other) {
        //If negative they win.
        return r.compareTo(other.getRank());
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        return byteArrayOutputStream.toByteArray();
    }

    public static Card deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (Card) objectInputStream.readObject();
    }

    public Suit getSuit() {
        return s;
    }

    public Rank getRank() {
        return r;
    }
}
