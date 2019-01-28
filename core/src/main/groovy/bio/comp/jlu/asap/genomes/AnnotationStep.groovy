
package bio.comp.jlu.asap.genomes


import java.io.IOException
import java.nio.file.*
import groovy.io.FileType
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import bio.comp.jlu.asap.api.GenomeSteps

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.RunningStates.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class AnnotationStep extends GenomeStep {

    private static final String QSUB_SLOTS = '8'
    private static final String PROKKA = "${ASAP_HOME}/share/prokka" // Prokka (http://bioinformatics.net.au/prokka-manual.html)
    private static final String BARNAP = "${ASAP_HOME}/share/barrnap"
    private static final String PROTEINS = "${ASAP_HOME}/db/sequences/asap-proteins.faa"

    private static final GenomeSteps STEP_DEPENDENCY = GenomeSteps.SCAFFOLDING

    private Path    genomePath
    private Path    contigsPath


    AnnotationStep( def config, def genome, boolean localMode ) {

        super( GenomeSteps.ANNOTATION.getAbbreviation(), config, genome, localMode )

        setName( "Annotation-Step-Thread-${genome.id}" )

    }


    @Override
    boolean isSelected() {

        return genome?.stepselection.contains( GenomeSteps.ANNOTATION.getCharCode() )

    }


    @Override
    boolean check() {

        log.trace( "check: genome.id=${genome.id}" )
        if( genome?.stepselection.contains( STEP_DEPENDENCY.getCharCode() ) ) { // draft genome should get scaffolded

            // wait for scaffolding step
            long waitingTime = System.currentTimeMillis()
            while( shouldWait() ) {
                if( System.currentTimeMillis() - waitingTime > MAX_STEP_WAITING_PERIOD ) {
                    log.warn( "max waiting period (${MAX_STEP_WAITING_PERIOD} s) exceeded!" )
                    return false
                }
                try {
                    sleep( 1000 * 60 )
                    log.trace( "${GenomeSteps.ANNOTATION.getName()} step slept for 1 min" )
                }
                catch( Throwable t ) { log.error( 'Error: could not sleep!', t ) }
            }

            // check necessary scaffolding analysis status
            if( !hasStepFinished( STEP_DEPENDENCY ) )
                return false

        } // else: contigs are already ordered & scaffolded

        contigsPath = Paths.get( projectPath.toString(), PROJECT_PATH_SCAFFOLDS, genomeName, "${genomeName}-pseudo.fasta" )
        if( Files.isReadable( contigsPath ) ) {
            log.debug( "genome.id=${genome.id}: found user provided pseudo genome: path=${contigsPath}" )
            return true
        }

        contigsPath = Paths.get( projectPath.toString(), PROJECT_PATH_SCAFFOLDS, genomeName, "${genomeName}.fasta" )
        if( Files.isReadable( contigsPath ) ) {
            log.debug( "genome.id=${genome.id}: found user provided scaffolded contigs: path=${contigsPath}" )
            return true
        } else {
            log.warn( "genome.id=${genome.id}: contigs/pseudogenome not found! path=${contigsPath}" )
            return false
        }

    }


    private boolean shouldWait() {

        def status = genome.steps[ STEP_DEPENDENCY.getAbbreviation() ]?.status
        log.trace( "scaffolding step status=${status}" )
        return (status != FINISHED.toString()
            &&  status != SKIPPED.toString()
            &&  status != FAILED.toString())

    }


    @Override
    void setup() throws Throwable {

        log.trace( "setup genome-id=${genome.id}" )

        // build names, files, directories...
        genomePath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName )
        try {
            if( Files.exists( genomePath ) ) {
                genomePath.toFile().deleteDir()
                log.debug( "run ${genome.id}: existing dir '${genomePath}' deleted!" )
            }
            Files.createDirectory( genomePath )
            log.debug( "run ${genome.id}: dir '${genomePath}' created" )
        } catch( IOException ioe ) {
            log.error( "run ${genome.id}: dir '${genomeName}' could not be created!" )
            throw ioe
        }

    }


    @Override
    void runStep() throws Throwable {

        log.trace( "run genome-id=${genome.id}" )

        // build process
        setStatus( SUBMITTING )
        ProcessBuilder pb = new ProcessBuilder()
            .directory( projectPath.toFile() )
            .redirectErrorStream( true )


        def env = pb.environment() // set path variables
        String pathEnv = env.get( 'PATH' )
            pathEnv += ":${PROKKA}/bin"
            pathEnv += ":${BARNAP}/bin"
        env.put( 'PATH', pathEnv )


        if( localMode ) {
            pb.redirectOutput( genomePath.resolve( 'std.log' ).toFile() )
        } else {
            pb.command( 'qsub',
                '-b', 'y',
                '-sync', 'y',
                '-cwd',
                '-V', // export all env vars to cluster job
                '-N', 'asap-ann',
                '-pe', 'multislot', QSUB_SLOTS,
                '-o', genomePath.resolve( 'stdout.log' ).toString(),
                '-e', genomePath.resolve( 'stderr.log' ).toString() )
            pb.redirectOutput( genomePath.resolve( 'qsub.log' ).toFile() )
        }


        String locusTag
        if( genome.species.length() > 4 )
            locusTag = "${config.project.genus.substring( 0, 1 )}${genome.species.substring( 0, 4 )}_${genome.strain}".toString()
        else
            locusTag = "${config.project.genus.substring( 0, 1 )}${genome.species}_${genome.strain}".toString()


        List<String> cmd = pb.command()
        cmd << "${PROKKA}/bin/prokka".toString()
        cmd << '--genus'
            cmd << config.project.genus
        cmd << '--species'
            cmd << genome.species
        cmd << '--strain'
            cmd << genome.strain
        cmd << '--prefix'
            cmd << genomeName
        cmd << '--locustag' // set custom locus tag
            cmd << locusTag
        cmd << '--centre' // workaround to shrink down long SPAdes contig names until bug in Prokka gets fixed
            cmd << 'JLU'
        cmd << '--cpus' // set custom sequencing centre
            cmd << QSUB_SLOTS
        cmd << '--outdir'
            cmd << genomePath.toString()
        cmd << '--proteins'
            cmd << PROTEINS
        cmd << '--usegenus' // use genus specific prokka blast db
        cmd << '--force' // overwrite existing files
        cmd << '--addgenes' // add gene features to corresponding CDS, [t,r,lnc]RNA
        cmd << '--rfam' // annotate ribosomal RNAs
        cmd << '--rawproduct' // do not modify product tags as they seem to be too dodgy

        cmd << contigsPath.toString()


        // start and wait for process to exit
        log.debug( "genome.id=${genome.id}: exec: ${pb.command()}" )
        Process ps = pb.start()
        setStatus( RUNNING )
        int exitCode = ps.waitFor()


        // check exit code
        if( exitCode != 0 )
            throw new IllegalStateException( "abnormal Prokka exit code! exitCode=${exitCode}" )


        // read annotation statistics
        def info = [
            genome: [
                id: genome.id,
                genus: config.project.genus,
                species: genome.species,
                strain: genome.strain
            ]
        ]

        // parse Prokka stats
        String prokkaSummary = genomePath.resolve( "${genomeName}.txt" ).text
        def m = prokkaSummary =~ /bases: (\d+)/
        info.genomeSize = m ? m[0][1] as int : 0
        m = prokkaSummary =~ /gene: (\d+)/
        info.noGenes = m ? m[0][1] as int : 0
        m = prokkaSummary =~ /tRNA: (\d+)/
        info.noTRna = m ? m[0][1] as int : 0
        m = prokkaSummary =~ /CDS: (\d+)/
        info.noCds = m ? m[0][1] as int : 0
        m = prokkaSummary =~ /tmRNA: (\d+)/
        info.noTmRna = m ? m[0][1] as int : 0
        m = prokkaSummary =~ /rRNA: (\d+)/
        info.noRRna = m ? m[0][1] as int : 0
        m = prokkaSummary =~ /misc_RNA: (\d+)/
        info.noNcRna = m ? m[0][1] as int : 0
        m = prokkaSummary =~ /repeat_region: (\d+)/
        info.noCRISPR = m ? m[0][1] as int : 0

        // parse gff
        info.features = []
        boolean isSequencePart = false
        genomePath.resolve( "${genomeName}.gff" ).eachLine( { line ->
            if( !isSequencePart ) {
                char firstChar = line.charAt( 0 )
                if( firstChar == '>' ) {
                    isSequencePart = true
                } else if( firstChar != '#' ) {
                    def (contigName, inference, type, start, end, score, strand, phase, attributesCol) = line.split( '\t' )
                    def attributes = attributesCol.split( ';' ).collect( {
                        def cols = it.split( '=' )
                        return [ tag: cols[0], value: cols[1] ]
                    } )
                    if( type != 'gene' ) {
                        def feature = [
                            contig: contigName,
                            locusTag: attributes.find( { it.tag == 'locus_tag' } )?.value ?: '',
                            type: type,
                            start: Integer.parseInt( start ),
                            end: Integer.parseInt( end ),
                            strand: strand,
                            //phase: phase,
                            gene: attributes.find( { it.tag == 'gene' } )?.value ?: '',
                            product: attributes.find( { it.tag == 'product' } )?.value ?: '',
                            inference: attributes.find( { it.tag == 'inference' } )?.value ?: ''
                        ]
                        if( feature.type == 'CDS' ) {
                            feature.ec = attributes.find( { it.tag == 'eC_number' } )?.value ?: ''
                        }
                        if( feature.product == '' ) {
                            feature.product = attributes.find( { it.tag == 'rpt_family' } )?.value ?: ''
                        }
                        info.features << feature
                    }
                }
            }
        } )

        // find hypothetical genes
        info.noHypProt = info.features.findAll( { it.product.toLowerCase().contains( 'hypothetical' ) } ).size()
        info.noAnnotations = info.noGenes - info.noHypProt

        // write info.json file
        File infoJson = genomePath.resolve( 'info.json' ).toFile()
        infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )

    }


    @Override
    void clean() throws Throwable  {

        log.trace( "clean genome-id=${genome.id}" )
        genomePath.eachFile( groovy.io.FileType.FILES, {
            File file = it.toFile()
            if( file.name.endsWith( '.log' )  &&  file.length() == 0 ) {
                log.debug( "remove empty log file: ${file}" )
                try{
                    Files.delete( it )
                } catch( Exception ex ) {
                    log.warn( "could not delete file: ${file}", ex )
                }
            }
        } )

        [
            genomePath.resolve( "${genomeName}.err" ),
            genomePath.resolve( "${genomeName}.fna" ),
            genomePath.resolve( "${genomeName}.fsa" ),
            genomePath.resolve( "${genomeName}.log" ),
            genomePath.resolve( "${genomeName}.sqn" ),
            genomePath.resolve( "${genomeName}.tsv" ),
            genomePath.resolve( "${genomeName}.tbl" ),
            genomePath.resolve( "${genomeName}.txt" )
        ].each( {
                try{ Files.delete( it ) }
                catch( Exception ex ) { log.warn( "could not delete file: ${it}", ex ) }
        } )

    }

}

