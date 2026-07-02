package airline.datastructure;

public class LinkNode<E> {
    public E data;
    public LinkNode<E> next;
    public LinkNode() {
        next = null;
    }

    public LinkNode(E d) {
        data = d;
        next = null;
    }
}