public class Data {
	private int pc;
	private int value;
	
	public Data(int value, int pc){
		this.setPc(pc);
		this.setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}
	
	public String toString(){
		return ""+value;
	}
}
