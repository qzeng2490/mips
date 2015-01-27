import java.util.LinkedList;
import java.util.Queue;

import basic_entry.ROB_Entry;
public class ROB {
	private int N = 6;
	private int size ;
	private Queue<ROB_Entry> queue;
	public ROB () {
		this.queue = new LinkedList<ROB_Entry>();
		this.size = 0;
	}
	
	public Queue<ROB_Entry> getQueue(){
		return queue;
	}
	
	public int size(){
		return size;
	}
	
	public ROB_Entry get(int rob_id){
		for(ROB_Entry r:queue){
			if(r.getEntry() == rob_id) return r;
		}
		return null;
	}
	
	public boolean add(ROB_Entry r){
		if(isFull()) return false;
		this.queue.add(r);
		size++;
		return true;
	}
	public ROB_Entry poll(){
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
