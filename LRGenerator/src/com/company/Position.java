package com.company;

import java.util.Objects;

public class Position
{
    public int rule;
    public int pos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return rule == position.rule &&
                pos == position.pos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rule, pos);
    }
}
