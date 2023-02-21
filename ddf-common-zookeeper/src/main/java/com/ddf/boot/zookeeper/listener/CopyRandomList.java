package com.ddf.boot.zookeeper.listener;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/02/11 17:18
 */
public class CopyRandomList {

    @AllArgsConstructor
    @Data
    @Accessors(chain = true)
    static class Node {
        private Integer id;
        private Integer val;
        private Node next;
        private Node random;

        @Override
        public String toString() {
            return "Node{" + "val=" + val + ", next=" + next + ", random=" + (random == null ? "null" : random.getVal()) + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Node node = (Node) o;
            return Objects.equals(getId(), node.getId()) && Objects.equals(getVal(), node.getVal()) && Objects.equals(
                    getNext(), node.getNext()) && Objects.equals(getRandom(), node.getRandom());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getVal(), getNext(), getRandom());
        }
    }

    public static void main(String[] args) {


        final Node first = new Node(1, 1, null, null);
        final Node second = new Node(1, 1, null, null);
        System.out.println(Objects.equals(first, second));
        final HashMap<Node, Node> map = Maps.newHashMap();
        map.put(first, first);
        map.put(second, second);
        System.out.println("map = " + map.size());


        String nodeStr = "7,'';13,0;11,4;10,2;1,0";
        final Node head = buildNode(nodeStr);
        printNode(head);
        System.out.println("===========================");

        final Node copyNode = copyNode(head);
        printNode(copyNode);
        System.out.println("===========================");

        nodeStr = "7,'';7,'';11,1;7,''";
        final Node head2 = buildNode(nodeStr);
        printNode(head2);
        System.out.println("===========================");

        final Node copyNode2 = copyNode(head2);
        printNode(copyNode2);
        System.out.println("===========================");
    }

    public static Node buildNode(String nodeStr) {
        final String[] split = nodeStr.split(";");
        List<Node> nodeList = new ArrayList<>();
        for (int i = 0, length = split.length; i < length; i++) {
            String s = split[i];
            final String[] item = s.split(",");
            nodeList.add(new Node(i, Integer.parseInt(item[0]), null, null));
        }
        for (int i = 0; i < nodeList.size(); i++) {
            final Node node = nodeList.get(i);
            if (i < nodeList.size() - 1) {
                node.next = nodeList.get(i + 1);
            }
            final String[] item = split[i].split(",");
            if (item[1] != null && !"''".equals(item[1])) {
                node.random = nodeList.get(Integer.parseInt(item[1]));
            }
        }
        return nodeList.get(0);
    }

    public static void printNode(Node node) {
        while (true) {
            if (node == null) {
                break;
            }
            System.out.printf("val: %s\tnext:%s\trandom:%s", node.getVal(), node.getNext() == null ? null : node.getNext().getVal(), node.getRandom() == null ? null : node.getRandom().getVal());
            System.out.println();
            Node next = node.next;
            if (next != null) {
                printNode(next);
                return;
            } else {
                break;
            }
        }
    }

    public static Node copyNode(Node node) {
        Node currentNode = node;
        Map<Integer, Node> nodeMap = new HashMap<>();
        while (currentNode != null) {
            nodeMap.put(currentNode.getId(), new Node(currentNode.getId(), currentNode.getVal(), null, null));
            currentNode = currentNode.next;
        }
        Node next;
        currentNode = node;
        while (currentNode != null) {
            next = currentNode.getNext();
            if (next != null) {
                nodeMap.get(currentNode.getId()).setNext(nodeMap.get(next.getId()));
            }
            if (currentNode.getRandom() != null) {
                nodeMap.get(currentNode.getId()).setRandom(nodeMap.get(currentNode.getRandom().getId()));
            }
            currentNode = next;
        }
        return nodeMap.get(node.getId());
    }
}
