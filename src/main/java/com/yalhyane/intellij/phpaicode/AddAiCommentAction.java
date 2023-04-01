package com.yalhyane.intellij.phpaicode;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
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
import com.yalhyane.intellij.phpaicode.settings.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AddAiCommentAction extends AnAction {

    private AppSettingsState settings;
    private OkHttpChatGptApi chatGptAPI;

    public AddAiCommentAction() {
        super();
        this.settings = AppSettingsState.getInstance();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        CaretModel caret = editor.getCaretModel();
        Document doc = editor.getDocument();
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) {
            return;
        }

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
                return;
            }

            FunctionImpl pe = PsiTreeUtil.getParentOfType(element1, FunctionImpl.class);

            if (pe == null) {
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
        chatGptAPI = new OkHttpChatGptApi(settings.chatgptToken);
        String prompt = getPrompt(funcBody, blockType);
        try {
            String comment = chatGptAPI.completion(prompt);

            comment = comment.trim();
            if (comment.startsWith("\"")) {
                comment = comment.substring(1);
            }
            if (comment.endsWith("\"")) {
                comment = comment.substring(0, comment.length() - 1);
            }
            return " " + funcName + " " + comment;
        } catch (Exception e) {
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

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        // Set the availability based on whether a project is open
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
         e.getPresentation().setEnabled(editor != null && psiFile != null && this.settings.chatgptToken != null);
    }
}
