package com.dsa.varchecking;

import algorithm.Trie;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;


@SpringBootTest
class VarCheckingApplicationTests {

  @Autowired
  ResourceLoader resourceLoader;

  private final Object mutexLock = new Object();

  @BeforeEach
  public void prepare() {
    System.out.println("Preparing a test:.............................");
  }

  @AfterEach
  public void finish() {
    System.out.println("Finish a test:.................................");
  }

//  @Test
//  void testTrie_GivenSimplePatterns() {
//    Trie trie = new Trie();
//
//    // List of patterns
//    List<String> patterns = Arrays.asList("he", "she", "his", "hers", "z");
//    for (String pattern : patterns) {
//      trie.addPattern(pattern);
//    }
//
//    // Build failure links
//    trie.buildFailureLinks();
//
//    // Text to search
//    String text = "ahishershers";
//    Map<String, List<Integer>> results = trie.search(text);
//
//    // Print results
//    System.out.println("Match results:");
//    for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
//      System.out.printf("Pattern '%s' found at positions: %s%n", entry.getKey(), entry.getValue());
//    }
//  }

//  @Test
//  void testReadPdf_GivenPdfFile() throws IOException {
//    Resource resource = resourceLoader.getResource("classpath:statements/file-1.pdf");
//    File file = resource.getFile();
//    PDDocument document = PDDocument.load(file);
//    PDFTextStripper stripper = new PDFTextStripper();
//    String text = stripper.getText(document);
//    System.out.print(text);
//    document.close();
//  }


  @Test
  void testSequentialVarCheckingDataPageByPage_GivenLongText() throws IOException {
    Long startTime = System.currentTimeMillis();
    Trie trie = new Trie();
    // List of patterns
    List<String> patterns = List.of("NGUYEN THANH NAM", "VO QUOC TRANG", "LE THANH LONG");
    for (String pattern : patterns) {
      trie.addPattern(pattern);
    }

    // Build failure links
    trie.buildFailureLinks();
    Resource resource = resourceLoader.getResource("classpath:statements/file-1.pdf");
    // Text to search
    File file = resource.getFile();
    PDDocument document = PDDocument.load(file);
    PDFTextStripper stripper = new PDFTextStripper();

    int length = document.getNumberOfPages();
    System.out.println("Length of text string is: " + length);
    Map<String, List<Integer>> results = new HashMap<>();

    Map<String, List<Integer>> pageIndexes = new HashMap<>();
    for (String pattern : patterns) {
      pageIndexes.put(pattern, new LinkedList<>());
    }

    IntStream.range(1, length)
      .sequential()
      .forEach((page) -> {
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        try {
          String text = stripper.getText(document);
          Map<String, List<Integer>> result = trie.search(text);
          if (!result.isEmpty()) {
            result.forEach((pattern, pages) -> {
              pageIndexes.get(pattern).add(page);
              if (!results.containsKey(pattern)) {
                results.put(pattern, pages);
              } else {
                results.get(pattern).addAll(pages);
              }
            });
          }

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

    System.out.println("Match file indexes");
    for (Map.Entry<String, List<Integer>> entry : pageIndexes.entrySet()) {
      System.out.printf("Pattern '%s' found at page %s%n", entry.getKey(), entry.getValue());
    }

    System.out.println("Match results:");
    for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
      System.out.printf("Pattern '%s' found at positions: %s%n", entry.getKey(), entry.getValue());
    }
    Long endTime = System.currentTimeMillis();
    System.out.println("Total time taken: " + (endTime - startTime) + "ms");
  }

  @Test
  void testSequentialVarCheckingData_GivenLongText() throws IOException {
    Long startTime = System.currentTimeMillis();
    Trie trie = new Trie();
    // List of patterns
    List<String> patterns = List.of("NGUYEN DUC DUNG", "VUONG BA THINH");
    for (String pattern : patterns) {
      trie.addPattern(pattern);
    }

    // Build failure links
    trie.buildFailureLinks();
    Resource resource = resourceLoader.getResource("classpath:statements/file-1.pdf");
    // Text to search
    File file = resource.getFile();
    PDDocument document = PDDocument.load(file);
    PDFTextStripper stripper = new PDFTextStripper();

    String text = stripper.getText(document);
    long length = text.length();
    System.out.println("Length of text string is: " + length);

    Map<String, List<Integer>> results = trie.search(text);

    System.out.println("Match results:");
    for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
      System.out.printf("Pattern '%s' found at positions: %s%n", entry.getKey(), entry.getValue());
    }

    Long endTime = System.currentTimeMillis();
    System.out.println("Total time taken: " + (endTime- startTime) + "ms");
  }

  @Test
  void testParallelVarCheckingDataPageByPage_GivenLongText() throws IOException {
    Long startTime = System.currentTimeMillis();
    Trie trie = new Trie();
    // List of patterns
    List<String> patterns = List.of("NGUYEN THANH NAM", "VO QUOC TRANG", "LE THANH LONG");
    for (String pattern : patterns) {
      trie.addPattern(pattern);
    }

    // Build failure links
    trie.buildFailureLinks();
    Resource resource = resourceLoader.getResource("classpath:statements/file-1.pdf");
    // Text to search
    File file = resource.getFile();
    PDDocument document = PDDocument.load(file);
    PDFTextStripper stripper = new PDFTextStripper();

    int length = document.getNumberOfPages();
    System.out.println("Length of text string is: " + length);
    Map<String, List<Integer>> results = new HashMap<>();

    Map<String, List<Integer>> pageIndexes = new HashMap<>();
    for (String pattern : patterns) {
      pageIndexes.put(pattern, new LinkedList<>());
    }

    IntStream.range(1, length)
      .parallel()
      .forEach((page) -> {
        try {
          String text = "";
          synchronized (mutexLock) {
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            text = stripper.getText(document);
          }
          Map<String, List<Integer>> result = trie.search(text);
          if (!result.isEmpty()) {
            result.forEach((pattern, pages) -> {
              synchronized (mutexLock) {
                pageIndexes.get(pattern).add(page);
                if (!results.containsKey(pattern)) {
                  results.put(pattern, pages);
                } else {
                  results.get(pattern).addAll(pages);
                }
              }
            });
          }

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

    System.out.println("Match file indexes");
    for (Map.Entry<String, List<Integer>> entry : pageIndexes.entrySet()) {
      System.out.printf("Pattern '%s' found at page %s%n", entry.getKey(), entry.getValue());
    }

    System.out.println("Match results:");
    for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
      System.out.printf("Pattern '%s' found at positions: %s%n", entry.getKey(), entry.getValue());
    }
    Long endTime = System.currentTimeMillis();
    System.out.println("Total time taken: " + (endTime - startTime) + "ms");
  }
}
