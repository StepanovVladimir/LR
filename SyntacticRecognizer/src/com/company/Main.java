package com.company;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            ArrayList<Row> table = readTable(args[0]);
            ArrayList<Token> input = readInput(args[1]);

            Stack<Integer> statesStack = new Stack<>();
            Stack<String> charsStack = new Stack<>();
            statesStack.push(0);

            Set<String> possibleChs = new TreeSet<>();

            Semantics semantics = new Semantics(charsStack, args[2]);
            while (!input.isEmpty())
            {
                Row row = table.get(statesStack.peek());
                Token token = input.get(0);
                if (row.transitions.containsKey(token.value))
                {
                    statesStack.push(row.transitions.get(token.value).state);
                    charsStack.push(token.value);

                    String function = row.transitions.get(token.value).function;
                    if (function != null)
                    {
                        semantics.doFunction(function);
                    }

                    if (!isNontermChar(token.value))
                    {
                        possibleChs.clear();
                    }

                    input.remove(0);

                    if (input.isEmpty())
                    {
                        System.out.println("Ok");
                    }
                }
                else if (row.transitions.containsKey("[" + token.tokentype.toString() + "]"))
                {
                    String tokenType = "[" + token.tokentype.toString() + "]";
                    statesStack.push(row.transitions.get(tokenType).state);
                    charsStack.push(token.value);

                    String function = row.transitions.get(tokenType).function;
                    if (function != null)
                    {
                        semantics.doFunction(function);
                    }

                    if (!isNontermChar(token.value))
                    {
                        possibleChs.clear();
                    }

                    input.remove(0);

                    if (input.isEmpty())
                    {
                        System.out.println("Ok");
                    }
                }
                else if (row.coagulation != null)
                {
                    Token coagulation = new Token(TokenType.String, row.coagulation.ch, token.line, token.pos);
                    input.add(0, coagulation);

                    for (String ch : row.transitions.keySet())
                    {
                        if (!isNontermChar(ch))
                        {
                            possibleChs.add(ch);
                        }
                    }

                    for (int i = 0; i < row.coagulation.count; i++)
                    {
                        statesStack.pop();
                        charsStack.pop();
                    }
                }
                else
                {
                    System.out.println("Ошибка в " + token.line + ":" + token.pos);
                    Set<String> keySet = row.transitions.keySet();
                    keySet.removeIf(Main::isNontermChar);
                    possibleChs.addAll(keySet);
                    System.out.println("Ожидалось: " + possibleChs);
                    System.out.println("Встретился: " + token.value);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static ArrayList<Row> readTable(String fileName) throws IOException
    {
        ArrayList<Row> table = new ArrayList<>();
        for (String line : Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)
                .collect(Collectors.toList()))
        {
            Scanner scanner = new Scanner(line);
            Row row = new Row();

            while (scanner.hasNext())
            {
                String cell = scanner.next();

                int index = cell.indexOf('_', 2);
                String ch = cell.substring(2, index);
                int index1 = cell.indexOf('_', index + 1);
                int i;
                String function = null;
                if (index1 == -1)
                {
                    i = Integer.parseInt(cell.substring(index + 1));
                }
                else
                {
                    i = Integer.parseInt(cell.substring(index + 1, index1));
                    function = cell.substring(index1 + 1);
                }

                if (cell.charAt(0) == 'S')
                {
                    row.transitions.put(ch, new Row.Transition(i, function));
                }
                else
                {
                    row.coagulation = new Coagulation(ch, i);
                }
            }

            table.add(row);
        }

        return table;
    }

    private static ArrayList<Token> readInput(String fileName) throws FileNotFoundException
    {
        ArrayList<Token> input = new ArrayList<>();

        Lexer lexer = new Lexer(fileName);
        Token token;
        do {
            token = lexer.getToken();
            input.add(token);
        } while (token.tokentype != TokenType.EndOfInput);

        return input;
    }

    private static boolean isNontermChar(String str)
    {
        return str.length() > 2 && str.charAt(0) == '<' && str.charAt(str.length() - 1) == '>';
    }
}
