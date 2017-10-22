package expressrules;

public class Entity extends Value {

    EntityAdapter value;

    public Entity(EntityAdapter elem) {
        value = elem;
    }

    @Override
    public Value eq(Value eval2) {
        return null;   // tODO value comparison
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
        return null; // TODO value comparison
    }

    @Override
    public Value in(Value eval2) {
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
    public Value eqi(Value eval2) {
        return null; // TODO instance comparison
    }

    @Override
    public Value neqi(Value eval2) {
        return null; // TODO instance comparison
    }

    @Override
    public Value resolveRef(String refName) {
        return value.resolveReference(refName);  // TODO: wrappedValues

    }

    @Override
    public Value resolveIndex(Value start, Value end) {
        throw new WrongTypeError();
    }

}
