package com.dsa.varchecking;

import algorithm.Trie;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@SpringBootTest
class VarCheckingApplicationTests {

  @Autowired
  ResourceLoader resourceLoader;

  @Test
  void testTrie_GivenSimplePatterns() {
    Trie trie = new Trie();

    // List of patterns
    List<String> patterns = Arrays.asList("he", "she", "his", "hers");
    for (String pattern : patterns) {
      trie.addPattern(pattern);
    }

    // Build failure links
    trie.buildFailureLinks();

    // Text to search
    String text = "ahishershers";
    Map<String, List<Integer>> results = trie.search(text);

    // Print results
    System.out.println("Match results:");
    for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
      System.out.printf("Pattern '%s' found at positions: %s%n", entry.getKey(), entry.getValue());
    }
  }

  @Test
  void testReadPdf_GivenPdfFile() throws IOException {
    Resource resource = resourceLoader.getResource("classpath:statements/file-1.pdf");
    File file = resource.getFile();
    PDDocument document = PDDocument.load(file);
    PDFTextStripper stripper = new PDFTextStripper();
    String text = stripper.getText(document);
    System.out.print(text);
    document.close();
  }

}
