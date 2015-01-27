import java.util.ArrayList;

import basic_entry.BTB_Entry;
public class BTB {
	private int N =16;
	private int size ;
	private ArrayList<BTB_Entry> btb ;
	public BTB() {
		this.btb = new ArrayList<BTB_Entry>();
		this.size = 0;
	}
	public void add(BTB_Entry e){
		btb.add(e);
		size++;
	}
	
	public ArrayList<BTB_Entry> getArr(){
		return btb;
	}
	
	//BTB is large enough
	public void remove() {
		
	}
	
	public BTB_Entry get(Command c){
		for(BTB_Entry e:btb){
			if(e.getPc() == c.getAddress()) return e;
		}
		return null;
	}
		
	public int target(Command c){
		BTB_Entry e = get(c);
		if(e != null && e.getOutcom().equals("1")){
			return e.getTarget();
		}
		
		return c.getAddress() +4;
	}
	
}
