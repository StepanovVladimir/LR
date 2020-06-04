package com.company;

import java.io.FileWriter;
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
            var grammar = readGrammar(args[0]);
            createGuideSets(grammar);

            Map<Set<Position>, Map<String, Transition>> table = new HashMap<>();

            Map<String, Transition> transitions = new HashMap<>();
            table.put(new HashSet<>(), transitions);

            for (RelationFirst rf : grammar.get(0).guideSet)
            {
                if (rf.ch.equals("@"))
                {
                    Transition transition = new Transition();
                    transition.isTransition = false;
                    transition.ch = grammar.get(rf.ruleNumber).left;
                    transition.count = 0;
                    transitions.put("", transition);
                }
                else
                {
                    Position position = new Position();
                    position.rule = rf.ruleNumber;
                    position.pos = 0;

                    if (transitions.containsKey(rf.ch))
                    {
                        transitions.get(rf.ch).positions.add(position);
                    }
                    else
                    {
                        Transition transition = new Transition();
                        transition.isTransition = true;
                        transition.positions.add(position);
                        transitions.put(rf.ch, transition);
                    }
                }
            }
            createTable(grammar, table, transitions);

            printTable(table, args[1]);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static ArrayList<Rule> readGrammar(String fileName) throws IOException
    {
        var grammar = new ArrayList<Rule>();
        for (String line : Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)
                .collect(Collectors.toList()))
        {
            Scanner scanner = new Scanner(line);
            Rule rule = new Rule();
            rule.left = scanner.next();

            scanner.next();
            while (scanner.hasNext())
            {
                rule.right.add(scanner.next());
            }

            if (grammar.isEmpty())
            {
                rule.right.add("#");
            }

            grammar.add(rule);
        }

        return grammar;
    }

    private static void createGuideSets(ArrayList<Rule> grammar)
    {
        var relationFirst = createRelationFirst(grammar);

        for (Rule rule : grammar)
        {
            Set<RelationFirst> chars = new HashSet<>();
            for (RelationFirst rf : rule.guideSet)
            {
                if (isNontermChar(rf.ch))
                {
                    chars.addAll(relationFirst.get(rf.ch));
                }
            }
            rule.guideSet.addAll(chars);
        }
    }

    private static void createTable(ArrayList<Rule> grammar, Map<Set<Position>, Map<String, Transition>> table, Map<String, Transition> transitions)
    {
        for (Map.Entry<String, Transition> transition : transitions.entrySet())
        {
            if (transition.getValue().isTransition && !table.containsKey(transition.getValue().positions))
            {
                Map<String, Transition> transitions1 = new HashMap<>();
                table.put(transition.getValue().positions, transitions1);
                for (Position position : transition.getValue().positions)
                {
                    Rule rule = grammar.get(position.rule);
                    if (rule.right.size() > position.pos + 1)
                    {
                        Position position1 = new Position();
                        position1.rule = position.rule;
                        position1.pos = position.pos + 1;

                        if (transitions1.containsKey(rule.right.get(position1.pos)))
                        {
                            transitions1.get(rule.right.get(position1.pos)).positions.add(position1);
                        }
                        else
                        {
                            Transition transition1 = new Transition();
                            transition1.isTransition = true;
                            transition1.positions.add(position1);
                            transitions1.put(rule.right.get(position1.pos), transition1);
                        }

                        if (isNontermChar(rule.right.get(position1.pos)))
                        {
                            for (int i = 1; i < grammar.size(); i++)
                            {
                                if (grammar.get(i).left.equals(rule.right.get(position1.pos)))
                                {
                                    for (RelationFirst rf : grammar.get(i).guideSet)
                                    {
                                        if (rf.ch.equals("@"))
                                        {
                                            Transition transition1 = new Transition();
                                            transition1.isTransition = false;
                                            transition1.ch = grammar.get(rf.ruleNumber).left;
                                            transition1.count = 0;
                                            transitions1.put("", transition1);
                                        }
                                        else
                                        {
                                            Position position2 = new Position();
                                            position2.rule = rf.ruleNumber;
                                            position2.pos = 0;

                                            if (transitions1.containsKey(rf.ch))
                                            {
                                                transitions1.get(rf.ch).positions.add(position2);
                                            }
                                            else
                                            {
                                                Transition transition1 = new Transition();
                                                transition1.isTransition = true;
                                                transition1.positions.add(position2);
                                                transitions1.put(rf.ch, transition1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        Transition transition1 = new Transition();
                        transition1.isTransition = false;
                        transition1.ch = rule.left;
                        transition1.count = rule.right.size();
                        transitions1.put("", transition1);
                    }
                }

                createTable(grammar, table, transitions1);
            }
        }
    }

    private static void printTable(Map<Set<Position>, Map<String, Transition>> table, String fileName) throws IOException
    {
        Map<Set<Position>, Integer> indexes = new HashMap<>();
        int i = 1;
        for (Map.Entry<Set<Position>, Map<String, Transition>> transitions : table.entrySet())
        {
            if (!transitions.getKey().isEmpty())
            {
                indexes.put(transitions.getKey(), i);
                i++;
            }
        }

        try (FileWriter writer = new FileWriter(fileName))
        {
            for (Map.Entry<String, Transition> transition : table.get(new HashSet<Position>()).entrySet())
            {
                if (transition.getValue().isTransition)
                {
                    writer.write("S");
                    writer.write("_");
                    writer.write(transition.getKey());
                    writer.write("_");
                    writer.write(indexes.get(transition.getValue().positions).toString());
                    writer.write(" ");
                }
                else
                {
                    writer.write("R");
                    writer.write("_");
                    writer.write(transition.getValue().ch);
                    writer.write("_");
                    writer.write(Integer.toString(transition.getValue().count));
                    writer.write(" ");
                }
            }
            writer.write("\n");
            for (Map.Entry<Set<Position>, Map<String, Transition>> transitions : table.entrySet())
            {
                if (!transitions.getKey().isEmpty())
                {
                    for (Map.Entry<String, Transition> transition : transitions.getValue().entrySet())
                    {
                        if (transition.getValue().isTransition)
                        {
                            writer.write("S");
                            writer.write("_");
                            writer.write(transition.getKey());
                            writer.write("_");
                            writer.write(indexes.get(transition.getValue().positions).toString());
                            writer.write(" ");
                        }
                        else
                        {
                            writer.write("R");
                            writer.write("_");
                            writer.write(transition.getValue().ch);
                            writer.write("_");
                            writer.write(Integer.toString(transition.getValue().count));
                            writer.write(" ");
                        }
                    }
                    writer.write("\n");
                }
            }
        }
    }

    private static Map<String, Set<RelationFirst>> createRelationFirst(ArrayList<Rule> grammar)
    {
        Map<String, Set<RelationFirst>> relationFirst = new TreeMap<>();
        for (int i = 0; i < grammar.size(); i++)
        {
            Rule rule = grammar.get(i);
            if (!relationFirst.containsKey(rule.left))
            {
                relationFirst.put(rule.left, new HashSet<>());
            }

            String firstChar = rule.right.get(0);
            //if (!firstChar.equals("@"))
            //{
                RelationFirst rf = new RelationFirst();
                rf.ch = firstChar;
                rf.ruleNumber = i;
                rule.guideSet.add(rf);
                relationFirst.get(rule.left).add(rf);
            //}
            /*else
            {
                Set<String> researchedNontermChars = new TreeSet<>();
                researchedNontermChars.add(rule.left);
                rule.guideSet.addAll(findNext(rule.left, grammar, researchedNontermChars, rule.left, i));
                relationFirst.get(rule.left).addAll(rule.guideSet);
            }*/
        }

        for (Map.Entry<String, Set<RelationFirst>> guideSet : relationFirst.entrySet())
        {
            Set<RelationFirst> chars = new HashSet<>();
            for (RelationFirst rf : guideSet.getValue())
            {
                if (isNontermChar(rf.ch))
                {
                    chars.addAll(getChars(rf.ch, relationFirst));
                }
            }
            guideSet.getValue().addAll(chars);
        }

        return relationFirst;
    }

    /*private static Set<RelationFirst> findNext(String nontermChar, ArrayList<Rule> grammar, Set<String> researchedNontermChars, String initialNonterm, int ruleNumber)
    {
        Set<RelationFirst> guideSet = new HashSet<>();
        for (Rule rule : grammar)
        {
            for (int i = 0; i < rule.right.size(); ++i)
            {
                if (rule.right.get(i).equals(nontermChar))
                {
                    if (rule.right.size() > i + 1)
                    {
                        if (!rule.right.get(i + 1).equals(initialNonterm))
                        {
                            RelationFirst rf = new RelationFirst();
                            rf.ch = rule.right.get(i + 1);
                            rf.ruleNumber = ruleNumber;
                            guideSet.add(rf);
                        }
                    }
                    else if (!researchedNontermChars.contains(rule.left))
                    {
                        researchedNontermChars.add(rule.left);
                        guideSet.addAll(findNext(rule.left, grammar, researchedNontermChars, initialNonterm, ruleNumber));
                        researchedNontermChars.remove(rule.left);
                    }
                }
            }
        }

        return guideSet;
    }*/

    private static Set<RelationFirst> getChars(String nontermChar, Map<String, Set<RelationFirst>> relationFirst)
    {
        Set<RelationFirst> termChars = new HashSet<>();
        for (RelationFirst rf : relationFirst.get(nontermChar))
        {
            termChars.add(rf);
            if (isNontermChar(rf.ch) && !rf.ch.equals(nontermChar))
            {
                termChars.addAll(getChars(rf.ch, relationFirst));
            }
        }

        return termChars;
    }

    private static boolean isNontermChar(String str)
    {
        return str.length() > 2 && str.charAt(0) == '<' && str.charAt(str.length() - 1) == '>';
    }
}
