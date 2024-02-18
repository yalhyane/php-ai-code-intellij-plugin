package com.yalhyane.intellij.phpaicode;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class NotificationUtils {

    public static final String PLUGIN_ID = "com.yalhyane.intellij.phpaicode.php-ai-code";

    public static void showErrorNotification(String title, String content, @Nullable AnAction... actions) {
        Notification notification = new Notification(PLUGIN_ID, title, content, NotificationType.ERROR);
        if (actions != null) {
            notification.addActions((Collection<? extends AnAction>) Arrays.asList(actions));
        }
        Notifications.Bus.notify(notification);
    }
}
