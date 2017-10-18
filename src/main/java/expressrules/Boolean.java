package expressrules;

public class Boolean extends Value {

    private final boolean value;

    public Boolean(boolean b) {
        this.value = b;
    }

    public Value power(Value value) {
        throw new OperatorNotAllowedException();
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
        return this;
    }

    @Override
    public int compareTo(Value o) {
        return 0;
    }

    public Boolean not() {
        return null;
    }
}
