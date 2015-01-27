import java.util.LinkedList;
import java.util.Queue;

import basic_entry.Reservation_Entry;
public class Reservation {
	private int  N = 10;
	private int size;
	private Queue<Reservation_Entry> queue;
	public Reservation() {
		this.queue = new LinkedList<Reservation_Entry>();
		this.size = 0;
	}
	
	public boolean add(Reservation_Entry r){
		if(isFull()) return false; 
		queue.add(r);
		size++;
		return true;
	}
	
	public Queue<Reservation_Entry> getQueue(){
		return queue;
	}
	
	public Reservation_Entry poll(){
		if(isEmpty()) return null;
		size--;
		return queue.poll();
	}
	public boolean isFull(){
		return size == N;
	}
	public boolean isEmpty(){
		return size == 0;
	}
}
