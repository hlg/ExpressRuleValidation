package expressrules

import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.commons.io.IOUtils
import org.bimserver.emf.IfcModelInterface
import org.bimserver.emf.PackageMetaData
import org.bimserver.emf.Schema
import org.bimserver.ifc.step.deserializer.Ifc2x3tc1StepDeserializer
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package
import org.bimserver.models.ifc4.Ifc4Factory
import org.bimserver.models.ifc4.IfcCShapeProfileDef
import org.bimserver.models.ifc4.IfcOccupant
import org.bimserver.models.ifc4.IfcOccupantTypeEnum
import org.bimserver.models.ifc4.IfcRoot
import org.junit.Test


import java.nio.file.Paths

public class IfcIntegrationTest {

    @Test
    public void testIfc4Coverage() throws Exception {
        new ExpressRuleVisitor(null).visit(getIfc4Parser().schema_decl());
    }

    @Test
    public void testDeclarationTableListener() throws Exception {
        ParseTreeWalker.DEFAULT.walk(new ExpressDeclarationTableListener(), getIfc4Parser().schema_decl());
    }

    @Test
    public void testIfcObjects() throws Exception {
        /*
        IfcCShapeProfileDef profile = Ifc4Factory.eINSTANCE.createIfcCShapeProfileDef();
        profile.with { depth =30; width =10; girth=5; wallThickness=1 }
        EntityAdapter ifcObject = new BIMserverEntityAdapter(profile);
        */

        IfcCShapeProfileDef profile = [depth:30, width:10, girth:5, wallThickness:1] as IfcCShapeProfileDef;
        IfcOccupant ifcOccupant =  Ifc4Factory.eINSTANCE.createIfcOccupant();
        ifcOccupant.setPredefinedType(IfcOccupantTypeEnum.TENANT);
        EntityAdapter ifcObject = new BIMserverEntityAdapter(ifcOccupant);

        ExpressParser.Schema_declContext schema = getIfc4Parser().schema_decl();
        ExpressDeclarationTableListener declarationTables = new ExpressDeclarationTableListener();
        ParseTreeWalker.DEFAULT.walk(declarationTables, schema);
        Value result = new ExpressRuleVisitor(ifcObject, declarationTables.entityDeclarations, declarationTables.enumerationTypeDeclarations).visit(schema);
        assert(((Simple)result).getBoolean())
    }

    private ExpressParser getIfc4Parser() throws IOException {
        InputStream schema = getClass().getClassLoader().getResourceAsStream("IFC4_ADD2.exp");
        def stream = new CaseInsensitiveANTLRInputStream(IOUtils.toString(schema))
        ExpressLexer lexer = new ExpressLexer(stream); // .toLowerCase()
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ExpressParser(tokens);
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
                ExpressParser.Entity_declContext entity_decl = declarationTableListener.entityDeclarations.get(clz.getSimpleName().toLowerCase());
                expressRuleVisitor.visit(entity_decl);
                // obj.eClass().getEStructuralFeature(attributeName);
            }
        }
    }


}
