package ru.sentidas.rangiffler.page;

public enum PeopleTab {
    ALL("All people", "simple-tabpanel-all"),
    OUTCOME("Outcome invitations","simple-tabpanel-outcome"),
    INCOME("Income invitations", "simple-tabpanel-income"),
    FRIENDS("Friends", "simple-tabpanel-friends");
    public final String label;
    public final String tableId;

    PeopleTab(String label, String tableId) {
        this.label = label;
        this.tableId = tableId;
    }

}
