package expressrules;

import org.antlr.v4.runtime.*
import org.junit.Test

import java.io.*
import java.util.*;

import expressrules.ExpressParser.Schema_declContext;
import expressrules.ExpressParser.Entity_declContext

import static expressrules.ExpressParser.*;

public class ExpressRuleVisitorTest {

    @Test
    public void testSimpleExpression() throws Exception {
        def schema = 'ENTITY dummy; WHERE valid: 1+1*3=4; END_ENTITY;'
        assert checkEntityAgainstSchema(null, schema);
    }

    @Test
    public void testEntityAccessAttribute() throws Exception {
        def schema = 'ENTITY test1; check: BOOLEAN; WHERE valid: check=true; END_ENTITY;';
        def entity = [type:"test1", attributes:[ 'check':  new Simple(true)]] as TestEntity;
        assert checkEntityAgainstSchema(entity, schema);
    }

    @Test
    public void testEntityAccessReference() throws Exception {
        def schema = """
            ENTITY test1; check: BOOLEAN; END_ENTITY;
            ENTITY test2; ref: test1; WHERE valid: ref.check=true; END_ENTITY;
            """
        def entity1 = [type:"test1", attributes:['check': new Simple(true)]] as TestEntity;
        def entity2 = [type:"test2", attributes:['ref': new Entity(entity1)]] as TestEntity;
        assert checkEntityAgainstSchema(entity2, schema);
    }

    @Test
    public void testAggregateAccessIndex() throws Exception {
        def schema = 'ENTITY test3; check: LIST of INTEGER; WHERE valid: check[1]=1; END_ENTITY;'
        def entity = [type:"test3", attributes:['check': new Aggregate(Arrays.asList(0, 1, 3))]] as TestEntity
        assert checkEntityAgainstSchema(entity, schema);
    }

    @Test
    public void testFunctions() throws Exception{
        def schema = 'ENTITY test4; WHERE valid: 1=ABS(COS(PI)); END_ENTITY;'
        assert checkEntityAgainstSchema(null, schema);
    }

    @Test
    public void testFunctionsExists() throws Exception{
        def schema = 'ENTITY test6; undefined: INTEGER; WHERE valid: EXISTS(undefined); END_ENTITY;'
        def entity = [type:"test6", attributes:['undefined': new Simple("tricky")]] as TestEntity;
        assert checkEntityAgainstSchema(entity, schema);
    }

    @Test
    public void testEnumReference() throws Exception{
        def schema = """
            TYPE names = ENUMERATION OF (ANNA, BOB, CHARLY); END_TYPE;
            ENTITY test5; name: names; WHERE valid: name=names.BOB; END_ENTITY;
            """
        def entity = [type:'test5', attributes:['name': new Simple('BOB')]] as TestEntity;
        assert checkEntityAgainstSchema(entity, schema);
    }

    @Test
    public void testSimpleFunctionRef() throws Exception{
        def schema = """
            FUNCTION doubleValue (a: INTEGER) : INTEGER; RETURN (a*2); END_FUNCTION;
            ENTITY dummy; WHERE valid: 12=doubleValue(doubleValue(2-1)+doubleValue(2)); END_ENTITY;
            """
        assert checkEntityAgainstSchema(null, schema)
    }

    @Test
    public void testLocalVariables() throws Exception{
        def schema = """
            FUNCTION localVar : INTEGER; LOCAL b: INTEGER := 3**2+2*7; END_LOCAL; RETURN (b); END_FUNCTION;
            ENTITY dummy; WHERE valid: 23=localVar(); END_ENTITY;
            """
       assert checkEntityAgainstSchema(null, schema);
    }

    @Test
    public void testPrematureReturn() throws Exception{
        def schema ="""
           FUNCTION early : INTEGER; RETURN (true); RETURN (false); END_FUNCTION;
           ENTITY dummy; WHERE valid: early(); END_ENTITY;
           """
        assert checkEntityAgainstSchema(null, schema);
    }

    private boolean checkEntityAgainstSchema(TestEntity entity, String schemaBody){
        Schema_declContext schema = getSchemaFor('SCHEMA test; '+schemaBody+'END_SCHEMA;')
        Value result = new ExpressRuleVisitor(entity, getEntityDeclarationTableFor(schema), getEnumTypeDeclarationTableFor(schema), getFunctionDeclarationTableFor(schema)).visit(schema)
        return  ((Simple) result).getBoolean();
    }

    private Schema_declContext getSchemaFor(String expressCode) throws IOException {
        ExpressLexer lexer = new ExpressLexer(new CaseInsensitiveANTLRInputStream(expressCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ExpressParser(tokens).schema_decl();
    }

    private Map<String, Entity_declContext> getEntityDeclarationTableFor(Schema_declContext schema) {
        schema.schema_body().declaration().findAll{ DeclarationContext declaration ->
            declaration.entity_decl()!=null
        }.collectEntries { DeclarationContext declaration ->
            [declaration.entity_decl().entity_head().IDENT().getText(), declaration.entity_decl()]
        }
    }

    private Map<String, Function_declContext> getFunctionDeclarationTableFor(Schema_declContext schema){
        schema.schema_body().declaration().findAll{ DeclarationContext declaration ->
            declaration.function_decl()!=null
        }.collectEntries { DeclarationContext declaration ->
            [declaration.function_decl().function_head().function_id().IDENT().getText(), declaration.function_decl()]
        }
    }

    private Map<String, Type_declContext> getEnumTypeDeclarationTableFor(Schema_declContext schema) {
        schema.schema_body().declaration().findAll{ DeclarationContext declaration ->
            declaration.type_decl()!= null
        }.collectEntries{ DeclarationContext declaration ->
            [declaration.type_decl().IDENT().getText(), declaration.type_decl().underlying_type().constructed_types().enumeration_type().enumeration_items().enumeration_id().collect {it.IDENT().getText()}]
        }
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

