package expressrules;

import org.antlr.v4.runtime.*;
import org.apache.commons.io.IOUtils;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.emf.Schema;
import org.bimserver.ifc.step.deserializer.Ifc2x3tc1StepDeserializer;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc4.IfcRoot;
import expressrules.ExpressLexer;
import org.junit.Test;
import expressrules.ExpressParser;

import java.io.*;
import java.nio.file.Paths;

public class ExpressRuleVisitorTest {

    @Test
    public void testExpressParser() throws Exception {
        InputStream schema = getClass().getClassLoader().getResourceAsStream("IFC4_ADD2.exp");
        ExpressLexer lexer = new ExpressLexer(new ANTLRInputStream(IOUtils.toString(schema).toLowerCase()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressParser parser = new ExpressParser(tokens);
        ExpressRuleVisitor expressRuleVisitor = new ExpressRuleVisitor("ifcwall");
        expressRuleVisitor.visitSchema_decl(parser.schema_decl());
    }

    @Test
    public void testValidation() throws Exception {
        Ifc2x3tc1StepDeserializer deserializer = new Ifc2x3tc1StepDeserializer();
        PackageMetaData packageMetaData = new PackageMetaData(Ifc2x3tc1Package.eINSTANCE, Schema.IFC2X3TC1, Paths.get("tmp"));
        deserializer.init(packageMetaData);
        File ifc = new File(getClass().getClassLoader().getResource("test.ifc").getFile());
        IfcModelInterface model = deserializer.read(ifc);
        for (IfcRoot obj : model.getAllWithSubTypes(IfcRoot.class)) {
            obj.getClass().getSimpleName();
            // obj.eClass().getEStructuralFeature(attributeName);
        }
    }
}