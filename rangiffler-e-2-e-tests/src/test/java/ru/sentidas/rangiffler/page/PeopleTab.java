package ru.sentidas.rangiffler.page;

public enum PeopleTab {
    ALL("All people"),
    OUTCOME("Outcome invitations"),
    INCOME("Income invitations"),
    FRIENDS("Friends");
    public final String label;

    PeopleTab(String label) {
        this.label = label;
    }

}
