package expressrules;

import org.antlr.v4.runtime.RuleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressDeclarationTableListener extends ExpressBaseListener {

    public Map<String, ExpressParser.Entity_declContext> entityDeclarations = new HashMap<String, ExpressParser.Entity_declContext>();
    public Map<String, ExpressParser.Type_declContext> typeDeclarations = new HashMap<String, ExpressParser.Type_declContext>();
    public Map<String, ExpressParser.Procedure_declContext> procedureDeclarations = new HashMap<String, ExpressParser.Procedure_declContext>();
    public Map<String, ExpressParser.Function_declContext> functionDeclarations = new HashMap<String, ExpressParser.Function_declContext>();
    public Map<String, List<String>> enumerationTypeDeclarations = new HashMap<String, List<String>>();

    @Override
    public void enterEntity_decl(ExpressParser.Entity_declContext ctx) {
        entityDeclarations.put(ctx.entity_head().IDENT().getSymbol().getText(), ctx);
    }

    @Override
    public void enterType_decl(ExpressParser.Type_declContext ctx) {
        if(ctx.underlying_type().constructed_types()!=null && ctx.underlying_type().constructed_types()!= null && ctx.underlying_type().constructed_types().enumeration_type()!= null){
            ExpressParser.Enumeration_typeContext enumType = ctx.underlying_type().constructed_types().enumeration_type();
            List<String> values = new ArrayList<String>();
            for (ExpressParser.Enumeration_idContext value : enumType.enumeration_items().enumeration_id()){
                values.add(value.IDENT().getText());
            }
            enumerationTypeDeclarations.put(ctx.IDENT().getText(),values);
        } else {
            typeDeclarations.put(ctx.IDENT().getSymbol().getText(), ctx);
        }
    }

    @Override
    public void enterProcedure_decl(ExpressParser.Procedure_declContext ctx) {
        procedureDeclarations.put(ctx.procedure_head().procedure_id().getText(), ctx);    // TODO unify
    }

    @Override
    public void enterFunction_decl(ExpressParser.Function_declContext ctx) {
        functionDeclarations.put(ctx.function_head().function_id().getText(), ctx);
    }

}
