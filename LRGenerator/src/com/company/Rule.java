package com.company;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Rule
{
    public String left;
    public ArrayList<String> right = new ArrayList<>();
    public Set<RelationFirst> guideSet = new HashSet<>();
}
