import java.io.File;

public class MIPSsim {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			if (args.length < 2) {
				System.out.println("Not enough args passed");
				System.out.println("arg[0] =  inputfilename");
				System.out.println("arg[1] =  outputfilename");
				System.out.println("arg[2] =  (optional) -Tm:n , where m=start cycle, n=end cycle");
				return;
			}
			int startCycle;
			int endCycle;
			String inputFileName = args[0];
			String outputFileName = args[1];
			String cycle ="";
			if(args.length  == 3 ) cycle = args[2];
			Tomasulo_Simulator ts = new Tomasulo_Simulator();
			ts.simulator(inputFileName, outputFileName, cycle);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
