/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author uesr
 */
public class Information {
    public ConnectionUtillities connection;
    public String username;
    public int state;
    public Information(ConnectionUtillities con,String User){
        username=User;
        connection=con;
        state=0;
    }
    public Information()
    {
        connection=null;
        username=" ";
        state=0;
    }
}
