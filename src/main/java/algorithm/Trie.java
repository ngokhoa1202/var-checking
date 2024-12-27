package algorithm;

import java.util.*;

public class Trie {
  private Node root = null;

  public Trie() {
    this.root = new Node();
  }

  public void addPattern(String pattern) {
    Node current = root;
    for (char ch : pattern.toCharArray()) {
      current.children.putIfAbsent(ch, new Node());
      current = current.children.get(ch);
    }
    current.output.add(pattern);
  }

  public void buildFailureLinks() {
    Queue<Node> queue = new LinkedList<>();

    // Set the fail link of root's children to root
    for (Node child : root.children.values()) {
      child.fail = root;
      queue.add(child);
    }

    // Build failure links for the rest of the trie
    while (!queue.isEmpty()) {
      Node current = queue.poll();

      for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
        char ch = entry.getKey();
        Node child = entry.getValue();

        Node fail = current.fail;
        while (fail != null && !fail.children.containsKey(ch)) {
          fail = fail.fail;
        }
        child.fail = (fail != null) ? fail.children.get(ch) : root;

        // Merge outputs
        if (child.fail != null) {
          child.output.addAll(child.fail.output);
        }
        queue.add(child);
      }
    }
  }

  public Map<String, List<Integer>> search(String text) {
    Map<String, List<Integer>> results = new HashMap<>();
    Node current = root;

    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);

      // Follow fail links if the character is not found
      while (current != root && !current.children.containsKey(ch)) {
        current = current.fail;
      }

      if (current.children.containsKey(ch)) {
        current = current.children.get(ch);
      }

      // Record matches
      for (String pattern : current.output) {
        results.putIfAbsent(pattern, new ArrayList<>());
        results.get(pattern).add(i - pattern.length() + 1);
      }
    }

    return results;
  }
}