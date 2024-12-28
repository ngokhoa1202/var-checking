package com.dsa.varchecking;

import algorithm.Trie;
import entity.Statement;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
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

  private final int PAGE_LIMIT = 10; // 13809

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
    String pattern = "NGUYEN VAN NAM";
    trie.addPattern(pattern);

    List<Map<String,List<Integer>>> results = new ArrayList<>();

    IntStream.rangeClosed(1, 1)
      .boxed()
      .parallel()
      .forEach((page) -> {
        HttpClient httpClient = HttpClient.create()
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
          .responseTimeout(Duration.ofMillis(5000))
          .doOnConnected(conn ->
            conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
              .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)))
          .headers((headers) ->{
            headers.add("Content-Type", "application/json");
            headers.add("Accept", "application/json");
          })
          .baseUrl(String.format("%s?page=%s", this.URL, page.toString()));

        WebClient.builder()
          .clientConnector(new ReactorClientHttpConnector(httpClient))
          .build()
          .get()
          .exchangeToMono(response -> {

            return response.bodyToMono(new ParameterizedTypeReference<List<Statement>>() {});
          })
          .subscribe((statements) -> {
            statements.forEach(
              (statement) -> {
                Map<String, List<Integer>> result = trie.search(statement.note());
                results.add(result);
              }
            );
          });
      });
    System.out.println("Match results:");
    for (var result: results) {
      for (Map.Entry<String, List<Integer>> entry : result.entrySet()) {
        System.out.printf("Pattern '%s' found at positions: %s%n", entry.getKey(), entry.getValue());
      }
    }
  }


}
