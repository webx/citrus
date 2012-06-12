function registerExceptionHandle() {
    $$('div.webx-exception-type>img').each(
        function (e) {
            e.observe('click', function (event) {
                var imgObj = Event.element(event);
                var imgSrc = imgObj.src;
                var details = imgObj.id.gsub(/^webx-exception-handle-/,
                                             'webx-exception-');
                var index = imgSrc.lastIndexOf('/') + 1;
                var file = imgSrc.substring(index);
                var prefix = imgSrc.substring(0, index);

                if (file == 'open.gif') {
                    $(details).hide();
                    Effect.SlideUp(details);
                    imgObj.src = prefix + 'close.gif';
                } else {
                    Effect.SlideDown(details);
                    imgObj.src = prefix + 'open.gif';
                }
            });

            var details = e.id.gsub(/^webx-exception-handle-/,
                                    'webx-exception-');

            if (e.src.endsWith('close.gif')) {
                $(details).hide();
            } else {
                $(details).show();
            }
        });
}

document.observe("dom:loaded", function () {
    registerExceptionHandle();
});
