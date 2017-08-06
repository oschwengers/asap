
package bio.comp.jlu.asap


import java.nio.file.Path
import java.nio.file.Paths
import bio.comp.jlu.asap.api.RunningStates


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
abstract class Step extends Thread {

    protected final static String ASAP_HOME = System.getenv()['ASAP_HOME']
    protected final static String ASAP_DB   = System.getenv()['ASAP_DB']
    protected final static String GROOVY_PATH = "${ASAP_HOME}/bin/groovy"

    protected final String stepName
    protected final def config
    protected final Path projectPath
    protected final boolean localMode

    protected boolean success = false


    protected Step( String stepName, def config, boolean localMode ) {

        if( stepName == null  ||  stepName.isEmpty() )
            throw new IllegalArgumentException( 'stepName is null or empty!' )

        if( config == null )
            throw new NullPointerException( 'config is null!' )

        this.stepName  = stepName
        this.config    = config
        this.localMode = localMode

        projectPath = Paths.get( config.project.path )

    }




    /*
     * setStatus
     * Set the current running state of this step.
     */
    abstract public boolean isSelected()


    /*
     * setStatus
     * Set the current running state of this step.
     */
    abstract void setStatus( RunningStates status )


    /*
     * getStatus
     * Get the current running state of this step.
     */
    abstract public RunningStates getStatus()


    /*
     * Check
     * Returns true if all necessary requirements for this step
     * are fullfilled in order to start processing.
     */
    abstract protected boolean check()


    /*
     * Init
     * Perform any step init logic here as for example
     * creating necessary subdirectories in project reports dir
     */
    abstract protected void setup() throws Throwable


    /*
     * Run logic
     * Perform actual step logic here
     */
    abstract protected void runStep() throws Throwable


    /*
     * Cleanup
     * Remove any unneeded resources, files, etc...
     */
    abstract protected void clean() throws Throwable




    /*
     * start
     * Start business logic of this step.
     */
    @Override
    void start() {

        super.start()

    }


    /*
     * waitFor
     * Blocks further execution until this step has finished.
     */
    void waitFor() throws InterruptedException {

        super.join()

    }


    /*
     * succeeded
     * Returns true upon successfull execution of thi step.
     */
    boolean succeeded() {

        return success

    }


    /*
     * getStepName
     * Returns this step's simple name (no abbreviation char).
     */
    public String getStepName() {

        return stepName

    }

}

