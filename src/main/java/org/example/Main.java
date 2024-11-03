package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    static final int numOfLines = 1000;
    static NGram nGram = new NGram(2);
    public static void main(String[] args) {

        /*
                TODO: Instead of sending numOfLines do numOfSentences. That way we can make sure not to split a sentence when sending to the model.
         */



        String filePath = "src/main/resources/all_tswift_lyrics.txt"; // Replace with the path to your file

        //this has logical errors (it could cut off or start mid sentence due to being separated by line and not sentence)
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath)); // need to split up
            String line;
            int counter = 0;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null)
            {
                if(counter<numOfLines)
                {
                    sb.append(line).append("\n");
                    counter++;
                }else
                {
                    counter=0;
                    processChunk(sb.toString()); // need to save freq and pass it in to maintain the list of frequencies
                    sb.setLength(0);
                }
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }

        if(nGram != null)
        {
            System.out.println(nGram.generateText(200,true));
        }else
        {
            System.out.println("ERROR: NGRAM NULL");
        }
    }
    static void processChunk(String text)
    {
        String preprocessedText = nGram.preprocess(text);
//        nGram.buildBigramModel(preprocessedText);
        nGram.buildNGramModel(preprocessedText);
    }
}