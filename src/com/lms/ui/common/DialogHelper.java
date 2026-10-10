package com.lms.ui.common;

import com.lms.util.UITheme;

import javax.swing.JOptionPane;
import java.awt.Component;

public class DialogHelper {

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int res = JOptionPane.showConfirmDialog(parent, message, "Please Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return res == JOptionPane.YES_OPTION;
    }

    public static String prompt(Component parent, String message, String initialValue) {
        return JOptionPane.showInputDialog(parent, message, initialValue);
    }
}
