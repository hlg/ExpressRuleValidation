package expressrules

import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.commons.io.IOUtils
import org.bimserver.models.ifc4.Ifc4Factory
import org.bimserver.models.ifc4.IfcOccupantTypeEnum
import org.bimserver.models.ifc4.IfcSystemFurnitureElement
import org.bimserver.models.ifc4.IfcSystemFurnitureElementTypeEnum
import org.eclipse.emf.common.util.EList
import org.junit.Test

public class IfcIntegrationTest {

    @Test
    public void testDeclarationTableListener() throws Exception {
        ExpressDeclarationTableListener declarationTables = getDeclarationTablesFor(getIfc4Parser().schema_decl());
        assert declarationTables.entityDeclarations.size() == 776
        assert declarationTables.functionDeclarations.size() == 47
    }

    @Test
    public void testValidIfcObjects() throws Exception {
        ExpressParser.Schema_declContext schema = getIfc4Parser().schema_decl();
        ExpressDeclarationTableListener declarationTables = getDeclarationTablesFor(schema);
        getValidIfcObjects().each{ EntityAdapter ifcAdapter ->
            // 123 abstract out of 776 that is 653 concrete entity types
            Value result = new ExpressRuleVisitor(ifcAdapter, declarationTables.entityDeclarations, declarationTables.enumerationTypeDeclarations).visit(schema);
            assert(((Simple)result).getBoolean())
        }
    }
    @Test
    public void testSingleValidIfcObject() throws Exception{
        Ifc4Factory ifc4Factory = Ifc4Factory.eINSTANCE
        EntityAdapter entity = new BIMserverEntityAdapter(ifc4Factory.createIfcRelDefinesByType().with{
            relatingType = ifc4Factory.createIfcSystemFurnitureElementType()
            def furniture = ifc4Factory.createIfcSystemFurnitureElement().with {
                predefinedType = IfcSystemFurnitureElementTypeEnum.USERDEFINED; delegate
            } as IfcSystemFurnitureElement
            relatedObjects.add(furniture)
            furniture
        } );
        ExpressParser.Schema_declContext schema = getIfc4Parser().schema_decl();
        ExpressDeclarationTableListener declarationTables = getDeclarationTablesFor(schema);
        Value result = new ExpressRuleVisitor(entity, declarationTables.entityDeclarations, declarationTables.enumerationTypeDeclarations).visit(schema);
        assert (((Simple)result).getBoolean())
    }
    @Test
    public void testInvalidIfcObjects() throws Exception {
        ExpressParser.Schema_declContext schema = getIfc4Parser().schema_decl();
        ExpressDeclarationTableListener declarationTables = getDeclarationTablesFor(schema)
        getInvalidIfcObjects().each{ EntityAdapter ifcAdapter ->
            Value result = new ExpressRuleVisitor(ifcAdapter, declarationTables.entityDeclarations, declarationTables.enumerationTypeDeclarations).visit(schema);
            assert(!((Simple)result).getBoolean())
        }
    }

    private List<EntityAdapter> getValidIfcObjects() {
        def ifc4Factory = Ifc4Factory.eINSTANCE
        Arrays.asList(
                ifc4Factory.createIfcCShapeProfileDef().with { depth = 30; width = 10; girth = 5; wallThickness = 1; delegate },
                ifc4Factory.createIfcOccupant().with { predefinedType = IfcOccupantTypeEnum.TENANT; delegate },
                ifc4Factory.createIfcRelDefinesByType().with{
                    relatingType = ifc4Factory.createIfcSystemFurnitureElementType()
                    def furniture = ifc4Factory.createIfcSystemFurnitureElement().with {
                        predefinedType = IfcSystemFurnitureElementTypeEnum.USERDEFINED; delegate
                    } as IfcSystemFurnitureElement
                    relatedObjects.add(furniture)
                    furniture
                }
        ).collect { new BIMserverEntityAdapter(it) }
    }

    private List<EntityAdapter> getInvalidIfcObjects() {
        def ifc4Factory = Ifc4Factory.eINSTANCE
        Arrays.asList(
                ifc4Factory.createIfcCShapeProfileDef().with { depth = 30; width = 10; girth = 5; wallThickness = 7; delegate },
                ifc4Factory.createIfcOccupant().with { predefinedType = IfcOccupantTypeEnum.USERDEFINED; delegate }
        ).collect { new BIMserverEntityAdapter(it) }
    }

    private ExpressDeclarationTableListener getDeclarationTablesFor(ExpressParser.Schema_declContext schema) {
        ExpressDeclarationTableListener declarationTables = new ExpressDeclarationTableListener();
        ParseTreeWalker.DEFAULT.walk(declarationTables, schema);
        declarationTables
    }

    private ExpressParser getIfc4Parser() throws IOException {
        InputStream schema = getClass().getClassLoader().getResourceAsStream("IFC4_ADD2.exp");
        def stream = new CaseInsensitiveANTLRInputStream(IOUtils.toString(schema))
        ExpressLexer lexer = new ExpressLexer(stream); // .toLowerCase()
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ExpressParser(tokens);
    }


}
