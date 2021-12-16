package com.nbow.advanceeditor.syntax;

import android.content.Context;
import android.graphics.Color;

import com.nbow.advanceeditor.R;
import com.nbow.advanceeditor.code.CodeView;

import java.util.regex.Pattern;

public class CSyntaxUtils {

    //Language Keywords
    private static final Pattern PATTERN_KEYWORDS = Pattern.compile("\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while)\\b");

    //    private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");
    private static final Pattern PATTERN_COMMENT = Pattern.compile("//(?!TODO )[^\\n]*" + "|" + "/\\*(.|\\R)*?\\*/");
    private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
    //    private static final Pattern PATTERN_OPERATION =Pattern.compile( ":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");
//    private static final Pattern PATTERN_GENERIC = Pattern.compile("<[a-zA-Z0-9,<>]+>");
    private static final Pattern PATTERN_ANNOTATION = Pattern.compile("@.[a-zA-Z0-9]+");
    private static final Pattern PATTERN_TODO_COMMENT = Pattern.compile("//TODO[^\n]*");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
    private static final Pattern PATTERN_CHAR = Pattern.compile("'[a-zA-Z0-9]'");
    //    private static final Pattern PATTERN_STRING = Pattern.compile("\".*\"");
    private static final Pattern PATTERN_STRING = Pattern.compile("\"[^\"]*\"");
//    private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");

    public static void applyMonokaiTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.monokia_pro_black));
        codeView.setLineNumberTextColor(Color.WHITE);

        //Syntax Colors
//        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.monokia_pro_purple));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.monokia_pro_purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.monokia_pro_pink));
//        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.monokia_pro_white));
        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.monokia_pro_green));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.monokia_pro_sky));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.monokia_pro_pink));
//        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.monokia_pro_green));
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

//        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.noctis_purple));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.noctis_purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.noctis_white_keyword));
//        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.noctis_dark_blue));

        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.noctis_white_annotation));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.noctis_blue));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.noctis_purple));
//        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.monokia_pro_pink));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.noctis_white_string));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.noctis_white_string));
        codeView.addSyntaxPattern(PATTERN_COMMENT, context.getResources().getColor(R.color.noctis_grey));
        //Default Color
        codeView.setTextColor( context.getResources().getColor(R.color.black));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.noctis_sky));

        codeView.reHighlightSyntax();
    }

    public static void applyFiveColorsDarkTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        codeView.setLineNumberTextColor(Color.WHITE);
        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.five_dark_black));

        //Syntax Colors
//        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.five_dark_light_purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.five_dark_purple));
//        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.five_dark_white));
        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.five_dark_green));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.five_dark_blue));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.five_dark_purple));
//        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.five_dark_purple));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.five_dark_yellow));
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
//        codeView.addSyntaxPattern(PATTERN_HEX, context.getResources().getColor(R.color.gold));
        codeView.addSyntaxPattern(PATTERN_NUMBERS, context.getResources().getColor(R.color.orange_box_purple));
        codeView.addSyntaxPattern(PATTERN_KEYWORDS, context.getResources().getColor(R.color.orange_box_orange1));
//        codeView.addSyntaxPattern(PATTERN_BUILTINS, context.getResources().getColor(R.color.orange_box_grey));
        codeView.addSyntaxPattern(PATTERN_ANNOTATION, context.getResources().getColor(R.color.orange_box_annotation));
        codeView.addSyntaxPattern(PATTERN_ATTRIBUTE, context.getResources().getColor(R.color.orange_box_orange3));
//        codeView.addSyntaxPattern(PATTERN_GENERIC, context.getResources().getColor(R.color.orange_box_orange1));
//        codeView.addSyntaxPattern(PATTERN_OPERATION, context.getResources().getColor(R.color.gold));
        codeView.addSyntaxPattern(PATTERN_CHAR, context.getResources().getColor(R.color.orange_box_orange2));
        codeView.addSyntaxPattern(PATTERN_STRING, context.getResources().getColor(R.color.orange_box_orange2));
        codeView.addSyntaxPattern(PATTERN_COMMENT, context.getResources().getColor(R.color.orange_box_grey));
        //Default Color
        codeView.setTextColor(context.getResources().getColor(R.color.white));

        codeView.addSyntaxPattern(PATTERN_TODO_COMMENT, context.getResources().getColor(R.color.gold));

        codeView.reHighlightSyntax();
    }

}
