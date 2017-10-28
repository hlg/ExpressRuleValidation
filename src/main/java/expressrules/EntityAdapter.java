package expressrules;

import java.util.Collection;

public interface EntityAdapter {

    Value resolveReference(String refName);

    String getExpressClassName();

    Collection<String> getTypes();

    Collection<Entity> getUsages(String type, String attribute);
}
