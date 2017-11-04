package expressrules;

public class WrongTypeError extends RuntimeException {
    WrongTypeError(){
        super();
    }
    WrongTypeError(String message){
        super(message);
    }
    WrongTypeError(Class expected, Class found){
        super("expecting "+expected.getName()+", found "+found.getName());
    }
}
