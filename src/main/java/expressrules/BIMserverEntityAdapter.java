package expressrules;

import org.bimserver.emf.IdEObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Collection;

public class BIMserverEntityAdapter implements EntityAdapter {

    IdEObject entity;

    public BIMserverEntityAdapter(IdEObject entity) {
        this.entity = entity;
    }

    @Override
    public Value resolveReference(String refName) {
        EStructuralFeature feature = entity.eClass().getEStructuralFeature(refName);
        Object resolved = entity.eGet(feature, true);
        if((resolved instanceof IdEObject) && ((IdEObject)resolved).eClass().getEAnnotation("wrapped")!=null) {
            EStructuralFeature unwrap =  ((IdEObject)resolved).eClass().getEStructuralFeature("wrappedValue");
            resolved = ((IdEObject)resolved).eGet(unwrap, true);
        }
        return Value.create(resolved);
    }

    @Override
    public String getExpressClassName() {
        return entity.eClass().getName();
    }
}
