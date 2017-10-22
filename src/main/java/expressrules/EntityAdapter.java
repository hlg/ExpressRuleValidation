package expressrules;

public interface EntityAdapter {

    Value resolveReference(String refName);

    String getExpressClassName();
}
