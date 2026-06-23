package com.ror2;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import com.ror2.parser.RoR2Lexer;
import com.ror2.parser.RoR2Parser;

public class App {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso: java -jar riskylang.jar <input> <output>");
            System.exit(1);
        }

        Path inputFile = Path.of(args[0]);
        Path outputFile = Path.of(args[1]);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            String source = Files.readString(inputFile);
            CharStream charStream = CharStreams.fromString(source);
            RoR2Lexer lexer = new RoR2Lexer(charStream);
            List<String> lexerErrors = new ArrayList<>();
            lexer.removeErrorListeners();
            lexer.addErrorListener(new ErrorCollector(lexerErrors));

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            RoR2Parser parser = new RoR2Parser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new ErrorCollector(lexerErrors));
            parser.setErrorHandler(new BailErrorStrategy());

            try {
                ParseTree tree = parser.program();
                if (!lexerErrors.isEmpty()) {
                    lexerErrors.forEach(writer::println);
                    return;
                }

                SemanticVisitor semanticVisitor = new SemanticVisitor();
                semanticVisitor.visit(tree);
                if (!semanticVisitor.getErrors().isEmpty()) {
                    semanticVisitor.getErrors().forEach(writer::println);
                    return;
                }
                if (!semanticVisitor.getWarnings().isEmpty()) {
                    semanticVisitor.getWarnings().forEach(writer::println);
                    writer.println();
                }

                InterpreterVisitor interpreter = new InterpreterVisitor(semanticVisitor.getArtifacts());
                String report = interpreter.visit(tree);
                writer.println(report);
            } catch (ParseCancellationException ex) {
                writer.println("Erro Sintático: entrada inválida");
            }
        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
            System.exit(2);
        }
    }

    private static class ErrorCollector extends ConsoleErrorListener {
        private final List<String> errors;

        ErrorCollector(List<String> errors) {
            this.errors = errors;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line,
                                int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            errors.add("Erro Sintático: " + msg);
        }
    }
}