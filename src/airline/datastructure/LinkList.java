package airline.datastructure;

import airline.model.Customer;

public class LinkList<E extends Comparable<E>> {
    LinkNode<E> head;

    public LinkList() {
        head = new LinkNode<E>();
        head.next = null;
    }

    public void insert(E data) {
        LinkNode<E> p = head;
        while (p.next != null && p.next.data.compareTo(data) < 0) {
            p = p.next;
        }
        LinkNode<E> s = new LinkNode<E>(data);
        s.next = p.next;
        p.next = s;
    }

    public E search(String name) {
        LinkNode<E> p = head.next;
        while (p != null) {
            if (p.data instanceof Customer && ((Customer) p.data).name.equals(name)) {
                return p.data;
            }
            p = p.next;
        }
        return null;
    }

    public boolean delete(String name) {
        LinkNode<E> pre = head;
        LinkNode<E> p = head.next;
        while (p != null) {
            if (p.data instanceof Customer && ((Customer) p.data).name.equals(name)) {
                pre.next = p.next;
                return true;
            }
            pre = p;
            p = p.next;
        }
        return false;
    }

    public LinkNode<E> getHead() {
        return head;
    }

    public boolean isEmpty() {
        return head.next == null;
    }

    public int size() {
        int cnt = 0;
        LinkNode<E> p = head.next;
        while (p != null) {
            cnt++;
            p = p.next;
        }
        return cnt;
    }
}