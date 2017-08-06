
package bio.comp.jlu.asap;


import java.util.Arrays;
import java.util.Objects;


/**
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public final class Table {

    private final int noRows;
    private final int noCols;

    private final String name;

    private final String[] data;


    public Table( final String name, final int noRows, final int noCols ) {

        if( name == null || name.isEmpty() )
            throw new NullPointerException( "File mustn't be null nor empty!" );

        if( noRows < 0 )
            throw new IllegalArgumentException( "number of rows can't be smaller than 0!" );

        if( noCols < 0 )
            throw new IllegalArgumentException( "number of columns can't be smaller than 0!" );

        this.noRows = noRows;
        this.noCols = noCols;

        this.name = name;

        this.data = new String[noRows * noCols];

    }


    public String getName() {

        return name;

    }


    public String getCellContent( final int row, final int col ) {

        if( row < 0 || row >= noRows )
            throw new ArrayIndexOutOfBoundsException( "row=" + row );

        if( col < 0 || col >= noCols )
            throw new ArrayIndexOutOfBoundsException( "col=" + col );


        return data[row * noCols + col];

    }


    public void setCellContent( final int row, final int col, final String val ) {

        if( row < 0 || row >= noRows )
            throw new ArrayIndexOutOfBoundsException( "row=" + row );

        if( col < 0 || col >= noCols )
            throw new ArrayIndexOutOfBoundsException( "col=" + col );


        data[row * noCols + col] = val;

    }


    public int getNoRows() {

        return noRows;

    }


    public String[] getRow( final int row ) {

        final String[] strRow = new String[noCols];

        final int tmpRowCount = row * noCols;
        for( int i = 0; i < noCols; i++ ) {

            strRow[i] = data[tmpRowCount + i];

        }

        return strRow;

    }


    public void setRow( final int row, final String[] rowVals ) {

        if( rowVals.length != noCols )
            throw new IllegalArgumentException( "size of row value array unequals number of columns!" );

        final int tmpRowCount = row * noCols;
        for( int i = 0; i < noCols; i++ ) {

            data[tmpRowCount + i] = rowVals[i];

        }

    }


    public int getNoColumns() {

        return noCols;

    }


    public String[] getColumn( final int col ) {

        final String[] strCol = new String[noRows];

        for( int i = 0; i < noRows; i++ ) {

            strCol[i] = data[i * noCols + col];

        }

        return strCol;

    }


    public void setColumn( final int col, final String[] colVals ) {

        if( colVals.length != noRows )
            throw new IllegalArgumentException( "size of column value array unequals number of rows!" );

        for( int i = 0; i < noRows; i++ ) {

            data[i * noCols + col] = colVals[i];

        }

    }


    @Override
    public boolean equals( Object obj ) {

        if( obj == null )
            return false;
        if( obj == this )
            return true;

        if( Table.class != obj.getClass() )
            return false;


        final Table other = (Table) obj;
        if( this.noRows != other.noRows )
            return false;

        if( this.noCols != other.noCols )
            return false;

        if( !Objects.equals( this.name, other.name ) )
            return false;

        return Arrays.deepEquals( this.data, other.data );

    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.noRows;
        hash = 97 * hash + this.noCols;
        hash = 97 * hash + Objects.hashCode( this.name );
        return hash;
    }


}
