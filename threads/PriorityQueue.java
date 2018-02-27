package nachos.threads;

import java.util.ArrayList;

/**
* A PriorityQueue is a class which uses a heap struture (ArrayList implementation) 
* to simulate a priority queue while efficiently committing the greatest valued
* element to the root, set to be the next element to be pulled from the queue.  
*/
class PriorityQueue<T extends Comparable<T>> {
	
	/** 
	* Constructor: Initializes an empty ArrayList 
	*/
    public PriorityQueue(){
        array = new ArrayList<T>();
    }
	
	/**
	* Adds given element to the heap and resorts it
	* @param element to be insert
	*/
    public void insert(T t){
        array.add(t);
        percolateUp(t);
    }
	
	/**
	* Removes root element from heap, and reheapifies
	* @return root element
	*/
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
	
	/**
	* Swaps an element t up through the heap until sorted
	* @param pivot element
	*/
    public void percolateUp(T t){
        int h = array.size();
        while (h > 1 && t.compareTo(array.get(h / 2 - 1)) > 0) {
            array.set(h - 1, array.get(h / 2 - 1));
            h = h / 2;
        }
        array.set(h - 1, t);
    }
	
	/**
	* Swaps an element r down through the heap until appropriately positioned
	* @param pivot element
	*/
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
	
	/**
	* Swaps an element r down through the heap until appropriately positioned
	*/
    public void heapify(){
         int r = array.size() / 2;
         while (r >= 0) {
            percolateDown(r + 1);
            r--;
         }
    }
	
	/**
	* Checks if the heap is empty
	* @return true if heap is empty
	*/
    public boolean isEmpty(){
        return array.isEmpty();
    }
    
	/**
	* Gets the root element, without removing it from the heap
	* @return root element
	*/
    public T peek(){
    	if (array.size() == 0)
    		return null;
    	else
    		return array.get(0);
    }
	
	/**
	* Prints out the Arraylist contents to the console
	*/
    public void print(){
    	System.out.println(array);
    }
    
	/**
	* Gets size of the ArrayList
	* @return size
	*/
    public int size(){
    	return array.size();
    }
    
    private ArrayList<T> array;

}
