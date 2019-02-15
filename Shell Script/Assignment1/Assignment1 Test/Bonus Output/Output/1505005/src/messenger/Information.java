/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import util.ConnectionUtillities;

/**
 *
 * @author uesr
 */
public class Information {
    public ConnectionUtillities connection;
    public String username;
    
    public Information(ConnectionUtillities con,String User){
        username=User;
        connection=con;
    }
}
