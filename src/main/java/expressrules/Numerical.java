package expressrules;

public class Numerical extends Value {

    private double value;

    public Numerical(double value) {
        this.value = value;
    }

    public Value power(Value value) {
        if (!(value instanceof Numerical)) throw new OperatorNotAllowedException();
        return new Numerical (Math.pow(this.value, ((Numerical)value).value));
    }

    @Override
    public Value multiply(Value value) {
        return null;
    }

    @Override
    public Value divide(Value value) {
        return null;
    }

    @Override
    public Value add(Value value) {
        return null;
    }

    @Override
    public Value subtract(Value value) {
        return null;
    }

    @Override
    public Boolean eq(Value value) {
        return null;
    }

    @Override
    public Boolean lt(Value value) {
        return null;
    }

    @Override
    public Boolean gt(Value eval2) {
        return null;
    }

    @Override
    public Boolean le(Value eval2) {
        return null;
    }

    @Override
    public Boolean ge(Value eval2) {
        return null;
    }

    @Override
    public Boolean getBoolean() {
        return new Boolean(value!=0);
    }

    @Override
    public int compareTo(Value o) {
        return 0;
    }
}
