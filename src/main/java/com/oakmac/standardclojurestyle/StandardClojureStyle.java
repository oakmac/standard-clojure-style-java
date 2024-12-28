package com.oakmac.standardclojurestyle;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StandardClojureStyle {
    /**
     * Reverses the input string.
     * @param input The string to reverse
     * @return The reversed string, or empty string if input is null
     */
    public String reverse(String input) {
        if (input == null) {
            return "";
        }
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * Counts the number of vowels in the input string.
     * @param input The string to count vowels in
     * @return The number of vowels, or 0 if input is null
     */
    public int countVowels(String input) {
        if (input == null) {
            return 0;
        }
        return (int) input.toLowerCase()
            .chars()
            .mapToObj(ch -> (char) ch)
            .filter(ch -> "aeiou".indexOf(ch) >= 0)
            .count();
    }

    /**
     * Capitalizes the first letter of each word in the input string.
     * @param input The string to capitalize
     * @return The capitalized string, or empty string if input is null
     */
    public String capitalizeWords(String input) {
        if (input == null) {
            return "";
        }
        return Arrays.stream(input.split("\\s+"))
            .map(word -> word.isEmpty() ? "" : 
                Character.toUpperCase(word.charAt(0)) + 
                word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "))
            .trim();
    }
}

