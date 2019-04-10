
// get all gradients
let nodes = document.querySelectorAll('[gradient]');
let dict = {};
// fill dict
Array.from( document.querySelectorAll('[gradient]') ).forEach( e => {
    const key = e.getAttribute( 'gradient' );
    if( dict[key] !== undefined ) {
        const list = Array.from( dict[key] );
        list.push(e);
        dict[key] = list;
    } else
        dict[key] = [e];
} );

// use dict
for (const [key, elem] of Object.entries(dict)) {
    let values = elem.map( (e, i, arr) => e.childNodes[0].textContent.replace( /\./g, '' ).replace( /,/g, '.' ) );
    const max = Math.max(...values.filter( it => it !== '-' ));
    for( let i = 0; i < elem.length; i++ ) {
        let left = values[i] / max * 100; // percents of left
        if( elem[i].getAttribute( 'zs-warning' ) === '' )
            elem[i].style.backgroundImage = `-webkit-linear-gradient(left, #FFA5A7 ${left}%, #FFD1D2 ${left}%)`;
        else
            elem[i].style.backgroundImage = `-webkit-linear-gradient(left, #A2CDF1 ${left}%, transparent ${left}%)`;
        elem[i].style.borderRight = '1px solid white';
    }
};