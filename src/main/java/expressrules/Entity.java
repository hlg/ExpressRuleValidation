package expressrules;

import java.util.Collection;
import java.util.Objects;

public class Entity extends Value {

    EntityAdapter value;

    public Entity(EntityAdapter elem) {
        value = elem;
    }

    @Override
    public Value eq(Value other) {
        if(!(other instanceof Entity)) throw new WrongTypeError();
        return new Simple(Objects.deepEquals(this.value, ((Entity)other).value));
    }

    @Override
    public Value lt(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value gt(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value le(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value ge(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value neq(Value other) {
        if(!(other instanceof Entity)) throw new WrongTypeError();
        return new Simple(!Objects.deepEquals(this.value, ((Entity)other).value));
    }

    @Override
    public Value in(Value other) {
        return null; // TODO
    }

    @Override
    public Value subtract(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value add(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value multiply(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value divide(Value other) {
        throw new WrongTypeError();
    }

    @Override
    public Value power(Value visit) {
        throw new WrongTypeError();
    }

    @Override
    public Value eqi(Value other) {
        if(!(other instanceof Entity)) throw new WrongTypeError();
        return new Simple(this.value==((Entity)other).value);
    }

    @Override
    public Value neqi(Value other) {
        if(!(other instanceof Entity)) throw new WrongTypeError();
        return new Simple(this.value!=((Entity)other).value);
    }

    @Override
    public Value resolveRef(String refName) {
        return value.resolveReference(refName);  // TODO: wrappedValues

    }

    @Override
    public Value resolveIndex(Value start, Value end) {
        throw new WrongTypeError();
    }

    @Override
    public Value or(Value visit) {
        throw new WrongTypeError();
    }

    @Override
    public Value xor(Value visit) {
        throw new WrongTypeError();
    }

    @Override
    public Value intDiv(Value visit) {
        throw new WrongTypeError();
    }

    @Override
    public Value combine(Value operand) {
        return null; // TODO complex entity constructor
    }

    @Override
    public Value modulo(Value operand) {
        throw new WrongTypeError();
    }

    @Override
    public Value and(Value operand) {
        throw new WrongTypeError();
    }

    public Collection<String> getTypes() {
        return value.getTypes();
    }

    public Collection<Entity> getUsages(String type, String attribute) {
        return value.getUsages(type, attribute);
    }
}
