package airline.datastructure;

import airline.model.Customer;

public class LinkList<E extends Comparable<E>> {
    LinkNode<E> head;

    // 初始化链表，建立哨兵头节点
    public LinkList() {
        head = new LinkNode<E>();
        head.next = null;
    }

    // 有序插入，按compareTo升序排列，保持链表有序
    public void insert(E data) {
        LinkNode<E> p = head;
        while (p.next != null && p.next.data.compareTo(data) < 0) {
            p = p.next;
        }
        LinkNode<E> s = new LinkNode<E>(data);
        s.next = p.next;
        p.next = s;
    }

    // 按客户姓名查找，返回匹配的Customer节点数据
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

    // 按客户姓名删除节点，删除成功返回true，未找到返回false
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

    // 返回哨兵头节点，调用方通过head.next遍历实际元素
    public LinkNode<E> getHead() {
        return head;
    }

    // 判断链表是否为空
    public boolean isEmpty() {
        return head.next == null;
    }

    // 返回链表元素个数
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