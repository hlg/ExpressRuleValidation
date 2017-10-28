package expressrules;

public class Simple extends Value implements Comparable<Simple>  {

    Comparable value;

    public Simple(Comparable value) {
        if(!allowedType(value)) throw  new WrongTypeError();
        this.value = value;
    }

    private boolean allowedType(Object value) {
        return value instanceof Boolean || value instanceof Double || value instanceof Integer || value instanceof String;
    }

    public boolean getBoolean(){
        if(!isBoolean()) throw  new WrongTypeError();
        return (Boolean) value;
    }

    private boolean isBoolean() {
        return value instanceof Boolean;
    }

    @Override
    public int compareTo(Simple other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public Value eq(Value eval2) {
        return new Simple(compare(eval2) ==0);
    }

    private int compare(Value eval2) {
        if (! (eval2 instanceof Simple)) throw new WrongTypeError();
        return this.compareTo((Simple) eval2);
    }

    @Override
    public Value lt(Value eval2) {
        return new Simple(compare(eval2) <0);
    }

    @Override
    public Value gt(Value eval2) {
        return new Simple(compare(eval2) >0);
    }

    @Override
    public Value le(Value eval2) {
        return new Simple(compare(eval2) <1);
    }

    @Override
    public Value ge(Value eval2) {
        return new Simple(compare(eval2) >-1);
    }

    @Override
    public Value neq(Value eval2) {
        return new Simple(compare(eval2) !=0);
    }

    @Override
    public Value in(Value eval2) {
        return null;  // TODO
    }

    @Override
    public Value subtract(Value other) {
        assertBothDouble(other);
        return new Simple((Double)this.value-(Double)((Simple)other).value);
    }

    private void assertBothDouble(Value other) {
        if(!this.isDouble() || !(other instanceof Simple) || !((Simple)other).isDouble()) throw new WrongTypeError();
    }

    @Override
    public Value add(Value other) {
        assertBothDouble(other);
        return new Simple((Double)this.value+(Double)((Simple)other).value);
    }

    @Override
    public Value multiply(Value other) {
        assertBothDouble(other);
        return new Simple((Double)this.value*(Double)((Simple)other).value);
    }

    @Override
    public Value divide(Value other) {
        assertBothDouble(other);
        return new Simple((Double)this.value/(Double)((Simple)other).value);
    }

    @Override
    public Value power(Value other) {
        assertBothDouble(other);
        return new Simple(Math.pow((Double) this.value,(Double)((Simple)other).value) );
    }

    @Override
    public Value eqi(Value eval2) {
        throw new WrongTypeError();
    }

    @Override
    public Value neqi(Value eval2) {
        throw new WrongTypeError();
    }

    @Override
    public Value resolveRef(String refName) {
        throw  new WrongTypeError();
    }

    @Override
    public Value resolveIndex(Value start, Value end) {
        throw new WrongTypeError();
    }

    public boolean isDouble() {
        return value instanceof Double;
    }

    public int getIntegerValue() {
        if (value instanceof Number) return ((Number)value).intValue();
        else throw new WrongTypeError();
    }
    public double getDoubleValue() {
        if (value instanceof Number) return ((Number)value).doubleValue();
        else throw new WrongTypeError();
    }

    public String getStringValue() {
        if (value instanceof String) return (String)value;
        else throw new WrongTypeError();
    }
}
