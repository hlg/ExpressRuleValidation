package expressrules;

import java.util.Collection;

public abstract class Value  {

    private boolean aDouble;

    public abstract Value eq(Value eval2);

    public abstract Value lt(Value eval2);

    public abstract Value gt(Value eval2);

    public abstract Value le(Value eval2);

    public abstract Value ge(Value eval2);

    public abstract Value neq(Value eval2);

    public abstract Value in(Value eval2); // TODO use constains instead

    public abstract Value subtract(Value other);

    public abstract Value add(Value other);

    public abstract Value multiply(Value other);

    public abstract Value divide(Value other);

    public abstract Value power(Value visit);

    public abstract Value eqi(Value eval2);

    public abstract Value neqi(Value eval2);

    public static Value create(Object elem) {
        if(elem instanceof Collection) return new Aggregate((Collection) elem);
        if(elem instanceof String || elem instanceof Boolean) return new Simple((Comparable)elem);
        if(elem instanceof Number) return new Simple(((Number)elem).doubleValue());
        if(elem instanceof EntityAdapter) return new Entity((EntityAdapter) elem);
        if(elem.getClass().isEnum()) return new Simple(((Enum) elem).name());  // TODO involve adapter?
        // if(elem == null) return new Simple(null);
        throw new WrongTypeError("expecting Collection, String, Number, EntityAdapter, or enum, found "+ elem.getClass().getName()); //TODO something missing?
    }

    public abstract Value resolveRef(String refName);

    public abstract Value resolveIndex(Value start, Value end);

    public abstract Value or(Value visit);

    public abstract Value xor(Value visit);

    public abstract Value intDiv(Value visit);

    public abstract Value combine(Value operand);

    public abstract Value modulo(Value operand);

    public abstract Value and(Value operand);

    public abstract Object getValue();
}

