package com.company;

public enum TokenType {
    EndOfInput, Multiply, Divide, Mod, Plus, Minus, Not, Less, LessEqual, Greater, GreaterEqual,
    Equal, NotEqual, Assign, And, Or, KeywordIf,
    KeywordElse, KeywordWhile, KeywordPrint, KeywordRead, KeywordInteger, KeywordString, KeywordHex, KeywordReturn, KeywordBinary, LeftParentheses, RightParentheses,
    Begin, End, Semicolon, Comma, Identifier, Integer, String, Binary, Hexadecimal, FloatPointNumber, Error, KeywordFloat, BeginBracket, EndBracket, Void, Bool, True, False, Main, KeywordChar,
    Char, KeywordEqual, KeywordNotEqual, KeywordLess, KeywordGreater, KeywordLessEqual, KeywordGreaterEqual
}