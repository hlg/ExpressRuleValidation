package expressrules;

import java.util.*;

public class Aggregation extends Value  {
    Collection<Value> values = new ArrayList<Value>();


    public void addElement(Value value) {
        values.add(value);
    }

    @Override
    public Value subtract(Value value) {
        if (!(value instanceof Aggregation)) throw new OperatorNotAllowedException();
        // TODO difference;
        return new Aggregation();
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
        return new Boolean(!values.isEmpty());
    }

    public Value power(Value value) {
        throw new OperatorNotAllowedException();
    }

    @Override
    public Value multiply(Value value) {
        if (!(value instanceof Aggregation)) throw new OperatorNotAllowedException();
        // TODO intersection;
        return new Aggregation();
    }

    @Override
    public Value divide(Value value) {
        throw new OperatorNotAllowedException();
    }

    @Override
    public Value add(Value value) {
        if (!(value instanceof Aggregation)) throw new OperatorNotAllowedException();
        // TODO union
        return new Aggregation();
    }

    @Override
    public int compareTo(Value o) {
        // TODO compare
        return 0;
    }
}
