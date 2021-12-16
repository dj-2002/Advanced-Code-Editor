package com.nbow.advanceeditor.syntax;

import android.content.Context;
import android.text.Html;

import com.nbow.advanceeditor.code.CodeView;
import com.nbow.advanceeditor.*;

public class SyntaxManager {

    public static void applyMonokaiTheme(Context context, CodeView codeView, Language language) {
        switch (language) {
            case JAVA:
                JavaSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case PYTHON:
                PythonSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case PHP:
                PhpSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case CPP:
                CppSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case C:
                CSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case JAVASCRIPT:
                JavaScriptSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case HTML:
                HtmlSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case GO_LANG:
                GoSyntaxUtils.applyMonokaiTheme(context, codeView);
                break;
            case TXT:
                NoSyntaxUtils.applyMonokaiTheme(context,codeView);
                break;
            case NO_SYNTAX:
                NoSyntaxUtils.applyMonokaiTheme(context,codeView);
                break;
            default:
                DefaultSyntaxUtils.applyMonokaiTheme(context,codeView);
        }
    }

    public static void applyNoctisWhiteTheme(Context context, CodeView codeView, Language language) {
        switch (language) {
            case JAVA:
                JavaSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case PYTHON:
                PythonSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case PHP:
                PhpSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case CPP:
                CppSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case C:
                CSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case JAVASCRIPT:
                JavaSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case HTML:
                HtmlSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case GO_LANG:
                GoSyntaxUtils.applyNoctisWhiteTheme(context, codeView);
                break;
            case TXT:
                NoSyntaxUtils.applyNoctisWhiteTheme(context,codeView);
                break;
            case NO_SYNTAX:
                NoSyntaxUtils.applyNoctisWhiteTheme(context,codeView);
                break;
            default:
                DefaultSyntaxUtils.applyNoctisWhiteTheme(context,codeView);
        }
    }

    public static void applyFiveColorsDarkTheme(Context context, CodeView codeView, Language language) {
        switch (language) {
            case JAVA:
                JavaSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case PYTHON:
                PythonSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case PHP:
                PhpSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case CPP:
                CppSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case C:
                CSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case JAVASCRIPT:
                JavaSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case HTML:
                HtmlSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case GO_LANG:
                GoSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case TXT:
                NoSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            case NO_SYNTAX:
                NoSyntaxUtils.applyFiveColorsDarkTheme(context, codeView);
                break;
            default:
                DefaultSyntaxUtils.applyFiveColorsDarkTheme(context,codeView);
        }
    }

    public static void applyOrangeBoxTheme(Context context, CodeView codeView, Language language) {
        switch (language) {
            case JAVA:
                JavaSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case PYTHON:
                PythonSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case PHP:
                PhpSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;

            case CPP:
                CppSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case C:
                CSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case JAVASCRIPT:
                JavaSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case HTML:
                HtmlSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case GO_LANG:
                GoSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case TXT:
                NoSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            case NO_SYNTAX:
                NoSyntaxUtils.applyOrangeBoxTheme(context, codeView);
                break;
            default:
                DefaultSyntaxUtils.applyOrangeBoxTheme(context,codeView);
        }
    }

}
