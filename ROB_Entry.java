public class ROB_Entry {
	private int Entry;
	private boolean busy;
	private Command instruction;
	//private State state;
	private Register_Entry register;
	private int value;
	private boolean ready;
	private int memAdd;
	
	public ROB_Entry (int entry, Command command){
		this.Entry = entry;
		this.busy = true;
		this.instruction = command;
		//this.state = State.Execute;
		this.ready = false;
	}
	
	public int getEntry() {
		return Entry;
	}
	public void setEntry(int entry) {
		Entry = entry;
	}
	public boolean isBusy() {
		return busy;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	public Command getInstruction() {
		return instruction;
	}
	public void setInstruction(Command instruction) {
		this.instruction = instruction;
	}
//	public State getState() {
//		return state;
//	}
//	public void setState(State state) {
//		this.state = state;
//	}
	public Register_Entry getRegister() {
		return register;
	}
	public void setRegister(Register_Entry register) {
		this.register = register;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public int getMemAdd() {
		return memAdd;
	}

	public void setMemAdd(int memAdd) {
		this.memAdd = memAdd;
	}
}
