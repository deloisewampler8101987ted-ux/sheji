package airline.datastructure;

import airline.model.Customer;

public class LinkList<T extends Comparable<T>> {
    private LinkNode<T> head;
    private int size;

    public LinkList() {
        this.head = null;
        this.size = 0;
    }

    public void insert(T data) {
        LinkNode<T> newNode = new LinkNode<>(data);
        if (head == null || data.compareTo(head.data) < 0) {
            newNode.next = head;
            head = newNode;
        } else {
            LinkNode<T> prev = head;
            while (prev.next != null && prev.next.data.compareTo(data) < 0) {
                prev = prev.next;
            }
            newNode.next = prev.next;
            prev.next = newNode;
        }
        size++;
    }

    public T search(String name) {
        LinkNode<T> curr = head;
        while (curr != null) {
            if (curr.data instanceof Customer && ((Customer) curr.data).name.equals(name)) {
                return curr.data;
            }
            curr = curr.next;
        }
        return null;
    }

    public boolean delete(String name) {
        if (head == null) return false;
        if (head.data instanceof Customer && ((Customer) head.data).name.equals(name)) {
            head = head.next;
            size--;
            return true;
        }
        LinkNode<T> prev = head;
        while (prev.next != null) {
            if (prev.next.data instanceof Customer
                    && ((Customer) prev.next.data).name.equals(name)) {
                prev.next = prev.next.next;
                size--;
                return true;
            }
            prev = prev.next;
        }
        return false;
    }

    public LinkNode<T> getHead() {
        return head;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        return size;
    }
}