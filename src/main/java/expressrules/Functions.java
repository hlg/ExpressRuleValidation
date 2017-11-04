package expressrules;

import java.util.HashSet;
import java.util.List;

public enum Functions {
    ABS (new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return  new Simple(Math.abs(((Simple) parameters.get(0)).getDoubleValue()));
        }
    }, ACOS(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.acos(((Simple)parameters.get(0)).getDoubleValue()));
        }
    }, ASIN(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.asin(((Simple)parameters.get(0)).getDoubleValue()));
        }
    }, ATAN(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.atan(((Simple)parameters.get(0)).getDoubleValue()));
        }
    }, BLENGTH(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Integer.bitCount(((Simple) parameters.get(0)).getIntegerValue()));
        }
    }, COS(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.cos(((Simple) parameters.get(0)).getDoubleValue()));
        }
    }, EXISTS(new Class[]{Value.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(parameters.get(0).getValue() != null);
        }
    }, EXP(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.exp(((Simple) parameters.get(0)).getDoubleValue()));
        }
    }, FORMAT(new Class[]{Simple.class, Simple.class}) {  // tODO: String as separate type?
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(String.format(((Simple) parameters.get(1)).getStringValue(), ((Simple) parameters.get(0)).getDoubleValue()));  // TODO check format, probably different
        }
    }, HIBOUND(new Class[]{Aggregate.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple((double) ((Aggregate)parameters.get(0)).value.size()); // TODO not used in IFC4, assumes lower bound is 0
        }
    }, HIINDEX(new Class[]{Aggregate.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple((double) ((Aggregate)parameters.get(0)).value.size()); // TODO should distiguish between declared (HIBOUND) and actual size
        }
    }, LENGTH(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(((Simple)parameters.get(0)).getStringValue().length());
        }
    }, LOBOUND(new Class[]{Aggregate.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(0); // TODO not used in IFC4, should support other lower bounds than 0
        }
    }, LOINDEX(new Class[]{}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(0); // TODO not used in IFC4, should support other lower indizes than 0
        }
    }, LOG(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.log(((Simple) parameters.get(0)).getDoubleValue()));
        }
    }, LOG2(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.log(((Simple) parameters.get(0)).getDoubleValue())/Math.log(2));
        }
    }, LOG10(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.log10(((Simple) parameters.get(0)).getDoubleValue()));
        }
    }, NVL(new Class[]{Value.class, Value.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return parameters.get(0)!=null ? parameters.get(0) : parameters.get(1) ;
        }
    }, ROLESOF(new Class[]{Entity.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return null; // TODO all roles (attribute names) where the entity may appear, not used in IFC4, implement like used in
        }
    }, SIN(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.sin(((Simple)parameters.get(0)).getDoubleValue()));
        }
    }, SIZEOF(new Class[]{Aggregate.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple((double) (parameters.get(0).getValue()==null ? 0 :((Aggregate) parameters.get(0)).value.size()));
        }
    }, SQRT(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.sqrt(((Simple) parameters.get(0)).getDoubleValue()));
        }
    }, TAN(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Math.tan(((Simple)parameters.get(0)).getDoubleValue()));
        }
    }, TYPEOF(new Class[]{Entity.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Aggregate(((Entity)parameters.get(0)).getTypes()); // TODO
        }
    }, USEDIN(new Class[]{Entity.class, Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            String[] path = ((Simple) parameters.get(1)).getStringValue().split(".");
            String type = path[1]; String attribute = path[2];
            return new Aggregate(((Entity)parameters.get(0)).getUsages(type, attribute)); // TODO revise self and group
        }
    }, VALUE(new Class[]{Simple.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(Double.parseDouble(((Simple) parameters.get(0)).getStringValue())); // TODO handle int separate?
        }
    }, VALUE_IN(new Class[]{Aggregate.class, Value.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            return new Simple(((Aggregate)parameters.get(0)).value.contains(parameters.get(1)));
        }
    }, VALUE_UNIQUE(new Class[]{Aggregate.class}) {
        @Override
        protected Value _evaluate(List<Value> parameters) {
            List<Value> collection = ((Aggregate) parameters.get(0)).value;
            return new Simple(collection.size() == new HashSet<Value>(collection).size());
        }
    };

    private final Class[] parameters;

    Functions(Class[] parameterClasses){
       this.parameters = parameterClasses;
    }

    public Value evaluate(List<Value> parameters){
        if(parameters.size()!= this.parameters.length) {
            System.out.println("wrong length of parameters for function " + this.name());
        }
        for(int i=0; i<parameters.size(); i++){
          if(!this.parameters[i].isInstance(parameters.get(i))) throw new WrongTypeError(this.parameters[i], parameters.get(i).getClass());

        }
        return _evaluate(parameters);

    }

    protected abstract Value _evaluate(List<Value> parameters);

}
