
import java.nio.file.*
import groovy.json.*

final String proteinHomologId = "40292" // "protein homolog model"
final def pResistance = ~/determinant of ([a-zA-Z _-]+) resistance/
final def pNumber = ~/[0-9]+/

Path cardDataPath = Paths.get( args[0] ).toRealPath()
(new JsonSlurper()).parseText( cardDataPath.text ).each( { k,val ->
	if( k ==~ pNumber  &&  val["model_type_id"] == proteinHomologId ) {
		def geneId = "ARO:${val["ARO_accession"]}"
		def geneName = val["ARO_name"]
		if( geneName.indexOf( ' ' ) != -1 ) {
			def cols = geneName.split( ' ' )
			if( cols.size() == 2 ) {
				geneName = cols[0]
			} else if( cols.size() == 3 ) {
				geneName = cols[2]
			} else {
				geneName = null
			}
		}
		def product  = ""
		val["ARO_category"].each( { k2,val2 ->
			def m = val2["category_aro_name"] =~ pResistance
			if( m ) product = "${m[0][1]} resistance"
		} )		
		def sequence = val["model_sequences"].values()[0].values()["protein_sequence"].sequence[0]
		
		if( geneId != null  &&  geneName != null  &&  product != null  &&  sequence != null ) {
			println ">$geneId ~~~$geneName~~~$product"
			println sequence
		} //else
			//println ">$geneId\t$geneName\t$product"
	}
} )
