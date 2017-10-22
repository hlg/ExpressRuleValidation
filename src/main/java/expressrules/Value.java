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

    public static Value create(Object o) {
        if (o instanceof Collection) return new Aggregate((Collection)o);      // TODO: extend Collection implement Value
        if (o instanceof EntityAdapter) return new Entity((EntityAdapter)o);   // TODO: extend IdEObject implement Value (adapter)
        return null; //TODO rest if needed
    }

    public abstract Value resolveRef(String refName);
}

