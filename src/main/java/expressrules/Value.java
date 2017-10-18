package expressrules;

public abstract class Value implements Comparable<Value> {


    public abstract Value power(Value value);

    public abstract Value multiply(Value value);

    public abstract Value divide(Value value);

    public abstract Value add(Value value);

    public abstract Value subtract(Value value);

    public abstract Boolean eq(Value value);

    public abstract Boolean lt(Value value);

    public abstract Boolean gt(Value eval2);

    public abstract Boolean le(Value eval2);

    public abstract Boolean ge(Value eval2);

    public abstract Boolean getBoolean();
}

