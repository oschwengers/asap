
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import groovy.util.CliBuilder

def cli = new CliBuilder( usage: 'convert-cnbi-taxonomy.groovy --ncbi-path <ncbi-path>' )
    cli.p( longOpt: 'ncbi-path', args: 1, argName: 'ncbi-path', required: true, 'Path to NCBI Taxonomy DB directory' )
def opts = cli.parse( args )


def ranks = [
	'superkingdom',
	'phylum',
	'class',
	'order',
	'family',
	'genus',
	'species'
]


Path ncbiTaxonomyPath = Paths.get( opts.p )
if( !Files.exists( ncbiTaxonomyPath ) ) {
    println( "Error: NCBI Taxonomy DB directory (${ncbiTaxonomyPath}) does not exist!" )
    System.exit(1)
}
ncbiTaxonomyPath = ncbiTaxonomyPath.toRealPath()


// parse names.dmp
def idNameMap = [:]
ncbiTaxonomyPath.resolve( 'names.dmp' ).eachLine( { line ->
    def cols = line.split( '\\|' ).collect( {it.trim()} )
    if( cols[3] == 'scientific name' )
        idNameMap[ (Integer.parseInt( cols[0] )) ] = cols[1]
} )


// parse nodes.dmp
def nodesMap = [:]
ncbiTaxonomyPath.resolve( 'nodes.dmp' ).eachLine( { line ->
    def cols = line.split( '\\|' ).collect( {it.trim()} )
    if( cols[1] != '' ) {
        int id = Integer.parseInt( cols[0] )
        nodesMap[ (id) ] = [
            id: id,
            parent: Integer.parseInt( cols[1] ),
            rank: cols[2]
        ]
    }
} )


def leafs = nodesMap.keySet().collect().toSet()


nodesMap.each( { taxId, node ->

    def lineage = [:]
    node.name = idNameMap[(taxId)]
    lineage[ (node.rank) ] = node.name
    def parent = nodesMap[ (node.parent) ]
    while( parent != null  &&  parent.id != 1 ) {// &&  parent.rank != 'superkingdom' ) {
        parent.name = idNameMap[(parent.id)]
	lineage[ (parent.rank) ] = parent.name
        parent = nodesMap[ (parent.parent) ]
    }
	
	def strippedLineage = ranks.collect( { lineage[(it)] } ).collect( { it == null ? '-' : it} )

    println( "${node.id}\t${node.name}\t${strippedLineage.join(';')}" )

} )
