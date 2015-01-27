
public class Reservation_Entry {
	private boolean busy;
	private int op;
	private int vj;
	private int vk;
	private int qj;
	private int qk;
	private int dest;
	private int A;
	private Command command;
	
	//only  need to write back once
	private boolean done;

	public Reservation_Entry ( Command command) {
		this.busy = true;
		this.command = command;
		this.qj = -1;
		this.qk = -1;
		this.done = false;
	}
	
	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	public int getOp() {
		return op;
	}

	public void setOp(int op) {
		this.op = op;
	}

	public int getVj() {
		return vj;
	}

	public void setVj(int vj) {
		this.vj = vj;
	}

	public int getVk() {
		return vk;
	}

	public void setVk(int vk) {
		this.vk = vk;
	}

	public int getQj() {
		return qj;
	}

	public void setQj(int qj) {
		this.qj = qj;
	}

	public int getQk() {
		return qk;
	}

	public void setQk(int qk) {
		this.qk = qk;
	}

	public int getDest() {
		return dest;
	}

	public void setDest(int dest) {
		this.dest = dest;
	}

	public int getA() {
		return A;
	}

	public void setA(int a) {
		A = a;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
}
