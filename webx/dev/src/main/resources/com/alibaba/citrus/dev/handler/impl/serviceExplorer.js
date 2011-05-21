function setQuery(resourceName, submit) {
    var form = $('webx-resource-query-form');
    var textbox = $('webx-resource-query');
    
    textbox.value = resourceName;
    
    if (submit) {
        form.submit();
    }
}
