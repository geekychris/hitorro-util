package ht.jsontypesystem.grouppredicates;

import ht.jsontypesystem.Group;

import java.util.function.Predicate;

public class GroupNameFilter implements Predicate<Group> {
    public static Predicate indexFilter = new GroupNameFilter("index");
    public static Predicate enrichFilter = new GroupNameFilter("enrich");
    private String name;

    public GroupNameFilter(String name) {
        this.name = name;
    }

    @Override
    public boolean test(final Group group) {
        return name.equals(group.getName());
    }
}
