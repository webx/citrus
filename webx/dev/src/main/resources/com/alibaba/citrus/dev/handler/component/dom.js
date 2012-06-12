function expandOrCollapse(imgObj, expand, toggle) {
    var imgSrc = imgObj.src;
    var collapsed = imgObj.id.gsub(/^handle-/, 'collapsed-');
    var expanded = imgObj.id.gsub(/^handle-/, 'expanded-');
    var index = imgSrc.lastIndexOf('/') + 1;
    var file = imgSrc.substring(index);
    var prefix = imgSrc.substring(0, index);

    if ((toggle || !expand) && file == 'open.png') {
        Effect.SlideUp(expanded);
        $(collapsed).style.display = 'inline';
        imgObj.src = prefix + 'close.png';
    } else if ((toggle || expand) && file == 'close.png') {
        Effect.SlideDown(expanded);
        $(collapsed).style.display = 'none';
        imgObj.src = prefix + 'open.png';
    }
}

function registerDomElementHandle() {
    var handleClick = function (event) {
        expandOrCollapse(event.target, false, true);
    };

    $$('img.handle-element').each(function (e) {
        e.observe('click', handleClick);
    });
}

function registerExpandCollapseButtons() {
    var expandAll = function (event) {
        $$('.webx-dom ol>li>.expanded-element').each(function (e) {
            var handle = $(e.id.gsub(/^expanded-/, 'handle-'));
            expandOrCollapse(handle, true, false);
        });
    };

    var collapseAll = function (event) {
        $$('.webx-dom ol>li>.expanded-element').each(function (e) {
            var handle = $(e.id.gsub(/^expanded-/, 'handle-'));
            expandOrCollapse(handle, false, false);
        });
    };

    var expandAllLink = $('expand-all');
    var collapseAllLink = $('collapse-all');

    if (expandAllLink != null) {
        expandAllLink.observe('click', expandAll);
    }

    if (collapseAllLink != null) {
        collapseAllLink.observe('click', collapseAll);
    }
}

document.observe("dom:loaded", function () {
    registerDomElementHandle();
    registerExpandCollapseButtons();
});
