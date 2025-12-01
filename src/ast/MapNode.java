package ast;

import ast.nodes.SyntaxNode;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * MapNode for Phase 3.
 * Represents: map f xs
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
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
    {
        // TODO: Implement full type checking logic
        throw new UnsupportedOperationException("MapNode.typeOf not implemented yet.");
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("MapNode(", indentAmt);
        func.displaySubtree(indentAmt + 2);
        listExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
