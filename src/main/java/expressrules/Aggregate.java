package expressrules;

import org.bimserver.emf.IdEObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Aggregate extends Value {

    List<Value> value;

    public Aggregate() {
        super();
        value = new ArrayList<Value>();
    }

    public Aggregate(Collection collection) {
        value = new ArrayList<Value>();
        for(Object elem : collection){
            if(elem instanceof String || elem instanceof Boolean) value.add(new Simple((Comparable)elem));
            if(elem instanceof Number) value.add(new Simple(((Number)elem).doubleValue()));
            if(elem instanceof EntityAdapter) value.add(new Entity((EntityAdapter) elem));
        }
    }

    @Override
    public Value eq(Value eval2) {
        return null;   // TODO
    }

    @Override
    public Value lt(Value eval2) {
        throw new WrongTypeError();
    }

    @Override
    public Value gt(Value eval2) {
        throw new WrongTypeError();
    }

    @Override
    public Value le(Value eval2) {
        throw new WrongTypeError();
    }

    @Override
    public Value ge(Value eval2) {
        throw new WrongTypeError();
    }

    @Override
    public Value neq(Value eval2) {
       return null; // TODO
    }

    @Override
    public Value in(Value eval2) {
        return null;  // TODO
    }

    @Override
    public Value subtract(Value other) {
       return null; // TODO
    }

    @Override
    public Value add(Value other) {
        return null; // TODO
    }

    @Override
    public Value multiply(Value other) {
        return null; // TODO
    }

    @Override
    public Value divide(Value other) {
        return null; // TODO
    }

    @Override
    public Value power(Value visit) {
        throw new WrongTypeError();
    }

    @Override
    public Value eqi(Value eval2) {
        return null;
    }

    @Override
    public Value neqi(Value eval2) {
        return null;
    }

    @Override
    public Value resolveRef(String refName) {
        throw new WrongTypeError();
    }

    public void addValue(Value value) {
        this.value.add(value);
    }
}
