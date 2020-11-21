package com.company;

import java.util.Map;
import java.util.TreeMap;

public class Row
{
    public static class Transition
    {
        public Transition(int state, String function)
        {
            this.state = state;
            this.function = function;
        }

        public int state;
        public String function;
    }

    Map<String, Transition> transitions = new TreeMap<>();
    Coagulation coagulation;
}
