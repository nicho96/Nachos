package nachos.threads;

import java.util.ArrayList;

class PriorityQueue<T extends Comparable<T>> {

    public PriorityQueue(){
        array = new ArrayList<T>();
    }

    public void insert(T t){
        array.add(t);
        percolateUp(t);
    }

    public T poll(){
        T tmp = array.get(0);
        if(array.size() > 1){
        	array.set(0, array.remove(array.size() - 1));
        	heapify();
        }else{
        	array.remove(0);
        }
        return tmp;
    }

    public void percolateUp(T t){
        int h = array.size();
        while (h > 1 && t.compareTo(array.get(h / 2 - 1)) > 0) {
            array.set(h - 1, array.get(h / 2 - 1));
            h = h / 2;
        }
        array.set(h - 1, t);
    }

    public void percolateDown(int r){
        int c = 2 * r;
        while (c - 1 < array.size()) {
            if (c < array.size() && array.get(c - 1).compareTo(array.get(c)) < 0) {
            	c += 1;
            }
            if(array.get(r - 1).compareTo(array.get(c - 1)) < 0){
           		T tmp = array.get(r - 1);
           		array.set(r - 1, array.get(c - 1));
           		array.set(c - 1, tmp);
           		r = c;
           		c = 2 * c;
           	} else {
           		return;
           	}
        }
    }

    public void heapify(){
         int r = array.size() / 2;
         while (r >= 0) {
            percolateDown(r + 1);
            r--;
         }
    }

    public boolean isEmpty(){
        return array.isEmpty();
    }
    
    public T peek(){
    	if (array.size() == 0)
    		return null;
    	else
    		return array.get(0);
    }

    public void print(){
    	System.out.println(array);
    }
    
    public int size(){
    	return array.size();
    }
    
    private ArrayList<T> array;

}
