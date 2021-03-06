package expressrules;

import org.bimserver.emf.IdEObject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class BIMserverEntityAdapter implements EntityAdapter {

    IdEObject entity;

    public BIMserverEntityAdapter(IdEObject entity) {
        this.entity = entity;
    }

    @Override
    public Value resolveReference(String refName) {
        EStructuralFeature feature = entity.eClass().getEStructuralFeature(refName);
        if (!entity.eIsSet(feature)) return feature.isMany() ? new Aggregate(null) : new Simple(null);
        Object resolved = entity.eGet(feature, true);
        if((resolved instanceof IdEObject) && ((IdEObject)resolved).eClass().getEAnnotation("wrapped")!=null) {
            EStructuralFeature unwrap =  ((IdEObject)resolved).eClass().getEStructuralFeature("wrappedValue");
            resolved = ((IdEObject)resolved).eGet(unwrap, true);
        }
        if(feature.isMany()) {
            if(IdEObject.class.isAssignableFrom(feature.getEGenericType().getERawType().getInstanceClass())){
                Collection<EntityAdapter> adapters = new ArrayList<EntityAdapter>();
                for (IdEObject entity : (Collection <IdEObject>) resolved){
                    adapters.add(new BIMserverEntityAdapter(entity));
                }
                return Value.create(adapters);
            }
        } else if (resolved instanceof IdEObject){
            return Value.create(new BIMserverEntityAdapter((IdEObject) resolved));
        }
        return Value.create(resolved);
    }

    @Override
    public String getExpressClassName() {
        return entity.eClass().getName();
    }

    @Override
    public Collection<String> getTypes() {
        Collection<String> result = new ArrayList<String>();
        for ( EClass superType : entity.eClass().getEAllSuperTypes()){
            result.add(superType.getName());
        }
        result.add(entity.eClass().getName());
        return result;
    }

    @Override
    public Collection<Entity> getUsages(String type, String attribute) {
        Collection<Entity> result = new ArrayList<Entity>();
        Iterator<EObject> otherEntities = entity.eClass().eResource().getAllContents();
        while(otherEntities.hasNext()){
            EObject otherEntity = otherEntities.next();
            if(type.equals(otherEntity.eClass().getName())){
                EStructuralFeature feature = otherEntity.eClass().getEStructuralFeature(attribute);
                if (otherEntity.eGet(feature, true).equals(entity)) {
                    Entity usingEntity = new Entity(new BIMserverEntityAdapter((IdEObject) otherEntity));
                    result.add(usingEntity);
                }
            }
        }
        return result;
    }
}
