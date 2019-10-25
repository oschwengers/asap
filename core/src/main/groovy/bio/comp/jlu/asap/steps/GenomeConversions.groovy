
package bio.comp.jlu.asap.steps


import java.nio.file.*
import java.time.*
import groovy.util.logging.Slf4j
import bio.comp.jlu.asap.api.FileFormat
import bio.comp.jlu.asap.api.FileType
import bio.comp.jlu.asap.api.RunningStates
import bio.comp.jlu.asap.Step
import bio.comp.jlu.asap.Misc

import static bio.comp.jlu.asap.ASAPConstants.*
import static bio.comp.jlu.asap.api.RunningStates.*
import static bio.comp.jlu.asap.api.Paths.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de>
 */
@Slf4j
class GenomeConversions extends Step {

    public static final String STEP_ABBR = 'genomeConversions'

    private Path sequencesPath

    enum Format{
        genbank,
        embl,
        fasta,
        unknown
    }


    GenomeConversions( def config ) {

        super( STEP_ABBR, config, true )

        config.steps[ stepName ] = [
            status: INIT.toString()
        ]

    }


    @Override
    boolean isSelected() {

        return true

    }


    @Override
    public void setStatus( RunningStates status ) {

        config.steps[ stepName ].status = status.toString()

    }


    @Override
    public RunningStates getStatus() {

        return RunningStates.getEnum( config.steps[ stepName ].status )

    }


    @Override
    void run() {

//        log.debug( 'run' )
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
                log.info( "finished ${stepName} step" )

            } else {
                log.warn( "skip ${stepName} step! check failed!" )
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

//        log.debug( 'check' )
        return true

    }


    @Override
    void setup() throws Throwable {

        log.debug( 'setup' )
        sequencesPath = projectPath.resolve( PROJECT_PATH_SEQUENCES )

    }


    @Override
    void runStep() throws Throwable {

        log.debug( 'run' )

        config.genomes.findAll( { FileType.getEnum( it.data[0].type ) == FileType.GENOME } ).each( { genome ->

            String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
            Path fastaPath    = sequencesPath.resolve( "${genomeName}.fasta" )

            if( genome.data[0].files.size() == 1 ) { // single file format (GenBank/GFF containing sequences

                switch( FileFormat.getEnum( genome.data[0].files[0] ) ) {

                    case FileFormat.GENBANK:
                        Path genbankPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.gbk" )
                        log.debug( "genbank: ${genbankPath}, fasta: ${fastaPath}" )
                        String script = /
from Bio import SeqIO
SeqIO.convert( "${genbankPath}", "${Format.genbank}", "${fastaPath}", "${Format.fasta}" )
/
                        try { // start gbk -> fasta conversion process
                            ProcessBuilder pb = new ProcessBuilder( '/usr/bin/env', 'python3',
                                '-c', script )
                                .redirectErrorStream( true )
                                .redirectOutput( ProcessBuilder.Redirect.INHERIT )
                                .directory( sequencesPath.toFile() )
                            log.debug( "exec: ${pb.command()}" )
                            log.debug( '----------------------------------------------------------------------------------------------' )
                            int exitCode = pb.start().waitFor()
                            if( exitCode != 0 )  throw new IllegalStateException( "exitCode = ${exitCode}" )
                            log.debug( '----------------------------------------------------------------------------------------------' )
                        } catch( Throwable t ) {
                            Misc.exit( log, 'genbank->fasta conversion failed!', t )
                        }
                        break

                    case FileFormat.EMBL:
                        Path emblPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.ebl" )
                        log.debug( "embl: ${emblPath}, fasta: ${fastaPath}" )
                        String script = /
from Bio import SeqIO
SeqIO.convert( "${emblPath}", "${Format.embl}", "${fastaPath}", "${Format.fasta}" )
/
                        try { // start ebl -> fasta conversion process
                            ProcessBuilder pb = new ProcessBuilder( '/usr/bin/env', 'python3',
                                '-c', script )
                                .redirectErrorStream( true )
                                .redirectOutput( ProcessBuilder.Redirect.INHERIT )
                                .directory( sequencesPath.toFile() )
                            log.debug( "exec: ${pb.command()}" )
                            log.debug( '----------------------------------------------------------------------------------------------' )
                            int exitCode = pb.start().waitFor()
                            if( exitCode != 0 )  throw new IllegalStateException( "exitCode = ${exitCode}" )
                            log.debug( '----------------------------------------------------------------------------------------------' )
                        } catch( Throwable t ) {
                            Misc.exit( log, 'embl->fasta conversion failed!', t )
                        }

                        // convert embl to Genbank for core/pan genome calculation
                        Path genbankPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.gbk" )
                        log.debug( "embl: ${emblPath}, fasta: ${genbankPath}" )
                        script = /
from Bio import SeqIO
SeqIO.convert( "${emblPath}", "${Format.embl}", "${genbankPath}", "${Format.genbank}" )
/
                        try { // start ebl -> genbank conversion process
                            ProcessBuilder pb = new ProcessBuilder( '/usr/bin/env', 'python3',
                                '-c', script )
                                .redirectErrorStream( true )
                                .redirectOutput( ProcessBuilder.Redirect.INHERIT )
                                .directory( sequencesPath.toFile() )
                            log.debug( "exec: ${pb.command()}" )
                            log.debug( '----------------------------------------------------------------------------------------------' )
                            int exitCode = pb.start().waitFor()
                            if( exitCode != 0 )  throw new IllegalStateException( "exitCode = ${exitCode}" )
                            log.debug( '----------------------------------------------------------------------------------------------' )
                        } catch( Throwable t ) {
                            Misc.exit( log, 'embl->genbank conversion failed!', t )
                        }
                        break

                    case FileFormat.GFF:
                        Path gffPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.gff" )
                        log.debug( "gff=${gffPath}, fasta=${fastaPath}" )
                        StringBuilder sb = new StringBuilder( 10000000 )
                        String fastaHeader = null
                        boolean isSequence = false
                        gffPath.eachLine( { line ->
                            if( isSequence ) {
                                sb.append( line ).append( '\n' )
                            } else if( line.charAt(0) == '>' ) {
                                fastaHeader = line
                                sb.append( line ).append( '\n' )
                                isSequence = true
                            }
                        } )
                        fastaPath.text = sb.toString()
                        log.debug( "extracted GFF3 sequence: fasta-header=${fastaHeader}" )
                        // TODO convert GFF file to GenBank format
                        break
                }

            } else {// two file format (GFF + Fasta)
                Path sourcePath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.fasta" )
                Files.createLink( fastaPath, sourcePath )
                log.debug( "gff-fasta: ${sourcePath}, sequence-fasta: ${fastaPath}" )
                // TODO convert GFF file to GenBank format
                // combine GFF3 annotation and Fasta sequence file (Roary expectation)
                Path gffPath = Paths.get( projectPath.toString(), PROJECT_PATH_ANNOTATIONS, genomeName, "${genomeName}.gff" )
                String sequence = fastaPath.text
                String gffAnnotation = gffPath.text.split( '\n' ).findAll( {!it.isEmpty()} ).join( '\n' ) // remove empty lines at the end
                gffPath.text = "${gffAnnotation}\n##FASTA\n${sequence}"
            }

        } )

    }


    @Override
    void clean() throws Throwable {

//        log.debug( 'clean' )

    }

}

