package expressrules;

import org.bimserver.emf.IdEObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class BIMserverEntityAdapter implements EntityAdapter {

    IdEObject entity;

    public BIMserverEntityAdapter(IdEObject entity) {
        this.entity = entity;
    }

    @Override
    public Value resolveReference(String refName) {
        EStructuralFeature feature = entity.eClass().getEStructuralFeature(refName);
        Object resolved = entity.eGet(feature, true);
        return new Entity(new BIMserverEntityAdapter((IdEObject) resolved)); // TODO: check reference/attribute, may return POJO
    }

    @Override
    public String getExpressClassName() {
        return entity.eClass().getName();
    }
}
