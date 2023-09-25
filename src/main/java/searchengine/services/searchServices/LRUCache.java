package searchengine.services.searchServices;

import java.util.HashMap;

public class LRUCache<K, V>{

    public static class Node<T, U> {
        Node<T, U> previous;
        Node<T, U> next;
        T key;
        U value;

        public Node(Node<T, U> previous, Node<T, U> next, T key, U value){
            this.previous = previous;
            this.next = next;
            this.key = key;
            this.value = value;
        }

        public U getValue() {
            return this.value;
        }
    }

    public HashMap<K, Node<K, V>> cache;
    public Node<K, V> leastRecentlyUsed;
    public Node<K, V> mostRecentlyUsed;
    private final int maxSize;
    private int currentSize;

    public LRUCache(int maxSize){
        this.maxSize = maxSize;
        this.currentSize = 0;
        leastRecentlyUsed = new Node<>(null, null, null, null);
        mostRecentlyUsed = leastRecentlyUsed;
        cache = new HashMap<>();
    }

    public V get(K key){
        Node<K, V> tempNode = cache.get(key);
        if (tempNode == null){
            return null;
        } else if (tempNode.key == mostRecentlyUsed.key){
            return mostRecentlyUsed.value;
        }

        Node<K, V> nextNode = tempNode.next;
        Node<K, V> previousNode = tempNode.previous;

        if (tempNode.key == leastRecentlyUsed.key){
            nextNode.previous = null;
            leastRecentlyUsed = nextNode;
        } else {
            previousNode.next = nextNode;
            nextNode.previous = previousNode;
        }

        tempNode.previous = mostRecentlyUsed;
        mostRecentlyUsed.next = tempNode;
        mostRecentlyUsed = tempNode;
        mostRecentlyUsed.next = null;
        return tempNode.value;
    }

    public void put(K key, V value){
        if (cache.containsKey(key)){
            return;
        }

        Node<K, V> myNode = new Node<>(mostRecentlyUsed, null, key, value);
        mostRecentlyUsed.next = myNode;
        cache.put(key, myNode);
        mostRecentlyUsed = myNode;

        if (currentSize == maxSize){
            cache.remove(leastRecentlyUsed.key);
            leastRecentlyUsed = leastRecentlyUsed.next;
            leastRecentlyUsed.previous = null;
        } else if (currentSize < maxSize){
            if (currentSize == 0){
                leastRecentlyUsed = myNode;
            }
            currentSize++;
        }
    }

    public boolean keyIsPresent(K key){
        return cache.containsKey(key);
    }
}
