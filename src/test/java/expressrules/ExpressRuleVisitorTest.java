package expressrules;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.IOUtils;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.emf.Schema;
import org.bimserver.ifc.step.deserializer.Ifc2x3tc1StepDeserializer;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc4.IfcRoot;
import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import expressrules.ExpressParser.Schema_declContext;
import expressrules.ExpressParser.Entity_declContext;

public class ExpressRuleVisitorTest {

    @Test
    public void testIfc4Coverage() throws Exception {
        new ExpressRuleVisitor(null).visit(getIfc4Parser().schema_decl());
    }

    @Test
    public void testDeclarationTableListener() throws Exception {
        ParseTreeWalker.DEFAULT.walk(new ExpressDeclarationTableListener(), getIfc4Parser().schema_decl());
    }

    @Test
    public void testSimpleExpression() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA expressions; ENTITY dummy; WHERE valid: 1+1*3=4; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(null).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testEntityAccessAttribute() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA entityAccess; ENTITY test1; check: BOOLEAN; WHERE valid: check=true; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test1Entity(), getEntityDeclarationTableFor(schema)).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testEntityAccessReference() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA entityAccess; ENTITY test1; check: BOOLEAN; END_ENTITY; ENTITY test2; ref: test; WHERE valid: ref.check=true; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test2Entity(), getEntityDeclarationTableFor(schema)).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testAggregateAccessIndex() throws Exception {
        Schema_declContext schema = getSchemaFor("SCHEMA entityAccess; ENTITY test3; check: LIST of INTEGER; WHERE valid: check[1]=1; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test3Entity(), getEntityDeclarationTableFor(schema)).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testFunctions() throws Exception{
        Schema_declContext schema = getSchemaFor("SCHEMA functions; ENTITY test4; WHERE valid: 1=ABS(COS(PI)); END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test4Entity(), getEntityDeclarationTableFor(schema)).visit(schema);
        assert (Boolean)((Simple)result).value;
    }

    public void testValidation() throws Exception { // TODO: integration tests
        ExpressDeclarationTableListener declarationTableListener = new ExpressDeclarationTableListener();
        ParseTreeWalker.DEFAULT.walk(declarationTableListener, getIfc4Parser().schema_decl());
        Ifc2x3tc1StepDeserializer deserializer = new Ifc2x3tc1StepDeserializer();
        PackageMetaData packageMetaData = new PackageMetaData(Ifc2x3tc1Package.eINSTANCE, Schema.IFC2X3TC1, Paths.get("tmp"));
        deserializer.init(packageMetaData);
        File ifc = new File(getClass().getClassLoader().getResource("test.ifc").getFile());

        byte[] bytes = "asdf".getBytes();
        deserializer.read(new ByteArrayInputStream(bytes), "onthefly.ifc", bytes.length, null);
        IfcModelInterface model = deserializer.read(ifc);
        for (IfcRoot obj : model.getAllWithSubTypes(IfcRoot.class)) {
            ExpressRuleVisitor expressRuleVisitor = new ExpressRuleVisitor(new BIMserverEntityAdapter(obj));
            for (Class clz : obj.getClass().getClasses()) {
                Entity_declContext entity_decl = declarationTableListener.entityDeclarations.get(clz.getSimpleName().toLowerCase());
                expressRuleVisitor.visit(entity_decl);
                // obj.eClass().getEStructuralFeature(attributeName);
            }
        }
    }

    @Test
    public void testCheckIfc4Scratch() throws Exception {

    }

    @Test
    public void testResultAggregation(){}

    private ExpressParser getIfc4Parser() throws IOException {
        InputStream schema = getClass().getClassLoader().getResourceAsStream("IFC4_ADD2.exp");
        ExpressLexer lexer = new ExpressLexer(new ANTLRInputStream(IOUtils.toString(schema).toLowerCase()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ExpressParser(tokens);
    }

    private Schema_declContext getSchemaFor(String expressCode) throws IOException {
        ExpressLexer lexer = new ExpressLexer(new ANTLRInputStream(expressCode.toLowerCase()));
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

    private abstract class TestEntity implements EntityAdapter {
        protected Map<String, Value> attributes = new HashMap<String, Value>();
        String expressClassName;

        @Override
        public Value resolveReference(String refName) {
            assert attributes.containsKey(refName);
            return attributes.get(refName);
        }

        @Override
        public String getExpressClassName() {
            return expressClassName;
        }

        @Override
        public Collection<Entity> getUsages(String type, String attribute) { return null; }

        @Override
        public Collection<String> getTypes() {
            return Arrays.asList(expressClassName);
        }
    }

    private class Test1Entity extends TestEntity {
        private Test1Entity(){
            super();
            expressClassName="test1";
            attributes.put("check", new Simple(true));
        }
    }

    private class Test2Entity extends TestEntity {
        private Test2Entity(){
            super();
            expressClassName = "test2";
            attributes.put("ref", new Entity(new Test1Entity()));
        }
    }

    private class Test3Entity extends TestEntity {
        private Test3Entity(){
            super();
            expressClassName = "test3";
            attributes.put("check", new Aggregate(Arrays.asList(0, 1, 3)));
        }
    }
    private class Test4Entity extends TestEntity {
        private Test4Entity(){
            super();
            expressClassName = "test4";
            attributes.put("check2", new Simple(2.));
            attributes.put("check1", new Simple(-2.));
        }

    }
}

