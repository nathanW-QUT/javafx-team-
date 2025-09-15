package group13.demo1.model;

public class Accomplishment
{
    private int id;
    private String username;
    private String label;
    private boolean completed;

    public Accomplishment(int id, String username, String label, boolean completed)
    {
        this.username = username;
        this.id = id;
        this.label = label;
        this.completed = completed;
    }

    public int getId() {return id;}
    public String getUsername() {return username;}
    public String getLabel() {return label;}
    public boolean isCompleted() {return completed;}

    public void setId(int id) {this.id = id;}
    public void setUsername(String username) {this.username = username;}
    public void setLabel(String label) {this.label = label;}
    public void setCompleted(boolean completed) {this.completed = completed;}

    @Override
    public String toString()
    {
        return "Accomplishment{" +
                "id = " + id +
                ", username = " + username +
                ", label = " + label +
                ", completed = " + completed +
                '}';
    }
}
