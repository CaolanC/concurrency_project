package whorchestrator;

public class Section {
    public final String name;
    private int starting_capacity;

    Section(String name, int starting_capacity) {
        this.name = name;
        this.starting_capacity = starting_capacity;
        System.out.println(String.format("Section: %s created.", this.name));
    }
}