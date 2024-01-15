package worker;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerInfo {
    private int id; //identifiant du worker
    private int cores; //Nombre de coeurs du worker
    private PrintWriter out; //flux de sortie pour communiquer avec le worker
    private AtomicInteger tasksInProgress; //Nombre de tâches en cours de traitement

    public WorkerInfo(int id, int cores, PrintWriter out) {
        this.id = id;
        this.cores = cores;
        this.out = out;
        this.tasksInProgress = new AtomicInteger(0);
    }

    public int getId() {
        return id;
    }

    public int getCores() {
        return cores;
    }

    public PrintWriter getOut() {
        return out;
    }

    public int getTasksInProgress() {
        return tasksInProgress.get();
    }

    public void incrementTasksInProgress() {
        tasksInProgress.incrementAndGet();
    }

    public void decrementTasksInProgress() {
        tasksInProgress.decrementAndGet();
    }
}
