
package bio.comp.jlu.asap.api;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum RunningStates {

    INIT( "init" ),
    SETUP( "setup" ),
    WAITING( "waiting" ),
    RUNNING( "running" ),
    SUBMITTING( "submitting" ),
    FINISHED( "finished" ),
    SKIPPED( "skipped" ),
    FAILED( "failed" );


    private final String state;

    RunningStates( String state ) {

        this.state = state;

    }


    @Override
    public String toString() {

        return state;

    }


    public static RunningStates getEnum( String str ) {

        str = str.toLowerCase();
        for( RunningStates rs : RunningStates.values() ) {
            if( rs.toString().equals( str ) )
                return rs;
        }

        return null;

    }

}
