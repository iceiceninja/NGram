package org.example;

import java.util.HashMap;

public class Frequencies {
    // Have an array of objects that have a word and then an array of each preceding or
    HashMap<String, Integer> unigram;
    HashMap<String, Integer> ngram;

    public Frequencies()
    {
        this.unigram = new HashMap<>();
        this.ngram = new HashMap<>();
    }
}
