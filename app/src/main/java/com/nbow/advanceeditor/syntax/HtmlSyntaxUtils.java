package com.nbow.advanceeditor.syntax;

import android.content.Context;
import android.graphics.Color;

import com.nbow.advanceeditor.R;
import com.nbow.advanceeditor.code.CodeView;

import java.util.regex.Pattern;

public class HtmlSyntaxUtils {

    //Language Keywords
//    private static final Pattern PATTERN_KEYWORDS = Pattern.compile("\\b()\\b");
    private static final Pattern PATTERN_HTML_TAG = Pattern.compile("<[\\s]*[a-zA-Z0-9]+[\\s]{0,1}"+"|"+"</[\\s]*[a-zA-Z0-9]+[\\s]*>"+"|"+">"+"|"+"/>");
    private static final Pattern PATTERN_HTML_ATTRIBUTE = Pattern.compile("[\\s]+[a-zA-Z]+[\\s]*=");


    private static final Pattern PATTERN_BUILTINS = Pattern.compile("[-></]");
    private static final Pattern PATTERN_COMMENT = Pattern.compile("\\<\\!\\-\\-(?:.|\\n|\\r)*?-->");
    //    private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
    private static final Pattern PATTERN_OPERATION =Pattern.compile( "=|-|\\&|\\|;");
    //    private static final Pattern PATTERN_GENERIC = Pattern.compile("<[a-zA-Z0-9,<>]+>");
//    private static final Pattern PATTERN_ANNOTATION = Pattern.compile("@.[a-zA-Z0-9]+");
    private static final Pattern PATTERN_TODO_COMMENT = Pattern.compile("//TODO[^\n]*");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    //    private static final Pattern PATTERN_CHAR = Pattern.compile("'[a-zA-Z]'");
//    private static final Pattern PATTERN_STRING = Pattern.compile("\".*\"");
    private static final Pattern PATTERN_STRING = Pattern.compile("\"[^<>\"]*\"" + "|" + "\'[^<>\']*\'");

    private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");

    public static void applyMonokaiTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.monokia_pro_black));
        codeView.setLineNumberTextColor(Color.WHITE);

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.monokia_pro_sky));
//        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.monokia_pro_green));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.monokia_pro_sky));

        codeView.addSyntaxPattern(PATTERN_HTML_TAG, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_HTML_ATTRIBUTE, context.getResources().getColor(R.color.monokia_pro_purple));

//        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.monokia_pro_white));
//        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.monokia_pro_pink));
//        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.monokia_pro_sky));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.monokia_pro_orange));
        codeView.addSyntaxPattern(PATTERN_COMMENT, context.getResources().getColor(R.color.monokia_pro_grey));

        //Default Color
        codeView.setTextColor( context.getResources().getColor(R.color.monokia_pro_white));
        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.gold));
        codeView.reHighlightSyntax();
    }

    public static void applyNoctisWhiteTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.noctis_white));
        codeView.setLineNumberTextColor(Color.BLACK);

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HTML_TAG, context.getResources().getColor(R.color.noctis_white_html_tag));
        codeView.addSyntaxPattern(PATTERN_HTML_ATTRIBUTE, context.getResources().getColor(R.color.noctis_white_html_attr));


        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.noctis_purple));
//        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.noctis_green));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.noctis_purple));
//        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.noctis_pink));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.noctis_dark_blue));

//        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.monokia_pro_pink));
//        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.noctis_blue));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.noctis_white_html_string));
        codeView.addSyntaxPattern(PATTERN_COMMENT, context.getResources().getColor(R.color.noctis_grey));
        //Default Color
        codeView.setTextColor( context.getResources().getColor(R.color.black));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.gold));

        codeView.reHighlightSyntax();
    }

    public static void applyFiveColorsDarkTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        codeView.setLineNumberTextColor(Color.WHITE);
        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.five_dark_black));

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HTML_TAG, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_HTML_ATTRIBUTE, context.getResources().getColor(R.color.noctis_blue));

        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.five_dark_purple));
//        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.five_dark_yellow));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.five_dark_purple));
//        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.five_dark_white));
//        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.five_dark_purple));
//        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.five_dark_blue));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.five_dark_yellow));
        codeView.addSyntaxPattern(PATTERN_COMMENT, context.getResources().getColor(R.color.five_dark_grey));
        //Default Color
        codeView.setTextColor( context.getResources().getColor(R.color.five_dark_white));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.gold));

        codeView.reHighlightSyntax();
    }

    public static void applyOrangeBoxTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();

        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.orange_box_black));
        codeView.setLineNumberTextColor(Color.WHITE);

        //Syntax Colors
        codeView.addSyntaxPattern(PATTERN_HTML_TAG, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_HTML_ATTRIBUTE, context.getResources().getColor(R.color.noctis_blue));

        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.gold));
//        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.orange_box_orange2));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.five_dark_purple));
//        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.orange_box_orange1));
        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.orange_box_grey));
//        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.orange_box_orange1));
//        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.orange_box_orange3));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.orange_box_orange1));
        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.gold));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.orange_box_orange2));
        codeView.addSyntaxPattern(PATTERN_COMMENT, context.getResources().getColor(R.color.orange_box_dark_grey));
        //Default Color
        codeView.setTextColor(context.getResources().getColor(R.color.five_dark_white));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.gold));

        codeView.reHighlightSyntax();
    }
}
