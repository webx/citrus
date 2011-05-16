function registerDomElementHandle() {
    var handleClick = function(event) {
        var imgObj = event.target;
        var imgSrc = imgObj.src;
        var collapsed = imgObj.id.gsub(/^handle-/, 'collapsed-');
        var expanded  = imgObj.id.gsub(/^handle-/, 'expanded-');
        var index = imgSrc.lastIndexOf('/') + 1;
        var file = imgSrc.substring(index);
        var prefix = imgSrc.substring(0, index);

        if (file == 'open.png') {
            Effect.SlideUp(expanded);
            $(collapsed).style.display = 'inline';
            imgObj.src = prefix + 'close.png';
        } else {
            Effect.SlideDown(expanded);
            $(collapsed).style.display = 'none';
            imgObj.src = prefix + 'open.png';
        }
    };

    $$('img.handle-element').each( function(e) {
        e.observe('click', handleClick);
    });
}

document.observe("dom:loaded", function() {
    registerDomElementHandle();
});