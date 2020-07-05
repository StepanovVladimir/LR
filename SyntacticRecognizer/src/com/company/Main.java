package com.company;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
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
            statesStack.push(0);

            while (!input.isEmpty())
            {
                if (input.get(0).value.equals("main"))
                {
                    input.get(0).tokentype = TokenType.KeywordIf;
                }
                Row row = table.get(statesStack.peek());
                Token token = input.get(0);
                if (row.transitions.containsKey(token.value))
                {
                    statesStack.push(row.transitions.get(token.value));
                    input.remove(0);
                    if (input.isEmpty())
                    {
                        System.out.println("Ok");
                    }
                }
                else if (row.transitions.containsKey("[" + token.tokentype.toString() + "]"))
                {
                    statesStack.push(row.transitions.get("[" + token.tokentype.toString() + "]"));
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
                    for (int i = 0; i < row.coagulation.count; i++)
                    {
                        statesStack.pop();
                    }
                }
                else
                {
                    System.out.println("Ошибка в " + token.line + ":" + token.pos);
                    Set<String> keySet = row.transitions.keySet();
                    keySet.removeIf(Main::isNontermChar);
                    System.out.println("Ожидалось: " + keySet);
                    System.out.println("Встретился: " + token.value);
                    break;
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static ArrayList<Row> readTable(String fileName) throws IOException
    {
        var table = new ArrayList<Row>();
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
                int i = Integer.parseInt(cell.substring(index + 1));

                if (cell.charAt(0) == 'S')
                {
                    row.transitions.put(ch, i);
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
