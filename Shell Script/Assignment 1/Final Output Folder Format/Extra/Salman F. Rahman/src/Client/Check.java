package Client;

/**
 * Created by Nahiyan on 01/10/2017.
 */
public class Check{
    private boolean done = true ;

    public Check()
    {
        done = true ;
    }

    public synchronized boolean get()
    {
        return done;
    }

    public synchronized void set()
    {
        done=true;
    }

    public synchronized void reset()
    {
        done=false;
    }
}
