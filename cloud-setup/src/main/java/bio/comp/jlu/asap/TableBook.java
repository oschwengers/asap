
package bio.comp.jlu.asap;


/**
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public final class TableBook {

    private final int noTables;

    private final String fileName;

    private final Table[] tables;


    public TableBook( String fileName, int noTables ) {

        if( fileName == null || fileName.isEmpty() )
            throw new NullPointerException( "File mustn't be null nor empty!" );

        if( noTables < 1 )
            throw new IllegalArgumentException( "Number of tables can't ve smaller than 1" );

        this.noTables = noTables;
        this.fileName = fileName;

        tables = new Table[noTables];

    }


    public String getFileName() {

        return fileName;

    }


    public int getNoTables() {

        return noTables;

    }


    public Table getTable( int tableIndx ) {

        if( tableIndx < 0 || tableIndx >= noTables )
            throw new IllegalArgumentException( "table index " + tableIndx + " is out of range [0," + (noTables - 1) + "]" );

        return tables[tableIndx];

    }


    public void setTable( int tableIndx, Table table ) {

        if( tableIndx < 0 || tableIndx >= tables.length )
            throw new IllegalArgumentException( "table index is out of range: " + tableIndx );

        if( table == null )
            throw new NullPointerException( "table is null!" );


        tables[tableIndx] = table;

    }


}
