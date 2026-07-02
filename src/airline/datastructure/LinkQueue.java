package airline.datastructure;

public class LinkQueue<E> {
    private LinkNode<E> front;
    private LinkNode<E> rear;

    public LinkQueue() {
        front = null;
        rear = null;
    }
    
    //判断当前队列是否为空
    public boolean empty() {
        return front == null;
    }

    //入队    
    public void push(E e ) {
		LinkNode<E> s = new LinkNode<E>(e);
		if (empty()) {
			front = rear = s;
		}
		else {
			rear.next = s;
			rear = s;
		}
	}

    //出队
    public 	E pop() {
		E e;
		if (empty()) {
			throw new IllegalArgumentException("队空");
		}
		if (front == rear) {
			e = front.data;
			front = rear = null;
		}
		else {
			e = front.data;
			front = front.next;
		}
		return e;
	}

    //取对头元素
    public E peek() {
        if (front == null) return null;
        return front.data;
    }

    //队列大小
    public int size() {
        int count = 0;
        LinkNode<E> s = front;
        while (s != null) {
            count++;
            s = s.next;
        }
        return count;
    }

    //取对头
    public LinkNode<E> getFront() {
        return front;
    }
}