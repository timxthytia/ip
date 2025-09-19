package tim.task;

import java.util.ArrayList;

/**
 * Represents a list of tasks managed by the application.
 * Provides methods to add, remove, access, and query the size of the task list.
 * This class acts as a wrapper around an {@link java.util.ArrayList} of
 * {@link tim.task.Task}.
 *
 */
public class TaskList {
    private final ArrayList<Task> tasks;
    /**
     * Creates a new, empty TaskList.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a new TaskList from an existing ArrayList of tasks.
     * If the provided list is null, an empty list will be created.
     *
     * @param tasks the existing list of tasks to initialise with, or null for empty.
     */
    public TaskList(ArrayList<Task> tasks) { //Overloaded constructor
        assert tasks == null || tasks.stream().allMatch(t -> t != null)
                : "Task list must not contain null elements";
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return the size of the task list.
     */
    public int size() {
        int result = tasks.size();
        assert result >= 0 : "Task list size must not be negative";
        return result;
    }

    /**
     * Retrieves the task at the specified index.
     *
     * @param idx the index of the task to retrieve (0-based).
     * @return the Task at the given index.
     */
    public Task get(int idx) {
        assert idx >= 0 && idx < tasks.size() : "Index out of bounds";
        return tasks.get(idx);
    }

    /**
     * Adds a task to the list.
     *
     * @param t the task to add.
     */
    public void add(Task t) {
        assert t != null : "Task to add must not be null";
        this.tasks.add(t);
    }

    /**
     * Removes the task at the specified index from the list.
     *
     * @param idx the index of the task to remove.
     * @return the removed Task.
     */
    public Task remove(int idx) {
        assert idx >= 0 && idx < tasks.size() : "Index out of bounds";
        return tasks.remove(idx);
    }

    /**
     * Returns the underlying ArrayList of tasks.
     *
     * @return the list of tasks as an ArrayList.
     */
    public ArrayList<Task> asList() {
        return tasks;
    }

    /**
     * Returns a string representation of the TaskList.
     *
     * @return a string representation of the tasks in the list.
     */
    @Override
    public String toString() {
        return tasks.toString();
    }
}
