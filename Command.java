public class Command {
	private int opcode;
	private int rs;
	private int rt;
	private int rd;
	private int shamt;
	private int func;
	
	private int address;
	private String instruction;
	
	private int imm;
	
	
	private int lw=0;
	
	// write result should be used in next cycle
	private boolean write_execute_j = false;
	
	private boolean write_execute_k = false;
	
	// branch and jump excution outcome should be reflected in next cycle
	private boolean branch_jump = true;
	
	//check break instruction
	private static boolean flag = false;
	// assert 
	public Command(int i, int addr){
		assert(addr%4 == 0 && addr < 716); 
		String instr = String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0');
		opcode = Integer.parseInt(instr.substring(0, 6), 2);
		rs = Integer.parseInt(instr.substring(6,11),2);
		rt = Integer.parseInt(instr.substring(11, 16),2);
		rd = Integer.parseInt(instr.substring(16, 21),2);
		shamt = Integer.parseInt(instr.substring(21,26),2);
		func = Integer.parseInt(instr.substring(26,32),2);
		
		setAddress(addr);
		Decode(i);
	}

	
	private void Decode(int instr){
		int immediate;
		int uimmediate;
		int address;
		int jumpAddr;
		int offset;

	    //unsigned immediate
	    uimmediate = instr & 0xffff;
	    
	    //the sign of immediate
	    immediate = uimmediate >>15;
	    if (immediate == 0) {
	        immediate = uimmediate;
	    }else{
	        immediate = uimmediate|0xffff0000;
	    }
	    
	    //signed offset
	    offset = immediate<<2;
	    address = instr & 0x3ffffff;
	    
	    jumpAddr = address<<2;
		
		switch(opcode) {
		case 0x00:
            switch (func) {
                case 0x00:
                	if (instr == 0 && flag){
                		instruction = "0";
                		break;
                	}
                    if (instr == 0) {
                        instruction = "NOP";
                        break;
                    }
                    //xp: changed from rs to rt in 3 following instr.
                    instruction = "SLL R"+rd+", R"+rt+", "+shamt;
                    break;
                case 0x02:
                	instruction = "SRL R"+rd+", R"+rt+", "+shamt;
                    break;
                case 0x03:
                	instruction = "SRA R"+rd+", R"+rt+", "+shamt;
                    break;
                case 0x20:
                	instruction = "ADD R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x0d:
                    instruction = "BREAK";
                    flag = true;
                    break;
                case 0x21:
                	instruction = "ADDU R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x22:
                	instruction = "SUB R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x23:
                	instruction = "SUBU R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x24:
                	instruction = "AND R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x25:
                	instruction = "OR R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x26:
                	instruction = "XOR R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x27:
                	instruction = "NOR R"+rd+", R"+rs+", R"+rt;
                    break;
                case 0x2a:
                	instruction = "SLT R"+rd+", R"+rs+", R"+rt;
                    break;
                //xp:added sltu instr.
                case 0x2b:
                	instruction = "SLTU R"+rd+", R"+rs+", R"+rt;
                    break;
                default:
                    instruction = "unsurported instructions";
                    break;
            }
            break;
        case 0x01:
            if (rs == 0) {
            	instruction = "BLTZ R"+rs+", #"+offset;
            	imm = offset;
            }else if(rs == 1){
            	instruction = "BGEZ R"+rs+", #"+offset;
            	imm = offset;
            	
            }
            break;
        case 0x02:
        	instruction = "J #"+jumpAddr;
        	imm = jumpAddr;
            break;
        case 0x04:
            instruction = "BEQ R" + rs +", R"+rt+", #"+offset;
            imm = offset;
            break;
        case 0x05:
        	instruction = "BNE R" + rs +", R"+rt+", #"+offset;
        	imm = offset;
            break;
        case 0x06:
        	instruction = "BLEZ R" + rs +", #"+offset;
        	imm = offset;
            break;
        case 0x07:
        	instruction = "BGTZ R" + rs +", #"+offset;
        	imm = offset;
            break;
        case 0x08:
        	instruction = "ADDI R" + rt + ", R" + rs +", #"+immediate;
        	imm = immediate;
            break;
        case 0x09:
        	instruction = "ADDIU R" + rt + ", R" + rs +", #"+immediate;
        	imm = immediate;
            break;
        case 0x0a:
        	instruction = "SLTI R" + rt + ", R" + rs +", #"+immediate;
        	imm = immediate;
            break;
        case 0x0b:
        	instruction = "SLTIU R" + rt + ", R" + rs +", #"+immediate;
        	imm = immediate;
            break;
        case 0x23:
        	instruction = "LW R" + rt + ", " + immediate +"(R"+rs+")";
        	imm = immediate;
            break;
        case 0x2b:
        	instruction = "SW R" + rt + ", " + immediate +"(R"+rs+")";
        	imm = immediate;
            break;
            
        default:
        	instruction = "unsurported instructions";
            break;

		}
	    
	    
	}
	

	public String getInstr(){
		return this.instruction;
	}
	
	public String operation() {
		return this.instruction.split(" ")[0];
	}
	
	
	public boolean equals(Command c){
		return this.address == c.address;
	}
	
	public int getOpcode() {
		return opcode;
	}

	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}

	public int getRt() {
		return rt;
	}

	public void setRt(int rt) {
		this.rt = rt;
	}

	public int getRs() {
		return rs;
	}

	public void setRs(int rs) {
		this.rs = rs;
	}

	public int getRd() {
		return rd;
	}

	public void setRd(int rd) {
		this.rd = rd;
	}

	public int getShamt() {
		return shamt;
	}

	public void setShamt(int shamt) {
		this.shamt = shamt;
	}

	public int getFunc() {
		return func;
	}

	public void setFunc(int func) {
		this.func = func;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}
	
	public String toString(){
		return "["+ instruction + "]" ;
		
	}


	public int getImm() {
		return imm;
	}


	public void setImm(int imm) {
		this.imm = imm;
	}


	public int getLw() {
		return lw;
	}


	public void setLw(int lw) {
		this.lw = lw;
	}


	public boolean isBranch_jump() {
		return branch_jump;
	}


	public void setBranch_jump(boolean branch_jump) {
		this.branch_jump = branch_jump;
	}


	public boolean isWrite_execute_j() {
		return write_execute_j;
	}


	public void setWrite_execute_j(boolean write_execute_j) {
		this.write_execute_j = write_execute_j;
	}


	public boolean isWrite_execute_k() {
		return write_execute_k;
	}


	public void setWrite_execute_k(boolean write_execute_k) {
		this.write_execute_k = write_execute_k;
	}


	

	
}
