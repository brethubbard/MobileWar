package edu.weber.bhubbard.cs3270.mobilewar.gameobjects;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Bret on 6/18/2015.
 */
public class Deck extends LinkedList<Card> {

    public Deck() {

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                this.add(new Card(suit, rank));
            }
        }
    }

    public ArrayList<Hand> Deal() {

        Collections.shuffle(this);

        Hand myHand = new Hand();
        myHand.addAll(Lists.partition(this, this.size() / 2).get(0));
        Hand theirHand = new Hand();
        theirHand.addAll(Lists.partition(this, this.size() / 2).get(1));
//this.size() / 2
        ArrayList<Hand> hands = new ArrayList<>();
        hands.add(myHand);
        hands.add(theirHand);
        return hands;
    }
}
