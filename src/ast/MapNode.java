package ast;

import ast.nodes.SyntaxNode;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Base implementation of MapNode for Phase 3.
 * This file currently contains only structure and stubs.
 */
public class MapNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode listExpr;

    public MapNode(SyntaxNode func, SyntaxNode listExpr, int lineNumber)
    {
        super(lineNumber);
        this.func = func;
        this.listExpr = listExpr;
    }

    @Override
    public Object evaluate(Environment env)
    {
        // TODO: Implement full evaluation logic
        throw new UnsupportedOperationException("MapNode.evaluate not implemented yet.");
    }

    @Override
    public Type typeOf(TypeEnvironment tenv)
    {
        // TODO: Implement full type checking logic
        throw new UnsupportedOperationException("MapNode.typeOf not implemented yet.");
    }

    @Override
    public void displaySubtree(StringBuilder sb, int indent)
    {
        indent(sb, indent);
        sb.append("MapNode\n");

        indent(sb, indent + 1);
        sb.append("Function:\n");
        func.displaySubtree(sb, indent + 2);

        indent(sb, indent + 1);
        sb.append("List:\n");
        listExpr.displaySubtree(sb, indent + 2);
    }

    private void indent(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'indent'");
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'typeOf'");
    }

    @Override
    public void displaySubtree(int indentAmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'displaySubtree'");
    }
}
