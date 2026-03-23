package whorchestrator;

public class Section {
    public final String name;

    Section(String name) {
        this.name = name;
        System.out.println(String.format("Section: %s created.", this.name));
    }
}