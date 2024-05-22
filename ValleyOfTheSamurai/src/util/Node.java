package util;

/*
Submission by Andrew Roberts (Student Number: 20450942)
 */

public class Node {
    private String character;
    private String icon;
    private String desc;
    private Node next;

    public Node(String character, String icon, String desc) {
        this.character=character;
        this.icon=icon;
        this.desc=desc;
        this.next=null;
    }
    public Node getNext() {
        return this.next;
    }
    public void setNext(Node next) {
        this.next=next;
    }
    public String getCharacter() {
        return this.character;
    }
    public void setCharacter(String character) {
        this.character=character;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getDesc() {
        return this.desc;
    }
    public void setDesc(String desc) {
        this.desc=desc;
    }
}
