const loaderElement =
    $("<div class=\"sk-folding-cube\">" +
        "<div class=\"sk-cube1 sk-cube\"></div>" +
        "<div class=\"sk-cube2 sk-cube\"></div>" +
        "<div class=\"sk-cube4 sk-cube\"></div>" +
        "<div class=\"sk-cube3 sk-cube\"></div>" +
    "</div>")

/**
 * Enables the loader for a specified field/element
 * @param name - content name of the div
 */
function enableLoader(name) {
    // Get original content
    let content = $("***REMOVED***content_" + name)
    let loader = loaderElement.clone();

    // ... and save it
    localStorage.setItem("content_" + name, content.html());

    // use loader element
    content.children().remove();
    content.append(loader);
}

/**
 * Little helper, to disable the loader after AJAX finished.
 * It needs a specified field/name where the loader is activated for
 * @param name - content name of the div
 */
function disableLoader(name) {
    let content = $("***REMOVED***content_" + name);
    let getOriginalContent = localStorage.getItem("content_" + name);

    // Remove loader
    content.children().remove();
    content.append(getOriginalContent);
}

/**
 * Wrapper function to popup messages
 * @param response - server response message
 */
function popup(response) {
    if(response["info"] == null && response["error"] == null) {
        defaultMsg(response)
    } else {
        info(response["info"])
        error(response["error"])
    }
}

/**
 * Helper function to popup information messages
 * @param data - server response message
 */
function info(data) {
    if(data != null) {
        M.toast({html: data, classes: "blue"});
    }
}

/**
 * Helper function to popup error messages
 * @param data - server response message
 */
function error(data) {
    if(data != null) {
        M.toast({html: data, classes: "red"})
    }
}

/**
 * Helper function to popup default messages
 * @param data - server response message
 */
function defaultMsg(data) {
    M.toast({html: data, classes: "grey"})
}

/**
 * This function marks an element (e.g. card-content) as deleted
 * @param querySelector - html element
 */
function markAsDelete(querySelector) {
    console.info("Mark "+ querySelector + " as deleted.")
    if(querySelector != null) {
        $(querySelector).addClass("deleted-overlay")
    } else {
        console.error("Query selector is null(..). Could not mark as delete.")
    }
}

/**
 * Load a specific CSS file for the wished mode (e.g. light/dark)
 * @param mode - light/dark/...
 */
function loadTheme(mode) {
    console.debug("Use " + mode + " theme");
    $.ajax({
        url: "/style/" + mode + ".css",
        dataType: 'text',
        success: function(data) {
            // Remove old style
            $("head").children().each(function(index, ele) {
                if (ele.innerHTML && ele.innerHTML.substring(0, 30).match(/\/\*current theme\*\//)) {
                    $(ele).remove();
                    return false;    // Stop iterating since we removed something
                }
            });
            $('<style type="text/css">/*current theme*/ \n' + data + '</style>').appendTo("head");
        }
    });
}