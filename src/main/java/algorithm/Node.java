package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Node {
  Map<Character, Node> children = new HashMap<>();
  Node fail;
  List<String> output = new ArrayList<>();
}