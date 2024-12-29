package com.dsa.varchecking;

import algorithm.Trie;
import entity.Statement;
import entity.StatementRequest;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@SpringBootTest
class VarCheckingApplicationTests {

  @Autowired
  ResourceLoader resourceLoader;

  private final String URL = "https://api.tracuusaoke.com/search";

  private final int PAGE_LIMIT = 1000; // 13809

  private final int TIMEOUT = 5000;

  private final int BUFFER_SIZE = 20;

  @Test
  void testTrie_GivenSimplePatterns() {
    Trie trie = new Trie();

    // List of patterns
    List<String> patterns = Arrays.asList("he", "she", "his", "hers", "z");
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

  @Test
  void testFetchVarCheckingData_GivenURL() {
    Trie trie = new Trie();
    // List of patterns
    List<String> patterns = List.of("NGUYEN VAN NAM");
    for (String pattern : patterns) {
      trie.addPattern(pattern);
    }

    // Build failure links
    trie.buildFailureLinks();

    // Text to search
    String text = "MBVCB.7026357059.Nguyen Thu Thuy - Ung ho dong bao Lu Lut.CT tu NGUYEN VAN NAM toi MAT TRAN TO QUOC VN - BAN CUU TRO TW";
    Map<String, List<Integer>> results = trie.search(text);

    // Print results
    System.out.println("Match results:");
    for (Map.Entry<String, List<Integer>> entry : results.entrySet()) {
      System.out.printf("Pattern '%s' found at positions: %s%n", entry.getKey(), entry.getValue());
    }
  }


}
