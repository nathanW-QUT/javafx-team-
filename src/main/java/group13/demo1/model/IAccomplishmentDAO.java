package group13.demo1.model;

import java.util.List;

public interface IAccomplishmentDAO
{
    void addAccomplishment(Accomplishment accomplishment);
    void updateAccomplishment(Accomplishment accomplishment);
    void deleteAccomplishment(Accomplishment accomplishment);
    Accomplishment getAccomplishmentById(int id);
    List<Accomplishment> getAccomplishments();
    List<Accomplishment> getAccomplishmentsByUsername(String username);
}
