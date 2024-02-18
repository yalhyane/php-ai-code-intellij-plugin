package com.yalhyane.intellij.phpaicode;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.impl.FunctionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AddAiCommentAction extends AiAction {


    // constants
    public static final String ACTION_ID = "PHP.GenerateDocComment";

    public static AiAction getInstance() {
        return (AddAiCommentAction) ActionManager.getInstance().getAction(ACTION_ID);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {


        if (this.hasInvalidSettings()) {
            return;
        }

        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) {
            NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, COULD_NOT_DETECT_EDITOR_OR_FILE_NOTIFICATION_CONTENT);
            return;
        }

        CaretModel caret = editor.getCaretModel();

        PsiElement element = null;
        String blockCode = "";
        String blockName = "";
        String blockType = "";
        PhpDocComment psiComment = null;
        Parameter[] functionParams;
        String returnType = "";


        // handle selection
        if (editor.getSelectionModel().hasSelection()) {
            returnType = "";
            functionParams = null;
            blockCode = editor.getSelectionModel().getSelectedText();
            element = psiFile.findElementAt(editor.getSelectionModel().getSelectionStart());
        } else {
            // handle function or method
            PsiElement element1 = psiFile.findElementAt(caret.getOffset());
            if (element1 == null) {
                NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, INVALID_BLOCK_NOTIFICATION_CONTENT);
                return;
            }

            FunctionImpl pe = PsiTreeUtil.getParentOfType(element1, FunctionImpl.class);

            if (pe == null) {
                NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, INVALID_BLOCK_NOTIFICATION_CONTENT);
                return;
            }
            if (pe.getReturnType() != null) {
                returnType = pe.getReturnType().getType().toString();
            } else if(pe.getDocComment() != null && pe.getDocComment().getReturnTag() != null) {
                returnType = pe.getDocComment().getReturnTag().getType().toString();
            }
//
            blockName = pe.getName();
            blockCode = pe.getText();
            blockType = "function";
            functionParams = pe.getParameters();
            element = pe;
            psiComment = pe.getDocComment();

        }


        if (element == null) {
            NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, INVALID_BLOCK_NOTIFICATION_CONTENT);
            return;
        }


        String finalBlockName = blockName;
        String finalBlockCode = blockCode;
        String finalBlockType = blockType;
        PsiElement finalElement = element;
        PhpDocComment finalPsiComment = psiComment;
        String finalReturnType = returnType;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            String comment = this.getComment(finalBlockName, finalBlockCode, finalBlockType);
            String commentWithParams = comment + "\n";

            if (functionParams != null) {
                for (Parameter param: functionParams) {
                    String type = param.getType().toString();
                    commentWithParams += "@param " + (type != "" ? type : "mixed") + " $" + param.getName() + "\n";
                }
            }
            if (!Objects.equals(finalReturnType, "")) {
                commentWithParams += "@return " + finalReturnType + "\n";
            }
            String fullComment = "/**\n" + commentWithParams + "\n */";
            PsiElement newPsiComment = PhpPsiElementFactory.createFromText(project, PhpDocComment.class, fullComment);
            if (finalBlockType.equals("")) {
                fullComment = "// " + commentWithParams;
                newPsiComment = PhpPsiElementFactory.createFromText(project, PsiComment.class, fullComment);
            }

            if (finalPsiComment != null) {
                finalPsiComment.replace(newPsiComment);
            } else {
                System.out.println("pe.getParent(): " + finalElement.getParent().getClass());
                finalElement.getParent().addBefore(newPsiComment, finalElement);
                finalElement.getParent().addBefore(PhpPsiElementFactory.createWhiteSpace(project), finalElement);
            }
        });

    }

    private String getComment(String funcName, String funcBody, String blockType) {
        try {
            String comment = promptService.executePrompt(getPrompt(funcBody, blockType));

            comment = comment.trim();
            if (comment.startsWith("\"")) {
                comment = comment.substring(1);
            }
            if (comment.endsWith("\"")) {
                comment = comment.substring(0, comment.length() - 1);
            }
            return " " + funcName + " " + comment;
        } catch (Exception e) {
            NotificationUtils.showErrorNotification(GENERAL_ERROR_NOTIFICATION_TITLE, e.getMessage());
            return "";
        }
    }

    private String getPrompt(String blockCode, String blockType) {
        return "Write an insightful but concise comment in a complete sentence"
                .concat("in present tense for the following")
                .concat("PHP " + blockType + " without prefacing it with anything,")
                .concat("the response must be in the language english")
                .concat(":\n")
                .concat(blockCode);
    }

}
