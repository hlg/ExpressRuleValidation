package expressrules;

import org.antlr.v4.runtime.RuleContext;
import expressrules.ExpressBaseVisitor;
import expressrules.ExpressParser;
import expressrules.ExpressVisitor;

import java.util.HashMap;
import java.util.Map;

public class ExpressRuleVisitor extends ExpressBaseVisitor<Boolean> {

    private String ifcClassName;
    public Map<String, RuleContext> declarationTable = new HashMap<String, RuleContext>();

    public ExpressRuleVisitor(String ifcClassName) {
        this.ifcClassName = ifcClassName;
    }

    // TODO: ifc object to check later on



    @Override
    public Boolean visitSchema_decl(ExpressParser.Schema_declContext ctx) {
        return visitSchema_body(ctx.schema_body());
    }

    @Override
    public Boolean visitSchema_body(ExpressParser.Schema_bodyContext ctx) {
        for(ExpressParser.DeclarationContext declaration : ctx.declaration()) {
            ExpressParser.Entity_declContext entity_decl = declaration.entity_decl();
            if(entity_decl !=null && entity_decl.entity_head().IDENT().getSymbol().getText().equals(ifcClassName)) return visitEntity_body(entity_decl.entity_body());
        }  // TODO: create a symbol table before to avoid looking up symbols in the parse tree
        return false; // throw exception, class not found in schema
    }
}
