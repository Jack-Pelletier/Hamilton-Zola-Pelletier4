package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Base implementation of FoldNode for Phase 3.
 * Handles both foldl and foldr based on the left flag.
 */
public class FoldNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode init;
    private SyntaxNode listExpr;
    private boolean left; // true for foldl, false for foldr

    public FoldNode(SyntaxNode func,
                    SyntaxNode init,
                    SyntaxNode listExpr,
                    boolean left,
                    int lineNumber)
    {
        super(lineNumber);
        this.func = func;
        this.init = init;
        this.listExpr = listExpr;
        this.left = left;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        // TODO: Implement full evaluation logic
        throw new UnsupportedOperationException("FoldNode.evaluate not implemented yet.");
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
    {
        // TODO: Implement full type checking logic
        throw new UnsupportedOperationException("FoldNode.typeOf not implemented yet.");
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented(left ? "FoldLNode(" : "FoldRNode(", indentAmt);
        func.displaySubtree(indentAmt + 2);
        init.displaySubtree(indentAmt + 2);
        listExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
