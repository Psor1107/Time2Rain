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

/**
 * Orquestrador principal do compilador/interpretador RiskyLang.
 * Pipeline: Lexer → Parser → SemanticAnalysis → Interpreter
 */
public class App {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso: java -jar riskylang.jar <input> <output>");
            System.exit(1);
        }

        Path inputFile = Path.of(args[0]);
        Path outputFile = Path.of(args[1]);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            // ========== ETAPA 1: LEITURA DO ARQUIVO ==========
            String source;
            try {
                source = Files.readString(inputFile);
            } catch (IOException e) {
                writer.println("Erro: Não foi possível ler o arquivo de entrada: " + e.getMessage());
                return;
            }

            // ========== ETAPA 2: ANÁLISE LÉXICA E SINTÁTICA ==========
            ParseTree tree = parseProgram(source, writer);
            if (tree == null) {
                // Erros sintáticos já foram escritos
                return;
            }

            // ========== ETAPA 3: ANÁLISE SEMÂNTICA ==========
            SemanticVisitor semanticAnalyzer = new SemanticVisitor();
            semanticAnalyzer.visit(tree);

            // Verificar erros semânticos
            if (!semanticAnalyzer.getErrors().isEmpty()) {
                writer.println("ERROS SEMÂNTICOS ENCONTRADOS:");
                writer.println("=============================");
                semanticAnalyzer.getErrors().forEach(writer::println);
                return;
            }

            // Exibir warnings se houver
            if (!semanticAnalyzer.getWarnings().isEmpty()) {
                writer.println("AVISOS:");
                writer.println("======");
                semanticAnalyzer.getWarnings().forEach(writer::println);
                writer.println();
            }

            // ========== ETAPA 4: INTERPRETAÇÃO E GERAÇÃO DE CÓDIGO ==========
            InterpreterVisitor interpreter = new InterpreterVisitor(semanticAnalyzer);
            interpreter.visit(tree);
            String report = interpreter.generateReport();
            writer.println(report);

        } catch (IOException e) {
            System.err.println("Erro de I/O: " + e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Realiza a análise léxica e sintática do código-fonte.
     * @return árvore de parse ou null em caso de erro
     */
    private static ParseTree parseProgram(String source, PrintWriter writer) {
        List<String> syntaxErrors = new ArrayList<>();

        try {
            // Criar lexer
            CharStream charStream = CharStreams.fromString(source);
            RoR2Lexer lexer = new RoR2Lexer(charStream);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new ErrorCollector(syntaxErrors));

            // Criar parser
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            RoR2Parser parser = new RoR2Parser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new ErrorCollector(syntaxErrors));
            parser.setErrorHandler(new BailErrorStrategy());

            // Fazer parsing
            ParseTree tree = parser.program();

            // Verificar se houve erros léxicos/sintáticos
            if (!syntaxErrors.isEmpty()) {
                writer.println("ERROS SINTÁTICOS ENCONTRADOS:");
                writer.println("=============================");
                syntaxErrors.forEach(writer::println);
                return null;
            }

            return tree;

        } catch (ParseCancellationException ex) {
            writer.println("ERRO SINTÁTICO:");
            writer.println("==============");
            writer.println("Sintaxe inválida detectada no programa.");
            if (!syntaxErrors.isEmpty()) {
                syntaxErrors.forEach(writer::println);
            }
            return null;
        }
    }

    /**
     * Coletor de erros de análise léxica e sintática.
     */
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
            String error = String.format("Linha %d, coluna %d: %s", line, charPositionInLine, msg);
            errors.add(error);
        }
    }
}