
// group elements by gradient id
let dict = {};
Array.from( document.querySelectorAll('[gradient]') ).forEach( e => {
    const key = e.getAttribute( 'gradient' );
    if( key in dict ) {
        let list = dict[key];
        list.push(e);
    } else
        dict[key] = [e];
} );

// calculate visual properties of elements
for (const [key, elem] of Object.entries(dict)) {
    let values = elem.map( (e, i, arr) => e.childNodes[0].textContent.replace( /\./g, '' ).replace( /,/g, '.' ) );
    const max = Math.max(...values.filter( it => it !== '-' ));
    for( let i = 0; i < elem.length; i++ ) {
        let val = values[i];
        if( val !== '-' ) {
            let left = val / max * 100; // percents of left
            if( elem[i].getAttribute( 'zs-warning' ) === '' )
                elem[i].style.backgroundImage = `-webkit-linear-gradient(left, #FFA5A7 ${left}%, #FFD1D2 ${left}%)`;
            else
                elem[i].style.backgroundImage = `-webkit-linear-gradient(left, #A2CDF1 ${left}%, transparent ${left}%)`;
            elem[i].style.borderRight = '1px solid white';
        }
    }
};