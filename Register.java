import java.util.ArrayList;

import basic_entry.Register_Entry;;
public class Register {
	public static int N =32;
	public static ArrayList<Register_Entry> registers;
	
	public Register(){
		registers = new ArrayList<Register_Entry>();
		for(int i=0;i<N;i++){
			registers.add(new Register_Entry());
		}
	}
	
	public void setValue(int i,int value){
		if(registers.get(i).isBusy()) return;
		registers.get(i).setValue(value);
	}
	
	public Register_Entry get(int i){
		return registers.get(i);
	}
	
}
