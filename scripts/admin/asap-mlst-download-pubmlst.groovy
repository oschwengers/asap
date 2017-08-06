
// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import groovy.util.CliBuilder




/************************
 *** Script Constants ***
************************/

ASAP_HOME = System.getenv()['ASAP_HOME']

MAKEBLASTDB = "${ASAP_HOME}/share/blast/bin/makeblastdb"



/*********************
 *** Script Params ***
*********************/


def cli = new CliBuilder( usage: '$ASAP_HOME/bin/groovy asap-download-pubmlst.groovy [--url <http://pubmlst.org/data/dbases.xml>] [--output <output_directory>]')
    cli.h( longOpt: 'help', 'Show usage information' )
    cli.u( longOpt: 'url', 'Input url xml', args: 1, required: false )
    cli.o( longOpt: 'output', 'Output directory', args:1, required: false )


def opt = cli.parse( args )
if( !opt  ||  opt.h ) {
    cli.usage()
    System.exit( 0 )
}


def xmlUrl
if( opt.u ) {
    xmlUrl = opt.u.toURL()
    println 'url: ' + opt.u
} else {
    xmlUrl = 'https://pubmlst.org/data/dbases.xml'.toURL()
    println xmlUrl
}


// destination path and directory name handling
def destinationPath = Paths.get( '' )
if( opt.o ){
    destinationPath = Paths.get( opt.o ).toRealPath()
    println "dest: ${destinationPath}"
}




/************************
 *** Script Logic ******
************************/


StringBuilder mlstDb = new StringBuilder( 100000000 )
if( !Files.exists( destinationPath ) )
    Files.createDirectory( destinationPath )
Path schemesPath = destinationPath.resolve( 'schemes' )
Files.createDirectory( schemesPath )

// parse PubMLST xml file
println 'download & parse PubMLST xml file...'
def parsedXml = (new XmlParser()).parseText( xmlUrl.text )
int noSchemes = parsedXml.species.size()
println "download ${noSchemes} MLST schemes:"
parsedXml.species.eachWithIndex( {species, idx ->

    def db = species.mlst.database
    def infoUrl = db.url[0].value()[0] // url to db
    def lastUpdate = db.retrieved[0].value()[0] // date
    def noProfiles = db.profiles.count[0].value()[0] // noSchemes

    // saving species.txt
    def schemeUrl = db.profiles.url[0].value()[0] // url to .txt
    def schemeName =  schemeUrl.split('/').last().split('\\.').first()
    if( schemeName.contains('_2')  ||  schemeName.contains('_3') )
        println "\t-/${noSchemes}: scheme: ${schemeName}, # profiles: ${noProfiles}, last Update: ${lastUpdate}"
    else {
        println "\t${idx+1}/${noSchemes}: scheme: ${schemeName}, # profiles: ${noProfiles}, last Update: ${lastUpdate}"
        Path schemePath = schemesPath.resolve( schemeName )
        Files.createDirectory( schemePath )
        schemePath.resolve( "${schemeName}.txt" ).text = schemeUrl.toURL().text
        schemePath.resolve( 'readme.txt' ).text = "URL:\t${infoUrl}\nDate:\t${lastUpdate}\n# Schemes:\t${noProfiles}"

        db.loci[0].each( { locus ->
            def alleleUrl = locus.url[0].value()[0]
            String allelesFasta = alleleUrl.toURL().text
            String alleleName = alleleUrl.split('/').last()
            schemePath.resolve( alleleName ).text = allelesFasta
            mlstDb.append( allelesFasta.replace( '>', ">${schemeName}." ) )
        } )
    }

} )


// write MLST fasta db to file
Path mlstFasta  = destinationPath.resolve( 'mlst.fna' )
mlstFasta.text = mlstDb.toString().trim()


// build blastn db
ProcessBuilder pb = new ProcessBuilder( MAKEBLASTDB,
    '-hash_index',
    '-in', mlstFasta.toString(),
    '-dbtype', 'nucl',
    '-title', 'PubMLST',
    '-parse_seqids' )
    .redirectErrorStream( true )
    .redirectOutput( ProcessBuilder.Redirect.INHERIT )
    .directory( destinationPath.toFile() )
println( "exec: ${pb.command()}" )
println( '----------------------------------------------------------------------------------------------' )
if( pb.start().waitFor() != 0 ) {
    println 'ERROR: could not exec makeblastdb!'
    System.exit( -1 )
}
println( '----------------------------------------------------------------------------------------------' )

