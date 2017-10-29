package expressrules;

public class WrongTypeError extends RuntimeException {
    WrongTypeError(){
        super();
    }
    WrongTypeError(String actualType){
        super(actualType);
    }
}
