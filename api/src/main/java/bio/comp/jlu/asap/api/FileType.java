
package bio.comp.jlu.asap.api;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum FileType {

    READS_ILLUMINA_PAIRED_END( "paired-end", DataType.READS ),
    READS_ILLUMINA_MATE_PAIRS( "mate-pairs", DataType.READS ),
    READS_ILLUMINA_HQ_MATE_PAIRS( "hq-mate-pairs", DataType.READS ),
    READS_ILLUMINA_SINGLE( "single", DataType.READS ),
    READS_NANOPORE( "nanopore", DataType.READS ),
    READS_NANOPORE_PAIRED_END( "nanopore-pe", DataType.READS ),
    READS_PACBIO_RSII( "pacbio-rs2", DataType.READS ),
    READS_PACBIO_SEQUEL( "pacbio-sequel", DataType.READS ),
    READS_SANGER( "sanger", DataType.READS ),
    CONTIGS( "contigs", DataType.CONTIGS ),
    CONTIGS_ORDERED( "contigs-ordered", DataType.CONTIGS ),
    CONTIGS_LINKED( "contigs-linked", DataType.CONTIGS ),
    GENOME( "genome", DataType.GENOME );


    private final String fileType;
    private final DataType dataType;

    FileType( String fileType, DataType dataType ) {

        this.fileType  = fileType;
        this.dataType = dataType;

    }


    public String getType() {

        return fileType;

    }


    public DataType getDataType() {

        return dataType;

    }


    @Override
    public String toString() {

        return fileType;

    }


    public static FileType getEnum( String str ) {

        for( FileType ft : FileType.values() ) {
            if( str.equals( ft.toString() ) )
                return ft;
        }

        return null;

    }

}
