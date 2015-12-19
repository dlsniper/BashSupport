package com.ansorgit.plugins.bash.lang.parser.eval;

import com.ansorgit.plugins.bash.file.BashFileType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class BashEvalElementType extends ILazyParseableElementType {
    public BashEvalElementType() {
        super("eval block", BashFileType.BASH_LANGUAGE);
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
        Project project = psi.getProject();

        String originalText = chameleon.getChars().toString();
        if (originalText.length() < 2) {
            throw new IncorrectOperationException("Can not handle empty strings");
        }

        boolean enhancedEscaping = originalText.startsWith("$'") && originalText.endsWith("'");
        boolean simpleEscaping = !enhancedEscaping && originalText.startsWith("\"") && originalText.endsWith("\"");

        String prefix = originalText.subSequence(0, enhancedEscaping ? 2 : 1).toString();
        String content = originalText.subSequence(enhancedEscaping ? 2 : 1, originalText.length() - 1).toString();
        String suffix = originalText.subSequence(originalText.length() - 1, originalText.length()).toString();

        TextPreprocessor textProcessor;
        if (enhancedEscaping) {
            textProcessor = new BashEnhancedTextPreprocessor(TextRange.from(2, content.length()));
        } else if (simpleEscaping) {
            textProcessor = new BashSimpleTextPreprocessor(TextRange.from(prefix.length(), content.length()));
        } else {
            textProcessor = new BashIdentityTextPreprocessor(TextRange.from(prefix.length(), content.length()));
        }

        StringBuilder unescapedContent = new StringBuilder(content.length());
        textProcessor.decode(content, unescapedContent);

        String unescpaedComplete = prefix + unescapedContent + suffix;

        ParserDefinition def = LanguageParserDefinitions.INSTANCE.forLanguage(BashFileType.BASH_LANGUAGE);
        PrefixSuffixAddingLexer prefixSuffixLexer = new PrefixSuffixAddingLexer(def.createLexer(project),
                prefix, TokenType.WHITE_SPACE,
                suffix, TokenType.WHITE_SPACE);

        UnescapingPsiBuilder adaptingPsiBuilder = new UnescapingPsiBuilder(project,
                def,
                prefixSuffixLexer,
                chameleon,
                originalText,
                unescpaedComplete,
                textProcessor);

        return def.createParser(project).parse(this, adaptingPsiBuilder).getFirstChildNode();
    }
}
