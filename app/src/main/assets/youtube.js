//[...document.querySelectorAll('ytm-promoted-sparkles-web-renderer,.GoogleActiveViewElement')].forEach(x=>x.remove())

if (window.location.hostname === "91porn.com") {
    const body = document.createElement('body');
    const element = document.querySelector('#wrapper .row');
    if (element) {
        body.appendChild(element);
        document.body.replaceWith(body);
    }
    element.style.margin = '0';
    const style=document.createElement('style');
    style.textContent="body{padding:0!important}";
    document.querySelector('head').appendChild(style);
}
