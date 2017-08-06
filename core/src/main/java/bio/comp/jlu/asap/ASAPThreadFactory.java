
package bio.comp.jlu.asap;


import java.util.concurrent.ThreadFactory;



/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public class ASAPThreadFactory implements ThreadFactory {

    private int no = 0;

    @Override
    public Thread newThread( Runnable r ) {

        no++;

        Thread t = new Thread( r );
        t.setName( "ASAP-RUN-THREAD-" + no );
        t.setDaemon( true );

        return t;

    }

}
