package org.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGram {
    final char[] endOfSentenceChars = {'.','!','?'};
    int n;
    Frequencies freq = new Frequencies();
    public NGram(int n)
    {
        this.n = n;
    }

    public String preprocess(String text)
    {

        // need to add <s> and </s> at the start and end of sentences
        String[] sentences = text.split("(?<=[.!?])"); // this splits by '.' , '!', and '?'. can try to replace with endOfSentenceChars array so that it can be modified easier
        String[] tokenizedSentences =  Arrays.stream(sentences).map(s -> "<s> " + s.toLowerCase() + " </s> ").toArray(String[]::new);
        return String.join("", tokenizedSentences);
    }

    public Frequencies buildNGramModel(String processedText) {
//        String[] tokens = processedText.split("\\s+"); // Splits by whitespace to get individual words

        Pattern pattern = Pattern.compile("\\w+|[.,!?'\"]|<s>|</s>|\\n");
        Matcher matcher = pattern.matcher(processedText);

        ArrayList<String> words = new ArrayList<>();

        // Find all matches and add them to the list
        while (matcher.find()) {
            words.add(matcher.group());
        }
        String[] tokens = words.toArray(String[]::new);
        for (int i = 0; i < tokens.length - n + 1; i++) {
            // Collect n-gram as a key, based on `n`
            String[] nGramWords = Arrays.copyOfRange(tokens, i, i + n);
            String nGramKey = String.join(" ", nGramWords);

            // Update n-gram counts in a generalized manner
            freq.ngram.put(nGramKey, freq.ngram.getOrDefault(nGramKey, 0) + 1);

            // Update unigram counts
            for (String word : nGramWords) {
                freq.unigram.put(word, freq.unigram.getOrDefault(word, 0) + 1);
            }
        }

        return freq;
    }

    public float nGramProbability(String[] prefix, String word, boolean smoothing) {
        String nGramKey = String.join(" ", prefix) + " " + word;
        String prefixKey = String.join(" ", prefix);

        int nGramCount = freq.ngram.getOrDefault(nGramKey, 0);
        int prefixCount = freq.ngram.getOrDefault(prefixKey, 0);

        // Apply smoothing if enabled
        if (smoothing) {
            int vocabSize = freq.unigram.size();
            return (float) (nGramCount + 1) / (prefixCount + vocabSize);
        } else {
            return prefixCount == 0 ? 0 : (float) nGramCount / prefixCount;
        }
    }
    public String generateText(int numOfWords, boolean smoothing) {
        Random random = new Random();
        StringBuilder generatedText = new StringBuilder("<s>");
        List<String> context = new ArrayList<>(Collections.singletonList("<s>"));

        for (int i = 1; i < numOfWords; i++) {
            // Get possible next words and their probabilities
            HashMap<String, Float> candidates = new HashMap<>();

            // Collect n-grams starting with the current context
            for (String nGram : freq.ngram.keySet()) {
                if (nGram.startsWith(String.join(" ", context))) {
                    String nextWord = nGram.split(" ")[context.size()];
                    float probability = nGramProbability(context.toArray(new String[0]), nextWord, smoothing);
                    candidates.put(nextWord, probability);
                }
            }

            // Select next word based on weighted probabilities
            String nextWord = selectRandomWord(candidates, random);

            // If we reach the end token, start a new sentence
            if (nextWord.equals("</s>")) {
                generatedText.append(" </s> <s>");
                context = new ArrayList<>(Collections.singletonList("<s>"));
                continue;
            }

            // Append the word and update context
            generatedText.append(" ").append(nextWord);
            if (context.size() >= n - 1) context.removeFirst();
            context.add(nextWord);
        }

        return formatGeneratedText(generatedText.toString());
    }

    private String formatGeneratedText(String generatedText)
    {
        return generatedText
                .replace(" <s>", "")
                .replace("</s>", "")
                .replace(" ' ","'")
                .replaceAll(" ([.!?,])", "$1")
                .replace("\" ", "\"");

    }
    // Utility method to select a word based on weighted probabilities
    private String selectRandomWord(Map<String, Float> candidates, Random random) {
        float totalProbability = candidates.values().stream().reduce(0.0f, Float::sum);
        float randomValue = random.nextFloat() * totalProbability;
        float cumulativeProbability = 0.0f;

        for (Map.Entry<String, Float> entry : candidates.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (cumulativeProbability >= randomValue) {
                return entry.getKey();
            }
        }
        return "</s>"; // fallback if no word found
    }

}
