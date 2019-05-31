package MIPSGenerator;

/**
 * @author agrmv
 * */

public class AssemblyHelper {

	private String opcode;
	private String parameter1, parameter2, parameter3;

	AssemblyHelper(String opcode, String param1, String param2, String param3) {
		this.opcode = opcode;
		this.parameter1 = param1;
		this.parameter2 = param2;
		this.parameter3 = param3;
	}
	String getOpcode() {
		return opcode;
	}

	public String toString() {
		String ret = opcode;
		if (!parameter1.equals("")) {
			ret = ret + " " + parameter1;
		}
		if (!parameter2.equals("")) {
			ret = ret + ", " + parameter2;
		}
		if (!parameter3.equals("")) {
			ret = ret + ", " + parameter3;
		}
		return ret;
	}
	
	
}
