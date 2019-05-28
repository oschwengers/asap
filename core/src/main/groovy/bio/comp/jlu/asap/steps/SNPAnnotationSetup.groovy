
package bio.comp.jlu.asap.steps


import java.nio.file.*
import java.time.*
import groovy.util.logging.Slf4j
import bio.comp.jlu.asap.api.FileFormat
import bio.comp.jlu.asap.api.RunningStates
import bio.comp.jlu.asap.Step

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.RunningStates.*
import static bio.comp.jlu.asap.api.Paths.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class SNPAnnotationSetup extends Step {

    public static final String STEP_ABBR = 'snpAnnotationSetup'


    private static String SNP_EFF = "${ASAP_HOME}/share/snpeff/snpEff.jar"

    private final Path snpDetectionPath


    SNPAnnotationSetup( def config ) {

        super( STEP_ABBR, config, true )

        config.steps[ stepName ] = [
            status: INIT.toString()
        ]

        // build necessary paths
        snpDetectionPath = projectPath.resolve( PROJECT_PATH_SNPS )

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    public void setStatus( RunningStates status ) {

        config.steps[ stepName ].status = status.toString()

    }


    public RunningStates getStatus() {

        return RunningStates.getEnum( config.steps[ stepName ].status )

    }


    @Override
    void run() {

        log.trace( "${stepName} running..." )
        config.steps[ stepName ].start = OffsetDateTime.now().toString()


        try {

            if( check() ) {

                setStatus( SETUP )
                setup()

                setStatus( RUNNING )
                runStep()

                clean()

                setStatus( FINISHED )
                success = true

            } else {
                log.warn( "skip ${stepName} step upon failed check!" )
                success = false
                setStatus( SKIPPED )
            }

        } catch( Throwable ex ) {
            log.error( "${stepName} step aborted!", ex )
            success = false
            setStatus( FAILED )
            config.steps[ stepName ].error = ex.getLocalizedMessage()
        }

        config.steps[ stepName ].end = OffsetDateTime.now().toString()

    }




    @Override
    boolean check() {

        log.trace( 'check' )

        // if reference genome is provided as fasta we have no annotation to setup
        if( FileFormat.FASTA == FileFormat.getEnum( config.references[0] ) )
            return false

        // wait for mapping step
        long waitingTime = System.currentTimeMillis()
        while( shouldWait() ) {
            if( System.currentTimeMillis() - waitingTime > MAX_STEP_WAITING_PERIOD ) {
                log.warn( "max waiting period (${MAX_STEP_WAITING_PERIOD} s) exceeded!" )
                return false
            }
            try {
                sleep( 1000 * 10 )
                log.trace( "${stepName} step slept for 1 min" )
            }
            catch( Throwable t ) { log.error( 'Error: could not sleep!', t ) }
        }

        // check necessary reference processing status
        return config.steps[ ReferenceProcessings.STEP_ABBR ]?.status == FINISHED.toString()

    }


    private boolean shouldWait() {

        def status = config?.steps[ ReferenceProcessings.STEP_ABBR ]?.status
        log.trace( "reference processing step status=${status}" )
        return (status != FINISHED.toString()
            &&  status != SKIPPED.toString()
            &&  status != FAILED.toString())

    }


    @Override
    void setup() throws Throwable {

        log.trace( 'setup' )

    }


    @Override
    void runStep() throws Throwable {

        log.trace( 'run' )

        // setup snpEff reference dirs / files
        String ref = config.references[0]
        String fileName = ref.substring( 0, ref.lastIndexOf( '.' ) )
        Path refGenbankPath = Paths.get( projectPath.toString(), PROJECT_PATH_REFERENCES, "${fileName}.gbk" )
        Path snpEffRefPath = snpDetectionPath.resolve( 'ref' )
        log.debug( "create snpEff ref dir: ${snpEffRefPath}" )
        Files.createDirectory( snpEffRefPath )
        log.debug( "create hard link reference genome->snpEff ref: ${snpEffRefPath.resolve( 'genes.gbk' )} -> ${refGenbankPath}" )
        Files.createLink( snpEffRefPath.resolve( 'genes.gbk' ), refGenbankPath )

        // build snpEff configuration
        def lines = refGenbankPath.readLines()
        String definition = lines.find( { it ==~ /^DEFINITION.+/ } ).split('DEFINITION')[1].trim()
        if( definition.contains(',')) {
            definition = definition.split(',')[0]
        }

        // check for versioned LOCUS identifier as in fasta headers
        def locusLines = lines.findAll( { it ==~ /^LOCUS.+/ } )
        def contigNames = locusLines.collect( { it.split( '\\s+' )[1] } )
        def configTemplate = """data.dir = .
codon.Bacterial_and_Plant_Plastid			: TTT/F, TTC/F, TTA/L, TTG/L+, TCT/S, TCC/S, TCA/S, TCG/S, TAT/Y, TAC/Y, TAA/*, TAG/*, TGT/C, TGC/C, TGA/*, TGG/W, CTT/L, CTC/L, CTA/L, CTG/L+, CCT/P, CCC/P, CCA/P, CCG/P, CAT/H, CAC/H, CAA/Q, CAG/Q, CGT/R, CGC/R, CGA/R, CGG/R, ATT/I+, ATC/I+, ATA/I+, ATG/M+, ACT/T, ACC/T, ACA/T, ACG/T, AAT/N, AAC/N, AAA/K, AAG/K, AGT/S, AGC/S, AGA/R, AGG/R, GTT/V, GTC/V, GTA/V, GTG/V+, GCT/A, GCC/A, GCA/A, GCG/A, GAT/D, GAC/D, GAA/E, GAG/E, GGT/G, GGC/G, GGA/G, GGG/G
        ref.genome : ${definition}
	ref.chromosomes : ${contigNames.join( ', ' )}
"""
        contigNames.each { configTemplate += "\tref.${it}: Bacterial_and_Plant_Plastid\n" }
        snpDetectionPath.resolve( 'snpEff.config' ).toFile().text = configTemplate

        // build snpEff ref database
        ProcessBuilder pb = new ProcessBuilder( 'java', '-jar',
            SNP_EFF,
            'build',
            '-c', snpDetectionPath.resolve( 'snpEff.config' ).toString(),
            '-genbank',
            '-v',
            'ref' )
            .directory( snpDetectionPath.toFile() )
            .redirectOutput( snpDetectionPath.resolve( 'stdout.ps.pe.log' ).toFile() )
            .redirectError( snpDetectionPath.resolve( 'stderr.ps.pe.log' ).toFile() )
        log.info( "build snpEff database: ${snpDetectionPath.resolve( 'snpEff.config' )}" )

        // wait for jobs
        int exitCode = pb.start().waitFor()
        if( exitCode != 0 )
            throw new IllegalStateException( "abnormal snpEff build exit code! exitCode=${exitCode}" )

    }


    @Override
    void clean() throws Throwable {

        log.trace( 'clean' )

    }

}

