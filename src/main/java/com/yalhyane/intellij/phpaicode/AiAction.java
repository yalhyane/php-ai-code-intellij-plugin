package com.yalhyane.intellij.phpaicode;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.yalhyane.intellij.phpaicode.settings.AppSettingsState;
import com.yalhyane.intellij.phpaicode.settings.OpenSettingsAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AiAction extends AnAction {

    // constants
    protected static final String MISSING_SETTINGS_NOTIFICATION_TITLE = "Missing settings";
    protected static final String UPDATE_SETTINGS_NOTIFICATION_CONTENT = "Please configure openAI token and model under AI comment settings";
    protected static final String INVALID_BLOCK_NOTIFICATION_CONTENT = "Please select code block or place the caret inside a function";
    protected static final String GENERAL_ERROR_NOTIFICATION_TITLE = "PHP AI Code";
    protected static final String COULD_NOT_DETECT_EDITOR_OR_FILE_NOTIFICATION_CONTENT = "Could not detect editor/file";
    static final AnAction OPEN_SETTINGS_ACTION = new OpenSettingsAction();


    protected AppSettingsState settings;
    protected PromptService promptService;

    public AiAction() {
        super();
        reloadSettings();
    }


    public void reloadSettings() {
        this.settings = AppSettingsState.getInstance();
        this.promptService = new PromptService(settings.openAiToken, settings.openAiModel);
    }

    public boolean hasInvalidSettings() {
        if (Objects.equals(this.settings.openAiToken, "") || Objects.equals(this.settings.openAiModel, "")) {
            NotificationUtils.showErrorNotification(MISSING_SETTINGS_NOTIFICATION_TITLE, UPDATE_SETTINGS_NOTIFICATION_CONTENT, OPEN_SETTINGS_ACTION);
            return true;
        }
        return false;
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
        e.getPresentation().setEnabled(editor != null && psiFile != null && this.settings.openAiToken != null);
    }


}
