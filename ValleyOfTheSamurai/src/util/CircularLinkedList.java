package util;

import java.util.LinkedList;

/*
Submission by Andrew Roberts (Student Number: 20450942)
 */
public class CircularLinkedList {
    LinkedList<Node> list = new LinkedList<>();
    Node curr;

    public CircularLinkedList() {
        curr = null;
    }

    public void insert(Node node) {
        if (list.size()>0) {
            list.getLast().setNext(node);
            list.add(node);
            node.setNext(list.getFirst());
        } else list.add(node);
        curr = list.getFirst();
    }

    public void remove(Node node) {
        if (list.contains(node)) {
            for (Node temp : list) {
                if (temp.getNext() == node) {
                    temp.setNext(node.getNext());
                    list.remove(node);
                    break;
                }
            }
        } else System.out.println("Node not found in list");
    }
    public Node getFirst() {
        return list.getFirst();
    }

    public Node get(int i) {
        return list.get(i);
    }
    public Node next() {
        curr = curr.getNext();
        return curr;
    }
    public Node getCurr() {
        if (this.curr!=null) {
            return this.curr;
        } else return null;
    }
}
