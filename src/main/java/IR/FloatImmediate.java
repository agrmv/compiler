package IR;

/**
 *
 * @author agrmv
 */

public class FloatImmediate extends Immediate {

    public float val;

    public FloatImmediate(float val){
        this.isInteger = false;
        this.val = val;
    }
    public String toString(){
        return Float.toString(val);
    }
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "float";
	}
}
