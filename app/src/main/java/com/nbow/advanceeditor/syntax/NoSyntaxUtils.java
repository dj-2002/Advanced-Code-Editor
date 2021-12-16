package com.nbow.advanceeditor.syntax;

import android.content.Context;
import android.graphics.Color;

import com.nbow.advanceeditor.R;
import com.nbow.advanceeditor.code.CodeView;

import java.util.regex.Pattern;

public class NoSyntaxUtils {

    //Language Keywords

    public static void applyMonokaiTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();

        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.monokia_pro_black));
        codeView.setLineNumberTextColor(Color.WHITE);
        codeView.setTextColor(Color.WHITE);

        //Syntax Colors

        codeView.reHighlightSyntax();
    }

    public static void applyNoctisWhiteTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.noctis_white));
        codeView.setLineNumberTextColor(Color.BLACK);
        codeView.setTextColor(Color.BLACK);

        //Syntax Colors

        codeView.reHighlightSyntax();
    }

    public static void applyFiveColorsDarkTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();
        codeView.setLineNumberTextColor(Color.WHITE);
        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.five_dark_black));
        codeView.setTextColor(Color.WHITE);

        //Syntax Colors

        codeView.reHighlightSyntax();
    }

    public static void applyOrangeBoxTheme(Context context, CodeView codeView) {
        codeView.resetSyntaxPatternList();

        //View Background
        codeView.setBackgroundColor(codeView.getResources().getColor(R.color.orange_box_black));
        codeView.setLineNumberTextColor(Color.WHITE);
        codeView.setTextColor(Color.WHITE);


        //Syntax Colors

        codeView.reHighlightSyntax();
    }
}
