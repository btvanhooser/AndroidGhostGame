package com.google.engedu.ghost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class SimpleDictionary implements GhostDictionary {
    private ArrayList<String> words;
    Random rand = new Random();

    public SimpleDictionary(InputStream wordListStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(wordListStream));
        words = new ArrayList<>();
        String line = null;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            if (word.length() >= MIN_WORD_LENGTH)
              words.add(line.trim());
        }
    }

    @Override
    public boolean isWord(String word) {
        return words.contains(word);
    }

    @Override
    public String getAnyWordStartingWith(String prefix) {
        // completely random first word if no prefix yet
        if (prefix == "")
            return words.get(rand.nextInt(words.size()));

        // run binary search to find a qualifying word from 'words' array list
        else
            return binarySearch(words,prefix);
    }

    @Override
    public String getGoodWordStartingWith(String prefix) {
        // grab one word from 'words'
        String first = binarySearch(words,prefix);

        // return null if nothing exists
        if (first == null)
            return null;
        ArrayList<String> results = new ArrayList<>();

        // determine if the first word is a good word
        // general rule: if both the prefix and the chosen word are odd, or both even, then that word is a good word.
        if (prefix.length()%2 == first.length()%2)
            results.add(first);
        int lower = words.indexOf(first) - 1;
        int upper = words.indexOf(first) + 1;

        // use while loop to expand outward from first word and grab all words with prefix
        while (lower != -1 || upper != words.size()){
            if (lower != -1){
                // check if still in good range
                if ((words.get(lower).length() >= prefix.length())
                        && (words.get(lower).substring(0,prefix.length()).equals(prefix))){
                    // check if good word
                    if ((words.get(lower).length()%2 == prefix.length()%2))
                        results.add(words.get(lower));
                    lower--;
                }
                else
                    lower = -1;
            }
            if (upper != words.size()){
                // check if still in good range
                if ((words.get(upper).length() >= prefix.length())
                        && (words.get(upper).substring(0,prefix.length()).equals(prefix))){
                    // check if good word
                    if (words.get(upper).length()%2 == prefix.length()%2)
                        results.add(words.get(upper));
                    upper++;
                }
                else
                    upper = words.size();
            }
        }

        // nothing found, use getAnyWordStartingWith method to get any word, regardless if it is good
        if (results.size() == 0)
            return getAnyWordStartingWith(prefix);

        // grab a random result and return it
        return results.get(rand.nextInt(results.size()));
    }

    private static String binarySearch(ArrayList<String> arr, String target){
        int low = 0; int high = arr.size() - 1; int mid = high/2;

        // run while loop until left index crossed over right index, or vice versa
        while (low <= high){

            // return if the selected word contains prefix
            if (target.length() <= arr.get(mid).length() && target.equals(arr.get(mid).substring(0,target.length())))
                return arr.get(mid);

            // if the selected word is higher in the dictionary than the prefix, check bottom end of dictionary
            else if (target.compareTo(arr.get(mid)) < 0)
                high = mid - 1;

            // check top end
            else
                low = mid + 1;

            // recalculate the mid index
            mid = (high + low)/2;
        }

        // nothing found
        return null;
    }
}
