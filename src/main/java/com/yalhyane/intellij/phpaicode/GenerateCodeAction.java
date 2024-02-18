package com.yalhyane.intellij.phpaicode;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocTokenImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GenerateCodeAction extends AiAction {

    // constants
    public static final String ACTION_ID = "PHP.GenerateCode";



    public static GenerateCodeAction getInstance() {
        return (GenerateCodeAction) ActionManager.getInstance().getAction(ACTION_ID);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {


        if (this.hasInvalidSettings()) {
            return;
        }

        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, COULD_NOT_DETECT_EDITOR_OR_FILE_NOTIFICATION_CONTENT);
            return;
        }

        Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, COULD_NOT_DETECT_EDITOR_OR_FILE_NOTIFICATION_CONTENT);
            return;
        }


        String codeDescription;
        PsiElement element;
        int insertAfter;

        // in case of selection
        if (editor.getSelectionModel().hasSelection()) {
            codeDescription = editor.getSelectionModel().getSelectedText();
            if (codeDescription.startsWith("//")) {
                codeDescription = codeDescription.substring(2).trim();
            }
            int start = primaryCaret.getSelectionStart();
            element = psiFile.findElementAt(start);
            insertAfter = primaryCaret.getSelectionEnd() + 1;
        } else {

            // in case of comment
            element = psiFile.findElementAt(editor.getCaretModel().getOffset());
            if (element instanceof PsiComment) {
                codeDescription = element.getText().substring(2).trim();
                insertAfter = element.getTextOffset() + element.getTextLength() + 1;
            } else {
                // in case of doc comment
                PhpDocComment c = PsiTreeUtil.getParentOfType(element, PhpDocComment.class);
                if (c != null) {
                    element = c;
                    StringBuilder prompt = new StringBuilder();
                    PsiElement e = c.getFirstChild().getNextSibling();
                    while (e instanceof PhpDocTokenImpl || e instanceof PsiWhiteSpace) {

                        if (e instanceof PhpDocTokenImpl && ((PhpDocTokenImpl) e).getElementType() == PhpDocTokenTypes.DOC_COMMENT_END) {
                            break;
                        }

                        if (e instanceof PhpDocTokenImpl && !Objects.equals(e.getText().trim(), "*")) {
                            prompt.append(e.getText().trim());
                        }
                        if (e instanceof PsiWhiteSpace) {
                            prompt.append(" ");
                        }
                        e = e.getNextSibling();
                    }
                    codeDescription = prompt.toString().trim();
                    insertAfter = element.getTextOffset() + element.getTextLength() + 1;
                } else {
                    // if not of the above show dialog
                    codeDescription = Messages.showMultilineInputDialog(project, "Describe your code", "Code Prompt", "", Messages.getQuestionIcon(), null);
                    insertAfter = primaryCaret.getOffset();
                }
            }

        }


        String finalCodeDescription = codeDescription;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            String code = this.getCode(finalCodeDescription);
            int position = insertAfter;
            if (position > document.getTextLength()) {
                position = document.getTextLength();
            }
            document.insertString(position, code + "\n");

        });

        // De-select the text range that was just replaced
        primaryCaret.removeSelection();

    }

    private String getCode(String codeDescription) {

        try {
            String prompt = getPrompt(codeDescription);
            String code = promptService.executePrompt(prompt);

            code = code.trim();
            if (code.startsWith("\"")) {
                code = code.substring(1);
            }
            if (code.endsWith("\"")) {
                code = code.substring(0, code.length() - 1);
            }
            return code;
        } catch (Exception e) {
            NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, e.getMessage());
            return "";
        }
    }

    private String getPrompt(String blockCode) {
        return "I want you to act as a PHP developer. I will describe what I want and you will reply with corresponding PHP Code."
                .concat("I want you to only reply with one unique code block without the PHP opening and closing tags, and nothing else. Do not write explanations.")
                .concat("My first request is")
                .concat(":\n")
                .concat("\"")
                .concat(blockCode)
                .concat("\"");
    }

}
