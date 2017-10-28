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
import java.util.Arrays;
import java.util.Collection;

public class ExpressRuleVisitorTest {

    private ExpressParser getIfc4Parser() throws IOException {
        InputStream schema = getClass().getClassLoader().getResourceAsStream("IFC4_ADD2.exp");
        ExpressLexer lexer = new ExpressLexer(new ANTLRInputStream(IOUtils.toString(schema).toLowerCase()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ExpressParser(tokens);
    }

    private ExpressParser getParserFor(String expressCode) throws IOException {
        ExpressLexer lexer = new ExpressLexer(new ANTLRInputStream(expressCode.toLowerCase()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ExpressParser(tokens);
    }


    @Test
    public void testRuleVisitor() throws Exception {
        new ExpressRuleVisitor(null).visit(getIfc4Parser().schema_decl());
    }

    @Test
    public void testDeclarationTableListener() throws Exception {
        ParseTreeWalker.DEFAULT.walk(new ExpressDeclarationTableListener(), getIfc4Parser().schema_decl());
    }

    @Test
    public void testSimpleExpression() throws Exception {
        ExpressParser parser= getParserFor("SCHEMA expressions; ENTITY dummy; WHERE valid: 1+1*3=4; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(null).visit(parser.schema_decl());
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testEntityAccessAttribute() throws Exception {
        ExpressParser parser=getParserFor("SCHEMA entityAccess; ENTITY test1; check: BOOLEAN; WHERE valid: check=true; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test1Entity()).visit(parser.schema_decl());
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testEntityAccessReference() throws Exception {
        ExpressParser parser=getParserFor("SCHEMA entityAccess; ENTITY test1; check: BOOLEAN; END_ENTITY; ENTITY test2; ref: test; WHERE valid: ref.check=true; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test2Entity()).visit(parser.schema_decl());
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testAggregateAccessIndex() throws Exception {
        ExpressParser parser=getParserFor("SCHEMA entityAccess; ENTITY test3; check: LIST of INTEGER; WHERE valid: check[1]=1; END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test3Entity()).visit(parser.schema_decl());
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testFunctions() throws Exception{
        ExpressParser parser=getParserFor("SCHEMA functions; ENTITY test4; WHERE valid: 1=ABS(COS(PI)); END_ENTITY; END_SCHEMA;");
        Value result = new ExpressRuleVisitor(new Test4Entity()).visit(parser.schema_decl());
        assert (Boolean)((Simple)result).value;
    }

    @Test
    public void testValidation() throws Exception {
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
                ExpressParser.Entity_declContext entity_decl = declarationTableListener.entityDeclarations.get(clz.getSimpleName().toLowerCase());
                expressRuleVisitor.visitEntity_decl(entity_decl);
                // obj.eClass().getEStructuralFeature(attributeName);
            }
        }
    }

    @Test
    public void testCheckIfc4Scratch() throws Exception {

    }

    @Test
    public void testResultAggregation(){}

    private abstract class TestEntity implements EntityAdapter {
        @Override
        public Collection<String> getTypes() { return null; }

        @Override
        public Collection<Entity> getUsages(String type, String attribute) { return null; }
    }

    private class Test1Entity extends TestEntity {
        @Override
        public Value resolveReference(String refName) {
            assert "check".equals(refName);
            return new Simple(true);
        }

        @Override
        public String getExpressClassName() {
            return "test1";
        }

    }

    private class Test2Entity extends TestEntity {

        @Override
        public Value resolveReference(String refName) {
            assert "ref".equals(refName);
            return new Entity(new Test1Entity());
        }

        @Override
        public String getExpressClassName() {
            return "test2";
        }
    }

    private class Test3Entity extends TestEntity {

        @Override
        public Value resolveReference(String refName) {
            assert "check".equals(refName);
            return new Aggregate(Arrays.asList(0, 1, 3));
        }

        @Override
        public String getExpressClassName() {
            return "test3";
        }
    }
    private class Test4Entity extends TestEntity {

        @Override
        public Value resolveReference(String refName) {
            if("check2".equals(refName)) return new Simple(2.);
            else {
                assert ("check1".equals(refName));
                return new Simple(-2.);
            }
        }

        @Override
        public String getExpressClassName() {
            return "test4";
        }
    }
}
