package expressrules;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import expressrules.ExpressLexer;
import expressrules.ExpressParser;

import java.io.InputStream;

public class ExpressDeclarationTableListenerTest {

    @Test
    public void testEnterEntity_decl() throws Exception {
        InputStream schema = getClass().getClassLoader().getResourceAsStream("IFC4_ADD2.exp");
        ExpressLexer lexer = new ExpressLexer(new ANTLRInputStream(IOUtils.toString(schema).toLowerCase()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressParser parser = new ExpressParser(tokens);
        ExpressDeclarationTableListener expressDeclarationTableListener = new ExpressDeclarationTableListener();
        ParseTreeWalker.DEFAULT.walk(expressDeclarationTableListener, parser.schema_decl());
        Assert.assertTrue(expressDeclarationTableListener.declarationTable.containsKey("ifccolumn"));
        Assert.assertTrue(expressDeclarationTableListener.declarationTable.containsKey("ifcwall"));
        Assert.assertTrue(expressDeclarationTableListener.declarationTable.containsKey("ifcbuildingelement"));
    }
}