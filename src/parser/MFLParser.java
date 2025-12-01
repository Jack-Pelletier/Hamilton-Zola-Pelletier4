/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import ast.FoldNode;
import ast.MapNode;
import ast.SyntaxTree;
import ast.nodes.ApplyNode;
import ast.nodes.BinOpNode;
import ast.nodes.HeadNode;
import ast.nodes.IfNode;
import ast.nodes.LambdaNode;
import ast.nodes.LenNode;
import ast.nodes.LetNode;
import ast.nodes.ListNode;
import ast.nodes.ProgNode;
import ast.nodes.RelOpNode;
import ast.nodes.SyntaxNode;
import ast.nodes.TailNode;
import ast.nodes.TokenNode;
import ast.nodes.UnaryOpNode;
import ast.nodes.ValNode;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

/**
 * this is the parser for the MFL language.
 */
public class MFLParser extends Parser
{
    /**
     * this is the constructor for parsing from a file.
     */
    public MFLParser(File src) throws FileNotFoundException
    {
        super(new Lexer(src));
    }

    /**
     * this is the constructor for parsing from a string.
     */
    public MFLParser(String str)
    {
        super(new Lexer(str));
    }

    /**
     * this is the entry point for parsing a program.
     */
    @Override
    public SyntaxTree parse() throws ParseException
    {
        SyntaxTree ast;

        nextToken();                     // this is where we get the first token
        ast = new SyntaxTree(evalProg()); // this is where we parse from the root

        match(TokenType.EOF, "EOF");
        return ast;
    }

    /****************************************
     * this is the non-terminal eval methods
     ****************************************/

    /**
     * this is <prog> -> <expr> { <expr> }
     */
    private SyntaxNode evalProg() throws ParseException
    {
        LinkedList<SyntaxNode> exprs = new LinkedList<>();

        trace("Enter <prog>");
        while (!checkMatch(TokenType.EOF))
        {
            SyntaxNode currNode = evalValues();
            if (currNode == null)
                break;

            // this is enforcing that every top level expr ends with ;
            match(TokenType.SEMI, ";");
            exprs.add(currNode);
        }

        if (exprs.isEmpty())
            return null;

        trace("Exit <prog>");
        return new ProgNode(exprs, getCurrLine());
    }

    /**
     * this is <values> = val-def | expr
     */
    private SyntaxNode evalValues() throws ParseException
    {
        if (checkMatch(TokenType.VAL))
            return getGoodParse(handleValues());
        else
            return getGoodParse(evalExpr());
    }

    /**
     * this is <expr> for let, if, fn, or boolean expressions.
     */
    private SyntaxNode evalExpr() throws ParseException
    {
        trace("Enter <expr>");
        SyntaxNode expr;

        if (checkMatch(TokenType.LET))
            return handleLet();
        else if (checkMatch(TokenType.IF))
            return handleIf();
        else if (checkMatch(TokenType.FN))
            return handleLambda();
        else
            expr = getGoodParse(evalBoolExpr());

        return expr;
    }

    /**
     * this is <bexpr> for and/or chains.
     */
    private SyntaxNode evalBoolExpr() throws ParseException
    {
        SyntaxNode rexpr;
        TokenType op;
        SyntaxNode expr;

        trace("Enter <bexpr>");

        expr = getGoodParse(evalRexpr());
        op = getCurrToken().getType();

        while (checkMatch(TokenType.AND) || checkMatch(TokenType.OR))
        {
            rexpr = getGoodParse(evalRexpr());
            expr = new BinOpNode(expr, op, rexpr, getCurrLine());
            op = getCurrToken().getType();
        }

        trace("Exit <bexpr>");
        return expr;
    }

    /**
     * this is <rexpr> for relational operators.
     */
    private SyntaxNode evalRexpr() throws ParseException
    {
        SyntaxNode left;
        SyntaxNode right;
        TokenType op;

        left = getGoodParse(evalMexpr());
        op = getCurrToken().getType();

        if (checkMatch(TokenType.LT)  || checkMatch(TokenType.LTE) ||
            checkMatch(TokenType.GT)  || checkMatch(TokenType.GTE) ||
            checkMatch(TokenType.EQ)  || checkMatch(TokenType.NEQ))
        {
            right = getGoodParse(evalMexpr());
            return new RelOpNode(left, op, right, getCurrLine());
        }

        return left;
    }

    /**
     * this is <mexpr> for + and -.
     */
    private SyntaxNode evalMexpr() throws ParseException
    {
        SyntaxNode expr;
        SyntaxNode rterm;
        TokenType op;

        expr = getGoodParse(evalTerm());
        op = getCurrToken().getType();

        while (checkMatch(TokenType.ADD) || checkMatch(TokenType.SUB))
        {
            rterm = getGoodParse(evalTerm());
            expr = new BinOpNode(expr, op, rterm, getCurrLine());
            op = getCurrToken().getType();
        }

        return expr;
    }

    /**
     * this is <term> for *, /, mod, and ++.
     */
    private SyntaxNode evalTerm() throws ParseException
    {
        SyntaxNode rfact;
        TokenType op;
        SyntaxNode term;

        trace("Enter <term>");

        // this is unary not
        if (checkMatch(TokenType.NOT))
        {
            SyntaxNode expr = getGoodParse(evalRexpr());
            return new UnaryOpNode(expr, TokenType.NOT, getCurrLine());
        }

        term = getGoodParse(evalFactor());
        op = getCurrToken().getType();

        while (checkMatch(TokenType.MULT) || checkMatch(TokenType.DIV) ||
               checkMatch(TokenType.MOD)  || checkMatch(TokenType.CONCAT))
        {
            rfact = getGoodParse(evalFactor());
            term = new BinOpNode(term, op, rfact, getCurrLine());
            op = getCurrToken().getType();
        }

        trace("Exit <term>");
        return term;
    }

    /**
     * this is <factor> for the tightest binding expressions.
     *
     * we also hook in:
     *   map f xs
     *   foldl f init xs
     *   foldr f init xs
     *   function application
     */
    private SyntaxNode evalFactor() throws ParseException
    {
        trace("Enter <factor>");
        SyntaxNode fact = null;

        // this is map f xs
        if (checkMatch(TokenType.MAP))
        {
            SyntaxNode func = getGoodParse(evalExpr());
            SyntaxNode lst  = getGoodParse(evalExpr());
            return new MapNode(func, lst, getCurrLine());
        }

        // this is foldl f init xs
        if (checkMatch(TokenType.FOLDL))
        {
            SyntaxNode func = getGoodParse(evalExpr());
            SyntaxNode init = getGoodParse(evalExpr());
            SyntaxNode lst  = getGoodParse(evalExpr());
            return new FoldNode(func, init, lst, true, getCurrLine());
        }

        // this is foldr f init xs
        if (checkMatch(TokenType.FOLDR))
        {
            SyntaxNode func = getGoodParse(evalExpr());
            SyntaxNode init = getGoodParse(evalExpr());
            SyntaxNode lst  = getGoodParse(evalExpr());
            return new FoldNode(func, init, lst, false, getCurrLine());
        }

        // this is unary minus
        if (checkMatch(TokenType.SUB))
        {
            SyntaxNode expr = getGoodParse(evalFactor());
            return new UnaryOpNode(expr, TokenType.SUB, getCurrLine());
        }

        // this is hd(expr)
        else if (checkMatch(TokenType.LST_HD))
        {
            if (match(TokenType.LPAREN, "("))
            {
                fact = getGoodParse(evalExpr());
                if (match(TokenType.RPAREN, ")"))
                    return new HeadNode(fact, getCurrLine());
            }
            return null;
        }

        // this is tl(expr)
        else if (checkMatch(TokenType.LST_TL))
        {
            if (match(TokenType.LPAREN, "("))
            {
                fact = getGoodParse(evalExpr());
                if (match(TokenType.RPAREN, ")"))
                    return new TailNode(fact, getCurrLine());
            }
            return null;
        }

        // this is len(expr)
        else if (checkMatch(TokenType.LEN))
        {
            if (match(TokenType.LPAREN, "("))
            {
                fact = getGoodParse(evalExpr());
                if (match(TokenType.RPAREN, ")"))
                    return new LenNode(fact, getCurrLine());
            }
            return null;
        }

        // this is list constructor
        else if (checkMatch(TokenType.LBRACK))
            return getGoodParse(evalListExpr());

        // this is parenthesized expression and chained application
        else if (checkMatch(TokenType.LPAREN))
        {
            fact = getGoodParse(evalExpr());
            match(TokenType.RPAREN, ")");

            // this is (E1)(E2) style application
            while (tokenIs(TokenType.LPAREN))
            {
                match(TokenType.LPAREN, "(");
                SyntaxNode arg = getGoodParse(evalExpr());
                match(TokenType.RPAREN, ")");
                fact = new ApplyNode(fact, arg, getCurrLine());
            }
        }

        // this is literals
        else if (tokenIs(TokenType.INT)  || tokenIs(TokenType.REAL) ||
                 tokenIs(TokenType.TRUE) || tokenIs(TokenType.FALSE))
        {
            fact = new TokenNode(getCurrToken(), getCurrLine());
            nextToken();
            return fact;
        }

        // this is identifier and possible id(expr) application
        else if (tokenIs(TokenType.ID))
        {
            Token ident = getCurrToken();
            nextToken();

            fact = new TokenNode(ident, getCurrLine());

            // this is id(expr) application
            if (tokenIs(TokenType.LPAREN))
            {
                match(TokenType.LPAREN, "(");
                SyntaxNode arg = getGoodParse(evalExpr());
                match(TokenType.RPAREN, ")");
                fact = new ApplyNode(fact, arg, getCurrLine());
            }
        }

        trace("Exit <factor>");
        return fact;
    }

    /****************************************
     * this is helper non-terminals
     ****************************************/

    /**
     * this is <listExpr> -> [ <expr> { , <expr> } ]
     */
    private SyntaxNode evalListExpr() throws ParseException
    {
        LinkedList<SyntaxNode> entries = new LinkedList<>();
        ListNode lst = null;
        SyntaxNode expr;

        trace("Enter <listExpr>");

        // this is an empty list
        if (checkMatch(TokenType.RBRACK))
        {
            lst = new ListNode(entries, getCurrLine());
            return lst;
        }

        // this is at least one element
        expr = getGoodParse(evalExpr());
        entries.add(expr);

        while (checkMatch(TokenType.COMMA))
        {
            expr = getGoodParse(evalExpr());
            entries.add(expr);
        }

        if (match(TokenType.RBRACK, "]"))
            lst = new ListNode(entries, getCurrLine());

        trace("Exit <listExpr>");
        return lst;
    }

    /**
     * this is handling value definitions:
     *
     *   val x := expr
     *   val f x := expr    (sugar for val f := fn x -> expr)
     */
    private SyntaxNode handleValues() throws ParseException
    {
        // this is the name being defined
        Token id = getCurrToken();
        match(TokenType.ID, "identifier");

        // this is optional parameter for named function definitions
        Token param = null;
        if (tokenIs(TokenType.ID))
        {
            param = getCurrToken();
            nextToken();
        }

        match(TokenType.ASSIGN, ":=");
        SyntaxNode expr = getGoodParse(evalExpr());

        // this is where we desugar val f x := e into val f := fn x -> e
        if (param != null)
        {
            expr = new LambdaNode(param, expr, getCurrLine());
        }

        return new ValNode(id, expr, getCurrLine());
    }

    /**
     * this is handling let expressions.
     */
    private SyntaxNode handleLet() throws ParseException
    {
        Token var = getCurrToken();
        SyntaxNode varExpr;
        SyntaxNode expr;

        trace("enter handleLet");

        match(TokenType.ID, "identifier");
        match(TokenType.ASSIGN, ":=");
        varExpr = getGoodParse(evalExpr());

        match(TokenType.IN, "in");
        expr = getGoodParse(evalExpr());

        return new LetNode(var, varExpr, expr, getCurrLine());
    }

    /**
     * this is handling if expressions.
     */
    private SyntaxNode handleIf() throws ParseException
    {
        SyntaxNode cond;
        SyntaxNode tBranch;
        SyntaxNode fBranch;

        trace("enter handleIf");

        cond = getGoodParse(evalExpr());

        match(TokenType.THEN, "then");
        tBranch = getGoodParse(evalExpr());

        match(TokenType.ELSE, "else");
        fBranch = getGoodParse(evalExpr());

        return new IfNode(cond, tBranch, fBranch, getCurrLine());
    }

    /**
     * this is handling lambda expressions: fn <id> -> <expr>
     */
    private SyntaxNode handleLambda() throws ParseException
    {
        Token param = getCurrToken();
        match(TokenType.ID, "identifier");

        match(TokenType.ARROW, "->");
        SyntaxNode body = getGoodParse(evalExpr());

        return new LambdaNode(param, body, getCurrLine());
    }
}
