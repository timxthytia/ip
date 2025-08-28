package tim.task;

import java.util.ArrayList;

public class TaskList {
    private final ArrayList<Task> tasks;
    public TaskList() { this.tasks = new ArrayList<>(); }
    public TaskList(ArrayList<Task> tasks) { //Overloaded constructor
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    public int size() {
        return tasks.size();
    }
    public Task get(int idx) {
        return tasks.get(idx);
    }
    public void add(Task t) { this.tasks.add(t); }
    public Task remove(int idx) { return tasks.remove(idx); }
    public ArrayList<Task> asList() { return tasks; }
    @Override
    public String toString() {
        return tasks.toString();
    }
}
