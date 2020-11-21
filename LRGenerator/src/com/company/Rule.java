package com.company;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Rule
{
    public static class Char
    {
        public Char(String ch)
        {
            this.ch = ch;
        }

        public String ch;
        public String function;
    }

    public String left;
    public ArrayList<Char> right = new ArrayList<>();
    public Set<RelationFirst> guideSet = new HashSet<>();
}
