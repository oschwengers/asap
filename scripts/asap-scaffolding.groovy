// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import groovy.io.FileType
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.CliBuilder
import org.slf4j.LoggerFactory

import static bio.comp.jlu.asap.api.MiscConstants.*
import static bio.comp.jlu.asap.api.Paths.*
import static bio.comp.jlu.asap.api.GenomeSteps.*




/************************
 *** Script Constants ***
************************/
final def env = System.getenv()
ASAP_HOME = env.ASAP_HOME
ASAP_DB   = env.ASAP_DB

MEDUSA = "${ASAP_HOME}/share/medusa"
MUMMER = "${ASAP_HOME}/share/mummer"

LINKER      = 'NNNNNNNNNNCTAGCTAGCTAGCNNNNNNNNNN'
LINE_LENGTH = 70


/*********************
 *** Script Params ***
*********************/


log = LoggerFactory.getLogger( getClass().getName() )

def cli = new CliBuilder( usage: 'asap-scaffolding.groovy --project-path <project-path> --genome-id <genome-id>' )
    cli.p( longOpt: 'project-path', args: 1, argName: 'project-path', required: true, 'Path to project directory' )
    cli.g( longOpt: 'genome-id', args: 1, argName: 'genome-id', required: false, 'Genome ID in config file' )
def opts = cli.parse( args )

if( !opts?.p ) {
    log.error( 'no project path provided!' )
    System.exit( 1 )
}

def genomeId
if( opts?.g  &&  opts.g ==~ /\d+/ )
    genomeId = Integer.parseInt( opts.g )
else if( env.SGE_TASK_ID  &&  env.SGE_TASK_ID ==~ /\d+/ )
    genomeId = Integer.parseInt( env.SGE_TASK_ID )
else {
    log.error( 'no genome id provided!' )
    System.exit( 1 )
}

// log system environment vars and Java properties
log.info( "SCRIPT: ${getClass().protectionDomain.codeSource.location.path}" )
log.info( "USER: ${env.USER}" )
log.info( "CWD: ${env.PWD}" )
log.info( "HOSTNAME: ${env.HOSTNAME}" )
log.info( "ASAP_HOME: ${env.ASAP_HOME}" )
log.info( "ASAP_DB: ${env.ASAP_DB}" )
log.info( "PATH: ${env.PATH}" )
def props = System.getProperties()
log.info( "script.name: ${props['script.name']}" )
log.info( "groovy.home: ${props['groovy.home']}" )
log.info( "file.encoding: ${props['file.encoding']}" )
log.info( "genome-id: ${genomeId}" )




/********************
 *** Script Paths ***
********************/


Path rawProjectPath = Paths.get( opts.p )
if( !Files.exists( rawProjectPath ) ) {
    println( "Error: project directory (${rawProjectPath}) does not exist!" )
    System.exit(1)
}
final Path projectPath = rawProjectPath.toRealPath()
log.info( "project-path: ${projectPath}")


// read config json
Path configPath = projectPath.resolve( 'config.json' )
if( !Files.isReadable( configPath ) ) {
    log.error( 'config.json not readable!' )
    System.exit( 1 )
}
final def config = (new JsonSlurper()).parseText( projectPath.resolve( 'config.json' ).toFile().text )


final def genome = config.genomes.find( { it.id == genomeId } )
if( !genome ) {
    log.error( "no genome found in config! genome-id=${genomeId}" )
    System.exit( 1 )
}
final String genomeName = "${config.project.genus}_${genome.species}_${genome.strain}"
log.info( "genome-name: ${genomeName}")


final Path genomeAssemblyPath = Paths.get( projectPath.toString(), PROJECT_PATH_ASSEMBLIES, genomeName, "${genomeName}.fasta" )
final Path genomeScaffoldsDirPath = Paths.get( projectPath.toString(), PROJECT_PATH_SCAFFOLDS, genomeName )
try { // create tmp dir
    if( !Files.exists( genomeScaffoldsDirPath ) ) {
        Files.createDirectory( genomeScaffoldsDirPath )
        log.info( "create genome-ordered-alignments folder: ${genomeScaffoldsDirPath}" )
    }
} catch( Throwable t ) {
    log.error( "could create genome ordered alignments dir! gid=${genomeId}, ordered-alignments-dir=${genomeScaffoldsDirPath}" )
    System.exit( 1 )
}
Files.createFile( genomeScaffoldsDirPath.resolve( 'state.running' ) ) // create state.running


// create local tmp directory
final Path tmpPath = Paths.get( '/', 'var', 'scratch', "tmp-${System.currentTimeMillis()}-${Math.round(Math.random()*1000)}" )
try { // create tmp dir
    log.info( "tmp-folder: ${tmpPath}" )
    Files.createDirectory( tmpPath )
} catch( Throwable t ) {
    terminate( "could create tmp dir! gid=${genomeId}, tmp-dir=${tmpPath}", t, genomeScaffoldsDirPath, tmpPath )
}


// create info object
def info = [
    time: [
        start: (new Date()).format( DATE_FORMAT )
    ],
    genome: [
        id: genome.id,
        species: genome.species,
        strain: genome.strain
    ],
    path: genomeScaffoldsDirPath.toString(),
    scaffolds: [:]
]




/********************
 *** Script Logic ***
********************/


log.info( 'copy references:' )
def references = []
Path referencesPath = Paths.get( projectPath.toString(), 'references' )
Path referencesTmpPath = tmpPath.resolve( 'references' )
Files.createDirectory( referencesTmpPath )
config.references.each( { ref ->
    String refName = ref.substring( 0, ref.lastIndexOf( '.' ) )
    log.info( "copy reference: ${refName}" )
    def reference = [
        name: refName
    ]
    Files.copy( referencesPath.resolve( "${refName}.fasta" ), referencesTmpPath.resolve( "${refName}.fasta" ) )
} )


log.info( 'copy assembly' )
Path assemblyPath = tmpPath.resolve( 'assembly.fasta' )
Files.copy( genomeAssemblyPath, assemblyPath )
Path scaffoldsPath = tmpPath.resolve( "${genomeName}.fasta" )
Path linkedScaffoldsPath = tmpPath.resolve( "${genomeName}-pseudo.fasta" )


// run MeDuSa
ProcessBuilder pb = new ProcessBuilder( 'java', '-jar', "${MEDUSA}/medusa.jar".toString(),
    '-f', referencesTmpPath.toString(), // reference
    '-i', assemblyPath.toString(), // assembled alignments
    '-o', scaffoldsPath.toString(), // new name
    '-random', '1000', // random rounds to find the best scaffolds
    '-scriptPath', Paths.get( MEDUSA ).resolve( 'medusa_scripts' ).toString() )// random rounds to find the best scaffolds
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( tmpPath.toFile() )

// set path variables
def pbEnv = pb.environment()
String pathEnv = pbEnv.get( 'PATH' )
pbEnv.put( 'PATH', "${MUMMER}:${pathEnv}" )

log.info( "exec: ${pb.command()}" )
log.info( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) terminate( 'could not exec MeDuSa!', genomeScaffoldsDirPath, tmpPath )
log.info( '----------------------------------------------------------------------------------------------' )


// parse MeDuSa output
String medusaOutput = tmpPath.resolve( 'assembly.fasta_SUMMARY' ).text
def m = medusaOutput =~ /Number of scaffolds: \d+ \(singletons = (\d+), multi-contig scaffold = (\d+)\)/
assert m != null
info.scaffolds.noContigs = m[0][1] as int
info.scaffolds.noScaffolds = m[0][2] as int

m = medusaOutput =~ /from (\d+) initial fragments/
assert m != null
info.scaffolds.noPreContigs = m[0][1] as int


// format MeDuSa scaffolds (add line breaks, replace 100N linker by custom 6 frame stop linker)
String locusTag
if( genome.species.length() > 4 )
    locusTag = "${config.project.genus.substring( 0, 1 )}${genome.species.substring( 0, 4 )}_${genome.strain}".toString()
else
    locusTag = "${config.project.genus.substring( 0, 1 )}${genome.species}_${genome.strain}".toString()

int i = 1
boolean isFirstContig = true
StringBuilder sbScaffolds = new StringBuilder( 10000000 )
StringBuilder sbPseudoGenome = new StringBuilder( 10000000 )
m = scaffoldsPath.text =~ /(?m)^>(.+)$\n([ATGCNatgcn\n]+)$/
m.each( { match ->
    sbScaffolds.append( ">${locusTag}_${i}\n" )
    String sequence = match[2].toUpperCase().replaceAll( '[^ATGCN]', '' ).replaceAll( 'N{100,}', LINKER )
    int pos = 0
    while( pos < sequence.length() ) {
        if( pos+LINE_LENGTH > sequence.length() )
            sbScaffolds.append( sequence.substring( pos ) ).append( '\n' )
        else
            sbScaffolds.append( sequence.substring( pos, pos+LINE_LENGTH ) ).append( '\n' )
        pos += LINE_LENGTH
    }
    i++

    if( isFirstContig )
        isFirstContig = false
    else
        sbPseudoGenome.append( LINKER )
    sbPseudoGenome.append( sequence )
} )
scaffoldsPath.text = sbScaffolds.toString()


// format pseudo genome
String pseudoGenomeSequence = sbPseudoGenome.toString().replaceAll( "(${LINKER}){2,}", LINKER )
sbPseudoGenome = new StringBuilder( 10000000 )
sbPseudoGenome.append( ">${genomeName}-pseudo\n" )
i = 0
int length = pseudoGenomeSequence.length()
while( i < length ) {
    if( i+LINE_LENGTH > length )
        sbPseudoGenome.append( pseudoGenomeSequence.substring( i ) ).append( '\n' )
    else
        sbPseudoGenome.append( pseudoGenomeSequence.substring( i, i+LINE_LENGTH ) ).append( '\n' )
    i += LINE_LENGTH
}
linkedScaffoldsPath.text = sbPseudoGenome.toString()


// copy scaffolds and pseudo genome files to genome scaffold dir
Files.copy( scaffoldsPath, genomeScaffoldsDirPath.resolve( "${genomeName}.fasta" ) )
Files.copy( linkedScaffoldsPath, genomeScaffoldsDirPath.resolve( "${genomeName}-pseudo.fasta" ) )


// copy scaffolds to sequence dir for characterization steps
Files.copy( scaffoldsPath, Paths.get( projectPath.toString(), PROJECT_PATH_SEQUENCES, "${genomeName}.fasta" ) )


// calc statistics
info.scaffolds << calcAssemblyStatistics( scaffoldsPath )


// check pre/post synteny with nucmer
log.info( 'check syntenies' )
info.scaffolds.syntenies = []
config.references.each( { ref ->
    String refName = ref.substring( 0, ref.lastIndexOf( '.' ) )
        def reference = [
        name: refName,
        pre: [],
        post: []
    ]
    pb = new ProcessBuilder( "${MUMMER}/nucmer".toString(),
        referencesPath.resolve( "${refName}.fasta" ).toString(), // reference
        assemblyPath.toString() ) // assembled alignments
        .redirectErrorStream( true )
        .redirectOutput( ProcessBuilder.Redirect.INHERIT )
        .directory( tmpPath.toFile() )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    if( pb.start().waitFor() != 0 ) terminate( 'could not exec nucmer!', genomeScaffoldsDirPath, tmpPath )
    log.info( '----------------------------------------------------------------------------------------------' )
    pb = new ProcessBuilder( 'sh', '-c',
        "${MUMMER}/delta-filter -q -i 0.8 ${tmpPath}/out.delta > ${tmpPath}/out-filtered.delta".toString() ) // assembled alignments
        .redirectErrorStream( true )
        .redirectOutput( ProcessBuilder.Redirect.INHERIT )
        .directory( tmpPath.toFile() )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    if( pb.start().waitFor() != 0 ) terminate( 'could not exec delta-filter!', genomeScaffoldsDirPath, tmpPath )
    log.info( '----------------------------------------------------------------------------------------------' )
    reference.pre = parseNucmer( tmpPath.resolve( 'out-filtered.delta' ), false )

    pb = new ProcessBuilder( "${MUMMER}/nucmer".toString(),
        referencesPath.resolve( "${refName}.fasta" ).toString(), // reference
        scaffoldsPath.toString() ) // scaffolds
        .redirectErrorStream( true )
        .redirectOutput( ProcessBuilder.Redirect.INHERIT )
        .directory( tmpPath.toFile() )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    if( pb.start().waitFor() != 0 ) terminate( 'could not exec nucmer!', genomeScaffoldsDirPath, tmpPath )
    log.info( '----------------------------------------------------------------------------------------------' )
    pb = new ProcessBuilder( 'sh', '-c',
        "${MUMMER}/delta-filter -q -i 0.8 ${tmpPath}/out.delta > ${tmpPath}/out-filtered.delta".toString() ) // assembled alignments
        .redirectErrorStream( true )
        .redirectOutput( ProcessBuilder.Redirect.INHERIT )
        .directory( tmpPath.toFile() )
    log.info( "exec: ${pb.command()}" )
    log.info( '----------------------------------------------------------------------------------------------' )
    if( pb.start().waitFor() != 0 ) terminate( 'could not exec delta-filter!', genomeScaffoldsDirPath, tmpPath )
    log.info( '----------------------------------------------------------------------------------------------' )
    reference.post = parseNucmer( tmpPath.resolve( 'out-filtered.delta' ), true )

    info.scaffolds.syntenies << reference
} )


// cleanup
log.debug( 'delete tmp-dir' )
if( !tmpPath.deleteDir() ) terminate( "could not recursively delete tmp-dir=${tmpPath}", genomeScaffoldsDirPath, tmpPath )


// store info.json
info.time.end = (new Date()).format( DATE_FORMAT )
File infoJson = genomeScaffoldsDirPath.resolve( 'info.json' ).toFile()
infoJson << JsonOutput.prettyPrint( JsonOutput.toJson( info ) )


// set state-file to finished
Files.move( genomeScaffoldsDirPath.resolve( 'state.running' ), genomeScaffoldsDirPath.resolve( 'state.finished' ) )




/**********************
 *** Script Methods ***
**********************/


private void terminate( String msg, Path genomePath, Path tmpPath ) {
    terminate( msg, null, genomePath, tmpPath )
}

private void terminate( String msg, Throwable t, Path genomePath, Path tmpPath ) {

    if( t ) log.error( msg, t )
    else    log.error( msg )
    Files.move( genomePath.resolve( 'state.running' ), genomePath.resolve( 'state.failed' ) ) // set state-file to failed
    tmpPath.deleteDir() // cleanup tmp dir
    log.debug( "removed tmp-dir: ${tmpPath}" )
    System.exit( 1 )

}

private def calcAssemblyStatistics( Path scaffoldsPath ) {

    def stats = [
        scaffolds: []
    ]

    String sequence = ''
    def m = scaffoldsPath.text =~ /(?m)^>(.+)$\n([ATGCNatgcn\n]+)$/
    m.each( { match ->
            String scaffold = match[2].replaceAll( '[^ATGCNatgcn]', '' )
            sequence += scaffold
            def scaffoldInfo = [
                name: match[1],
                length: scaffold.length(),
                gc:   (scaffold =~ /[GCgc]/).count / (scaffold =~ /[ATGCatgc]/).count,
                noAs: (scaffold =~ /[Aa]/).count,
                noTs: (scaffold =~ /[Tt]/).count,
                noGs: (scaffold =~ /[Gg]/).count,
                noCs: (scaffold =~ /[Cc]/).count,
                noNs: (scaffold =~ /[Nn]/).count
            ]
            stats.scaffolds << scaffoldInfo
    } )

    stats.length = sequence.length()
    stats.gc   = (sequence =~ /[GCgc]/).count / (sequence =~ /[ATGCatgc]/).count
    stats.noAs = (sequence =~ /[Aa]/).count
    stats.noTs = (sequence =~ /[Tt]/).count
    stats.noGs = (sequence =~ /[Gg]/).count
    stats.noCs = (sequence =~ /[Cc]/).count
    stats.noNs = (sequence =~ /[Nn]/).count

    // calc length stats
    def scaffoldLengths = stats.scaffolds*.length
    stats.lengths = [
        min:    scaffoldLengths.min(),
        max:    scaffoldLengths.max(),
        mean:   scaffoldLengths.sum() / scaffoldLengths.size(),
        median: calcMedian( scaffoldLengths ),
        noLt1kb:   0,
	noGt1kb:   0,
        noGt10kb:  0,
        noGt100kb: 0,
        noGt1mb:   0
    ]
    scaffoldLengths.each( {
            if( it >= 10**6 ) stats.lengths.noGt1mb++
            else if( it >= 10**5 ) stats.lengths.noGt100kb++
            else if( it >= 10**4 ) stats.lengths.noGt10kb++
            else if( it >= 10**3 ) stats.lengths.noGt1kb++
            else stats.lengths.noLt1kb++
    } )

    // calc N50 / N90
    Collections.sort( scaffoldLengths )
    scaffoldLengths = scaffoldLengths.reverse()
    int nSum = 0
    int i = -1
    while( nSum < 0.5*stats.length) {
        i++
        nSum += scaffoldLengths[ i ]
    }
    stats.n50 = scaffoldLengths[ i ]
    stats.l50 = i + 1

    while( nSum < 0.9*stats.length) {
        i++
        nSum += scaffoldLengths[ i ]
    }
    stats.n90 = scaffoldLengths[ i ]
    stats.l90 = i + 1

    return stats

}

private static Number calcMedian( def numbers ) {

    Collections.sort( numbers )
    int midNumber = (int)(numbers.size() / 2)

    return numbers.size()%2 != 0 ? numbers[midNumber] : (numbers[midNumber] + numbers[midNumber-1]) / 2

}

private def parseNucmer( Path nucmerResultsPath, boolean isScaffolded ) {

    def alignments = []
    String contigName
    String contigLength
    nucmerResultsPath.text.eachLine( { line ->
        if( line.charAt(0) == '>' ) {
            def cols = line.split( ' ' )
            contigName = cols[1]
            contigLength = cols[3]
        } else if( contigName ) {
            def cols = line.split( ' ' )
            if( cols.size() > 1 ) {
                def (rStart, rStop, cStart, cStop, noNonIdentities, noNonSimilarities, noStopCodons ) = cols
                def alignment = [
                    contig: contigName,
                    contigLength: contigLength,
                    rStart: Integer.parseInt( rStart ),
                    rEnd: Integer.parseInt( rStop ),
                    cStart: Integer.parseInt( cStart ),
                    cEnd: Integer.parseInt( cStop ),
                    noNonIdentities: Integer.parseInt( noNonIdentities )
                ]
                alignment.length = Math.abs( alignment.cEnd - alignment.cStart ) + 1
                alignment.strand = alignment.cStart < alignment.cEnd ? '+' : '-'
                if( alignment.cStart > alignment.cEnd ) {
                    int tmp = alignment.cStart
                    alignment.cStart = alignment.cEnd
                    alignment.cEnd = tmp
                }
                alignments << alignment
            }
        }
    } )

    if( alignments.isEmpty() )
	return []
    else {
        if( isScaffolded ) // contig names adhere to asap convention
            return alignments.toSorted( { a,b -> a.contig.split('_').last() as int <=> b.contig.split('_').last() as int ?: a.cStart <=> b.cStart } )
        else { // raw output from assembler
            if( alignments[0].contig.contains( '_' ) ) // SPAdes assembly
                return alignments.toSorted( { a,b -> (a.contig.split('_')[1]) as int <=> (b.contig.split('_')[1]) as int ?: a.cStart <=> b.cStart } )
            else // HGAP assembly
                return alignments.toSorted( { a,b -> (a.contig-'|quiver'-'F') as int <=> (b.contig-'|quiver'-'F') as int ?: a.cStart <=> b.cStart } )
        }
    }

}
