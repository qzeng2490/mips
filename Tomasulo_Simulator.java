import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import basic_entry.BTB_Entry;
import basic_entry.ROB_Entry;
import basic_entry.Reservation_Entry;

public class Tomasulo_Simulator {
	static int pc = 600;
	static int cycle = 0;
	static ArrayList<Command> commands = new ArrayList<Command>();
	static ArrayList<Data> data = new ArrayList<Data>();
	static ROB rob = new ROB();
	static Reservation rs = new Reservation();
	static BTB btb = new BTB();
	static Queue<Command> iq = new LinkedList<Command>();
	static Register registers = new Register();
	static HashSet<String> branchInstructions = new HashSet<String>();
	
	static int btbid = 1;
	
	static int robid = 1;
	
	static boolean end = false;
	
	public void simulator(String inputFile,String outputFile,String requiredCycle){
		BinaryIn in = new BinaryIn(inputFile);
		Out out = new Out(outputFile);		
		init(in,out);
		pc = 600;
		int startCycle;
		int endCycle;
		if (!requiredCycle.equalsIgnoreCase("")) {
			requiredCycle = requiredCycle.replace("-T", "");
			String[] tokens = requiredCycle.split(":");
			startCycle = Integer.parseInt(tokens[0]);
			endCycle = Integer.parseInt(tokens[1]);
			System.out.println("start:" + startCycle + " end:" + endCycle);
			if(startCycle==0 && endCycle==0){
				do{
					cycle();
				}while(!end);
				print(out);
			}else if(startCycle>0 && endCycle>0 && endCycle>=startCycle){
				while(startCycle != 1 && cycle < startCycle -1) {
					cycle();
				}
				do{
					cycle();
					print(out);
				}while(cycle < endCycle && !end);
			}else {
				throw new IllegalArgumentException("arg[0] =  inputfilename" + 
						"arg[1] =  outputfilename"+
						"arg[2] =  (optional) -Tm:n , where m=start cycle, n=end cycle");
			}
		}else{
			do{
				cycle();
				print(out);
			}while(!end);
		}
	}

	public static void cycle(){
		commit();
		writeback();
		execute();	
		issue();
		fetch();
		cycle++;
	}
	
	public static void print(Out out){
		out.println("Cycle <"+cycle+">:");
		out.println("IQ:");
		for(Command c:iq){
			out.println(c);
		}
		out.println("RS:");
		for(Reservation_Entry e:rs.getQueue()){
			out.println(e.getCommand());
		}
		out.println("ROB:");
		for(ROB_Entry e:rob.getQueue()){
			out.println(e.getInstruction());
		}
		out.println("BTB:");
		for(BTB_Entry e:btb.getArr()){
			out.println(e);
		}
		out.println("Registers:");
		out.print("R00:");
		printHelper(out, 0);
		out.print("R08:");
		printHelper(out, 8);
		out.print("R16:");
		printHelper(out, 16);
		out.print("R24:");
		printHelper(out, 24);
		
		out.println("Data Segment:");
		for(int addr = 716,k=0;addr < 716+4*data.size();addr+=40,k+=10){
			printHelper1(out,addr,k);
		}
	}
	public static void printHelper1(Out out,int addr,int k){
		out.print(addr+":");
		for(int i=k;i<k+10 && i<data.size();i++){
			out.print("    "+data.get(i));
		}
		out.print("\n");
	}
	public static void printHelper(Out out,int k){
		for(int i=k;i<k+8;i++){
			out.print("    "+registers.get(i));
		}
		out.print("\n");
	}
	
	/*
	 *determine the end of program in cycle function 
	 *assume five stage get normal instructions
	 */
	public static void fetch() {
		int index = (pc - 600)/4;
		Command c = commands.get(index);
		
		if(!c.getInstr().equals("0"))iq.add(c);
		
		// if it is a new branch instruction
		//System.out.println(c.operation());
		if(btb.get(c)== null && branchInstructions.contains(c.operation())){
			//if target do not get its value?? 
			int target = pc;
			switch(c.operation()){
				case "J":
					target = Integer.parseInt(c.getInstr().split("#")[1]);
					break;
				case "BEQ":
				case "BNE":
				case "BLTZ":
				case "BLEZ":
				case "BGTZ":
				case "BGEZ":
				
					target = pc+4+Integer.parseInt(c.getInstr().split("#")[1]);
					break;
				default :
					break;
			}
			btb.add(new BTB_Entry(btbid++,pc,target));
		}
		pc = btb.target(c);
	}
	
	//fetch operands to rs is they are available in rs or rob
	// register r 
	public static void issueHelper(Command c){
		int source = c.getRs();
		int target = c.getRt();
		int dest = c.getRd();
		
		Reservation_Entry rs_entry = new Reservation_Entry(c);
		ROB_Entry rb_entry = new ROB_Entry(robid++,c);
		
		if(c.operation().equals("NOP") || c.operation().equals("BREAK")){
			rob.add(rb_entry);
			rb_entry.setReady(true);
			return;
		}
		rs.add(rs_entry);
		rob.add(rb_entry);
		rs_entry.setBusy(true);
		rs_entry.setDest(rb_entry.getEntry());
		//xp:all op excepte SLL,SRL and SRA;
		if(!c.operation().equals("J")&&!c.operation().equals("SLL")&&!c.operation().equals("SRL")&&!c.operation().equals("SRA")){
			if(registers.get(source).isBusy()) {
				int h = registers.get(source).getReorder();
				if(rob.get(h).isReady()) {
					
					rs_entry.setVj(rob.get(h).getValue());
					rs_entry.setQj(0);
				}else {
					rs_entry.setQj(h);
				}
			}else {
				rs_entry.setVj(registers.get(source).getValue());
				rs_entry.setQj(0);
			}
		}
		
		//xp:added sll,srl and sra
		else if(c.operation().equals("SLL")||c.operation().equals("SRL")||c.operation().equals("SRA")){
			if(registers.get(target).isBusy()) {
				int h = registers.get(target).getReorder();
				if(rob.get(h).isReady()) {
					
					rs_entry.setVk(rob.get(h).getValue());
					rs_entry.setQk(0);
				}else {
					rs_entry.setQk(h);
				}
			}else {
				rs_entry.setVk(registers.get(target).getValue());
				rs_entry.setQk(0);
			}
		}
		
		// rt and rd have different situations
		
		switch(c.operation()){
		
			// RT is the destination and update A with imm
			case "ADDI":
			case "ADDIU":
			case "SLTI":
			case "SLTIU":
			case "LW":
				rb_entry.setRegister(registers.get(target));
				registers.get(target).setReorder(rb_entry.getEntry());
				registers.get(target).setBusy(true);
				rs_entry.setA(c.getImm());
				break;
				
			// RD is the destination and no rt
			//xp: should no rs here
			case "SLL":
			case "SRL":
			case "SRA":
				rb_entry.setRegister(registers.get(dest));
				registers.get(dest).setReorder(rb_entry.getEntry());
				registers.get(dest).setBusy(true);
				rs_entry.setA(c.getShamt());
				break;
			// get RT (source oprand) and no destination 
			case "SW":
			case "BEQ":
			case "BNE":
				if(registers.get(target).isBusy()) {
					int h = registers.get(target).getReorder();
					if(rob.get(h).isReady()) {
						
						rs_entry.setVk(rob.get(h).getValue());
						rs_entry.setQk(0);
					}else {
						rs_entry.setQk(h);
					}
				}else {
					rs_entry.setVk(registers.get(target).getValue());
					rs_entry.setQk(0);
				}
				rs_entry.setA(c.getImm());
				break;
			// only  rs and imm
			//xp: J has no rs?
			case "J":
			case "BLTZ":
			case "BGEZ":
			case "BLEZ":
			case "BGTZ":
				rs_entry.setA(c.getImm());
				break;
		
			default :
				if(registers.get(target).isBusy()) {
					int h = registers.get(target).getReorder();
					if(rob.get(h).isReady()) {
						
						rs_entry.setVk(rob.get(h).getValue());
						rs_entry.setQk(0);
					}else {
						rs_entry.setQk(h);
					}
				}else {
					rs_entry.setVk(registers.get(target).getValue());
					rs_entry.setQk(0);
				}
				
				rb_entry.setRegister(registers.get(dest));
				registers.get(dest).setReorder(rb_entry.getEntry());
				registers.get(dest).setBusy(true);
				break;
		}
	}
	
	public static void issue() {
		// wait for rob and rs
		if(iq.isEmpty()) return;
		if(rob.isFull() || rs.isFull()) return;		
		Command c = iq.poll();
		issueHelper(c);
		
	}
	
	// check in  the reservation to make sure that all the LW and SW have get their address
	// ready false means they are ready
	public static boolean checkLWSW(Command c){
		for(Reservation_Entry e:rs.getQueue()){
			if(e.getCommand().equals(c)) break;
			if(e.getCommand().equals("LW") || e.getCommand().equals("SW")){
				
				if(e.isBusy()) return true;
			}
		}
		return false;
	}
	
	
	// mispredict
	public static void flush(Command c){
		Reservation temprs = new Reservation();
		ROB temprob = new ROB();
		iq.clear();
		for(Reservation_Entry e: rs.getQueue()){
			temprs.add(e);
			if(e.getCommand().equals(c)) break;
			
		}
		for(ROB_Entry e: rob.getQueue()){
			temprob.add(e);
			if(e.getInstruction().equals(c)) break;
		}
		rs = temprs;
		rob = temprob;
	}
	
	public static void executeHelper(Reservation_Entry e){
		Command c =e.getCommand();
		if(c.isWrite_execute_j()){
			c.setWrite_execute_j(false);
			return;
		}
		if(c.isWrite_execute_k()){
			c.setWrite_execute_k(false);
			return;
		}
		
		switch(c.operation()){
		
			// RT is the destination and update A with imm
			case "ADDI":
			case "ADDIU":
				if(e.getQj() == 0 ){
					e.setA(e.getA() + e.getVj());
					e.setBusy(false);
				}
				break;
			// calculate the address	
			case "LW":
				if(e.getQj() == 0 && !checkLWSW(c)){
					e.setA(e.getA() + e.getVj());
					e.setBusy(false);
				}
				break;
				
			case "SW":
				if(e.getQj() == 0 && e.getQk() == 0 && !checkLWSW(c)){
					e.setBusy(false);
				}
				
				if(e.getQj() == 0 && !checkLWSW(c)){
					int index = (e.getA() + e.getVj()-716)/4;
					for(ROB_Entry re:rob.getQueue()){
						if(re.getEntry() == e.getDest()) {
							re.setMemAdd(index);						
							re.setReady(true);
						}
					}
				}
				
				if(e.getQk() == 0) {
					for(ROB_Entry re:rob.getQueue()){
						if(re.getEntry() == e.getDest()) {
							re.setValue(e.getVk());
							re.setReady(true);
						}
					}
				}
				break;
			//xp	
			case "SLTI":
			case "SLTIU":
				if(e.getQj() == 0 ){
					if(e.getA() > e.getVj()){
						e.setA(1);
					}else{
						e.setA(0);
					}
					e.setBusy(false);
				}
				break;
			// RD is the destination and no rs
			//xp for three following instr.
			case "SLL":
				if(e.getQk() == 0 ){
					e.setA(e.getVk()<<e.getA());
					e.setBusy(false);
				}
				break;
			case "SRL"://logic shift right
				if(e.getQk() == 0 ){
					e.setA(e.getVk()>>>e.getA());
					e.setBusy(false);
				}
				break;
			case "SRA"://arithmetic shift right
				if(e.getQk() == 0 ){
					e.setA(e.getVk()>>e.getA());
					e.setBusy(false);
				}
				break;
			// get RT (source oprand) and no destination 

			case "BEQ":
				if(c.isBranch_jump()){
					c.setBranch_jump(false);
					return;
				}
				if(e.getQj() ==0 && e.getQk() == 0){
					// update BTB .  Not taken, but calculation is taken  
					if(e.getVj() == e.getVk() && btb.target(c) == c.getAddress()+4){
						btb.get(c).setOutcom("1");
						flush(c);
						pc = btb.target(c);
					}else if (e.getVj() != e.getVk() && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("0");
						flush(c);
						pc = c.getAddress()+4;
					}else if (e.getVj() == e.getVk() && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("1");
					}else if (e.getVj() != e.getVk() && btb.target(c) == c.getAddress()+4) {
						btb.get(c).setOutcom("0");
					}
					c.setBranch_jump(true);
					e.setBusy(false);
				}
				break;
			//xp
			case "BNE":
				if(c.isBranch_jump()){
					c.setBranch_jump(false);
					return;
				}
				if(e.getQj() ==0 && e.getQk() == 0){
					// update BTB .  Not taken, but calculation is taken  
					if(e.getVj() != e.getVk() && btb.target(c) == c.getAddress()+4){
						btb.get(c).setOutcom("1");
						flush(c);
						pc = btb.target(c);
					}else if (e.getVj() == e.getVk() && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("0");
						flush(c);
						pc = c.getAddress()+4;
					}else if (e.getVj() != e.getVk() && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("1");
					}else if (e.getVj() == e.getVk() && btb.target(c) == c.getAddress()+4) {
						btb.get(c).setOutcom("0");
					}
					c.setBranch_jump(true);
					e.setBusy(false);
				}
				break;
			// only  rs and imm
			case "J":
				if(c.isBranch_jump()){
					c.setBranch_jump(false);
					return;
				}
				// mispredict
				if(btb.target(c) == c.getAddress() +  4){
					btb.get(c).setOutcom("1");
					flush(c);
					pc = btb.target(c);
				}
				
				c.setBranch_jump(true);
				e.setBusy(false);
				break;
			case "BLTZ":
				if(c.isBranch_jump()){
					c.setBranch_jump(false);
					return;
				}
				if(e.getQj() ==0){
					// update BTB .  Not taken, but calculation is taken  
					if(e.getVj() < 0 && btb.target(c) == c.getAddress()+4){
						btb.get(c).setOutcom("1");
						flush(c);
						pc = btb.target(c);
					}else if (e.getVj() >=0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("0");
						flush(c);
						pc = c.getAddress()+4;
					}else if (e.getVj() < 0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("1");
					}else if (e.getVj() >= 0 && btb.target(c) == c.getAddress()+4) {
						btb.get(c).setOutcom("0");
					}
					c.setBranch_jump(true);
					e.setBusy(false);
				}
				break;
			case "BGEZ":
				if(c.isBranch_jump()){
					c.setBranch_jump(false);
					return;
				}
				if(e.getQj() ==0){
					// update BTB .  Not taken, but calculation is taken  
					if(e.getVj() >= 0 && btb.target(c) == c.getAddress()+4){
						btb.get(c).setOutcom("1");
						flush(c);
						pc = btb.target(c);
					}else if (e.getVj() < 0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("0");
						flush(c);
						pc = c.getAddress()+4;
					}else if (e.getVj() >= 0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("1");
					}else if (e.getVj() < 0 && btb.target(c) == c.getAddress()+4) {
						btb.get(c).setOutcom("0");
					}
					c.setBranch_jump(true);
					e.setBusy(false);
				}
				break;
			case "BLEZ":
				if(c.isBranch_jump()){
					c.setBranch_jump(false);
					return;
				}
				if(e.getQj() ==0){
					// update BTB .  Not taken, but calculation is taken  
					if(e.getVj() <= 0 && btb.target(c) == c.getAddress()+4){
						btb.get(c).setOutcom("1");
						flush(c);
						pc = btb.target(c);
					}else if (e.getVj() >0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("0");
						flush(c);
						pc = c.getAddress()+4;
					}else if (e.getVj() <= 0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("1");
					}else if (e.getVj() > 0 && btb.target(c) == c.getAddress()+4) {
						btb.get(c).setOutcom("0");
					}
					c.setBranch_jump(true);
					e.setBusy(false);
				}
				break;
			case "BGTZ":
				if(c.isBranch_jump()){
					c.setBranch_jump(false);
					return;
				}
				if(e.getQj() ==0){
					// update BTB .  Not taken, but calculation is taken  
					if(e.getVj() > 0 && btb.target(c) == c.getAddress()+4){
						btb.get(c).setOutcom("1");
						flush(c);
						pc = btb.target(c);
					}else if (e.getVj() <= 0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("0");
						flush(c);
						pc = c.getAddress()+4;
					}else if (e.getVj() > 0 && btb.target(c) != c.getAddress()+4){
						btb.get(c).setOutcom("1");
					}else if (e.getVj() <= 0 && btb.target(c) == c.getAddress()+4) {
						btb.get(c).setOutcom("0");
					}
					c.setBranch_jump(true);
					e.setBusy(false);
				}
				break;
			
				
			case "ADD":
			case "ADDU":
				if(e.getQj() ==0 && e.getQk() == 0){
					e.setA(e.getVj()+e.getVk());
					e.setBusy(false);
				}
				break;
			case "SUB":
			case "SUBU":
				if(e.getQj() ==0 && e.getQk() == 0){
					e.setA(e.getVj()-e.getVk());
					e.setBusy(false);
				}
				break;
			case "SLT":
			case "SLTU":
				if(e.getQj() ==0 && e.getQk() == 0){
					if(e.getVj()<e.getVk()){
						e.setA(1);
					}else{
						e.setA(0);
					}
					e.setBusy(false);
				}
				break;
			case "AND":
				if(e.getQj() ==0 && e.getQk() == 0){
					e.setA(e.getVj()&e.getVk());
					e.setBusy(false);
				}
				break;
			case "OR":
				if(e.getQj() ==0 && e.getQk() == 0){
					e.setA(e.getVj()|e.getVk());
					e.setBusy(false);
				}
				break;
			case "XOR":
				if(e.getQj() ==0 && e.getQk() == 0){
					e.setA(e.getVj()^e.getVk());
					e.setBusy(false);
				}
				break;
			case "NOR":
				if(e.getQj() ==0 && e.getQk() == 0){
					e.setA(~(e.getVj()|e.getVk()));
					e.setBusy(false);
				}
				break;
			default :
				break;
		}
		
	}
	
	public static void execute() {
		for(Reservation_Entry e:rs.getQueue()){
			if(e.isBusy()) {
				executeHelper(e);
			}
		}
	}
	// check in ROB is the same as check in RS
	// false means do no find
	// no same address
	public static boolean checkLW_IN_RSROB(Reservation_Entry e0){
		for(Reservation_Entry e:rs.getQueue()){
			if(e == e0) return false;
			if(e.getCommand().operation().equals("SW") && !e.isBusy() &&e.getA() == e0.getA()){
				return true;
			}
		}
		return false;
	}
	
	public static void writebackHelper(Reservation_Entry e){
		
		Command c = e.getCommand();
		if(c.operation().equals("LW") && checkLW_IN_RSROB(e)) return;
		
		if(c.operation().equals("LW") && c.getLw() ==0) {
			c.setLw(1);
			int index = (e.getA() - 716)/4;
			e.setA( data.get(index).getValue());
			//e.setDone(true);
			return;
		}
		if(c.operation().equals("LW")) {
			e.setDone(true);
			c.setLw(0);
		}
		// resume Lw
		
      //write result		
		for(ROB_Entry rb : rob.getQueue()){
			if(rb.getEntry() == e.getDest()) {
				rb.setReady(true);
				rb.setValue(e.getA());
			}
		}
		
		// update  reservation station 
		// excution need to start in next cycle 
		for(Reservation_Entry re: rs.getQueue()){
			if(re.getQj() == e.getDest()) {
				re.setVj(e.getA());
				re.setQj(0);
				//re.getCommand().setWrite_execute(true);
				re.getCommand().setWrite_execute_j(true);
			}
			if(re.getQk() == e.getDest()) {
				re.setVk(e.getA());
				re.setQk(0);
				//re.getCommand().setWrite_execute(true);
				re.getCommand().setWrite_execute_k(true);
			}
		}
		
		e.setDone(true);
	}
	
	public static void writeback(){
		for(Reservation_Entry e:rs.getQueue()){
			if(!e.isDone() && !e.isBusy() && !e.getCommand().operation().equals("SW")){
				writebackHelper(e);
			} 
		}
	}
	
	
	public static void commit(){
		ROB_Entry h = rob.getQueue().peek();
		if(h==null || !h.isReady()) return;
		

		
		if(h.isBusy()) {
			h.setBusy(false);
			return;
		}
		
		h = rob.poll();
		rs.poll();

		switch(h.getInstruction().operation()){
			case "SW":
				data.get(h.getMemAdd()).setValue(h.getValue());
				break;
			case "BREAK":
				end = true;
				break;
			//exclude branch and jump operation and NOP
			case "J":
			case "BLTZ":
			case "BGEZ":
			case "BLEZ":
			case "BGTZ":
			case "BEQ":
			case "BNE":
			case "NOP":
				break;
			default:
				h.getRegister().setValue(h.getValue());
				h.getRegister().setBusy(false);
				break;
		}
		

		h = rob.getQueue().peek();

		if(h != null &&h.isReady() &&h.isBusy()) {
			h.setBusy(false);
		}
		
	}
	
	public static void init (BinaryIn in, Out out){
		while(!in.isEmpty() && pc < 716){
			int x = in.readInt();
			commands.add(new Command(x,pc));
			//out.println(new Command(x,pc));
			pc = pc+4;
		}
		while(!in.isEmpty()){
			int x = in.readInt();
			data.add(new Data(x,pc));
			pc = pc+4;
		}
		branchInstructions.add("J");
		branchInstructions.add("BEQ");
		branchInstructions.add("BNE");
		branchInstructions.add("BGEZ");
		branchInstructions.add("BGTZ");
		branchInstructions.add("BLEZ");
		branchInstructions.add("BLTZ");
	}

}
