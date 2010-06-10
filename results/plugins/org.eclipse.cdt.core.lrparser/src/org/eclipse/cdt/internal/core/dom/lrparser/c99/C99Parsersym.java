/*******************************************************************************
* Copyright (c) 2006, 2009 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.lrparser.c99;

public interface C99Parsersym {
    public final static int
      TK_auto = 25,
      TK_break = 31,
      TK_case = 32,
      TK_char = 41,
      TK_const = 19,
      TK_continue = 33,
      TK_default = 34,
      TK_do = 35,
      TK_double = 42,
      TK_else = 79,
      TK_enum = 54,
      TK_extern = 26,
      TK_float = 43,
      TK_for = 36,
      TK_goto = 37,
      TK_if = 38,
      TK_inline = 27,
      TK_int = 44,
      TK_long = 45,
      TK_register = 28,
      TK_restrict = 20,
      TK_return = 39,
      TK_short = 46,
      TK_signed = 47,
      TK_sizeof = 12,
      TK_static = 23,
      TK_struct = 55,
      TK_switch = 40,
      TK_typedef = 29,
      TK_union = 56,
      TK_unsigned = 48,
      TK_void = 49,
      TK_volatile = 21,
      TK_while = 30,
      TK__Bool = 50,
      TK__Complex = 51,
      TK__Imaginary = 52,
      TK_integer = 13,
      TK_floating = 14,
      TK_charconst = 15,
      TK_stringlit = 16,
      TK_identifier = 1,
      TK_Completion = 3,
      TK_EndOfCompletion = 4,
      TK_Invalid = 93,
      TK_LeftBracket = 53,
      TK_LeftParen = 2,
      TK_LeftBrace = 6,
      TK_Dot = 66,
      TK_Arrow = 80,
      TK_PlusPlus = 10,
      TK_MinusMinus = 11,
      TK_And = 9,
      TK_Star = 5,
      TK_Plus = 7,
      TK_Minus = 8,
      TK_Tilde = 17,
      TK_Bang = 18,
      TK_Slash = 67,
      TK_Percent = 68,
      TK_RightShift = 62,
      TK_LeftShift = 63,
      TK_LT = 69,
      TK_GT = 70,
      TK_LE = 71,
      TK_GE = 72,
      TK_EQ = 74,
      TK_NE = 75,
      TK_Caret = 76,
      TK_Or = 77,
      TK_AndAnd = 78,
      TK_OrOr = 81,
      TK_Question = 82,
      TK_Colon = 59,
      TK_DotDotDot = 64,
      TK_Assign = 61,
      TK_StarAssign = 83,
      TK_SlashAssign = 84,
      TK_PercentAssign = 85,
      TK_PlusAssign = 86,
      TK_MinusAssign = 87,
      TK_RightShiftAssign = 88,
      TK_LeftShiftAssign = 89,
      TK_AndAssign = 90,
      TK_CaretAssign = 91,
      TK_OrAssign = 92,
      TK_Comma = 57,
      TK_RightBracket = 65,
      TK_RightParen = 60,
      TK_RightBrace = 58,
      TK_SemiColon = 22,
      TK_ERROR_TOKEN = 24,
      TK_EOF_TOKEN = 73;

      public final static String orderedTerminalSymbols[] = {
                 "",
                 "identifier",
                 "LeftParen",
                 "Completion",
                 "EndOfCompletion",
                 "Star",
                 "LeftBrace",
                 "Plus",
                 "Minus",
                 "And",
                 "PlusPlus",
                 "MinusMinus",
                 "sizeof",
                 "integer",
                 "floating",
                 "charconst",
                 "stringlit",
                 "Tilde",
                 "Bang",
                 "const",
                 "restrict",
                 "volatile",
                 "SemiColon",
                 "static",
                 "ERROR_TOKEN",
                 "auto",
                 "extern",
                 "inline",
                 "register",
                 "typedef",
                 "while",
                 "break",
                 "case",
                 "continue",
                 "default",
                 "do",
                 "for",
                 "goto",
                 "if",
                 "return",
                 "switch",
                 "char",
                 "double",
                 "float",
                 "int",
                 "long",
                 "short",
                 "signed",
                 "unsigned",
                 "void",
                 "_Bool",
                 "_Complex",
                 "_Imaginary",
                 "LeftBracket",
                 "enum",
                 "struct",
                 "union",
                 "Comma",
                 "RightBrace",
                 "Colon",
                 "RightParen",
                 "Assign",
                 "RightShift",
                 "LeftShift",
                 "DotDotDot",
                 "RightBracket",
                 "Dot",
                 "Slash",
                 "Percent",
                 "LT",
                 "GT",
                 "LE",
                 "GE",
                 "EOF_TOKEN",
                 "EQ",
                 "NE",
                 "Caret",
                 "Or",
                 "AndAnd",
                 "else",
                 "Arrow",
                 "OrOr",
                 "Question",
                 "StarAssign",
                 "SlashAssign",
                 "PercentAssign",
                 "PlusAssign",
                 "MinusAssign",
                 "RightShiftAssign",
                 "LeftShiftAssign",
                 "AndAssign",
                 "CaretAssign",
                 "OrAssign",
                 "Invalid"
             };

    public final static boolean isValidForParser = true;
}
