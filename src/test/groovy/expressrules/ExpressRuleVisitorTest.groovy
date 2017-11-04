package expressrules;

import org.antlr.v4.runtime.*
import org.junit.Test

import java.io.*
import java.util.*;

import expressrules.ExpressParser.Schema_declContext;
import expressrules.ExpressParser.Entity_declContext;

public class ExpressRuleVisitorTest {

    @Test
    public void testSimpleExpression() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA expressions; ENTITY dummy; WHERE valid: 1+1*3=4; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(null).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testEntityAccessAttribute() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA entityAccess; ENTITY test1; check: BOOLEAN; WHERE valid: check=true; END_ENTITY; END_SCHEMA;");
        def entity = [type:"test1", attributes:[ 'check':  new Simple(true)]] as TestEntity;
        Value result = new ExpressRuleVisitor(entity, getEntityDeclarationTableFor(schema), new HashMap<String, List<String>>()).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testEntityAccessReference() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA entityyAccess; ENTITY test1; check: BOOLEAN; END_ENTITY; ENTITY test2; ref: test1; WHERE valid: ref.check=true; END_ENTITY; END_SCHEMA;");
        def entity1 = [type:"test1", attributes:['check': new Simple(true)]] as TestEntity;
        def entity2 = [type:"test2", attributes:['ref': new Entity(entity1)]] as TestEntity;
        Value result = new ExpressRuleVisitor(entity2, getEntityDeclarationTableFor(schema), new HashMap<String, List<String>>()).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testAggregateAccessIndex() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA entityAccess; ENTITY test3; check: LIST of INTEGER; WHERE valid: check[1]=1; END_ENTITY; END_SCHEMA;");
        def entity = [type:"test3", attributes:['check': new Aggregate(Arrays.asList(0, 1, 3))]] as TestEntity;
        Value result = new ExpressRuleVisitor(entity, getEntityDeclarationTableFor(schema), new HashMap<String, List<String>>()).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testFunctions() throws Exception{
        Schema_declContext schema = getSchemaFor("SCHEMA functions; ENTITY test4; WHERE valid: 1=ABS(COS(PI)); END_ENTITY; END_SCHEMA;");
        def entity = [type:"test4", attributes:['check1': new Simple(2 as Double), 'check2': new Simple(-2 as Double)]] as TestEntity;
        Value result = new ExpressRuleVisitor(entity, getEntityDeclarationTableFor(schema), new HashMap<String, List<String>>()).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testFunctionsExists() throws Exception{
        Schema_declContext schema = getSchemaFor("SCHEMA functions; ENTITY test6; undefined: INTEGER; WHERE valid: EXISTS(undefined); END_ENTITY; END_SCHEMA;");
        def entity = [type:"test6", attributes:['undefined': new Simple(null)]] as TestEntity;
        Value result = new ExpressRuleVisitor(entity, getEntityDeclarationTableFor(schema), new HashMap<String, List<String>>()).visit(schema);
        assert !(Boolean)((Simple)result).value;
    }

    @Test
    public void testEnumReference() throws Exception{
        Schema_declContext schema = getSchemaFor("SCHEMA functions; TYPE names = ENUMERATION OF (ANNA, BOB, CHARLY); END_TYPE; ENTITY test5; name: names; WHERE valid: name=names.BOB; END_ENTITY; END_SCHEMA;");
        def entity = [type:'test5', attributes:['name': new Simple('BOB')]] as TestEntity;
        Value result = new ExpressRuleVisitor(entity, [test5: schema.schema_body().declaration(1).entity_decl()], [names: ["ANNA", "BOB", "CHARLY"]]).visit(schema);
        assert ((Simple)result).getBoolean();
    }

    @Test
    public void testCheckIfc4Scratch() throws Exception {

    }

    @Test
    public void testResultAggregation(){}

    private Schema_declContext getSchemaFor(String expressCode) throws IOException {
        ExpressLexer lexer = new ExpressLexer(new CaseInsensitiveANTLRInputStream(expressCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ExpressParser(tokens).schema_decl();
    }

    private Map<String, Entity_declContext> getEntityDeclarationTableFor(Schema_declContext schema) {
        Map<String, Entity_declContext> entityTable = new HashMap<String, Entity_declContext>();
        for (ExpressParser.DeclarationContext declaration : schema.schema_body().declaration()){
            Entity_declContext entityDeclaration = declaration.entity_decl();
            entityTable.put(entityDeclaration.entity_head().IDENT().getText(), entityDeclaration);
        }
        return entityTable;
    }

    protected class TestEntity implements EntityAdapter {
        protected Map<String, Value> attributes = new HashMap<String, Value>();
        String type;

        @Override
        public Value resolveReference(String refName) {
            assert attributes.containsKey(refName);
            return attributes.get(refName);
        }

        @Override
        public String getExpressClassName() {
            return type;
        }

        @Override
        public Collection<Entity> getUsages(String type, String attribute) { return null; }

        @Override
        public Collection<String> getTypes() {
            return Arrays.asList(type);
        }
    }


}

