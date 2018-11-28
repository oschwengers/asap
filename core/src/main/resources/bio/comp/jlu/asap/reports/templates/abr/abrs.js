
(function (d3) {

    let dict = {
        ab: ["Aminoglycoside",],
        bl: ["Beta-lactam"],
        da: ["diaminopyrimidine"],
        em: ["Elfamycin"],
        fl: ["Fluoroquinolone"],
        gl: ["Glycopeptide"],
        ls: ["Lincosamide"],
        lp: ["Lipopeptide"],
        mc: ["Macrocyclic"],
        ml: ["Macrolide"],
        nf: ["Nitrofuran"],
        ni: ["Nitroimidazole"],
        nc: ["Nucleoside"],
        oa: ["Organoarsenic"],
        oz: ["Oxazolidinone"],
        pt: ["Peptide"],
        pc: ["Phenicol"],
        pm: ["Pleuromutilin"],
        pa: ["Polyamine"],
        px: ["Polymyxin"],
        ra: ["Rifamycin"],
        sg: ["Streptogramin"],
        sa: ["Sulfonamide"],
        sf: ["Sulfone"],
        tc: ["Tetracycline"]
    };
    let dictLen = Object.keys(dict).length;

    Object.keys(dict).forEach( (key, idx) => {
        dict[key][1] = d3.schemeCategory20[idx];
    } );

    Array.from( document.getElementsByClassName( "abrs" ) ).forEach( (e) => {

        let classList = e.classList;
        let x = 8;
        let y = 8;
        let r = 8;
        let xPlus = 18;

        const svgContainer = d3.select(e).append( "svg" )
            .attr( "width", (x + r) * dictLen + (dictLen - 1) * (xPlus - x - r) + 2)
            .attr( "height", r*2 + 2)
            .style( "background-color", "");

        Object.keys(dict).forEach( (key, idx ) => {
            let svg = svgContainer.append( "circle" )
                .attr( 'class', 'abr-circle' )
                .attr( "cx", (idx == 0) ? (x+1) : (x+1+xPlus*idx) )
                .attr( "cy", y + 1 )
                .attr( "r", r )
                .attr( 'title', dict[key][0] )
            if( gotABR( key, classList ) ) {
                svg.attr( 'fill', dict[key][1] )
            } else {
                svg.attr( 'fill', 'white' )
                   .attr( "stroke-width", 1 )
                   .attr( "stroke", "#8ACAF5" );
            }
        } );

    } );

} )(d3);

function gotABR( abr, abrList ) {
    for( let i=0; i<abrList.length; i++) {
        if( abr == abrList[i] ) return true;
    }
    return false;
}

