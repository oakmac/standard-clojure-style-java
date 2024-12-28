package com.oakmac.standardclojurestyle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StandardClojureStyleTest {
    private final StandardClojureStyle scsLib = new StandardClojureStyle();

    @Test
    void testReverse() {
        assertEquals("olleh", scsLib.reverse("hello"));
        assertEquals("", scsLib.reverse(""));
        assertEquals("", scsLib.reverse(null));
        assertEquals("a", scsLib.reverse("a"));
        assertEquals("321", scsLib.reverse("123"));
    }

    @Test
    void testCountVowels() {
        assertEquals(2, scsLib.countVowels("hello"));
        assertEquals(0, scsLib.countVowels(""));
        assertEquals(0, scsLib.countVowels(null));
        assertEquals(5, scsLib.countVowels("aEiOu"));
        assertEquals(0, scsLib.countVowels("xyz"));
    }

    @Test
    void testCapitalizeWords() {
        assertEquals("Hello World", scsLib.capitalizeWords("hello world"));
        assertEquals("", scsLib.capitalizeWords(""));
        assertEquals("", scsLib.capitalizeWords(null));
        assertEquals("A B C", scsLib.capitalizeWords("a b c"));
        assertEquals("This Is A Test", scsLib.capitalizeWords("THIS is a TEST"));
        assertEquals("Single", scsLib.capitalizeWords("single"));
    }
}