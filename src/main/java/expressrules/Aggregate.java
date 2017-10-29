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
            value.add((elem instanceof Value) ? (Value) elem : Value.create(elem));
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

    @Override
    public Value resolveIndex(Value start, Value end) {
        if(!(start instanceof Simple) || !(end instanceof Simple)) throw new WrongTypeError();
        int s = ((Simple) start).getIntegerValue();
        int e = ((Simple) start).getIntegerValue();
        return (s==e) ?  value.get(s) : new Aggregate(value.subList(s,e));
    }

    @Override
    public Value or(Value visit) {
        throw new WrongTypeError();
    }

    @Override
    public Value xor(Value visit) {
        throw  new WrongTypeError();
    }

    @Override
    public Value intDiv(Value visit) {
        throw new WrongTypeError();
    }

    @Override
    public Value combine(Value operand) {
        throw new WrongTypeError();
    }

    @Override
    public Value modulo(Value operand) {
        throw new WrongTypeError();
    }

    @Override
    public Value and(Value operand) {
        throw new WrongTypeError();
    }

    public void addValue(Value value) {
        throw new WrongTypeError();
    }
}
